plugins {
    java
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
}

springBoot {
    mainClass.set("com.herron.bitstamp.consumer.BitstampConsumerApplicationTest")
}

// Project Configs
allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "maven-publish")
    apply(plugin = "java-library")

    group = "com.herron.bitstamp.consumer"
    version = "1.0.0-SNAPSHOT"
    if (project.hasProperty("releaseVersion")) {
        val releaseVersion: String by project
        version = releaseVersion
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    //Project Modules
    implementation(project(":bitstamp-consumer-server"))

    // External Libs
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.parent)
    implementation(libs.spring.kafka)
    implementation(libs.tyrus.standalone.client)
    implementation(libs.javax.json.api)
    implementation(libs.javax.json)

    // External Test Libs
    testImplementation(testlibs.junit.jupiter.api)
    testImplementation(testlibs.junit.jupiter.engine)
    testImplementation(testlibs.spring.boot.starter.test)
    testImplementation(testlibs.spring.kafka.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
