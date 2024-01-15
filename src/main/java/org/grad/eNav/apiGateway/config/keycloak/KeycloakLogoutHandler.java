/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.apiGateway.config.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * The Keycloak Logout Handler
 *
 * This logout handler implementation takes care of the logout from the
 * Keycloak SSO side after a successful service logout.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class KeycloakLogoutHandler implements ServerLogoutHandler {

    // Class Variables
    private final RestTemplate restTemplate;

    /**
     * Class Constructor.
     *
     * @param restTemplate  the REST template
     */
    public KeycloakLogoutHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * The implementation of the interface function that defines the logout
     * operation. This simply makes sure that the user has also logged of the
     * Keycloak SSO side.
     *
     * @param exchange  The web filter exchange
     * @param auth      The authentication
     */
    @Override
    public Mono<Void> logout(WebFilterExchange exchange,
                             Authentication auth) {
        logoutFromKeycloak((OidcUser) auth.getPrincipal());
        return Mono.empty();
    }

    /**
     * This function performs the actual Keycloak logout operation by calling
     * the appropriate endpoing of the server in order to logout.
     *
     * @param user  The user to be logged off
     */
    private void logoutFromKeycloak(OidcUser user) {
        // Setup the logout query
        final String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());

        // Perform the logout request
        final ResponseEntity<String> logoutResponse = this.restTemplate.getForEntity(
                builder.toUriString(),
                String.class
        );

        // Log the output for debug purposes
        log.debug(logoutResponse.getStatusCode().is2xxSuccessful() ?
                "Successfully logged out from Keycloak" :
                "Could not propagate logout to Keycloak");
    }

}
