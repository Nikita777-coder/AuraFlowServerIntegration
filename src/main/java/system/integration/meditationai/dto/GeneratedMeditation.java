package system.integration.meditationai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GeneratedMeditation {
    @JsonProperty("audio_link")
    private String audioLink;
    private String videoLink;
}
