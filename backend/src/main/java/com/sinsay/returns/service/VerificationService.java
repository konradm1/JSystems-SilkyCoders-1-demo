package com.sinsay.returns.service;

import com.sinsay.returns.dto.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String modelName;

    public VerificationService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> chat(List<Message> messages, String intent) {
        // 1. Convert DTOs to Spring AI Messages
        List<org.springframework.ai.chat.messages.Message> springMessages = new ArrayList<>();

        // 2. Identify Policy Context (RAG)
        // We assume the last user message contains the relevant info for RAG if it's
        // the start,
        // or we use a general intent. For PoC, let's inject policy based on Intent.

        String policyContext = "";
        if (messages != null && !messages.isEmpty()) {
            // Basic RAG strategy: Use the intent or the first user message to find policy
            String query = (intent != null && !intent.isEmpty()) ? intent : messages.get(0).content();
            List<Document> similarDocuments = vectorStore
                    .similaritySearch(SearchRequest.builder().query(query).topK(2).build());
            policyContext = similarDocuments.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n---\n"));
        }

        // 3. Construct System Prompt
        String systemPrompt = """
                You are an expert returns verification agent for Sinsay.
                Your goal is to analyze customer photos and descriptions to determine if a return or complaint is valid.

                Intent: %s

                Policy Context:
                %s

                Instructions:
                - Think in English, but ALWAYS reply in Polish.
                - Be polite, professional, and empathetic.
                - If the image shows clear damage or wear described in the policy as invalid, respectfully reject.
                - If ambiguous, ask for more details or a better photo.
                - Format your verdict clearly.
                """.formatted(intent, policyContext);

        springMessages.add(new SystemMessage(systemPrompt));

        for (Message msg : messages) {
            switch (msg.role()) {
                case "user" -> springMessages.add(new UserMessage(msg.content()));
                case "assistant" -> springMessages.add(new AssistantMessage(msg.content()));
                case "system" -> {
                    /* skip, we injected our own system message */ }
            }
        }

        // 4. Stream Response
        return chatClient.prompt()
                .messages(springMessages)
                .stream()
                .content();
    }
}
