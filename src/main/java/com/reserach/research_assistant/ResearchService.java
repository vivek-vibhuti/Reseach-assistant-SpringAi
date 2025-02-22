package com.reserach.research_assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ResearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Injected values for API URL and key
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Constructor injection
    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(geminiApiUrl).build();
        this.objectMapper = objectMapper;
    }

    public String processContent(ResearchRequest researchRequest) {
        // Build the prompt to send to the AI model
        String prompt = buildPrompt(researchRequest);

        // Prepare the request body for the API call
        Map<String, Object> requestBody = Map.of(
                "content", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // Make the API call and capture the response
        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder.path(geminiApiUrl + geminiApiKey).build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse the API response and return the text
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try {
            // Deserialize the response into a GeminiResponse object
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);

            // Check if the response contains candidates
            if (geminiResponse.getCandidatelist() != null && !geminiResponse.getCandidatelist().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidatelist().get(0); // Use get(0) instead of getFirst()

                // Check if the candidate has content and parts, then extract the text
                if (firstCandidate.getContent() != null && !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText(); // Use get(0) instead of getFirst()
                }
            }
        } catch (Exception e) {
            // Handle parsing errors
            return "Error parsing response: " + e.getMessage();
        }

        return "No valid content found in the response.";
    }

    private String buildPrompt(ResearchRequest researchRequest) {
        StringBuilder prompt = new StringBuilder();

        // Construct the prompt based on the operation requested
        switch (researchRequest.getOperation()) {
            case "Summarize":
                prompt.append("Provide a clear and concise summary of the following text in a few sentences:\n\n");
                break;
            case "Suggest":
                prompt.append("Based on the following content, suggest related topics and further readings, formatted with clear headings and bullet points.");
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + researchRequest.getOperation());
        }

        // Add the content from the request to the prompt
        prompt.append(researchRequest.getContent());
        return prompt.toString();
    }
}
