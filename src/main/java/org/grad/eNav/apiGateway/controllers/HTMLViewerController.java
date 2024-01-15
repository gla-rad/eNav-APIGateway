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

package org.grad.eNav.apiGateway.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * The Home Viewer Controller.
 *
 * This is the home controller that allows user to view the main options.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Controller
@Slf4j
public class HTMLViewerController {

    /**
     * The index HTML source file.
     */
    @Value("${gla.rad.api-gateway.resources.index:index}")
    String resourceFile;

    /**
     * The URL of the Eureka micro-service Springboot Admin interface.
     */
    @Value("${gla.rad.api-gateway.eureka.url:/eureka/admin}")
    String eurekaUrl;

    /**
     * The URL of the Niord front-end interface.
     */
    @Value("${gla.rad.api-gateway.niord.url:/niord}")
    String niordUrl;

    /**
     * The home page of the API gateway.
     *
     * This can just be a list of all the services currently available along
     * with link for easy access.
     *
     * @param model The application UI model
     * @return The home page output
     */
    @GetMapping("/")
    public String index(Model model) throws IOException {
        model.addAttribute("eurekaUrl", this.eurekaUrl);
        model.addAttribute("niordUrl", this.niordUrl);
        // Return the rendered index
        return this.resourceFile;
    }

}
