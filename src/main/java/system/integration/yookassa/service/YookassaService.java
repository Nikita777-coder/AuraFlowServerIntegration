package system.integration.yookassa.service;

import org.springframework.stereotype.Service;
import system.integration.yookassa.dto.PaymentData;

@Service
public class YookassaService {
    public PaymentData makePayment() {
        throw new RuntimeException();
    }
}
