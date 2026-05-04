package com.company.ai.rag.ingest;

import com.company.ai.rag.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngestWorkflowService {

    private final DocumentIndexingService documentIndexingService;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public void handle(IngestDocumentCommand command) {
        if (alreadyProcessed(command)) {
            return;
        }

        String parsedText = loadAndParseText(command);
        documentIndexingService.index(command, parsedText, Map.of(
                "tenantId", command.getTenantId(),
                "knowledgeBaseId", command.getKnowledgeBaseId(),
                "datasetVersion", command.getDatasetVersion(),
                "fileName", command.getFileName(),
                "documentId", command.getDocumentId().toString()
        ));

        markAsProcessed(command);
    }

    private boolean alreadyProcessed(IngestDocumentCommand command) {
        return knowledgeDocumentRepository.exists(
                command.getTenantId(),
                command.getKnowledgeBaseId(),
                command.getDatasetVersion(),
                command.getFileName()
        );
    }

    private String loadAndParseText(IngestDocumentCommand command) {
        Resource resource;
        if (command.getObjectKey().startsWith("classpath:")) {
            resource = new ClassPathResource(command.getObjectKey().substring("classpath:".length()));
        } else if (command.getObjectKey().startsWith("file:")) {
            resource = new FileSystemResource(command.getObjectKey().substring("file:".length()));
        } else {
            throw new IllegalArgumentException("Unsupported objectKey: " + command.getObjectKey());
        }

        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load document: " + command.getObjectKey(), e);
        }
    }

    private void markAsProcessed(IngestDocumentCommand command) {
        // Demo workflow writes READY status during indexing.
    }
}
