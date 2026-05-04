package com.company.ai.rag.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AskResponse {
    String answer;
    List<ReferenceItem> references;
    String rewrittenQuery;
    long costMs;

    @Value
    @Builder
    public static class ReferenceItem {
        String documentId;
        Integer chunkNo;
        String fileName;
        String titlePath;
        Double score;
    }
}
