package com.company.ai.rag.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagAutoConfiguration {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${dashscope.api-key}") String apiKey,
            RagProperties properties
    ) {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(properties.getModel().getChatModel())
                .temperature(0.2f)
                .topP(0.8)
                .maxTokens(1200)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${dashscope.api-key}") String apiKey,
            RagProperties properties
    ) {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(properties.getModel().getEmbeddingModel())
                .build();
    }

    @Bean
    public Cache<String, String> answerCache(RagProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(Duration.ofMinutes(properties.getCache().getAnswerTtlMinutes()))
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, String> queryRewriteCache(RagProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterWrite(Duration.ofMinutes(properties.getCache().getQueryRewriteTtlMinutes()))
                .recordStats()
                .build();
    }
}
