package system.kinescope.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestService {
    private final WebClient webClient;
    public <T, B> T post(String url, Map<String, String> headers, B body, Class<T> tClass) {
        var ans = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(tClass);

        return ans.block();
    }

    public <T> T post(String url, Map<String, String> headers, Class<T> tClass) {
        var ans = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .bodyToMono(tClass);

        return ans.block();
    }

    public <T> Mono<T> get(String url,
                           Map<String, String> headers,
                           Class<T> tClass) {
        return webClient
                .get()
                .uri(url)
                .headers(headers1 -> headers1.setAll(headers))
                .retrieve()
                .bodyToMono(tClass);
    }

    public void delete(String url, Map<String, String> headers) {
        webClient
                .delete()
                .uri(url)
                .headers(headers1 -> headers1.setAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {})
                .block();
    }
}
