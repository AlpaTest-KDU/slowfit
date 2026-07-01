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
import java.util.concurrent.CompletableFuture;

@Service
public class ImageModerationService {

    private static final String VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate";
    private static final List<String> UNSAFE_SEVERITIES = List.of("LIKELY", "VERY_LIKELY");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public ImageModerationService(@Value("${google.cloud.vision.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> moderateImageAsync(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "requests", List.of(Map.of(
                            "image", Map.of("source", Map.of("imageUri", imageUrl)),
                            "features", List.of(Map.of("type", "SAFE_SEARCH_DETECTION"))
                    ))
            );

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    VISION_API_URL + "?key=" + apiKey,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            String responseBody = responseEntity.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                return CompletableFuture.completedFuture(false);
            }

            Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            if (response == null || response.get("responses") == null) {
                return CompletableFuture.completedFuture(false);
            }

            List<Map<String, Object>> responses = extractResponses(response);
            if (responses.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            Map<String, Object> safeSearch = extractSafeSearch(responses.get(0));
            if (safeSearch == null) {
                return CompletableFuture.completedFuture(false);
            }

            boolean unsafe = isUnsafe(safeSearch.get("adult"))
                    || isUnsafe(safeSearch.get("spoof"))
                    || isUnsafe(safeSearch.get("medical"))
                    || isUnsafe(safeSearch.get("violence"))
                    || isUnsafe(safeSearch.get("racy"));

            return CompletableFuture.completedFuture(unsafe);
        } catch (IllegalArgumentException | RestClientException | JsonProcessingException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractResponses(Map<String, Object> response) {
        Object responses = response.get("responses");
        if (responses instanceof List<?> responseList) {
            return responseList.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractSafeSearch(Map<String, Object> response) {
        Object safeSearch = response.get("safeSearchAnnotation");
        if (safeSearch instanceof Map<?, ?> safeSearchMap) {
            return (Map<String, Object>) safeSearchMap;
        }
        return null;
    }

    private boolean isUnsafe(Object severity) {
        return severity instanceof String severityText
                && UNSAFE_SEVERITIES.contains(severityText.toUpperCase());
    }
}
