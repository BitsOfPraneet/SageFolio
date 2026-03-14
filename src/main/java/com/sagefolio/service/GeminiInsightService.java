package com.sagefolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class GeminiInsightService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={key}";

    public String generateInsight(String symbol, String volatility, String trend,
                                  double returnPct, String compatibility, String investorProfile) {
        // If no key configured, return a fallback message
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_FREE_GEMINI_API_KEY")) {
            return generateFallbackInsight(symbol, volatility, trend, returnPct, compatibility, investorProfile);
        }

        String prompt = String.format("""
            You are a financial literacy assistant for beginner investors in India.
            Explain simply. No jargon. Max 80 words.
            Never recommend buying or selling. Be educational only.

            Stock: %s | Volatility: %s | Trend: %s
            1-Year Return: %.1f%% | Compatibility: %s | Investor: %s

            Write a plain-English educational insight for this beginner investor.
            """, symbol, volatility, trend, returnPct, compatibility, investorProfile);

        try {
            Map<String, Object> body = Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                    })
                }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                GEMINI_URL, request, String.class, Map.of("key", apiKey)
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText("Unable to generate insight.");
        } catch (Exception e) {
            return generateFallbackInsight(symbol, volatility, trend, returnPct, compatibility, investorProfile);
        }
    }

    private String generateFallbackInsight(String symbol, String volatility, String trend,
                                           double returnPct, String compatibility, String investorProfile) {
        String direction = returnPct >= 0 ? "gained" : "lost";
        String absReturn = String.format("%.1f%%", Math.abs(returnPct));
        String trendAdj = trend.equals("Uptrend") ? "currently trading above its 20-day average, suggesting short-term positive momentum" :
                                                    "currently trading below its 20-day average, suggesting short-term caution";
        return String.format(
            "%s has %s %s over the past year and is %s. With %s volatility, it is rated '%s' for a %s investor. " +
            "Always consider diversifying your portfolio and consult a financial advisor before investing.",
            symbol, direction, absReturn, trendAdj, volatility.toLowerCase(), compatibility, investorProfile
        );
    }
}
