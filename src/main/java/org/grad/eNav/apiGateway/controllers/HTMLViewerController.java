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

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * The Home Viewer Controller.
 *
 * This is the home controller that allows user to view the main options.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
public class HTMLViewerController {

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
    public Mono<String> index(WebSession session) {
        return Mono.just(
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                "    <meta name=\"description\" content=\"\">\n" +
                "    <meta name=\"author\" content=\"\">\n" +
                "    <title>Please sign in</title>\n" +
                "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n" +
                "    <link href=\"https://getbootstrap.com/docs/4.0/examples/signin/signin.css\" rel=\"stylesheet\" crossorigin=\"anonymous\"/>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "     <div class=\"container\">\n" +
                "         <div class=\"container\"><h2 class=\"form-signin-heading\">e-Navigation Services</h2><table class=\"table table-striped\">\n" +
                "             <tr><td><a href='/msg-broker/'>Message Broker</a></td></tr>\n" +
                "             <tr><td><a href='/vdes-ctrl/'>VDES Controller</a></td></tr>\n" +
                "             <tr><td><a href='/ckeeper'>Certificate Keeper</a></td></tr>\n" +
                "             <tr><td><a href='/raiman'>RAIM Availability Notifier</a></td></tr>\n" +
                "         </table></div>"+
                "    </div>\n" +
                "  </body>\n" +
                "</html>");
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
