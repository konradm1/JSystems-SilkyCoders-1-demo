package com.sinsay.returns.service.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class PolicyVectorStoreConfig {

    private static final Logger logger = LoggerFactory.getLogger(PolicyVectorStoreConfig.class);

    @Value("classpath:data/policies.json")
    private Resource policiesResource;

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        return store;
    }

    @Bean
    public PolicyIngestor policyIngestor(SimpleVectorStore vectorStore) {
        return new PolicyIngestor(vectorStore, policiesResource);
    }

    public static class PolicyIngestor {
        private final SimpleVectorStore vectorStore;
        private final Resource policiesResource;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public PolicyIngestor(SimpleVectorStore vectorStore, Resource policiesResource) {
            this.vectorStore = vectorStore;
            this.policiesResource = policiesResource;
        }

        @PostConstruct
        public void init() {
            try {
                List<Map<String, String>> rawPolicies = objectMapper.readValue(
                        policiesResource.getInputStream(),
                        new TypeReference<List<Map<String, String>>>() {
                        });

                List<Document> documents = rawPolicies.stream()
                        .map(policy -> new Document(
                                policy.get("text"),
                                Map.of("id", policy.get("id"))))
                        .collect(Collectors.toList());

                vectorStore.add(documents);
                logger.info("Loaded {} policies into Vector Store", documents.size());
            } catch (IOException e) {
                logger.error("Failed to load policies", e);
                throw new RuntimeException(e);
            }
        }
    }
}
