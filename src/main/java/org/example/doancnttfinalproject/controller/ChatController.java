package org.example.doancnttfinalproject.controller;

import org.example.doancnttfinalproject.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @PostMapping("/send-chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> body,
                                       OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        String userMessage = body.get("message");
        String reply = chatService.sendMessageToFlask(userMessage, email, name);
        return ResponseEntity.ok(reply);
    }
}
