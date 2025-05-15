package system.integration.mainserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import system.integration.mainserver.service.MainServerService;

@RestController
@RequestMapping("/main-server")
@RequiredArgsConstructor
public class MainServerController {
    private final MainServerService mainServerService;
    @GetMapping
    public Mono<String> getToken(@RequestParam String email, @RequestParam String date) {
        return mainServerService.get(email, date);
    }
}
