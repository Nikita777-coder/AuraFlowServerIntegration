package system.extra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import system.integration.mainserver.service.MainServerService;

@Component
@RequiredArgsConstructor
public class AuthorizedRequestBuilder {
    private final ReactiveOAuth2AuthorizedClientManager manager;
    private final MainServerService mainServerService;
    private @Value("${service-configs.main-server.oidc-email}") String oidcEmail;

    public ClientRequest withAuthHeaders(ClientRequest original) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("zitadel")
                .principal("zitadel-client")
                .build();

        var client = manager.authorize(authorizeRequest).block();
        if (client == null) throw new IllegalStateException("Failed to authorize client");

        String token = client.getAccessToken().getTokenValue();

        var date = mainServerService.update(oidcEmail, token);
        return ClientRequest.from(original)
                .headers(headers -> {
                    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.set("X-Token-Date", date);
                })
                .build();
    }

    public Mono<ClientRequest> withAuthHeadersReactive(ClientRequest original) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("zitadel")
                .principal("zitadel-client")
                .build();

        return manager.authorize(authorizeRequest)
                .switchIfEmpty(Mono.error(new IllegalStateException("Authorization failed")))
                .flatMap(client -> {
                    String token = client.getAccessToken().getTokenValue();

                    return Mono.fromCallable(() -> mainServerService.update(oidcEmail, token))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(date -> {
                                return ClientRequest.from(original)
                                        .headers(headers -> {
                                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                                            headers.set("X-Token-Date", date);
                                        })
                                        .build();
                            });
                });
    }
}

