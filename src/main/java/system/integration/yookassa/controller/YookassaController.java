package system.integration.yookassa.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import system.integration.yookassa.dto.PaymentData;
import system.integration.yookassa.dto.TransactionStatus;
import system.integration.yookassa.service.YookassaService;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class YookassaController {
    private final YookassaService yookassaService;
    @PostMapping
    public PaymentData makePayment() throws JsonProcessingException {
        var ans = yookassaService.makePayment();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(ans));
        return ans;
    }
}
