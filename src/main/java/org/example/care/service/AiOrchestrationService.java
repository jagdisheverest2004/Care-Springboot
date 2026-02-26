package org.example.care.service;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.example.care.dto.SafetyCheckRequest;
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

    public AiOrchestrationService(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public Map<String, Object> analyzeXray(MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = multipartBody(file);
        return aiWebClient.post()
                .uri("/analyze_xray")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    public String summarizeReports(MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = multipartBody(file);
        return aiWebClient.post()
                .uri("/summarize_reports")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Map<String, Object> checkSafety(SafetyCheckRequest request) {
        return aiWebClient.post()
                .uri("/check_safety")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Objects.requireNonNull(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    private MultipartBodyBuilder multipartBody(MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        try {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            bodyBuilder.part("file", file.getBytes())
                    .filename(fileName)
                    .contentType(MediaType.parseMediaType(contentType));
            return bodyBuilder;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to process uploaded file");
        }
    }
}
