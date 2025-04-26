package system.service;

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
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                System.out.println("Error Response Body: " + responseBody);
                                return Mono.error(new IllegalArgumentException());
                            });
                })
                .bodyToMono(tClass);

        return ans.block();
    }

    public <T, R> T post(String baseUrl, String uri, Map<String, String> headers, R body, Class<T> tClass) {
//        String token = getToken();

        var ans = webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                //                .header("Authorization", "Bearer " + token)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                System.out.println("Error Response Body: " + responseBody);
                                return Mono.error(new IllegalArgumentException());
                            });
                })
                .bodyToMono(tClass)
                .block();

        return ans;
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
    public <T> T get(String baseUrl,
                           String path,
                           String pathVariable,
                           Map<String, String> headers,
                           Class<T> tClass) {
        return webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .build(pathVariable)
                )
                .headers(headers1 -> headers1.setAll(headers))
                .retrieve()
                .bodyToMono(tClass)
                .block();
    }
    public <T> T getWithoutMono(String url,
                           Map<String, String> headers,
                           Class<T> tClass) {
        return webClient
                .get()
                .uri(url)
                .headers(headers1 -> headers1.setAll(headers))
                .retrieve()
                .bodyToMono(tClass)
                .block();
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
