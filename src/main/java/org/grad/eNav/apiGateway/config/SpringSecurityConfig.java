/*
 * Copyright (c) 2022 GLA Research and Development Directorate
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
import org.grad.eNav.apiGateway.components.X509PrincipalExtractor;
import org.grad.eNav.apiGateway.utils.KeycloakJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

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
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
class SpringSecurityConfig {

    /**
     * The default application name.
     */
    @Value("${spring.application.name:api-gateway}")
    private String appName;

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
     * Specify a converter for the Keycloak authority claims.
     *
     * @return The Keycloak JWT Authentication Converter
     */
    @Bean
    KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter(appName);
    }

    /**
     * Defines the security web-filter chains.
     *
     * Allows open access to the health and info actuator endpoints.
     * All other actuator endpoints are only available for the actuator role.
     * Finally, all other exchanges need to be authenticated.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // Authenticate through configured OpenID Provider
        http.oauth2Login();
        // Also, logout at the OpenID Connect provider
        http.logout(logout -> logout.logoutSuccessHandler(
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)));
        // Require authentication for all requests
        http.x509()
                .principalExtractor(this.x509PrincipalExtractor)
                .authenticationManager(this.x509AuthenticationManager)
                .and()
                .authorizeExchange(exchanges ->
                    exchanges.matchers(EndpointRequest.to(
                                InfoEndpoint.class,         //info endpoints
                                HealthEndpoint.class        //health endpoints
                            )).permitAll()
                            .pathMatchers(openResources).permitAll()
                            .matchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                            .pathMatchers("/*/actuator", "/*/actuator/**").denyAll()
                            .anyExchange().authenticated()
                )
                .oauth2Login(withDefaults())
                .oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter());

        // Add an authentication filter to handle forwarded requests
        http.addFilterAt(new ForwardedX509HeadersFilter(x509AuthenticationManager), SecurityWebFiltersOrder.AUTHENTICATION);

        // Disable the CSRF
        http.csrf().disable();
        return http.build();
    }

}
