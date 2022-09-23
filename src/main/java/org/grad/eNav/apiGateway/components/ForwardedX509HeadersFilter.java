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
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;
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
@Component
@Slf4j
public class ForwardedX509HeadersFilter extends AuthenticationWebFilter {

    /**
     * The Forwarded SSL headers.
     */
    public static String HOST_HEADER = "Host";
    public static String X_SSL_FORWARDED_HEADER = "X-SSL-Forwarded";
    public static String X_SSL_CERT_HEADER = "X-SSL-CERT";
    public static String X_SSL_SDN_HEADER = "X-SSL-SDN";

    /**
     * Whether SSL forwarding is allowed.
     */
    @Value("${gla.rad.api-gateway.x509.forwarding.enabled:true}")
    private boolean allowedForwarding;

    /**
     * The allowed SSL forwarding hosts.
     */
    @Value("${gla.rad.api-gateway.x509.forwarding.hosts:}")
    private List<String> allowedForwardingHosts;

    /**
     * The allowed organisation MRNs.
     */
    @Value("${gla.rad.api-gateway.x509.organisation.mrn:}")
    private String allowedOrganisationMrn;

    // Certificate Factory
    protected CertificateFactory certificateFactory;

    /**
     * Implements the filter's functionality where the exchange request is
     * checked to make sure that all the necessary headers are in place and
     * that the included information is accurate and valid.
     *
     * @param authenticationManager The current X.509 authentication manager
     */
    public ForwardedX509HeadersFilter(@Autowired X509AuthenticationManager authenticationManager) {
        super(authenticationManager);

        // Initialise a certificate factory
        try {
            this.certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            log.error(ex.getMessage());
        }

        // And define the authentication matcher
        this.setRequiresAuthenticationMatcher(exchange -> {
            // Get the exchange request
            ServerHttpRequest req = exchange.getRequest();

            // If not using SSL then continue as usual
            if(Objects.isNull(req.getSslInfo()) || Arrays.isNullOrEmpty(req.getSslInfo().getPeerCertificates())) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }

            // If verification not forwarded then continue as usual
            if(!req.getHeaders().containsKey(X_SSL_FORWARDED_HEADER)) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }

            // Otherwise we need to make sure that:
            //   - That forwarding is allowed in the first place
            //   - That the host is allowed to forward the SSL request
            //   - The original client certificate exists in the headers
            //   - The original client certificate is still valid
            //   - The organisation is allowed to access the service
            if(!this.allowedForwarding) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            if(!this.allowedForwardingHosts.contains(req.getHeaders().getFirst(HOST_HEADER))) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            if(!req.getHeaders().containsKey(X_SSL_CERT_HEADER)) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            if(req.getHeaders().containsKey(X_SSL_CERT_HEADER))  {
                // Get the certificate
                String pem = req.getHeaders().getFirst(X_SSL_CERT_HEADER);
                // Make sure it's not empty of null
                if(pem == null || pem.isBlank()) {
                    return ServerWebExchangeMatcher.MatchResult.notMatch();
                }
                // Validate the certificate
                try {
                    Certificate cert = this.certificateFactory.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
                } catch (CertificateException ex) {
                    log.error(ex.getMessage());
                    return ServerWebExchangeMatcher.MatchResult.notMatch();
                }
            }
            if(req.getHeaders().containsKey(X_SSL_SDN_HEADER)) {
                final X500Principal x500Principal = new X500Principal(String.join(",", req.getHeaders().getOrEmpty(X_SSL_SDN_HEADER)));
                final Map<ASN1ObjectIdentifier,String> x500PrincipalMap = authenticationManager.parseX509Principal(x500Principal);
                return x500PrincipalMap.get(BCStyle.O).startsWith(this.allowedOrganisationMrn)
                        ? ServerWebExchangeMatcher.MatchResult.match()
                        : ServerWebExchangeMatcher.MatchResult.notMatch();
            }

            // Otherwise by default deny access
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        });
    }

}
