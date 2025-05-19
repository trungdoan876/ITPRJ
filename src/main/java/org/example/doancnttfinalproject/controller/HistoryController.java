package org.example.doancnttfinalproject.controller;
import org.example.doancnttfinalproject.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HistoryController {

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);
    private final HistoryService deepseekService;

    public HistoryController(HistoryService deepseekService) {
        this.deepseekService = deepseekService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChatRequest(@RequestParam Map<String, String> formData) {
        Map<String, String> response = new HashMap<>();
        try {
            String prompt = formData.get("prompt");
            String trainContent = formData.getOrDefault("trainContent", "");
            String conversationId = formData.getOrDefault("conversationId", String.valueOf(System.currentTimeMillis()));

            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("error", "Prompt cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            String deepseekResponse = deepseekService.getDeepseekResponse(prompt, trainContent, conversationId);
            response.put("response", deepseekResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chat request: {}", e.getMessage(), e);
            response.put("error", "Failed to process request: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/save-conversation")
    public ResponseEntity<Map<String, String>> saveConversation(@RequestBody Map<String, Object> payload) {
        Map<String, String> response = new HashMap<>();
        try {
            String conversationId = (String) payload.get("conversationId");
            String prompt = (String) payload.get("prompt");
            String reply = (String) payload.get("response");

            if (conversationId == null || prompt == null || reply == null) {
                response.put("error", "Missing required fields: conversationId, prompt, or response");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("message", "Conversation saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving conversation: {}", e.getMessage(), e);
            response.put("error", "Failed to save conversation: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/train-content")
    public ResponseEntity<Map<String, String>> getTrainContent() {
        Map<String, String> response = new HashMap<>();
        try {
            Path filePath = Path.of("src/main/resources/static/data/train.txt");
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            response.put("trainContent", content);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error reading train.txt: {}", e.getMessage(), e);
            response.put("error", "Failed to read train.txt: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}