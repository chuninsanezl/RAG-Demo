package com.company.ai.rag.ingest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestTaskConsumer {

    private final IngestWorkflowService ingestWorkflowService;

    @KafkaListener(
            topics = "rag.ingest.document",
            groupId = "rag-ingest-group",
            autoStartup = "${rag.ingest.kafka-enabled:false}"
    )
    public void onMessage(ConsumerRecord<String, IngestDocumentCommand> record, Acknowledgment acknowledgment) {
        IngestDocumentCommand command = record.value();
        try {
            ingestWorkflowService.handle(command);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("ingest task failed, documentId={}", command.getDocumentId(), ex);
            throw ex;
        }
    }
}
