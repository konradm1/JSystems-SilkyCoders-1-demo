package com.sinsay.returns.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinsay.returns.dto.ChatRequest;
import com.sinsay.returns.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final VerificationService verificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        return verificationService.chat(request.messages(), request.intent())
                .map(this::formatVercelMessage);
    }

    /**
     * Vercel AI SDK expects text chunks prefixed with '0:' and JSON encoded
     * strings.
     * Example: 0:"Hello"
     */
    private String formatVercelMessage(String chunk) {
        if (chunk == null)
            return "";
        try {
            // Escape special JSON characters (newlines, quotes)
            String jsonEscaped = objectMapper.writeValueAsString(chunk);
            return "0:" + jsonEscaped + "\n";
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
