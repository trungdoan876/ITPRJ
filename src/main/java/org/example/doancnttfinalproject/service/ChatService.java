package org.example.doancnttfinalproject.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {
    private final String flaskApiUrl = "https://web-production-27cf.up.railway.app/chat";
    private final String flaskApiUrlReset = "https://web-production-27cf.up.railway.app/reset";
    private RestTemplate restTemplate = new RestTemplate();

    public String sendMessageToFlask(String userMessage, String email, String name) {
        // Tạo body JSON
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", userMessage);
        requestBody.put("name", name);
        requestBody.put("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(flaskApiUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("reply");
        } else {
            return "Lỗi khi gọi API Flask";
        }
    }
    public String reset(String email) {
        // Tạo body JSON
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(flaskApiUrlReset, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("reply");
        } else {
            return "Lỗi khi gọi API Flask";
        }
    }
}
