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

package org.grad.eNav.apiGateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * The Netty Configuration.
 *
 * A class to customize the netty configuration and allow larger web-socket
 * content sizes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
public class NettyConfiguration implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    /**
     * The Maximum HTTP Header Size.
     */
    @Value("${server.max-http-header-size:65536}")
    private int maxHttpHeaderSize;

    /**
     * The Maximum Initial Line Length.
     */
    @Value("${server.max-initial-line-length:65536}")
    private int maxInitialLingLength;

    /**
     * The Maximum HTTP Request Upgrade Content Length.
     */
    @Value("${server.h2c-max-content-length:1000000}")
    private int h2cMaxContentLength;

    public void customize(NettyReactiveWebServerFactory container) {
        container.addServerCustomizers(
                httpServer -> httpServer.httpRequestDecoder(
                        httpRequestDecoderSpec -> {
                            httpRequestDecoderSpec.maxHeaderSize(maxHttpHeaderSize);
                            httpRequestDecoderSpec.maxInitialLineLength(maxInitialLingLength);
                            httpRequestDecoderSpec.h2cMaxContentLength(h2cMaxContentLength);
                            return httpRequestDecoderSpec;
                        }
                )
        );
    }
}