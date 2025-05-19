package org.example.doancnttfinalproject.controller;

import org.example.doancnttfinalproject.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        String reply = chatService.sendMessageToFlask(userMessage);
        return ResponseEntity.ok(reply);
    }
}
