package system.integration.onesignal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneSignalRequestFromController {
    @NotBlank
    private String to;

    @NotBlank
    private String message;
}
