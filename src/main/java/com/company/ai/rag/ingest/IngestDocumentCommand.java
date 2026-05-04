package com.company.ai.rag.ingest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestDocumentCommand implements Serializable {
    private UUID taskId;
    private UUID documentId;
    private String tenantId;
    private String knowledgeBaseId;
    private String datasetVersion;
    private String fileName;
    private String contentType;
    private String objectKey;
}
