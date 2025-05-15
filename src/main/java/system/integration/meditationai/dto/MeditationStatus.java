package system.integration.meditationai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeditationStatus {
    private String status;
    private String url;
    private String wasUsed;
}
