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

@Component
public class StripContextAndPrefixGatewayFilterFactory extends AbstractGatewayFilterFactory<StripPrefixGatewayFilterFactory.Config> {


    public static final String PARTS_KEY = "parts";

    public StripContextAndPrefixGatewayFilterFactory() {
        super(StripPrefixGatewayFilterFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("parts");
    }

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
                        .stream(StringUtils.tokenizeToStringArray(newPath.toString(), "/"))
                        .skip(config.getParts())
                        .collect(Collectors.joining("/"));

                ServerHttpRequest newRequest = request.mutate().contextPath(contextPath).path(newPath.toString()).build();
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());
                return chain.filter(exchange.mutate().request(newRequest).build());
            }

            public String toString() {
                return GatewayToStringStyler.filterToStringCreator(StripContextAndPrefixGatewayFilterFactory.this).append("parts", config.getParts()).toString();
            }
        };
    }

    public static class Config {
        private int parts = 1;

        public Config() {
        }

        public int getParts() {
            return this.parts;
        }

        public void setParts(int parts) {
            this.parts = parts;
        }
    }

}
