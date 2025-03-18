package system.integration.meditationai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import system.integration.meditationai.dto.GeneratedMeditation;
import system.integration.meditationai.dto.MeditationGenerationRequest;

@RestController
@RequestMapping("/integration/meditation-ai")
public class MeditationAiController {
    @PostMapping
    public GeneratedMeditation generateMeditation(@RequestBody MeditationGenerationRequest meditationGenerationRequest) {
        throw new RuntimeException();
    }
}
