package system.integration.onesignal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import system.integration.onesignal.dto.Contents;
import system.integration.onesignal.dto.OneSignalRequest;
import system.integration.onesignal.dto.OneSignalRequestFromController;
import system.service.RestService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OneSignalService {
    private final RestService restService;

    @Value("${service-configs.one-signal.url}")
    private String oneSignalUrl;
    @Value("${service-configs.one-signal.api-key}")
    private String apiKey;
    @Value("${service-configs.one-signal.app-id}")
    private String appId;
    public void sendMessage(OneSignalRequestFromController message) {
        Map<String, String> headers = Map.of(
                "Authorization", String.format("Key %s", apiKey),
                "Content-Type", "application/json",
                "Accept", "application/json"
        );

        OneSignalRequest b = new OneSignalRequest(appId, new Contents(message.getMessage()), List.of(message.getTo()));
        System.out.println(restService.post(oneSignalUrl, headers, b, String.class));
    }
}
