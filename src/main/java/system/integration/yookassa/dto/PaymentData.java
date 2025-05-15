package system.integration.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentData {
    private UUID id;

    @JsonProperty("confirmation")
    private Confirmation confirmation;

    private boolean test;
}
