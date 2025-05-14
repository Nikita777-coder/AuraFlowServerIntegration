package system.integration.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Amount amount;

    @JsonProperty("payment_method_data")
    private MethodType methodType;

    private boolean capture;
    private String description;
    private Confirmation confirmation;
}
