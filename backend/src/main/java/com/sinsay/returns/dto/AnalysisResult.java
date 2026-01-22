package com.sinsay.returns.dto;

public record AnalysisResult(
        String verdict,
        double confidence,
        String reasoning,
        String suggestedAction) {
}
