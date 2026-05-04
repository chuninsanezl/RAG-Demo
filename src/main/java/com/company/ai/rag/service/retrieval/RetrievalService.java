package com.company.ai.rag.service.retrieval;

import com.company.ai.rag.config.RagProperties;
import com.company.ai.rag.repository.KnowledgeChunkSearchRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final EmbeddingModel embeddingModel;
    private final KnowledgeChunkSearchRepository searchRepository;
    private final RagProperties ragProperties;

    public List<RetrievedChunk> retrieve(
            String tenantId,
            String knowledgeBaseId,
            String datasetVersion,
            String rewrittenQuery
    ) {
        Embedding embedding = embeddingModel.embed(rewrittenQuery).content();
        String vectorLiteral = toVectorLiteral(embedding.vectorAsList());

        List<RetrievedChunk> raw = searchRepository.searchByEmbedding(
                tenantId,
                knowledgeBaseId,
                datasetVersion,
                vectorLiteral,
                ragProperties.getRetrieval().getMaxResults()
        );

        return raw.stream()
                .filter(item -> item.getScore() >= ragProperties.getRetrieval().getMinScore())
                .sorted(Comparator.comparing(RetrievedChunk::getScore).reversed())
                .toList();
    }

    public static String toVectorLiteral(List<Float> vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector.get(i));
        }
        builder.append(']');
        return builder.toString();
    }
}
