package system.integration.mainserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import system.integration.mainserver.service.MainServerService;

@RestController
@RequestMapping("/main-server")
@RequiredArgsConstructor
public class MainServerController {
    private final MainServerService mainServerService;
    @GetMapping
    public String getToken(@RequestParam String email, @RequestParam String date) {
        var ans = mainServerService.get(email, date);
        System.out.printf("Method /main-server get, ans: %s\n", ans);
        return ans;
    }
}
