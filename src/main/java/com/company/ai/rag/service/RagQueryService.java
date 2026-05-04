package com.company.ai.rag.service;

import com.company.ai.rag.api.dto.AskRequest;
import com.company.ai.rag.api.dto.AskResponse;
import com.company.ai.rag.service.retrieval.RerankService;
import com.company.ai.rag.service.retrieval.RetrievalService;
import com.company.ai.rag.service.retrieval.RetrievedChunk;
import com.github.benmanes.caffeine.cache.Cache;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private final QueryRewriteService queryRewriteService;
    private final RetrievalService retrievalService;
    private final RerankService rerankService;
    private final ContextAssembler contextAssembler;
    private final ChatLanguageModel chatLanguageModel;
    private final Cache<String, String> answerCache;
    private final MeterRegistry meterRegistry;

    public AskResponse ask(AskRequest request) {
        long start = System.currentTimeMillis();
        String cacheKey = buildCacheKey(request);
        String cachedAnswer = answerCache.getIfPresent(cacheKey);

        String rewrittenQuery = queryRewriteService.rewrite(
                request.getTenantId(),
                request.getQuestion(),
                ""
        );

        if (cachedAnswer != null) {
            meterRegistry.counter("rag.answer.cache.hit").increment();
            return AskResponse.builder()
                    .answer(cachedAnswer)
                    .rewrittenQuery(rewrittenQuery)
                    .references(List.of())
                    .costMs(System.currentTimeMillis() - start)
                    .build();
        }

        List<RetrievedChunk> recalled = retrievalService.retrieve(
                request.getTenantId(),
                request.getKnowledgeBaseId(),
                request.getDatasetVersion(),
                rewrittenQuery
        );

        List<RetrievedChunk> reranked = rerankService.rerank(rewrittenQuery, recalled);
        if (CollectionUtils.isEmpty(reranked)) {
            return AskResponse.builder()
                    .answer("未在知识库中检索到足够可信的依据，请补充问题或联系人工支持。")
                    .rewrittenQuery(rewrittenQuery)
                    .references(List.of())
                    .costMs(System.currentTimeMillis() - start)
                    .build();
        }

        String context = contextAssembler.assemble(reranked);
        String prompt = buildPrompt(request.getQuestion(), context);
        String answer = chatLanguageModel.generate(prompt);

        answerCache.put(cacheKey, answer);
        meterRegistry.timer("rag.query.latency")
                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);

        return AskResponse.builder()
                .answer(answer)
                .rewrittenQuery(rewrittenQuery)
                .references(reranked.stream().map(item ->
                        AskResponse.ReferenceItem.builder()
                                .documentId(item.getDocumentId().toString())
                                .chunkNo(item.getChunkNo())
                                .fileName(item.getFileName())
                                .titlePath(item.getTitlePath())
                                .score(item.getScore())
                                .build()
                ).toList())
                .costMs(System.currentTimeMillis() - start)
                .build();
    }

    private String buildPrompt(String question, String context) {
        return """
                你是企业知识库问答助手。
                你的回答必须严格依据提供的资料，不得编造。

                回答要求：
                1. 若资料足够，给出简洁准确答案。
                2. 若资料不足，明确说明资料不足，不要自行补充事实。
                3. 优先使用最新、生效中的政策与规则。
                4. 回答结尾给出“依据来源”摘要。

                用户问题：
                %s

                参考资料：
                %s
                """.formatted(question, context);
    }

    private String buildCacheKey(AskRequest request) {
        return String.join("::",
                request.getTenantId(),
                request.getKnowledgeBaseId(),
                String.valueOf(request.getDatasetVersion()),
                request.getQuestion()
        );
    }
}
