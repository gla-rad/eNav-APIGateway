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

package org.grad.eNav.apiGateway.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * The Web Security Configuration.
 *
 * This is the security definition for the filter chains of the API gateway.
 * Therefore, is it slightly different from all the other microservice. It
 * is still required though that the actuator points are handled differently
 * so that the spingboot admin connection works properly.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
class SpringSecurityConfig {

    /**
     * Defines the security web-filter chains.
     *
     * Allows open access to the health and info actuator endpoints.
     * All other actuator endpoints are only available for the actuator role.
     * Finally, all other exchanges need to be authenticated.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges ->
                    exchanges.matchers(EndpointRequest.to("health", "info")).permitAll()
                            .and().authorizeExchange()
                            .matchers(EndpointRequest.toAnyEndpoint().excluding(MappingsEndpoint.class)).hasRole("ACTUATOR")
                            .and().authorizeExchange()
                            .matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .and().authorizeExchange()
                            .pathMatchers("/", "/login").permitAll()
                            .and().authorizeExchange()
                            .anyExchange().authenticated()
                )
                .oauth2Login(withDefaults());
        http.csrf().disable();
        return http.build();
    }

}
