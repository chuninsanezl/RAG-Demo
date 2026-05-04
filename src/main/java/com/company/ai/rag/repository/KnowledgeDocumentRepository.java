package com.company.ai.rag.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public boolean exists(String tenantId, String knowledgeBaseId, String datasetVersion, String fileName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM knowledge_document
                WHERE tenant_id = ?
                  AND knowledge_base_id = ?
                  AND dataset_version = ?
                  AND file_name = ?
                """,
                Integer.class,
                tenantId,
                knowledgeBaseId,
                datasetVersion,
                fileName
        );
        return count != null && count > 0;
    }

    public void upsert(
            UUID id,
            String tenantId,
            String knowledgeBaseId,
            String datasetVersion,
            String fileName,
            String contentType,
            String sourceUri,
            String status
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO knowledge_document (
                    id, tenant_id, knowledge_base_id, dataset_version,
                    file_name, content_type, source_uri, status, enabled
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                ON CONFLICT (id) DO UPDATE
                SET file_name = EXCLUDED.file_name,
                    content_type = EXCLUDED.content_type,
                    source_uri = EXCLUDED.source_uri,
                    status = EXCLUDED.status,
                    updated_at = CURRENT_TIMESTAMP
                """,
                id,
                tenantId,
                knowledgeBaseId,
                datasetVersion,
                fileName,
                contentType,
                sourceUri,
                status
        );
    }
}
