package system.integration.meditationai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import system.integration.meditationai.dto.MeditationStatus;
import system.integration.meditationai.dto.MeditationGenerationRequest;
import system.integration.meditationai.service.MeditationAiService;

import java.util.UUID;

@RestController
@RequestMapping("/meditation-ai")
@RequiredArgsConstructor
public class MeditationAiController {
    private final MeditationAiService meditationAiService;
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public String generateMeditation(@Valid @RequestBody MeditationGenerationRequest meditationGenerationRequest) {
        return meditationAiService.generateMeditation(meditationGenerationRequest);
    }
    @GetMapping
    @ResponseBody
    public MeditationStatus getMeditationStatus(@RequestParam String id) {
        var ans = meditationAiService.getMeditationStatus(id);
        return ans;
    }
}
