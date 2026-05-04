package com.company.ai.rag.service.retrieval;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class RetrievedChunk {
    UUID chunkId;
    UUID documentId;
    Integer chunkNo;
    String content;
    String fileName;
    String titlePath;
    Double score;
    Map<String, Object> metadata;
}
