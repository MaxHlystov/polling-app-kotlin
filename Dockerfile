FROM gradle:5.4.1-jdk-11 AS build

ENV PROJECT_DIR="/opt/polling_app"
RUN mkdir -p $PROJECT_DIR
WORKDIR $PROJECT_DIR

ADD ./build.gradle.kts $PROJECT_DIR
ADD ./settings.gradle.kts $PROJECT_DIR
ADD ./src $PROJECT_DIR/src

RUN gradle build jar



FROM azul/zulu-openjdk:11.0.1 AS runtime

ENV PROJECT_DIR="/opt/polling_app"
RUN mkdir -p $PROJECT_DIR
WORKDIR $PROJECT_DIR
COPY --from=0 $PROJECT_DIR/build/libs/hw_polling_app-0.0.1-SNAPSHOT.jar $PROJECT_DIR/

EXPOSE 8080

CMD ["java", "-jar", "/opt/polling_app/hw_polling_app-0.0.1-SNAPSHOT.jar"]
