# Build like this:
#     docker build -t <version> -f Dockerfile ..
# e.g.
#     docker build -t glarad/enav-api-gateway:latest -f Dockerfile ..
#
# Run like this:
#     sudo docker run -t -i --rm -p 8760:8760 -v /path/to/config-directory/on/machine:/conf <image-id>
#
# You can also push to docker-hub like:
#     docker push glarad/enav-api-gateway:tagname
#
# A customized conf file (application.properties) can be made available in the folder mounted to /conf.
# When using in non-local environment it is recommended to generate new trust and keystores and place them in
# the conf-folder and point to them in application.properties.
#

# Use the official OpenJDK 21 image as the base image
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/gla-rad/eNav-APIGateway"

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/api-gateway-0.0.4-SNAPSHOT.jar /app/

# Create a modules path just in case
ENV LOADER_PATH /modules
RUN mkdir $LOADER_PATH

# Set the environment variable to activate the "prod" profile
ENV SPRING_PROFILES_ACTIVE=prod

# Define a variable for the configuration directory
ENV CONFIG_DIR=/conf

EXPOSE 8760

CMD ["java", "-jar", "api-gateway-0.0.4-SNAPSHOT.jar", "--spring.config.location=optional:classpath:/,optional:file:${CONFIG_DIR}/"]
