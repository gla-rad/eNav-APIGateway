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

package org.grad.eNav.apiGateway.components;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * The X.509 Client Certificate Filter
 *
 * When X.509 SSL requests have been authenticated by the API Gateway,
 * they can subsequently be forwarded onto the internal micro-services.
 * However, the client certificate is not easily (or sometimes not at all)
 * accessible, so we should add the client certificate information into the
 * forwarded request headers.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
public class X509ClientCertificateFilter implements WebFilter {

    // Static Variables
    private static final String MRN_HEADER = "X-SECOM-MRN";
    private static final String CERT_HEADER = "X-SECOM-CERT";
    private static final String ANS10_MRN_OBJECT_IDENTIFIER = "0.9.2342.19200300.100.1.1";

    /**
     * This operation implements the X509 client certificate filtering process
     * where the client SSL certificate, if it exists and has been
     * authenticated will be used to extract information such as the MRN. This
     * will subsequently be added onto the forwarded request headers for
     * the client to be able to access that easily. The full certificate
     * will also be encoded and added into the request headers.
     *
     * @param exchange  The server web exchange
     * @param chain     The web filter chain
     * @return the webfilter chain result
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(context -> {
                    // Try to get the current authentication credentials
                    Optional.of(context)
                            .map(SecurityContext::getAuthentication)
                            .map(Authentication::getCredentials)
                            .filter(X509Certificate.class::isInstance)
                            .ifPresent(credentials -> {
                                final X509Certificate clientX509Certificate = (X509Certificate) credentials;
                                final X500Name x500Name = new X500Name(clientX509Certificate.getSubjectX500Principal()
                                        .getName(X500Principal.RFC2253));
                                final String mrn = Arrays.stream(x500Name.getRDNs(new ASN1ObjectIdentifier(ANS10_MRN_OBJECT_IDENTIFIER)))
                                        .map(RDN::getFirst)
                                        .map(AttributeTypeAndValue::getValue)
                                        .map(IETFUtils::valueToString)
                                        .findFirst()
                                        .orElse(null);
                                final String encodedClientX509Certificate = Optional.of(clientX509Certificate)
                                        .map(c -> {
                                            try {
                                                return c.getEncoded();
                                            } catch (CertificateEncodingException ex) {
                                                return null;
                                            }
                                        })
                                        .map(Base64.getEncoder()::encodeToString)
                                        .orElse(null);

                                // Append to the request headers
                                exchange.getRequest().mutate()
                                        .header(MRN_HEADER, mrn)
                                        .header(CERT_HEADER, encodedClientX509Certificate);
                            });
                })
                .then(chain.filter(exchange));
    }

}
