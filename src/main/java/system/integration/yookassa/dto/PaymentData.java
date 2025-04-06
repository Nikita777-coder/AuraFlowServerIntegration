package system.integration.yookassa.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentData {
    private UUID paymentId;
    private String sbpPaymentUrl;
}
