package system.integration.onesignal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import system.service.RestService;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OneSignalService {
    private final RestService restService;
    private String oneSignalUrl;
    private String apiKey;
    private String appId;
    public void sendMessage(String message) {
        Map<String, String> headers = Map.of(
                "Authorization", String.format("Key %s", apiKey)
        );

        OneSignalRequest b = new OneSignalRequest(appId, new Contents(message));
        restService.post(oneSignalUrl, headers, b, void.class);
    }
}
