package com.company.ai.rag.service.retrieval;

import java.util.List;

public interface RerankService {
    List<RetrievedChunk> rerank(String question, List<RetrievedChunk> chunks);
}
