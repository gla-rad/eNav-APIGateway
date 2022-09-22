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

package org.grad.eNav.apiGateway.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * The Home Viewer Controller.
 *
 * This is the home controller that allows user to view the main options.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@Slf4j
public class HTMLViewerController {

    /**
     * The index HTML source file.
     */
    @Value("${gla.rad.api-gateway.resources.index:classpath:/templates/index.html}")
    Resource resourceFile;

    /**
     * The home page of the API gateway.
     *
     * This can just be a list of all the services currently available along
     * with link for easy access.
     *
     * @param session the current session.
     * @return The home page output
     */
    @GetMapping("/")
    public Mono<String> index(WebSession session) throws IOException {
        return Mono.just(new String(resourceFile.getInputStream().readAllBytes()));
    }

    /**
     * Allows users to easily access their keycloak JWT token.
     *
     * @param authorizedClient the authorized client
     * @return the JWT token of the authorized client
     */
    @GetMapping(value = "/token")
    public Mono<String> getHome(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        return Mono.just(authorizedClient.getAccessToken().getTokenValue());
    }

}
