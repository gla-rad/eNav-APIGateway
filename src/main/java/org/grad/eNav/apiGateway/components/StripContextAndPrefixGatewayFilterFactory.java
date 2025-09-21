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
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.support.GatewayToStringStyler;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The StripContextAndPrefixGatewayFilterFactory Class.
 *
 * When hosting the API-Gateway in a domain sub-path we need to route all
 * requests to the microservices appropriately. However, the forwarding strategy
 * that is able to correctly pick up the base-path (i.e. framework strategy)
 * does not correctly route the discovered services calls, by not removing the
 * base paths.
 * <p/>
 * This is related to a known issue in the spring cloud gateway and a workaround
 * was suggested
 * <a href="https://github.com/spring-cloud/spring-cloud-gateway/issues/1759">
 *     here
 * </a>.
 * </p>
 * This filter factory follows this work around to allow the correct service
 * routing through the service configuration as follows:
 * <p>
 *  # Adding Route for the "/enav" sub-path support
 *  spring.cloud.gateway.routes[0].id=service-discovery-route-msg-broker
 *  spring.cloud.gateway.routes[0].uri=lb://msg-broker
 *  spring.cloud.gateway.routes[0].predicates[0]=Path=/enav/msg-broker/**
 *  spring.cloud.gateway.routes[0].filters[0]=StripContextAndPrefix=2
 *  spring.cloud.gateway.routes[1].id=service-discovery-route-vdes-ctrl
 *  spring.cloud.gateway.routes[1].uri=lb://vdes-ctrl
 *  spring.cloud.gateway.routes[1].predicates[0]=Path=/enav/vdes-ctrl/**
 *  spring.cloud.gateway.routes[1].filters[0]=StripContextAndPrefix=2
 *  spring.cloud.gateway.routes[2].id=service-discovery-route-aton-service
 *  spring.cloud.gateway.routes[2].uri=lb://aton-service
 *  spring.cloud.gateway.routes[2].predicates[0]=Path=/enav/aton-service/**
 *  spring.cloud.gateway.routes[2].filters[0]=StripContextAndPrefix=2
 *  spring.cloud.gateway.routes[3].id=service-discovery-route-aton-admin-service
 *  spring.cloud.gateway.routes[3].uri=lb://aton-admin-service
 *  spring.cloud.gateway.routes[3].predicates[0]=Path=/enav/aton-admin-service/**
 *  spring.cloud.gateway.routes[3].filters[0]=StripContextAndPrefix=2
 *  spring.cloud.gateway.routes[4].id=service-discovery-route-ckeeper
 *  spring.cloud.gateway.routes[4].uri=lb://ckeeper
 *  spring.cloud.gateway.routes[4].predicates[0]=Path=/enav/ckeeper/**
 *  spring.cloud.gateway.routes[4].filters[0]=StripContextAndPrefix=2
 * </p>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Component
public class StripContextAndPrefixGatewayFilterFactory extends AbstractGatewayFilterFactory<StripPrefixGatewayFilterFactory.Config> {

    public static final String PARTS_KEY = "parts";

    /**
     * The Class Constructor.
     */
    public StripContextAndPrefixGatewayFilterFactory() {
        super(StripPrefixGatewayFilterFactory.Config.class);
    }

    /**
     * Extending the field ordering function of the
     * AbstractGatewayFilterFactory. In this case there only one filter, i.e.
     * the "parts".
     *
     * @return the list with the ordering fields
     */
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("parts");
    }

    /**
     * Applies the filter's operation. In this the incoming web-exchange will
     * be stripped of the first N parts. The context path for the call will also
     * get stripped in a similar manner so that the annoying error does not
     * occur.
     *
     * @param config the gateway filter factory configuration
     * @return the initialised gateway filter
     */
    public GatewayFilter apply(final StripPrefixGatewayFilterFactory.Config config) {
        return new GatewayFilter() {
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                ServerWebExchangeUtils.addOriginalRequestUrl(exchange, request.getURI());
                String path = request.getURI().getRawPath();
                String[] originalParts = StringUtils.tokenizeToStringArray(path, "/");
                StringBuilder newPath = new StringBuilder("/");

                for(int i = 0; i < originalParts.length; ++i) {
                    if (i >= config.getParts()) {
                        if (newPath.length() > 1) {
                            newPath.append('/');
                        }

                        newPath.append(originalParts[i]);
                    }
                }

                if (newPath.length() > 1 && path.endsWith("/")) {
                    newPath.append('/');
                }

                // Generate the suffix path
                String contextPath = "/" + Arrays
                        .stream(StringUtils.tokenizeToStringArray(path, "/"))
                        .skip(config.getParts())
                        .collect(Collectors.joining("/"));

                ServerHttpRequest newRequest = request.mutate().contextPath(contextPath).path(newPath.toString()).build();
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());
                log.debug("New request URI: " + newRequest.getURI().toString());
                return chain.filter(exchange.mutate().request(newRequest).build());
            }

            public String toString() {
                return GatewayToStringStyler.filterToStringCreator(StripContextAndPrefixGatewayFilterFactory.this).append("parts", config.getParts()).toString();
            }
        };
    }

}
