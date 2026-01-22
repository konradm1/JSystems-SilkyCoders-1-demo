package com.sinsay.returns.controller;

import com.sinsay.returns.service.ReturnAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnsController {

    private final ReturnAnalysisService analysisService;

    @PostMapping(value = "/analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> analyzeReturn(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) throws IOException {
        return analysisService.analyzeReturn(file, description);
    }
}
