package system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import system.extra.AuthorizedRequestBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public <T> T get(String url, Map<String, String> headers, Class<T> tClass) {
        return webClient
                .get()
                .uri(url)
                .headers(h -> h.setAll(headers))
                .retrieve()
                .bodyToMono(tClass)
                .block();
    }

    public <T, B> T postWithDefaultHeaders(String url, Map<String, String> headers, String username, String pass, B body, Class<T> tClass) {
        var ans = webClient
                .mutate()
                .defaultHeaders(header -> header.setBasicAuth(username, pass))
                .build()
                .post()
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

    public <T> T post(String url, Map<String, String> headers, Class<T> tClass) {
        var ans = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .bodyToMono(tClass);

        return ans.block();
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
    public <T> T patch(String baseUrl, String uri, Map<String, String> headers, String body, Class<T> tClass) {
        return webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .patch()
                .uri(uri)
                .headers(h -> h.setAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(tClass)
                .block();
    }

    public <T> T get(String baseUrl, String uri, Map<String, String> params, Map<String, String> headers, Class<T> tClass) {
        return webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri(builder -> {
                    builder.path(uri);
                    params.forEach(builder::queryParam);
                    return builder.build();
                })
                .headers(h -> h.setAll(headers))
                .retrieve()
                .bodyToMono(tClass)
                .block();
    }

    public <T, R> T post(String baseUrl, String uri, Map<String, String> headers, R body, Class<T> tClass) {
        return webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(uri)
                .headers(h -> h.setAll(headers))
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(responseBody -> {
                                    System.out.println("Error Response Body: " + responseBody);
                                    return Mono.error(new IllegalArgumentException("4xx Error"));
                                })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(responseBody -> {
                                    System.out.println("Error Response Body: " + responseBody);
                                    return Mono.error(new IllegalStateException("5xx Error"));
                                })
                )
                .bodyToMono(tClass)
                .block();
    }
}
