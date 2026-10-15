plugins {
    id("org.springframework.boot")
}

dependencies {
    // Modular monolith: the gateway assembles every module into one deployable.
    rootProject.subprojects
        .filter { it.name.startsWith("oenexa-") && it.name != "oenexa-gateway" && it.name != "oenexa-ui" }
        .forEach { implementation(project(it.path)) }

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(rootProject.libs.springdoc.webflux)
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    testImplementation("org.springframework.security:spring-security-test")
}

springBoot {
    mainClass.set("ai.oenexa.OenexaApplication")
}
