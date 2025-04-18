package system.integration.meditationai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import system.integration.meditationai.dto.GeneratedMeditation;
import system.integration.meditationai.dto.MeditationGenerationRequest;
import system.service.RestService;

import java.util.Map;

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
    public GeneratedMeditation generateMeditation(MeditationGenerationRequest meditationGenerationRequest) {
        Map<String, String> headers = Map.of("Authorization", String.format("Bearer %s", token));
        return restService.post(
                baseUrl,
                generatePath,
                headers,
                meditationGenerationRequest,
                GeneratedMeditation.class
        );
    }
}
