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

package org.grad.eNav.apiGateway.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Custom API-Gateway X.509 Certificate Authentication Manager Component.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class X509AuthenticationManager implements ReactiveAuthenticationManager {

    /**
     * The allowed organisation MRNs.
     */
    @Value("${gla.rad.api-gateway.x509.organisation.mrn:}")
    private String allowedOrganisationMrn;

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
        // Make sure we have what appears to be valid authentication credentials
        if(Objects.nonNull(authentication.getCredentials()) && (authentication.getCredentials() instanceof X509Certificate)) {
            // Retrieve the principles from the authorisation credentials
            final X500Principal x500Principal = ((X509Certificate)authentication.getCredentials()).getSubjectX500Principal();
            final Map<ASN1ObjectIdentifier,String> x500PrincipalMap = this.parseX509Principal(x500Principal);

            // If the allowed organisations are restricted, apply that to the access
            if(Strings.isNotBlank(this.allowedOrganisationMrn)) {
                authentication.setAuthenticated(x500PrincipalMap.get(BCStyle.O).startsWith(this.allowedOrganisationMrn));
            }
        } else {
            authentication.setAuthenticated(false);
        }

        // Return the authentication
        return Mono.just(authentication);
    }

    /**
     * Parses the provided X.509 principal into a nicely formatted map.
     *
     * @param x500Principal the X.509 principal
     * @return the generated map
     */
    Map<ASN1ObjectIdentifier,String> parseX509Principal(X500Principal x500Principal) {
        // Initialise the map
        final Map<ASN1ObjectIdentifier, String> principalMap = new HashMap();

        // Parse using X500Name
        X500Name x500name = new X500Name(x500Principal.getName(X500Principal.RFC1779));
        principalMap.put(BCStyle.UID, IETFUtils.valueToString(x500name.getRDNs(BCStyle.UID)[0].getFirst().getValue()));
        principalMap.put(BCStyle.CN, IETFUtils.valueToString(x500name.getRDNs(BCStyle.CN)[0].getFirst().getValue()));
        principalMap.put(BCStyle.O, IETFUtils.valueToString(x500name.getRDNs(BCStyle.O)[0].getFirst().getValue()));
        principalMap.put(BCStyle.OU, IETFUtils.valueToString(x500name.getRDNs(BCStyle.OU)[0].getFirst().getValue()));
        principalMap.put(BCStyle.C, IETFUtils.valueToString(x500name.getRDNs(BCStyle.C)[0].getFirst().getValue()));

        // Return the map
        return principalMap;
    }
}
