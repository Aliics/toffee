FROM openjdk:11

ENV APP_DIR /toffee/
CMD mkdir ${APP_DIR}
COPY target/toffee-jar-with-dependencies.jar ${APP_DIR}/toffee.jar
WORKDIR ${APP_DIR}

EXPOSE 8080

ENTRYPOINT java -jar toffee.jar