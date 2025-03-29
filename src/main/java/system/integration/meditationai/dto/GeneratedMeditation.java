package system.integration.meditationai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GeneratedMeditation {
    private String audioLink;
    private String videoLink;
}
