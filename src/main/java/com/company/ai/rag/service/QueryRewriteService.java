package com.company.ai.rag.service;

import com.github.benmanes.caffeine.cache.Cache;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QueryRewriteService {

    private final ChatLanguageModel chatLanguageModel;
    private final Cache<String, String> queryRewriteCache;

    public String rewrite(String tenantId, String question, String conversationContext) {
        String cacheKey = tenantId + "::" + question + "::" + conversationContext;
        String cached = queryRewriteCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        String prompt = """
                你是企业知识检索查询改写器。
                目标：将用户问题改写为更适合知识库检索的标准问句。
                要求：
                1. 保留原意，不增加事实。
                2. 若上下文能补足主语，请补足。
                3. 输出仅返回改写后的一个问题。

                会话上下文：
                %s

                用户问题：
                %s
                """.formatted(
                StringUtils.hasText(conversationContext) ? conversationContext : "无",
                question
        );

        String rewritten = chatLanguageModel.generate(prompt);
        queryRewriteCache.put(cacheKey, rewritten);
        return rewritten;
    }
}
