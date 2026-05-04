package com.company.ai.rag.ingest;

import com.company.ai.rag.config.RagProperties;
import com.company.ai.rag.repository.KnowledgeChunkWriteRepository;
import com.company.ai.rag.repository.KnowledgeChunkWriteRepository.ChunkRecord;
import com.company.ai.rag.repository.KnowledgeDocumentRepository;
import com.company.ai.rag.service.retrieval.RetrievalService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexingService {

    private final RagProperties ragProperties;
    private final EmbeddingModel embeddingModel;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeChunkWriteRepository knowledgeChunkWriteRepository;

    public void index(IngestDocumentCommand command, String text, Map<String, Object> metadata) {
        Document document = Document.from(text, Metadata.from(metadata));
        List<TextSegment> segments = DocumentSplitters.recursive(
                ragProperties.getChunk().getMaxSize(),
                ragProperties.getChunk().getOverlapSize()
        ).split(document);

        knowledgeDocumentRepository.upsert(
                command.getDocumentId(),
                command.getTenantId(),
                command.getKnowledgeBaseId(),
                command.getDatasetVersion(),
                command.getFileName(),
                command.getContentType(),
                command.getObjectKey(),
                "READY"
        );

        List<ChunkRecord> chunkRecords = new ArrayList<>();
        int batchSize = 16;
        int chunkNo = 1;
        for (int i = 0; i < segments.size(); i += batchSize) {
            int end = Math.min(i + batchSize, segments.size());
            List<TextSegment> batch = new ArrayList<>(segments.subList(i, end));
            Response<List<Embedding>> embeddings = embeddingModel.embedAll(batch);
            for (int j = 0; j < batch.size(); j++) {
                TextSegment segment = batch.get(j);
                Map<String, Object> chunkMetadata = new HashMap<>(metadata);
                chunkMetadata.put("chunkNo", chunkNo);
                chunkRecords.add(ChunkRecord.builder()
                        .id(UUID.randomUUID())
                        .documentId(command.getDocumentId())
                        .tenantId(command.getTenantId())
                        .knowledgeBaseId(command.getKnowledgeBaseId())
                        .datasetVersion(command.getDatasetVersion())
                        .chunkNo(chunkNo)
                        .titlePath(command.getFileName())
                        .content(segment.text())
                        .metadata(chunkMetadata)
                        .embeddingVectorLiteral(RetrievalService.toVectorLiteral(embeddings.content().get(j).vectorAsList()))
                        .build());
                chunkNo++;
            }
            log.info("indexed chunk batch: {}/{}", end, segments.size());
        }

        knowledgeChunkWriteRepository.replaceDocumentChunks(command.getDocumentId(), chunkRecords);
    }
}
