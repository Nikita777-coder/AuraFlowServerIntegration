package system.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import system.integration.mainserver.service.MainServerService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    @Value("${service-configs.zitadel.introspect-uri}")
    private String jwtSetUri;
    @Value("${service-configs.zitadel.client-id}")
    private String clientId;
    @Value("${service-configs.zitadel.client-secret}")
    private String clientSecret;
    @Value("${service-configs.main-server.oidc-email}")
    private String oidcEmail;
    private final MainServerService mainServerService;
    private final TokenDateHolder tokenDateHolder;
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> logAllRequestsFilter() {
        return new FilterRegistrationBean<>(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                System.out.println("👉 Входящий запрос: " + request.getMethod() + " " + request.getRequestURI());
                filterChain.doFilter(request, response);
            }
        });
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    FilterChain filterChain) throws ServletException, IOException {
                        try {
                            String date = request.getHeader("X-Token-Date");
                            if (date != null) {
                                tokenDateHolder.set(date);
                            }
                            filterChain.doFilter(request, response);
                        } finally {
                            tokenDateHolder.clear();
                        }
                    }
                }, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/v3/api-docs/**"),
                                antMatcher("/swagger-ui.html"),
                                antMatcher("/actuator/health/**")
                        )
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .opaqueToken(opaque -> opaque.introspector(introspector(tokenDateHolder)))
                );


        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector introspector(TokenDateHolder tokenDateHolder) {
        var delegate = new SpringOpaqueTokenIntrospector(jwtSetUri, clientId, clientSecret);

        return token -> {
            try {
                return delegate.introspect(token);
            } catch (OAuth2IntrospectionException | AuthenticationServiceException ex) {
                if (ex.getMessage().contains("unauthorized_client")) {
                    String date = tokenDateHolder.get();
                    if (date != null) {
                        String serverToken = mainServerService.get(oidcEmail, date).block();
                        if (token.equals(serverToken)) {
                            return new DefaultOAuth2AuthenticatedPrincipal(
                                    Map.of(
                                            "sub", "anonymous-user",
                                            "client_id", "fallback-client",
                                            "scope", "limited"
                                    ),
                                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                            );
                        }
                    }
                }
                throw ex;
            }
        };
    }
}
