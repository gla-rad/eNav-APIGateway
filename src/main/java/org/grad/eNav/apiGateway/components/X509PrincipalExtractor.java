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
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * The Custom API-Gateway X.509 Certificate Principal Extractor Component.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@Slf4j
public class X509PrincipalExtractor extends SubjectDnX509PrincipalExtractor {


    /**
     * The Component initialisation function.
     */
    @PostConstruct
    public void init() {
        log.info("initialising the X.509 Certificate Principal Extractor");
        // Set a subject regex to allow
        this.setSubjectDnRegex("OU=(.*?)(?:,|$)");
    }

}
