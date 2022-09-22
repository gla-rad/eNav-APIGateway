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

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

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
public class ForwardedX509HeadersFilter extends AuthenticationWebFilter {

    /**
     * The Forwarded SSL headers.
     */
    public static String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static String X_SSL_CERT_HEADER = "X-SSL-CERT";

    /**
     * Implements the filter's functionality where the exchange request is
     * checked to make sure that all the necessary headers are in place and
     * that the included information is accurate and valid.
     *
     * @param authenticationManager The current X.509 authentication manager
     */
    public ForwardedX509HeadersFilter(ReactiveAuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.setRequiresAuthenticationMatcher(exchange -> {
            // If not forwarded then just move on
            if(!exchange.getRequest().getHeaders().containsKey(X_FORWARDED_FOR_HEADER)) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            // Otherwise we need to make sure that:
            //   - The original client certificate exists if the headers
            //   - The original client certificate is still valid
            //   - The organisation is allowed to access the service
            return exchange.getRequest().getHeaders().containsKey(X_SSL_CERT_HEADER)
                    ? ServerWebExchangeMatcher.MatchResult.match()
                    : ServerWebExchangeMatcher.MatchResult.notMatch();
        });
    }

}
