import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.1.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    kotlin("jvm") version "1.3.50"
    kotlin("plugin.spring") version "1.3.50"
}

group = "ru.fmtk.khlystov"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.1.6.RELEASE")
    }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    compile("org.springframework.boot:spring-boot-starter-webflux") {
        exclude("hibernate-validator")
    }

    compile("org.springframework.security:spring-security-core:5.1.6.RELEASE")
    compile("org.springframework.boot:spring-boot-starter-security")
    compile("org.springframework.security:spring-security-config:5.1.6.RELEASE")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("commons-codec:commons-codec:1.13")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compile("org.springframework.boot:spring-boot-starter-actuator")

    compile("org.springframework.cloud:spring-cloud-netflix-dependencies:1.0.6.RELEASE")
    compile("org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.1.3.RELEASE")
    compile("org.springframework.cloud:spring-cloud-starter-netflix-hystrix-dashboard:2.1.2.RELEASE")


    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    testImplementation("org.junit.platform:junit-platform-commons:1.4.2")
    testRuntime("org.junit.platform:junit-platform-engine:1.4.2")
    testCompile("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks {
    bootJar {
        archiveFileName.set("${rootProject.name}.jar")
    }
}