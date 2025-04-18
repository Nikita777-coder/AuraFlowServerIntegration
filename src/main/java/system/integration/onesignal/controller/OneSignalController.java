package system.integration.onesignal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/remind-service")
@RequiredArgsConstructor
public class OneSignalController {
    @PostMapping
    public void sendMessage(@RequestBody String message) {

    }
}
