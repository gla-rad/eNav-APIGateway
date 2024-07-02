# The GLA e-Navigation Service Architecture - API-Gateway Service

## Quick Reference

* Maintained by:<br/>
[GRAD](https://www.gla-rad.org/)
* Where to get help:<br/>
[Unix & Linux](https://unix.stackexchange.com/help/on-topic),
[Stack Overflow](https://stackoverflow.com/help/on-topic),
[GRAD Wiki](https://rnavlab.gla-rad.org/wiki/E-Navigation_Service_Architecture)
(for GRAD members only)

## What is e-Navigation

The maritime domain is facing a number for challenges, mainly due to the
increasing demand, that may increase the risk of an accident or loss of life.
These challenges require technological solutions and e-Navigation is one such
solution. The International Maritime Organization ([IMO](https://www.imo.org/))
adopted a ‘Strategy for the development and implementation of e‐Navigation’
(MSC85/26, Annexes 20 and 21), providing the following definition of
e‐Navigation:

<div style="padding: 4px;
    background:lightgreen;
    border:2px;
    border-style:solid;
    border-radius:20px;
    color:black">
E-Navigation, as defined by the IMO, is the harmonised collection, integration,
exchange, presentation and analysis of maritime information on-board and ashore
by electronic means to enhance berth-to-berth navigation and related services,
for safety and security at sea and protection of the marine environment.
</div>

In response, the International Association of Lighthouse Authorities 
([IALA](https://www.iala-aism.org/)) published a number of guidelines such as 
[G1113](https://www.iala-aism.org/product/g1113/) and
[G1114](https://www.iala-aism.org/product/g1114/), which establish the relevant
principles for the design and implementation of harmonised shore-based technical
system architectures and propose a set of best practices to be followed. In
these, the terms Common Shore‐Based System (CSS) and Common Shore‐based System
Architecture (CSSA) were introduced to describe the shore‐based technical system
of the IMO’s overarching architecture.

To ensure the secure communication between ship and CSSA, the International
Electrotechnical Commission (IEC), in coordination with IALA, compiled a set of
system architecture and operational requirements for e-Navigation into a
standard better known as [SECOM](https://webstore.iec.ch/publication/64543).
This provides mechanisms for secure data exchange, as well as a TS interface
design that is in accordance with the service guidelines and templates defined
by IALA. Although SECOM is just a conceptual standard, the Maritime Connectivity
Platform ([MCP](https://maritimeconnectivity.net/)) provides an actual
implementation of a decentralised framework that supports SECOM.

## What is the GRAD e-Navigation Service Architecture

The GLA follow the developments on e-Navigation closely, contributing through
their role as an IALA member whenever possible. As part of their efforts, a
prototype GLA e-Navigation Service Architecture is being developed by the GLA
Research and Development Directorate (GRAD), to be used as the basis for the
provision of the future GLA e-Navigation services.

As a concept, the CSSA is based on the Service Oriented Architecture (SOA). A
pure-SOA approach however was found to be a bit cumbersome for the GLA
operations, as it usually requires the entire IT landscape being compatible,
resulting in high investment costs. In the context of e-Navigation, this could
become a serious problem, since different components of the system are designed
by independent teams/manufacturers. Instead, a more flexible microservice
architecture was opted for. This is based on a break-down of the larger
functional blocks into small independent services, each responsible for
performing its own orchestration, maintaining its own data and communicating
through lightweight mechanisms such as HTTP/HTTPS. It should be pointed out that
SOA and the microservice architecture are not necessarily that different.
Sometimes, microservices are even considered as an extension or a more
fine-grained version of SOA.

## The e-Navigation API-Gateway Service

The “API Gateway” implements the GWY component of G1114 and acts as the entry
point for the e-Navigation Service Architecture. Its basic function is to
protect the internal microservices and ensure that only authenticated (whether
through SECOM on OpenID Connect) requests are admitted. In addition, it is able
to complement the architecture functionality by appropriately routing the
incoming requests, providing rate limiting and throttling, and handle the client
HTTP/HTTPS sessions if necessary.

## How to use this image

This image can be used in two ways (based on the use or not of the Spring Cloud
Config server). 
* Enabling the cloud config client and using the configurations located in an 
online repository.
* Disabling the cloud config client and using the configuration provided
locally.

### Cloud Config Configuration

In order to run the image in a **Cloud Config** configuration, you just need
to provide the environment variables that allow it to connect to the cloud
config server. This is assumed to be provided the GRAD e-Navigation Service
Architecture
[Eureka Service](https://hub.docker.com/repository/docker/glarad/enav-eureka/).

The available environment variables are:
    
    ENAV_CLOUD_CONFIG_URI=<The URL of the eureka cloud configuration server>
    ENAV_CLOUD_CONFIG_BRANCH=<The cloud configuration repository branch to be used>
    ENAV_CLOUD_CONFIG_USERNAME=<The cloud configration server username>
    ENAV_CLOUD_CONFIG_PASSWORD=<The cloud configration server password>
    
The parameters will be picked up and used to populate the default
**application.properties** of the service that look as follows:

    server.port=8760
    spring.application.name=api-gateway
    spring.application.version=<application.version>

    # The Spring Cloud Discovery Config
    spring.config.import=optional:configserver:${ENAV_CLOUD_CONFIG_URI}
    spring.cloud.config.username=${ENAV_CLOUD_CONFIG_USERNAME}
    spring.cloud.config.password=${ENAV_CLOUD_CONFIG_PASSWORD}
    spring.cloud.config.label=${ENAV_CLOUD_CONFIG_BRANCH}
    spring.cloud.config.fail-fast=false

As you can see, the service is called **api-gateway** and uses the **8760** port
when running.

To run the image, along with the aforementioned environment variables, you can
use the following command:

    docker run -t -i --rm \
        -p 8760:8760 \
        -e ENAV_CLOUD_CONFIG_URI='<cloud config server url>' \
        -e ENAV_CLOUD_CONFIG_BRANCH='<cloud config config repository branch>' \
        -e ENAV_CLOUD_CONFIG_USERNAME='<config config repository username>' \
        -e ENAV_CLOUD_CONFIG_PASSWORD='<config config repository passord>' \
        <image-id>

### Local Config Configuration

In order to run the image in a **Local Config** configuration, you just need
to mount a local configuration directory that contains the necessary 
**application.properties** files into the **/conf** directory of the image.

This can be done in the following way:

    docker run -t -i --rm \
        -p 8760:8760 \
        -v /path/to/config-directory/on/machine:/conf \
        <image-id>

Examples of the required properties files can be seen below.

While the application properties need to provide the service with an OAuth2.0
server like keycloak, logging configuration, the eureka client connection etc.:

    # Configuration Variables
    service.variable.hostname=<service.hostname>
    service.variable.eureka.server.name=<eureka.server.name>
    service.variable.eureka.server.port=<eureka.server.port>
    service.variable.eureka.server.url=<eureka.server.url>
    service.variable.keycloak.server.name=<keycloak.server.name>
    service.variable.keycloak.server.port=<keycloak.server.port>
    service.variable.keycloak.server.realm=<keycloak.realm>

    # Service properties
    server.port=8760
    spring.application.name=api-gateway
    spring.application.version=0.0.4
    
    # Disable the cloud config
    spring.cloud.config.enabled=false
    
    # Logging Configuration
    logging.file.name=/var/log/${spring.application.name}.log
    logging.logback.rollingpolicy.max-file-size=10MB
    logging.logback.rollingpolicy.file-name-pattern=${spring.application.name}-%d{yyyy-MM-dd}.%i.log
    
    # Management Endpoints
    management.endpoints.web.exposure.include=*
    management.endpoint.health.show-details=when_authorized
    
    # Springdoc configuration
    springdoc.swagger-ui.path=/swagger-ui.html
    springdoc.packagesToScan=org.grad.eNav.apiGateway.controllers
    
    # Eureka Client Configuration
    eureka.client.service-url.defaultZone=http://${service.variable.eureka.server.name}:${service.variable.eureka.server.port}/eureka/
    eureka.client.registryFetchIntervalSeconds=5
    eureka.instance.preferIpAddress=false
    eureka.instance.leaseRenewalIntervalInSeconds=10
    eureka.instance.hostname=localhost
    eureka.instance.securePortEnabled=true
    eureka.instance.nonSecurePortEnabled=false
    
    # Spring-boot Admin Configuration
    spring.boot.admin.client.url=http://${service.variable.eureka.server.name}:${service.variable.eureka.server.port}/admin
    
    # Enable route discovery automatically through Eureka
    spring.cloud.gateway.discovery.locator.enabled=true
    spring.cloud.gateway.discovery.locator.lower-case-service-id=true
    
    # Increase the web-socket sizes
    server.max-http-header-size=1000000
    server.max-initial-line-length=1000000
    server.h2c-max-content-length=10000000
    spring.cloud.gateway.httpclient.websocket.max-frame-payload-length=10000000
    
    # Allow CORS for SECOM requests
    spring.cloud.gateway.default-filters[0]=DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST
    spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOriginPatterns=*
    spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
    spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=*
    spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=false
    
    # Organisation MRN allowed access via X.509 certificates
    gla.rad.api-gateway.x509.organisation.mrn=urn:mrn:mcp:org:mcc
    
    # Keycloak Configuration
    spring.security.oauth2.client.registration.keycloak.client-id=api-gateway
    spring.security.oauth2.client.registration.keycloak.client-secret=<changeit>
    spring.security.oauth2.client.registration.keycloak.client-name=Keycloak
    spring.security.oauth2.client.registration.keycloak.provider=keycloak
    spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
    spring.security.oauth2.client.registration.keycloak.scope=openid
    spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
    spring.security.oauth2.client.provider.keycloak.issuer-uri=http://${service.variable.keycloak.server.name}:${service.variable.keycloak.server.port}/realms/${service.variable.keycloak.server.realm}
    spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
    spring.security.oauth2.resourceserver.jwt.issuer-uri=http://${service.variable.keycloak.server.name}:${service.variable.keycloak.server.port}/realms/${service.variable.keycloak.server.realm}
    
    # Add web-socket rewriting configuration
    server.forward-headers-strategy=framework
    
    # Change the index for PROD and open the appropriate resources
    gla.rad.api-gateway.resources.index:classpath:/templates/index.html
    gla.rad.api-gateway.resources.open:/,/login,/static/**
    
    # The Server SSL Configuration
    server.ssl.enabled=true
    server.ssl.key-store=<path.to.keystore>
    server.ssl.key-store-password=<changeit>
    server.ssl.key-alias=api-gateway
    server.ssl.key-password=<changeit>
    server.ssl.trust-store=<path.to.struststore>
    server.ssl.trust-store-password=<changeit>
    server.ssl.client-auth=want
    
    # Front-end Information
    gla.rad.api-gateway.eureka.url=${service.variable.eureka.server.url}

## Operation

The “API Gateway” is usually located behind an nginx web-server. The main
reasoning behind this setup is to allow multiple e-Navigation test-bed
environments, as well as other third-party services such as Keycloak, to
co-exist in the same public sub-domain. In addition, the presence of a
dedicated web-server outside the microservice eco-system allows for easier
on-the-fly configuration changes, for example in the rate-limiting behaviour,
without the need to reload the microservices themselves.

The current implementation of this component is based on the
[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) that
already offers most of the necessary functionality. Once major aspect missing
however, was the integration with the SECOM authentication flow. This has now
been implemented as an authentication manager component, integrated to the “API
Gateway” security procedure. This manager however needs to be able to handle two
types of SECOM requests:

1. Direct SECOM calls performed by a SECOM client with a valid certificate.
2. Forwarded requests by the nginx server, as described in the previous
paragraph.

## Contributing

For contributing in this project, please have a look at the Github repository
[eNav-APIGateway](https://github.com/gla-rad/eNav-APIGateway). Pull requests are
welcome. For major changes, please open an issue first to discuss what you would
like to change.

Please make sure to update tests as appropriate.

## License

Distributed under the Apache License, Version 2.0.

## Contact

Nikolaos Vastardis -
[Nikolaos.Vastardis@gla-rad.org](mailto:Nikolaos.Vastardis@gla-rad.org)
