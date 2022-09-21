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
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.security.cert.X509Certificate;

/**
 * The Custom API-Gateway X.509 Certificate Authentication Manager Component.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class X509AuthenticationManager implements ReactiveAuthenticationManager {

    /**
     * The Component initialisation function.
     */
    @PostConstruct
    public void init() {
        log.info("initialising the X.509 Certificate Authentication Manager");
    }

    /**
     * Implements the reactive authentication manager operations when an X.509
     * certificate is provided alonside the request.
     *
     * @param authentication the authentication provided
     * @return The authentication result
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        authentication.setAuthenticated(((X509Certificate)authentication.getCredentials()).getSubjectX500Principal().getName().contains("O=urn:mrn:mcp:org:mcc:grad"));
        return Mono.just(authentication);
    }
}
