package com.company.ai.rag.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AskRequest {

    @NotBlank
    private String tenantId;

    @NotBlank
    private String userId;

    @NotBlank
    private String knowledgeBaseId;

    @NotBlank
    private String question;

    private String sessionId;
    private String datasetVersion;
}
