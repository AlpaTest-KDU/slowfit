package com.slowfit.slowfit.global.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class TextModerationService {

    private static final String MODERATION_API_URL = "https://api.openai.com/v1/moderations";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public TextModerationService(@Value("${openai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> moderateTextAsync(String text) {
        if (text == null || text.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.setBearerAuth(Objects.requireNonNull(apiKey));
            }

            Map<String, Object> requestBody = Map.of("input", text);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    MODERATION_API_URL,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            String responseBody = responseEntity.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                return CompletableFuture.completedFuture(false);
            }

            Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            if (response == null || response.get("results") == null) {
                return CompletableFuture.completedFuture(false);
            }

            List<Map<String, Object>> results = extractResults(response);
            if (results.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            Map<String, Object> firstResult = results.get(0);
            Object flagged = firstResult.get("flagged");

            boolean isFlagged = flagged instanceof Boolean && (Boolean) flagged;
            return CompletableFuture.completedFuture(isFlagged);
        } catch (IllegalArgumentException | RestClientException | JsonProcessingException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractResults(Map<String, Object> response) {
        Object results = response.get("results");
        if (results instanceof List<?> resultList) {
            return resultList.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }
}
