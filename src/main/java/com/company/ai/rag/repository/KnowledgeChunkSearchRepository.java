package com.company.ai.rag.repository;

import com.company.ai.rag.service.retrieval.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class KnowledgeChunkSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<RetrievedChunk> searchByEmbedding(
            String tenantId,
            String knowledgeBaseId,
            String datasetVersion,
            String embeddingVectorLiteral,
            int limit
    ) {
        String sql = """
                SELECT kc.id,
                       kc.document_id,
                       kc.chunk_no,
                       kc.content,
                       kc.metadata,
                       kd.file_name,
                       kc.title_path,
                       1 - (kc.embedding <=> CAST(? AS vector)) AS score
                FROM knowledge_chunk kc
                JOIN knowledge_document kd ON kd.id = kc.document_id
                WHERE kc.tenant_id = ?
                  AND kc.knowledge_base_id = ?
                  AND (? IS NULL OR kc.dataset_version = ?)
                ORDER BY kc.embedding <=> CAST(? AS vector)
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> map(rs),
                embeddingVectorLiteral,
                tenantId,
                knowledgeBaseId,
                datasetVersion,
                datasetVersion,
                embeddingVectorLiteral,
                limit
        );
    }

    private RetrievedChunk map(ResultSet rs) throws SQLException {
        return RetrievedChunk.builder()
                .chunkId(rs.getObject("id", UUID.class))
                .documentId(rs.getObject("document_id", UUID.class))
                .chunkNo(rs.getInt("chunk_no"))
                .content(rs.getString("content"))
                .fileName(rs.getString("file_name"))
                .titlePath(rs.getString("title_path"))
                .score(rs.getDouble("score"))
                .metadata(java.util.Map.of())
                .build();
    }
}
