package org.gdgoc.server.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final RetirementCounselingService counselingService;

    @PostMapping("/{userId}")
    public ResponseEntity<String> chat(
            @PathVariable String userId,
            @RequestBody ChatRequest request) {
        String response = counselingService.processUserMessage(userId, request.getMessage());
        return ResponseEntity.ok(response);
    }

    @Data
    public static class ChatRequest {
        String message;
    }
}
