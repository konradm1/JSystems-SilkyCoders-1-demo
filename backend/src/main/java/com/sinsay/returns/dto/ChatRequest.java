package com.sinsay.returns.dto;

import java.util.List;

public record ChatRequest(
        List<Message> messages,
        String intent, // "return" or "complaint" (optional, mainly for initial context)
        String orderId // optional
) {
}
