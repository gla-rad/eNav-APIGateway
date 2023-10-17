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
FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.source="https://github.com/gla-rad/eNav-APIGateway"

ENV LOADER_PATH /modules

RUN mkdir /conf

RUN mkdir $LOADER_PATH

ADD target/api-gateway-0.0.4-SNAPSHOT.jar .

EXPOSE 8760

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "api-gateway-0.0.4-SNAPSHOT.jar", "--spring.config.location=/conf/"]
