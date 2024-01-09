# The GLA e-Navigation Service Architecture - API-Gateway Service

The API Gateway repository contains the implementation of a service that acts
as a single gateway for the GRAD e-Navigation Service Architecture. This entails
multiple advantages, such as the fact that the involved microservices  operate
inside a more protected environment, as well as operational flexibility like the
option for horizontal scaling. The core micro-service is built using the
Springboot framework.

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

### Development Setup
To start developing just open the repository with the IDE of your choice. The
original code has been generated using
[Intellij IDEA](https://www.jetbrains.com/idea). Just open it by going to:

    File -> New -> Project From Version Control

Provide the URL of the current repository and the local directory you want.

You don't have to use it if you have another preference. Just make sure you
update the *.gitignore* file appropriately.

### Build Setup
The project is using the latest OpenJDK 21 to build, although earlier versions
should also work.

To build the project you will need Maven, which usually comes along-side the
IDE. Nothing exotic about the goals, just clean and install should do:

    mvn clean package

### How to Run
This service can be run in two ways (based on the use or not of the Spring Cloud
Config server).
* Enabling the cloud config client and using the configurations located in an
  online repository.
* Disabling the cloud config client and using the configuration provided
  locally.

#### Cloud Config Configuration
In order to run the service in a **Cloud Config** configuration, you just need
to provide the environment variables that allow it to connect to the cloud
config server. This is assumed to be provided the GRAD e-Navigation Service
Architecture [Eureka Service](https://github.com/gla-rad/enav-Eureka/).

The available environment variables are:

    ENAV_CLOUD_CONFIG_URI=<The URL of the eureka cloud configuration server>
    ENAV_CLOUD_CONFIG_BRANCH=<The cloud configuration repository branch to be used>
    ENAV_CLOUD_CONFIG_USERNAME=<The cloud configration server username>
    ENAV_CLOUD_CONFIG_PASSWORD=<The cloud configration server password>

The parameters will be picked up and used to populate the default
**bootstrap.properties** of the service that look as follows:

    server.port=8760
    spring.application.name=api-gateway
    spring.application.version=<application.version>

    # The Spring Cloud Discovery Config
    spring.cloud.config.uri=${ENAV_CLOUD_CONFIG_URI}
    spring.cloud.config.username=${ENAV_CLOUD_CONFIG_USERNAME}
    spring.cloud.config.password=${ENAV_CLOUD_CONFIG_PASSWORD}
    spring.cloud.config.label=${ENAV_CLOUD_CONFIG_BRANCH}
    spring.cloud.config.fail-fast=false

As you can see, the service is called **api-gateway** and uses the **8760** port
when running.

To run the service, along with the aforementioned environment variables, you can
use the following command:

    java -jar \
        -DENAV_CLOUD_CONFIG_URI='<cloud config server url>' \
        -DENAV_CLOUD_CONFIG_BRANCH='<cloud config config repository branch>' \
        -DENAV_CLOUD_CONFIG_USERNAME='<config config repository username>' \
        -DENAV_CLOUD_CONFIG_PASSWORD='<config config repository passord>' \
        <api-gateway.jar>

#### Local Config Configuration
In order to run the service in a **Local Config** configuration, you just need
to create a local configuration directory that contains the necessary
**.properties** files (including bootstrap) of the service.

Then we can run the service in the following way:

    java -jar \
        --spring.config.location=optional:classpath:/,optional:file:<config_dir>/ \
        <api-gateway.jar>

Examples of the required properties files can be seen below.

For bootstrapping, we need to disable the cloud config client, and clear our the
environment variable inputs:

    server.port=8760
    spring.application.name=api-gateway
    spring.application.version=<application.version>
    
    # Disable the cloud config
    spring.cloud.config.enabled=false
    
    # Clear out the environment variables
    spring.cloud.config.uri=
    spring.cloud.config.username=
    spring.cloud.config.password=
    spring.cloud.config.label=

In the application properties we need to provide the service with an OAuth2.0
server like keycloak, logging configuration, the eureka client connection etc.:

    # Configuration Variables
    service.variable.hostname=<service.hostname>
    service.variable.eureka.server.name=<eureka.server.name>
    service.variable.eureka.server.port=<eureka.server.port>
    service.variable.eureka.server.url=<eureka.server.url>
    service.variable.keycloak.server.name=<keycloak.server.name>
    service.variable.keycloak.server.port=<keycloak.server.port>
    service.variable.keycloak.server.realm=<keycloak.realm>
    
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

### PKI and X.509 Certificates
The API Gateway can also be accessed using X.509 certificates, as it is supposed
to support SECOM. It is using the MCP PKI so all certificates should be
generated through the MCP MIR. 

The MCP MIR currently supports three levels of certificates:

* Level 1: The root certificate mcp-root (which is the base for everything else)
* Level 2: The Identity Registry certificate mcp-idreg (which is used to generate
  all level 3 certificates)
* Level 3: The entity certificates (for service, devices, vessels, users, roles,
  etc.)

Therefore, the service it-self supports HTTPS (using TLS/SSL), based on a 
service certificate provided by the MCP MIR. This is supposed to be contained
in a keystore, with a known alias so that it can be picked up. After generating
the certificate in the MIR (and supposing that you already have the
public/private key pair), you can issue the following command to generate the
keystore:

    openssl pkcs12 -export -out keystore.p12 -name "api-gateway" -inkey api-gateway-private-key.pem -in api-gateway.crt

This will create a *pk12* file, which for Springboot should be translated to the
proprietary JKS format.

    keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS

The configuration for these settings can be found in the *application.properties*
file as follows:

    server.ssl.enabled=true
    server.ssl.key-store=classpath:keystore.jks
    server.ssl.key-store-password=<keystore-password>
    server.ssl.key-alias=<keystore-key-alias>
    server.ssl.key-password=<keystore-key-password>

Clients that want to connect using an X.509 certificate also need to acquire it
through the MCP MIR. To avoid the complexity of adding each level 3 certificate
separately into a trust-store, the API Gateway is provided with a trust-store
that contains both the level 1 and 2 certificates These can be found online in
GitHub through this
[link](https://github.com/maritimeconnectivity/developers.maritimeconnectivity.net/tree/gh-pages/identity/prod-certificate).

To include them into a single JKS trust-store, the following commands can be
issued:

    keytool -import -trustcacerts -noprompt -alias mcp-root -file mcp-root-cert.cer -keystore truststore.p12
    keytool -import -trustcacerts -noprompt -alias mcp-idreg -file mcp-idreg-cert.cer -keystore truststore.p12
    keytool -importkeystore -srckeystore truststore.p12 -srcstoretype PKCS12 -destkeystore truststore.jks -deststoretype JKS

Then the *application.properties* file should be configured as follows:

    server.ssl.trust-store=classpath:truststore.jks
    server.ssl.trust-store-password=<truststore-key-password>

You can find a nice tutorial on all of this process
[here](https://www.baeldung.com/x-509-authentication-in-spring-security).

### Operation
The operation of the gateway is pretty simple. It will receive the incoming
requests and route them according to the active micro-service currently 
registered with the Eureka client of the environment. The name of the service
needs to match exactly the path segment in lowercase. For example, if you are
looking for a service registered in eureka as '''TEST-SERVICE''', then the
path required to be provided in the gateway is:

    api-gateway/test-service/

In addition to routing, the gateway also supports a simple home page where the
most common micro-service are listed as links.

Finally, in order for the gateway service to be registered with the Eureka 
Springboot Admin service, the Spring actuators have been configured 
appropriately.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to
discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
Distributed under the Apache License, Version 2.0. See [LICENSE](./LICENSE.md)
for more information.

## Contact
Nikolaos Vastardis - Nikolaos.Vastardis@gla-rad.org



