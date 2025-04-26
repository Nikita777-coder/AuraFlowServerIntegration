package system.integration.onesignal.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OneSignalRequest {
    private String appId;
    private Contents content;
}
