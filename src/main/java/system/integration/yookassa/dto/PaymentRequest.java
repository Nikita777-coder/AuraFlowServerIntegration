package system.integration.yookassa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Amount amount;
    private boolean capture;
    private String description;
    private Confirmation confirmation;
}
