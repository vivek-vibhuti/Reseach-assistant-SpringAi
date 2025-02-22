package com.reserach.research_assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")  // Allow all origins for cross-origin requests
@RequestMapping("/api/research")  // Base URL path for this controller
@RequiredArgsConstructor   // Lombok annotation for constructor injection (removes need for default constructor)
public class ResearchController {

    private final ResearchService researchService;  // Inject ResearchService to call methods

    @RequestMapping("/process")  // Define endpoint to process content
    public ResponseEntity<String> processContent(@RequestBody ResearchRequest researchRequest) {
        // Call the processContent method of ResearchService and pass the researchRequest
        String result = researchService.processContent(researchRequest);

        // Return the result in the response body with HTTP status OK
        return ResponseEntity.ok(result);
    }
}
