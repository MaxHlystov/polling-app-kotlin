FROM gradle:5.4.1-jdk-11 AS build

ENV PROJECT_DIR="/opt/otus_library"
RUN mkdir -p $PROJECT_DIR
WORKDIR $PROJECT_DIR

ADD ./pom.xml $PROJECT_DIR
ADD ./src $PROJECT_DIR/src

RUN mvn clean package



FROM azul/zulu-openjdk:11.0.1 AS runtime

ENV PROJECT_DIR="/opt/otus_library"
RUN mkdir -p $PROJECT_DIR
WORKDIR $PROJECT_DIR
COPY --from=0 $PROJECT_DIR/target/m4-h1-1.0-SNAPSHOT.jar $PROJECT_DIR/

EXPOSE 8080

CMD ["java", "-jar", "/opt/otus_library/m4-h1-1.0-SNAPSHOT.jar"]








#------------------------------------
FROM adoptopenjdk:11-jdk-hotspot

CMD ["gradle"]

ENV GRADLE_HOME /opt/gradle

RUN set -o errexit -o nounset \
    && echo "Adding gradle user and group" \
    && groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle \
    && mkdir /home/gradle/.gradle \
    && chown --recursive gradle:gradle /home/gradle \
    \
    && echo "Symlinking root Gradle cache to gradle Gradle cache" \
    && ln -s /home/gradle/.gradle /root/.gradle

VOLUME /home/gradle/.gradle

WORKDIR /home/gradle

RUN apt-get update \
    && apt-get install --yes --no-install-recommends \
        fontconfig \
        unzip \
        wget \
        \
        bzr \
        git \
        mercurial \
        openssh-client \
        subversion \
    && rm -rf /var/lib/apt/lists/*

ENV GRADLE_VERSION 5.6.2
ARG GRADLE_DOWNLOAD_SHA256=32fce6628848f799b0ad3205ae8db67d0d828c10ffe62b748a7c0d9f4a5d9ee0
RUN set -o errexit -o nounset \
    && echo "Downloading Gradle" \
    && wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    \
    && echo "Checking download hash" \
    && echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - \
    \
    && echo "Installing Gradle" \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
    && ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
    \
    && echo "Testing Gradle installation" \
    && gradle --version