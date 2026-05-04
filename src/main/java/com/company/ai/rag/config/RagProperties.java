package com.company.ai.rag.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Model model = new Model();
    private Retrieval retrieval = new Retrieval();
    private Chunk chunk = new Chunk();
    private Cache cache = new Cache();
    private Degrade degrade = new Degrade();
    private Ingest ingest = new Ingest();
    private Demo demo = new Demo();

    @Data
    public static class Model {
        private String chatModel;
        private String embeddingModel;
    }

    @Data
    public static class Retrieval {
        @Min(1)
        private int maxResults = 10;
        private double minScore = 0.65;
        @Min(1)
        private int rerankTopN = 5;
        @Min(1000)
        private int maxContextTokens = 5000;
    }

    @Data
    public static class Chunk {
        @Min(100)
        private int maxSize = 600;
        @Min(0)
        private int overlapSize = 80;
    }

    @Data
    public static class Cache {
        @Min(1)
        private int answerTtlMinutes = 10;
        @Min(1)
        private int queryRewriteTtlMinutes = 30;
    }

    @Data
    public static class Degrade {
        private boolean enableKeywordFallback = true;
    }

    @Data
    public static class Ingest {
        private boolean kafkaEnabled;
    }

    @Data
    public static class Demo {
        private boolean loadSampleOnStartup = true;
        private String sampleTenantId;
        private String sampleUserId;
        private String sampleKnowledgeBaseId;
        private String sampleDatasetVersion;
    }
}
