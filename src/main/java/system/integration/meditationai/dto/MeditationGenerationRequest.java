package system.integration.meditationai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeditationGenerationRequest {
    private List<String> themes;
    private String musicTitle;
    private int meditationTimeMinutes;
}
