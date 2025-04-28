package system.integration.onesignal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OneSignalRequestFromController {
    private String to;

    private List<String> listTo;

    @NotBlank
    private String message;
}
