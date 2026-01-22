package com.sinsay.returns.dto;

public record Message(
        String role, // "user", "assistant", "system"
        String content) {
}
