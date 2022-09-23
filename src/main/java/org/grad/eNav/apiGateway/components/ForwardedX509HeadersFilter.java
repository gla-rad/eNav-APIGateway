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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import java.util.Objects;

/**
 * The Forwarded X.509 Headers Filter
 *
 * When X.509 SSL requests are being forwarded by another service (e.g. nginx)
 * a new client certificate is provided. Then we need to check that the original
 * request certificate is available in the headers and that these are also
 * correct.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class ForwardedX509HeadersFilter extends AuthenticationWebFilter {

    /**
     * The Forwarded SSL headers.
     */
    public static String X_SSL_VERIFY_HEADER = "X-SSL-Verify";
    public static String X_SSL_CERT_HEADER = "X-SSL-CERT";
    public static String X_SSL_SDN_HEADER = "X-SSL-SDN";

    /**
     * Implements the filter's functionality where the exchange request is
     * checked to make sure that all the necessary headers are in place and
     * that the included information is accurate and valid.
     *
     * @param authenticationManager The current X.509 authentication manager
     */
    public ForwardedX509HeadersFilter(@Autowired X509AuthenticationManager authenticationManager) {
        super(authenticationManager);

        // Set the authentication converter
        this.setServerAuthenticationConverter(new ForwardedX509AuthenticationConverter());

        // And define the authentication matcher
        this.setRequiresAuthenticationMatcher(exchange -> {
            // Get the exchange request
            ServerHttpRequest request = exchange.getRequest();

            // Decide on whether to apply the filter
            return request.getHeaders().containsKey(X_SSL_VERIFY_HEADER)
                    && Objects.equals(request.getHeaders().getFirst(X_SSL_VERIFY_HEADER), "SUCCESS")
                    && request.getHeaders().containsKey(X_SSL_CERT_HEADER)
                    && request.getHeaders().containsKey(X_SSL_SDN_HEADER)
                    ? ServerWebExchangeMatcher.MatchResult.match()
                    : ServerWebExchangeMatcher.MatchResult.notMatch();
        });
    }

}
