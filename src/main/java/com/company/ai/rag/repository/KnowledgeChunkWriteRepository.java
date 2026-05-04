package com.company.ai.rag.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KnowledgeChunkWriteRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void replaceDocumentChunks(UUID documentId, List<ChunkRecord> chunks) {
        jdbcTemplate.update("DELETE FROM knowledge_chunk WHERE document_id = ?", documentId);

        String sql = """
                INSERT INTO knowledge_chunk (
                    id, document_id, tenant_id, knowledge_base_id,
                    dataset_version, chunk_no, title_path, content,
                    metadata, embedding
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS vector))
                """;

        for (ChunkRecord chunk : chunks) {
            jdbcTemplate.update(
                    sql,
                    chunk.getId(),
                    chunk.getDocumentId(),
                    chunk.getTenantId(),
                    chunk.getKnowledgeBaseId(),
                    chunk.getDatasetVersion(),
                    chunk.getChunkNo(),
                    chunk.getTitlePath(),
                    chunk.getContent(),
                    toJsonb(chunk.getMetadata()),
                    chunk.getEmbeddingVectorLiteral()
            );
        }
    }

    private PGobject toJsonb(Map<String, Object> metadata) {
        PGobject object = new PGobject();
        object.setType("jsonb");
        try {
            object.setValue(objectMapper.writeValueAsString(metadata));
            return object;
        } catch (JsonProcessingException | SQLException e) {
            throw new IllegalStateException("Failed to serialize chunk metadata", e);
        }
    }

    @Value
    @Builder
    public static class ChunkRecord {
        UUID id;
        UUID documentId;
        String tenantId;
        String knowledgeBaseId;
        String datasetVersion;
        Integer chunkNo;
        String titlePath;
        String content;
        Map<String, Object> metadata;
        String embeddingVectorLiteral;
    }
}
