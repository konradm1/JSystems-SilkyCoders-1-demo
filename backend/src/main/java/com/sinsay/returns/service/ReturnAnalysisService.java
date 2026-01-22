package com.sinsay.returns.service;

import com.sinsay.returns.util.ImageUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReturnAnalysisService {

        private final ChatClient chatClient;
        private final VectorStore vectorStore;

        @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
        private String modelName;

        public ReturnAnalysisService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
                this.chatClient = chatClientBuilder.build();
                this.vectorStore = vectorStore;
        }

        public Flux<String> analyzeReturn(MultipartFile image, String description) throws IOException {
                ImageUtils.validateImage(image);

                // 1. RAG Retrieval
                List<Document> similarDocuments = vectorStore
                                .similaritySearch(SearchRequest.builder().query(description).topK(2).build());
                String policyContext = similarDocuments.stream()
                                .map(Document::getText)
                                .collect(Collectors.joining("\n---\n"));

                // 2. Prompt Construction
                String systemPrompt = """
                                You are an expert returns agent for Sinsay.
                                Analyze the customer's photo and description against the return policy provided below.

                                Policy Context:
                                %s

                                Respond with a valid JSON object (no markdown formatting) containing:
                                - verdict: "ACCEPTED", "REJECTED", or "MANUAL_REVIEW"
                                - confidence: double between 0.0 and 1.0
                                - reasoning: concise explanation referencing the specific policy section
                                - suggestedAction: what to tell the customer
                                """.formatted(policyContext);

                UserMessage userMessage = new UserMessage(
                                "User Description: " + description,
                                List.of(new Media(MimeTypeUtils.parseMimeType(image.getContentType()),
                                                new InputStreamResource(image.getInputStream()))));

                // 3. Multimodal Analysis (Streaming)
                return chatClient.prompt()
                                .system(systemPrompt)
                                .messages(userMessage)
                                .stream()
                                .content();
        }
}
