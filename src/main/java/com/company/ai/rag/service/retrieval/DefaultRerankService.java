package com.company.ai.rag.service.retrieval;

import com.company.ai.rag.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultRerankService implements RerankService {

    private final RagProperties ragProperties;

    @Override
    public List<RetrievedChunk> rerank(String question, List<RetrievedChunk> chunks) {
        return chunks.stream()
                .sorted(Comparator.comparing(RetrievedChunk::getScore).reversed())
                .limit(ragProperties.getRetrieval().getRerankTopN())
                .toList();
    }
}
