package org.example.care.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotEmpty;
import org.example.care.exception.GlobalExceptionHandler;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@SuppressWarnings("null")
public class AiOrchestrationService {

    private final WebClient aiWebClient;

    public AiOrchestrationService(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public String analyzeXray(MultipartFile file) {
        // For X-rays, Flask expects the key "file"
        log.info("Initiating request to Flask AI engine for X-ray analysis. File: {}", file.getOriginalFilename());
        try{
            MultipartBodyBuilder bodyBuilder = multipartBody(file, "file");
            List<Map<String,Object>> responseList = aiWebClient.post()
                    .uri("/analyze_xray")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            log.info("Successfully received X-ray analysis response from Flask API.");

            if (responseList != null && !responseList.isEmpty()) {
                Map<String, Object> firstResult = responseList.get(0);

                // Extract ONLY the generated_report value
                if (firstResult.containsKey("generated_report")) {
                    String rawReport = (String) firstResult.get("generated_report");
                    // Clean and return the string using the method we created earlier
                    return cleanSummaryText(rawReport);
                }
            }
            throw new RuntimeException("Flask API returned an empty analysis result");
        }
        catch (Exception e) {
            log.error("Error during X-ray analysis request: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze X-ray: " + e.getMessage());
        }
    }

    public String summarizeReports(MultipartFile file) {
        // CRITICAL FIX: Flask expects the key "report" for this endpoint
        log.info("Initiating request to Flask AI engine for report summarization. File: {}", file.getOriginalFilename());
        try{
            MultipartBodyBuilder bodyBuilder = multipartBody(file, "report");
            List<Map<String,Object>> responseList = aiWebClient.post()
                    .uri("/summarize_reports")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            log.info("Successfully received summarization response from Flask API.");

            if (responseList != null && !responseList.isEmpty()) {
                Map<String, Object> firstResult = responseList.get(0);

                if (firstResult.containsKey("summary")) {
                    String rawSummary = (String) firstResult.get("summary");
                    // Clean and return the string
                    return cleanSummaryText(rawSummary);
                }
            }

            log.error("Flask API returned an empty or invalid summary result");
            throw new RuntimeException("Flask API returned an empty or invalid summary result");
        }
        catch (Exception e) {
            log.error("Error during report summarization request: {}", e.getMessage());
            throw new RuntimeException("Failed to summarize report: " + e.getMessage());
        }
    }

    public Map<String, Object> checkSafety(Long id, List<String> currentDrugNames, @NotEmpty List<String> newDrugs) {

        log.info("Initiating request to Flask AI engine for drug safety check. Patient ID: {}, Current Meds: {}, New Drugs: {}",
                id, currentDrugNames, newDrugs);
        try{
            Map<String, Object> requestBody = Map.of(
                    "patient_id", id,
                    "current_meds", currentDrugNames,
                    "new_drugs", newDrugs
            );

            Map<String,Object> response = aiWebClient.post()
                    .uri("/check_safety")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Objects.requireNonNull(requestBody))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            log.info("Successfully received drug safety check response from Flask API.");
            return response;
        }
        catch (Exception e) {
            log.error("Error during drug safety check request: {}", e.getMessage());
            throw new RuntimeException("Failed to check drug safety: " + e.getMessage());
        }
    }

    /**
     * Updated helper to accept a dynamic key name
     */
    private MultipartBodyBuilder multipartBody(MultipartFile file, String keyName) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        try {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            // Map the file to the specific keyName required by the Flask endpoint
            bodyBuilder.part(keyName, file.getBytes())
                    .filename(fileName)
                    .contentType(MediaType.parseMediaType(contentType));

            return bodyBuilder;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to process uploaded file");
        }
    }

    // --- NEW METHOD: Cleans out markdown, slashes, and newlines ---
    public String cleanSummaryText(String text) {
        if (text == null) return "";

        // Remove markdown asterisks (**)
        text = text.replace("*", "");
        // Remove backslashes (\)
        text = text.replace("\\", "");
        // Replace newlines and carriage returns with a space
        text = text.replace("\n", " ").replace("\r", " ");
        // Collapse multiple spaces into a single space
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}