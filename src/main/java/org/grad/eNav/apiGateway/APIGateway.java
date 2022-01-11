package org.grad.eNav.apiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * The API Gateway Micro-Service.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class APIGateway {

    /**
     * The main function to run the application.
     *
     * @param args 		The application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(APIGateway.class, args);
    }

    @GetMapping(value = "/token")
    public Mono<String> getHome(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        return Mono.just(authorizedClient.getAccessToken().getTokenValue());
    }

    @GetMapping("/")
    public Mono<String> index(WebSession session) {
        return Mono.just(session.getId());
    }

}