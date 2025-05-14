package system.integration.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    @JsonProperty("amount.value")
    private BigDecimal amount;

    @JsonProperty("amount.currency")
    private String currency;

    @JsonProperty("payment_method_data.type")
    private String methodType;

    private boolean capture;
    private String description;
}
