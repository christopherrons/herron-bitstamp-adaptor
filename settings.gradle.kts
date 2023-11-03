rootProject.name = "event-generator"
include("event-generator-server")
include("event-generator-deploy")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("spring.boot.starter.web", "org.springframework.boot:spring-boot-starter-web:3.0.1")
            library("spring.boot.starter.parent", "org.springframework.boot:spring-boot-starter-parent:3.0.1")
            library("spring.kafka", "org.springframework.kafka:spring-kafka:3.0.1")
            library("javafaker", "com.github.javafaker:javafaker:1.0.2")
            library("common.api", "com.herron.exchange:common-api:1.0.271")
            library("common", "com.herron.exchange:common:1.0.271")
            library("integration.api", "com.herron.exchange:integration-api:1.0.31")
            library("integrations", "com.herron.exchange:integrations:1.0.31")
        }

        create("testlibs") {
            library("spring.boot.starter.test", "org.springframework.boot:spring-boot-starter-test:3.0.1")
            library("junit.jupiter.api", "org.junit.jupiter:junit-jupiter-api:5.8.1")
            library("junit.jupiter.engine", "org.junit.jupiter:junit-jupiter-engine:5.8.1")
            library("spring.kafka.test", "org.springframework.kafka:spring-kafka:3.0.1")
        }
    }
}
