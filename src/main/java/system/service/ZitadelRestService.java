package system.service;

import lombok.RequiredArgsConstructor;
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
public class ZitadelRestService {
    private final AuthorizedRequestBuilder authorizedRequestBuilder;
    private final WebClient zitadelWebClient;
    public <T, R> T post(String baseUrl, String uri, Map<String, String> headers, R body, Class<T> tClass) {
        ClientRequest request = ClientRequest.create(HttpMethod.POST, URI.create(baseUrl + uri))
                .headers(h -> h.setAll(headers))
                .build();

        ClientRequest authorizedRequest = authorizedRequestBuilder.withAuthHeaders(request);

        var ans = zitadelWebClient
                .method(authorizedRequest.method())
                .uri(authorizedRequest.url())
                .headers(h -> h.addAll(authorizedRequest.headers()))
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

    public <T> T get(String baseUrl,
                     String path,
                     String pathVariable,
                     Map<String, String> headers,
                     Class<T> tClass) {
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create(baseUrl + path + "/" + pathVariable))
                .headers(h -> h.setAll(headers))
                .build();

        ClientRequest authorizedRequest = authorizedRequestBuilder.withAuthHeaders(request);

        return zitadelWebClient
                .method(authorizedRequest.method())
                .uri(authorizedRequest.url())
                .headers(h -> h.addAll(authorizedRequest.headers()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                System.out.println("Error Response Body: " + responseBody);
                                return Mono.error(new IllegalArgumentException());
                            });
                })
                .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                System.out.println("Error Response Body: " + responseBody);
                                return Mono.error(new IllegalArgumentException());
                            });
                })
                .bodyToMono(tClass)
                .block();
    }
}
