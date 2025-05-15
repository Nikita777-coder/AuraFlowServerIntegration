package system.integration.mainserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import system.integration.mainserver.dto.Login;
import system.service.RestService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MainServerService {
    @Value("${service-configs.main-server.oidc-email}")
    private String oidcEmail;
    @Value("${service-configs.main-server.oidc-password}")
    private String oidcPassword;
    @Value("${service-configs.main-server.base-url}")
    private String mainServerBaseUrl;
    @Value("${service-configs.main-server.token-uri}")
    private String tokenUri;
    @Value("${service-configs.main-server.login-uri}")
    private String loginUri;
    private String jwt;
    private final RestService restService;
    public Mono<String> get(String oidcEmail, String date) {
        checkUserData(oidcEmail);
        checkJwt();

        return restService.get(
                mainServerBaseUrl,
                tokenUri,
                Map.of("time", date),
                Map.of(
                        "Authorization", "Bearer " + jwt
                ),
                String.class
        ).onErrorResume(IllegalStateException.class, ex ->
                Mono.fromCallable(() -> {
                            jwt = null;
                            checkJwt();
                            return jwt;
                        })
                        .flatMap(updatedJwt ->
                                restService.get(
                                        mainServerBaseUrl,
                                        tokenUri,
                                        Map.of("time", date),
                                        Map.of("Authorization", "Bearer " + updatedJwt),
                                        String.class
                                )
                        )
        );
    }
    public Mono<String> update(String oidcEmail, String token) {
        checkUserData(oidcEmail);
        checkJwt();

        try {
            return restService.patch(
                    mainServerBaseUrl,
                    tokenUri,
                    Map.of(
                            "Authorization", "Bearer " + jwt
                    ),
                    token,
                    String.class
            );
        } catch (IllegalArgumentException ex) {
            jwt = null;
            checkJwt();

            return restService.patch(
                    mainServerBaseUrl,
                    tokenUri,
                    Map.of(
                            "Authorization", "Bearer " + jwt
                    ),
                    token,
                    String.class
            );
        }
    }
    private void checkUserData(String email) {
        if (!email.equals(this.oidcEmail)) {
            throw new AccessDeniedException("access deny");
        }
    }
    private void checkJwt() {
        if (jwt == null) {
            jwt = restService.post(
                    mainServerBaseUrl,
                    loginUri,
                    Map.of(),
                    new Login(
                            oidcEmail,
                            oidcPassword
                    ),
                    String.class
            );
        }
    }
}
