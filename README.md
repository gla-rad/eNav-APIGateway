# e-Navigation API Gateway Service
The API Gateway repository contains the implementation of a service
that act as a single gateway for the GRAD e-Navigation architecture. This
entails multiple advantages, such as the fact that the involved microservices 
operate inside a more protected environment, as well as operational flexibility
like the option for horizontal scaling. The core micro-service is built using 
the Springboot framework.

## Development Setup
To start developing just open the repository with the IDE of your choice. The
original code has been generated using
[Intellij IDEA](https://www.jetbrains.com/idea). Just open it by going to:

    File -> New -> Project From Verson Control

Provide the URL of the current repository and the local directory you want.

You don't have to use it if you have another preference. Just make sure you
update the *.gitignore* file appropriately.

## Build Setup
The project is using the latest OpenJDK 17 to build, and only that should be
used. The main issue is that the current Geomesa library only supports Java 8
at the moment. We can only upgrade after later JDK versions are also supported
by Geomesa.

To build the project you will need Maven, which usually comes along-side the
IDE. Nothing exotic about the goals, just clean and install should do:

    mvn clean package

## Configuration
The configuration of the eureka server is based on the properties files found
in the *main/resources* directory.

The *boostrap.properties* contains the necessary properties to start the service
while the *application.properties* everything else i.e. the security
configuration.

Note that authentication is provided by Keycloak, so before you continue, you
need to make sure that a keycloak server is up and running, and that a client
is registered. Once that is done, the Spring security OAuth2 client provider 
will require the following *application.properties* to be provided:

    spring.security.oauth2.client.provider.keycloak.token-uri=<The keycloak address>/auth/realms/niord/protocol/openid-connect/token
    spring.security.oauth2.client.provider.keycloak.authorization-uri<The keycloak address>/auth/realms/niord/protocol/openid-connect/auth
    spring.security.oauth2.client.provider.keycloak.userinfo-uri=<The keycloak address>/realms/niord/protocol/openid-connect/userinfo
    spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
    spring.security.oauth2.client.registration.keycloak.client-id=<The client name>
    spring.security.oauth2.client.registration.keycloak.client-secret=<The generated client sercet>

## PKI and X.509 Certificates
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

    keytool -import -trustcacerts -noprompt -alias mcp-root -file mcp-root-cert.cer -keystore truststore.jks
    keytool -import -trustcacerts -noprompt -alias mcp-idreg -file mcp-idreg-cert.cer -keystore truststore.jks

Then the *application.properties* file should be configured as follows:

    server.ssl.trust-store=classpath:truststore.jks
    server.ssl.trust-store-password=<truststore-key-password>

You can find a nice tutorial on all of this process
[here](https://www.baeldung.com/x-509-authentication-in-spring-security).

## Running the Service
To run the service, just like any other Springboot micro-service, all you need
to do is run the main class, i.e. APIGateway. No further arguments are
required. Everything should be picked up through the properties files.

## Description
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
Distributed under the Apache License. See [LICENSE](./LICENSE) for more
information.

## Contact
Nikolaos Vastardis - Nikolaos.Vastardis@gla-rad.org



