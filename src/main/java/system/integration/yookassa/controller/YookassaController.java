package system.integration.yookassa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import system.integration.yookassa.dto.PaymentData;
import system.integration.yookassa.dto.TransactionStatus;
import system.integration.yookassa.service.YookassaService;

import java.util.UUID;

@RestController
@RequestMapping("/integration/payment")
@RequiredArgsConstructor
public class YookassaController {
    private final YookassaService yookassaService;
    @PostMapping
    public PaymentData makePayment() {
        return yookassaService.makePayment();
    }
    @GetMapping("/status")
    public TransactionStatus getTransactionStatus(@RequestParam("payment-id") UUID id) {
        throw new RuntimeException();
    }
}
