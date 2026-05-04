package com.company.ai.rag.bootstrap;

import com.company.ai.rag.config.RagProperties;
import com.company.ai.rag.ingest.IngestDocumentCommand;
import com.company.ai.rag.ingest.IngestWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final RagProperties ragProperties;
    private final IngestWorkflowService ingestWorkflowService;

    @Override
    public void run(ApplicationArguments args) {
        if (!ragProperties.getDemo().isLoadSampleOnStartup()) {
            return;
        }

        IngestDocumentCommand command = IngestDocumentCommand.builder()
                .taskId(UUID.randomUUID())
                .documentId(UUID.nameUUIDFromBytes("sample-after-sale-policy".getBytes(StandardCharsets.UTF_8)))
                .tenantId(ragProperties.getDemo().getSampleTenantId())
                .knowledgeBaseId(ragProperties.getDemo().getSampleKnowledgeBaseId())
                .datasetVersion(ragProperties.getDemo().getSampleDatasetVersion())
                .fileName("after-sale-policy.md")
                .contentType("text/markdown")
                .objectKey("classpath:sample-data/after-sale-policy.md")
                .build();

        log.info("initializing sample knowledge base data");
        ingestWorkflowService.handle(command);
    }
}
