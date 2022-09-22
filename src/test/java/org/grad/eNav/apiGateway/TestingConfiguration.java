/*
 * Copyright (c) 2022 GLA Research and Development Directorate
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

package org.grad.eNav.apiGateway;

import org.grad.eNav.apiGateway.components.X509AuthenticationManager;
import org.grad.eNav.apiGateway.components.X509PrincipalExtractor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * The Test Configuration.
 */
@TestConfiguration
public class TestingConfiguration {

    /**
     * Provide an X.509 Authentication Manager Bean if required.
     */
    @Bean
    public X509AuthenticationManager x509AuthenticationManager() {
        return new X509AuthenticationManager();
    }

    /**
     * Provide an X.509 Principal Extractor Bean if required.
     */
    @Bean
    public X509PrincipalExtractor x509PrincipalExtractor() {
        return new X509PrincipalExtractor();
    }

}
