CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS knowledge_document (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(64) NOT NULL,
    knowledge_base_id VARCHAR(64) NOT NULL,
    dataset_version   VARCHAR(32) NOT NULL,
    file_name         VARCHAR(256) NOT NULL,
    content_type      VARCHAR(64) NOT NULL,
    source_uri        VARCHAR(1024),
    status            VARCHAR(32) NOT NULL,
    enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id                UUID PRIMARY KEY,
    document_id       UUID NOT NULL,
    tenant_id         VARCHAR(64) NOT NULL,
    knowledge_base_id VARCHAR(64) NOT NULL,
    dataset_version   VARCHAR(32) NOT NULL,
    chunk_no          INT NOT NULL,
    title_path        VARCHAR(512),
    content           TEXT NOT NULL,
    metadata          JSONB NOT NULL,
    embedding         VECTOR(1536) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chunk_doc ON knowledge_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_tenant_kb ON knowledge_chunk(tenant_id, knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_chunk_embedding_hnsw
ON knowledge_chunk
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

CREATE TABLE IF NOT EXISTS ingest_task (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(64) NOT NULL,
    knowledge_base_id VARCHAR(64) NOT NULL,
    document_id       UUID NOT NULL,
    status            VARCHAR(32) NOT NULL,
    retry_count       INT NOT NULL DEFAULT 0,
    error_message     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
