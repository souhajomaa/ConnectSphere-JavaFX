package com.example.projetjavafx.root.jobApplications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LLMService {
    private static final String OLLAMA_ENDPOINT = "http://localhost:11434/api/generate";

    public static int getCoverLetterRating(String jobDescription, String coverLetter) {
        try {
            // Sanitize inputs (escape newlines and quotes)
            String sanitizedJobDesc = jobDescription.replace("\n", "\\n").replace("\"", "\\\"");
            String sanitizedCoverLetter = coverLetter.replace("\n", "\\n").replace("\"", "\\\"");

            String prompt = String.format(
                    "Rate the following cover letter from 0 to 10 based on its relevance to the job description.\\n\\nJob Description:\\n%s\\n\\nCover Letter:\\n%s",
                    sanitizedJobDesc, sanitizedCoverLetter
            );

            String requestBody = String.format(
                    "{\"model\": \"qwen2.5:3b\", \"prompt\": \"%s\", \"stream\": false}",
                    prompt.replace("\"", "\\\"") // Double-escape quotes
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonResponse = new ObjectMapper().readTree(response.body());

            // Check for Ollama errors
            if (jsonResponse.has("error")) {
                System.err.println("LLM Error: " + jsonResponse.get("error").asText());
                return 0;
            }

            // Extract response (handle different formats)
            JsonNode outputNode = jsonResponse.get("response");
            if (outputNode == null) {
                outputNode = jsonResponse.path("choices").path(0).path("text");
            }

            if (outputNode == null || outputNode.isNull()) {
                System.err.println("Unexpected response format: " + jsonResponse);
                return 0;
            }

            String output = outputNode.asText().trim();
            return parseRating(output);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int parseRating(String output) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)(/10|out of 10)?");
        java.util.regex.Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        System.err.println("Failed to parse rating from: " + output);
        return 0;
    }
}