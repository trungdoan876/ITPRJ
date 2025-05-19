package org.example.doancnttfinalproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class HistoryService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";

    @Value("${deepseek.api.key}")
    private String DEEPSEEK_API_KEY;

    // Hàm làm sạch chuỗi, loại bỏ ký tự điều khiển
    private String sanitizeString(String input) {
        if (input == null) return "";
        // Loại bỏ các ký tự điều khiển (0x00-0x1F) ngoại trừ newline (\n) và tab (\t)
        return input.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    }

    public String getDeepseekResponse(String prompt, String trainContent, String conversationId) throws Exception {
        if (DEEPSEEK_API_KEY == null || DEEPSEEK_API_KEY.trim().isEmpty()) {
            logger.error("DeepSeek API key chưa được cung cấp");
            throw new IllegalStateException("DeepSeek API key chưa được cung cấp");
        }

        String systemContext = trainContent.isEmpty() ? "Không có ngữ cảnh huấn luyện." : trainContent;
        String cleanSystemContext = sanitizeString(systemContext);
        String cleanPrompt = sanitizeString(prompt);

        logger.info("System Context: {}", cleanSystemContext);
        logger.info("Prompt: {}", cleanPrompt);

        String requestBody = String.format(
                "{\"model\": \"deepseek-chat\", \"messages\": [{\"role\": \"system\", \"content\": \"%s\"}, {\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 150}",
                cleanSystemContext.replace("\"", "\\\""),
                cleanPrompt.replace("\"", "\\\"")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode jsonResponse = mapper.readTree(response.body());
            if (jsonResponse.has("choices") && jsonResponse.get("choices").isArray() && !jsonResponse.get("choices").isEmpty()) {
                JsonNode choice = jsonResponse.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    return choice.get("message").get("content").asText();
                }
                throw new IOException("Phản hồi từ DeepSeek không chứa nội dung hợp lệ");
            }
            throw new IOException("Phản hồi từ DeepSeek không chứa choices");
        }
        throw new IOException("Lỗi API DeepSeek: " + response.statusCode() + ", " + response.body());
    }
}