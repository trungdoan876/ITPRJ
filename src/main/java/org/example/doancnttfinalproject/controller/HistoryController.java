package org.example.doancnttfinalproject.controller;

import org.example.doancnttfinalproject.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HistoryController {

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);
    private final HistoryService deepseekService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HistoryController(HistoryService deepseekService, JdbcTemplate jdbcTemplate) {
        this.deepseekService = deepseekService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChatRequest(@RequestParam Map<String, String> formData, OAuth2AuthenticationToken authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            String prompt = formData.get("prompt");
            String trainContent = formData.getOrDefault("trainContent", "");
            String conversationId = formData.getOrDefault("conversationId", String.valueOf(System.currentTimeMillis()));

            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("error", "Prompt cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            String deepseekResponse = deepseekService.getDeepseekResponse(prompt, trainContent, conversationId);
            response.put("response", deepseekResponse);

            // Lưu vào SQLite
            jdbcTemplate.update(
                    "INSERT INTO conversations (email, conversation_id, prompt, response, timestamp) VALUES (?, ?, ?, ?, ?)",
                    email, conversationId, prompt, deepseekResponse, System.currentTimeMillis()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chat request: {}", e.getMessage(), e);
            response.put("error", "Failed to process request: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/save-conversation")
    public ResponseEntity<Map<String, String>> saveConversation(@RequestBody Map<String, Object> payload, OAuth2AuthenticationToken authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            String conversationId = (String) payload.get("conversationId");
            String prompt = (String) payload.get("prompt");
            String reply = (String) payload.get("response");

            if (conversationId == null || prompt == null || reply == null) {
                response.put("error", "Missing required fields: conversationId, prompt, or response");
                return ResponseEntity.badRequest().body(response);
            }

            // Lưu vào SQLite
            jdbcTemplate.update(
                    "INSERT INTO conversations (email, conversation_id, prompt, response, timestamp) VALUES (?, ?, ?, ?, ?)",
                    email, conversationId, prompt, reply, System.currentTimeMillis()
            );

            response.put("message", "Conversation saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving conversation: {}", e.getMessage(), e);
            response.put("error", "Failed to save conversation: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/load-conversations")
    public ResponseEntity<List<Map<String, Object>>> loadConversations(OAuth2AuthenticationToken authentication) {
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            List<Map<String, Object>> conversations = jdbcTemplate.query(
                    "SELECT conversation_id, prompt, response, timestamp FROM conversations WHERE email = ? ORDER BY timestamp DESC",
                    new Object[]{email},
                    (rs, rowNum) -> {
                        Map<String, Object> conv = new HashMap<>();
                        conv.put("conversationId", rs.getString("conversation_id"));
                        conv.put("prompt", rs.getString("prompt"));
                        conv.put("response", rs.getString("response"));
                        conv.put("timestamp", rs.getLong("timestamp"));
                        return conv;
                    }
            );
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            logger.error("Error loading conversations: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @DeleteMapping("/delete-conversation")
    public ResponseEntity<Map<String, String>> deleteConversation(@RequestParam String conversationId, OAuth2AuthenticationToken authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            int rowsAffected = jdbcTemplate.update(
                    "DELETE FROM conversations WHERE email = ? AND conversation_id = ?",
                    email, conversationId
            );
            if (rowsAffected > 0) {
                response.put("message", "Conversation deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Conversation not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            logger.error("Error deleting conversation: {}", e.getMessage(), e);
            response.put("error", "Failed to delete conversation: " + e.getMessage());
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