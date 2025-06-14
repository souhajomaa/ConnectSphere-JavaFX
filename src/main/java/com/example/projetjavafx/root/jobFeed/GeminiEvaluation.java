package com.example.projetjavafx.root.jobFeed;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class GeminiEvaluation {

    private static final String API_KEY = "AIzaSyASzUusx2wKgSf2YPbgRdu-PRydF2dWpls";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static void main(String[] args) {
        String jobDescription = "example job description";
        String coverLetter = "example cover letter";

        String inputText = "Job Description: " + jobDescription + "\nCover Letter: " + coverLetter;

        try {
            String response = sendPostRequest(inputText);
            System.out.println("API Response: " + response);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sendPostRequest(String inputText) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{\"contents\": [    {        \"role\": \"user\",        \"parts\": [            {                \"text\": \"" + inputText + "\"            }        ]    }],\"systemInstruction\": {    \"role\": \"user\",    \"parts\": [        {            \"text\": \"for the provided job description and cover letter, you will always give an integer evaluation of the cover from 1 to 10\"        }    ]},\"generationConfig\": {    \"temperature\": 1,    \"topK\": 40,    \"topP\": 0.95,    \"maxOutputTokens\": 8192,    \"responseMimeType\": \"application/json\",    \"responseSchema\": {        \"type\": \"object\",        \"properties\": {            \"rating\": {                \"type\": \"number\"            }        }    }}}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

    private static int parseRatingFromResponse(String jsonResponse) {
        // Assuming the response is in the format: {"rating": 7}
        // You can use a JSON library like Jackson or Gson for more complex parsing
        int startIndex = jsonResponse.indexOf("\"rating\":") + 9;
        int endIndex = jsonResponse.indexOf("}", startIndex);
        String ratingStr = jsonResponse.substring(startIndex, endIndex).trim();
        return Integer.parseInt(ratingStr);
    }
}
