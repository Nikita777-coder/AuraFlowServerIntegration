package system.integration.meditationai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeditationGenerationRequest {
    @NotNull
    @Min(value = 1)
    @Max(value = 30)
    @JsonProperty("duration")
    private int durationInMinutes;

    @NotBlank
    private String topic;

    @NotBlank
    private String melodyDescription;
}
