package system.integration.meditationai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import system.integration.meditationai.dto.GeneratedMeditation;
import system.integration.meditationai.dto.MeditationGenerationRequest;
import system.integration.meditationai.service.MeditationAiService;

@RestController
@RequestMapping("/integration/meditation-ai")
@RequiredArgsConstructor
public class MeditationAiController {
    private final MeditationAiService meditationAiService;
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public GeneratedMeditation generateMeditation(@Valid @RequestBody MeditationGenerationRequest meditationGenerationRequest) {
        return meditationAiService.generateMeditation(meditationGenerationRequest);
    }
}
