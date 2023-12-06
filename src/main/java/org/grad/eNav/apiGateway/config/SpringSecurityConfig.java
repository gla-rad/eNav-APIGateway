/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.apiGateway.config;

import org.grad.eNav.apiGateway.components.ForwardedX509HeadersFilter;
import org.grad.eNav.apiGateway.components.X509AuthenticationManager;
import org.grad.eNav.apiGateway.components.X509ClientCertificateFilter;
import org.grad.eNav.apiGateway.components.X509PrincipalExtractor;
import org.grad.eNav.apiGateway.config.keycloak.KeycloakGrantedAuthoritiesMapper;
import org.grad.eNav.apiGateway.config.keycloak.KeycloakJwtAuthenticationConverter;
import org.grad.eNav.apiGateway.config.keycloak.KeycloakLogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.client.RestTemplate;

/**
 * The Web Security Configuration.
 *
 * This is the security definition for the filter chains of the API gateway.
 * Therefore, is it slightly different from all the other microservice. It
 * is still required though that the actuator points are handled differently
 * so that the spingboot admin connection works properly.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
class SpringSecurityConfig {

    /**
     * The default application name.
     */
    @Value("${keycloak.clientId:api-gateway}")
    private String clientId;

    /**
     * The default application name.
     */
    @Value("${gla.rad.api-gateway.resources.open:/,/login}")
    private String[] openResources;

    /**
     * The X509 Principal Extractor.
     */
    @Autowired
    X509PrincipalExtractor x509PrincipalExtractor;

    /**
     * The X509 Authentication Manager.
     */
    @Autowired
    X509AuthenticationManager x509AuthenticationManager;

    /**
     * The REST Template.
     *
     * @return the REST template
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Specify a converter for the Keycloak authority claims.
     *
     * @return The Keycloak JWT Authentication Converter
     */
    @Bean
    KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter(this.clientId);
    }

    /**
     * Specify a mapper for the keycloak authority claims.
     *
     * @return the Keycloak Granted Authority Mapper
     */
    @Bean
    protected GrantedAuthoritiesMapper keycloakGrantedAuthoritiesMapper() {
        return new KeycloakGrantedAuthoritiesMapper(this.clientId);
    }

    /**
     * Define a logout handler for handling Keycloak logouts.
     *
     * @param restTemplate the REST template
     * @return the Keycloak logout handler
     */
    @Bean
    protected KeycloakLogoutHandler keycloakLogoutHandler(RestTemplate restTemplate) {
        return new KeycloakLogoutHandler(restTemplate);
    }

    /**
     * Define the session authentication strategy which uses a simple session
     * registry to store our current sessions.
     *
     * @return the session authentication strategy
     */
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * Defines the security web-filter chains.
     * <p/>
     * Allows open access to the health and info actuator endpoints.
     * All other actuator endpoints are only available for the actuator role.
     * Finally, all other exchanges need to be authenticated.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                            RestTemplate restTemplate) {
        // Authenticate through configured OpenID Provide
        http.oauth2Login(oauth2 -> oauth2
                .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}"))
        );
        // Also, logout at the OpenID Connect provider
        http.logout(logout -> logout
                .logoutHandler(keycloakLogoutHandler(restTemplate))
//                .logoutSuccessHandler(new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository))
        );
        // Require authentication for all requests
        http.x509( x509 -> x509
                    .principalExtractor(this.x509PrincipalExtractor)
                    .authenticationManager(this.x509AuthenticationManager)
             )
            .authorizeExchange(exchanges -> exchanges
                    .matchers(EndpointRequest.to(
                        InfoEndpoint.class,         //info endpoints
                        HealthEndpoint.class        //health endpoints
                    )).permitAll()
                    .pathMatchers(this.openResources).permitAll()
                    .matchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                    .pathMatchers(
                            "/*/actuator",
                            "/*/actuator/**"
                    ).denyAll()
                    .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2Rs -> oauth2Rs
                    .jwt( jwt -> jwt
                            .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter())
                    )
            );

        // Add the forwarded X.509 certificate authentication support
        http.addFilterAt(new ForwardedX509HeadersFilter(this.x509AuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION);
        http.addFilterAfter(new X509ClientCertificateFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        // Disable the CSRF
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        // Build and return
        return http.build();
    }

}
