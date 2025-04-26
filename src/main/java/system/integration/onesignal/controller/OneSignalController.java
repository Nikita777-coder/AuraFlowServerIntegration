package system.integration.onesignal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import system.integration.onesignal.dto.OneSignalRequest;
import system.integration.onesignal.dto.OneSignalRequestFromController;
import system.integration.onesignal.service.OneSignalService;

@RestController
@RequestMapping("/remind-service")
@RequiredArgsConstructor
public class OneSignalController {
    private final OneSignalService oneSignalService;
    @PostMapping
    public void sendMessage(OneSignalRequestFromController oneSignalRequestFromController) {
        oneSignalService.sendMessage(oneSignalRequestFromController);
    }
}
