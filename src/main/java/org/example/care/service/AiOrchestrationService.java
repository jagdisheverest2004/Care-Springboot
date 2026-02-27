package org.example.care.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.example.care.dto.SafetyCheckRequest;
import org.example.care.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@SuppressWarnings("null")
public class AiOrchestrationService {

    private final WebClient aiWebClient;

    @Autowired
    private PatientService patientService;

    public AiOrchestrationService(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public Map<String, Object> analyzeXray(MultipartFile file) {
        // For X-rays, Flask expects the key "file"
        MultipartBodyBuilder bodyBuilder = multipartBody(file, "file");
        return aiWebClient.post()
                .uri("/analyze_xray")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public String summarizeReports(MultipartFile file) {
        // CRITICAL FIX: Flask expects the key "report" for this endpoint
        MultipartBodyBuilder bodyBuilder = multipartBody(file, "report");
        return aiWebClient.post()
                .uri("/summarize_reports")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Map<String, Object> checkSafety(SafetyCheckRequest request) {
        Patient patient = patientService.getPatientById(request.getPatientId());
        List<String> currentDrugs = patient.getCurrentMeds();
        Map<String, Object> requestBody = Map.of(
                "patient_id", patient.getId(),
                "current_meds", currentDrugs,
                "new_drugs", request.getNewDrugs()
        );

        return aiWebClient.post()
                .uri("/check_safety")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Objects.requireNonNull(requestBody))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
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
}