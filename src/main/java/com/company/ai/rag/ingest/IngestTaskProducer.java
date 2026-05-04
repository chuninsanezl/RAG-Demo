package com.company.ai.rag.ingest;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestTaskProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(IngestDocumentCommand command) {
        kafkaTemplate.send("rag.ingest.document", command.getDocumentId().toString(), command);
    }
}
