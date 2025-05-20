package org.example.doancnttfinalproject.controller;

import org.example.doancnttfinalproject.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/send-chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> body,
                                       OAuth2AuthenticationToken authentication) {
        try {
            OAuth2User user = authentication.getPrincipal();
            String email = user.getAttribute("email");
            String name = user.getAttribute("name");
            String userMessage = body.get("message");
            String conversationId = body.getOrDefault("conversationId", String.valueOf(System.currentTimeMillis()));

            String reply = chatService.sendMessageToFlask(userMessage, email, name);

            // Lưu vào SQLite
            jdbcTemplate.update(
                    "INSERT INTO conversations (email, conversation_id, prompt, response, timestamp) VALUES (?, ?, ?, ?, ?)",
                    email, conversationId, userMessage, reply, System.currentTimeMillis()
            );

            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/reset")
    public ResponseEntity<String> resetConversation(OAuth2AuthenticationToken authentication) {
        try {
            OAuth2User user = authentication.getPrincipal();
            String email = user.getAttribute("email");
            String reply = chatService.reset(email);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

}