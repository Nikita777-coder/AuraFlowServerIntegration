package system.integration.meditationai.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import system.extra.AuthorizedRequestBuilder;
import system.integration.meditationai.dto.MeditationGenerationRequest;
import system.integration.meditationai.dto.MeditationStatus;
import system.service.RestService;
import system.service.ZitadelRestService;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeditationAiService {
    private final ZitadelRestService restService;
    @Value("${service-configs.meditation-generator-service.auth-jwt-secret}")
    private String token;
    @Value("${service-configs.meditation-generator-service.base-url}")
    private String baseUrl;
    @Value("${service-configs.meditation-generator-service.generate-path}")
    private String generatePath;
    @Value("${service-configs.meditation-generator-service.status-path}")
    private String statusUrl;
    public String generateMeditation(MeditationGenerationRequest meditationGenerationRequest) {
        Map<String, String> headers = Map.of("Authorization", String.format("Bearer %s", token));
        return restService.post(
                baseUrl,
                generatePath,
                headers,
                meditationGenerationRequest,
                String.class
        );
    }
    public MeditationStatus getMeditationStatus(String id) {
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
