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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;

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
    public static String X_SSL_CERT_HEADER = "X-SSL-CERT";

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
            // Initialise the local variables
            final CertificateFactory fact = CertificateFactory.getInstance("X.509");
            final HttpHeaders httpHeaders = request.getHeaders();

            // Check if there is a certificate in the headers being forwarded
            if(!httpHeaders.containsKey(X_SSL_CERT_HEADER) || Objects.isNull(httpHeaders.getFirst(X_SSL_CERT_HEADER))) {
                return Mono.empty();
            }

            // Extract the certificate and it's OU principal
            final String decodedPem = URLDecoder.decode(httpHeaders.getFirst(X_SSL_CERT_HEADER), StandardCharsets.UTF_8);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedPem.getBytes());
            final X509Certificate certificate = (X509Certificate) fact.generateCertificate(inputStream);
            final X500Name x500name = new X500Name(certificate.getSubjectX500Principal().getName(X500Principal.RFC1779));
            final String principal = IETFUtils.valueToString(x500name.getRDNs(BCStyle.OU)[0].getFirst());

            // Finally assign the new authentication token to be used
            Authentication authentication =  new PreAuthenticatedAuthenticationToken(principal, certificate);
            return Mono.just(authentication);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return Mono.empty();
        }
    }

}
