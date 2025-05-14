package system.integration.yookassa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import system.integration.yookassa.dto.PaymentData;
import system.integration.yookassa.dto.PaymentRequest;
import system.service.RestService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class YookassaService {
    private final RestService restService;

    @Value("${service-configs.yookassa.base-url}")
    private String yookassaBaseUrl;
    @Value("${service-configs.yookassa.test-api-key}")
    private String apiKey;
    @Value("${service-configs.yookassa.shop-id}")
    private String shopId;
    @Value("${service-configs.yookassa.method-type}")
    private String methodType;
    @Value("${service-configs.yookassa.currency}")
    private String currency;
    @Value("${service-configs.yookassa.description}")
    private String description;
    @Value("${service-configs.yookassa.amount}")
    private long amount;
    public PaymentData makePayment() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Idempotence-Key", UUID.randomUUID().toString());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(BigDecimal.valueOf(amount));
        paymentRequest.setCurrency(currency);
        paymentRequest.setCapture(true);
        paymentRequest.setMethodType(methodType);
        paymentRequest.setDescription(description);

        return restService.postWithDefaultHeaders(
                yookassaBaseUrl,
                headers,
                shopId,
                apiKey,
                paymentRequest,
                PaymentData.class
        );
    }
}
