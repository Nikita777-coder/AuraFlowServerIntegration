package system.integration.meditationai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import system.integration.meditationai.dto.MeditationGenerationRequest;
import system.integration.meditationai.dto.MeditationStatus;
import system.service.RestService;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeditationAiService {
    private final RestService restService;

    @Value("${service-configs.meditation-generator-service.auth-jwt-secret}")
    private String token;
    @Value("${service-configs.meditation-generator-service.base-url}")
    private String baseUrl;
    @Value("${service-configs.meditation-generator-service.generate-path}")
    private String generatePath;
    @Value("${service-configs.meditation-generator-service.status-path}")
    private String statusUrl;
    public UUID generateMeditation(MeditationGenerationRequest meditationGenerationRequest) {
        Map<String, String> headers = Map.of("Authorization", String.format("Bearer %s", token));
        return restService.post(
                baseUrl,
                generatePath,
                headers,
                meditationGenerationRequest,
                UUID.class
        );
    }
    public MeditationStatus getMeditationStatus(UUID id) {
        Map<String, String> headers = Map.of("Authorization", String.format("Bearer %s", token));
        return restService.get(
             baseUrl,
             statusUrl,
                id,
             headers,
             MeditationStatus.class
        );
    }
}
