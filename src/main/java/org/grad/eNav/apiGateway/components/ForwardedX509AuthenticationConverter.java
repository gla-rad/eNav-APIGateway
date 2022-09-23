/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

package org.grad.eNav.apiGateway.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The Forwarded X.509 Authentication Converter.
 *
 * This helper class is used to convert the X.509 certificate information
 * provided in the request header into the actual authentication token
 * used for the X.509 authentication.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class ForwardedX509AuthenticationConverter implements ServerAuthenticationConverter {

    /**
     * The Forwarded SSL headers.
     */
    public static String X_SSL_VERIFY_HEADER = "X-SSL-Verify";
    public static String X_SSL_CERT_HEADER = "X-SSL-CERT";
    public static String X_SSL_SDN_HEADER = "X-SSL-SDN";

    /**
     * This is the actual function that performs the authentication token
     * conversion into the X.509 certificate information provided in the
     * headers of the exchange request.
     *
     * @param exchange the server web-exchange
     * @return the authentication token to be used
     */
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        try {
            // extract credentials here
            Authentication authentication =  new PreAuthenticatedAuthenticationToken("service", request.getHeaders().getFirst(X_SSL_SDN_HEADER));
            return Mono.just(authentication);
        } catch (Exception e) {
            // log error here
            return Mono.empty();
        }
    }

}
