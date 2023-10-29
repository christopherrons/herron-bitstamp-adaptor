dependencies {
    // External Libs
    implementation(libs.spring.boot.starter.web)
    implementation(libs.javafaker)
    implementation(libs.spring.kafka)
    implementation(libs.javafaker)

    // Internal Libs
    implementation(libs.common.api)
    implementation(libs.common)
    implementation(libs.integration.api)
    implementation(libs.integrations)

    // External Test Libs
    testImplementation(testlibs.junit.jupiter.api)
    testImplementation(testlibs.junit.jupiter.engine)
    testImplementation(testlibs.spring.boot.starter.test)
}