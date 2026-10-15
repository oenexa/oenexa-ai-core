plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    java
}

val javaVersion = 25

allprojects {
    group = "ai.oenexa"
    version = "0.1.0-SNAPSHOT"
    repositories { mavenCentral() }
}

subprojects {
    if (name == "oenexa-ui") return@subprojects
    apply{
        plugin("java-library")
        plugin("io.spring.dependency-management")
        plugin("jacoco")
    }
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }

    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            mavenBom("org.testcontainers:testcontainers-bom:${rootProject.libs.versions.testcontainers.get()}")
            mavenBom("org.springframework.ai:spring-ai-bom:${rootProject.libs.versions.spring.ai.get()}")
        }
    }

    dependencies {
        "compileOnly"(rootProject.libs.lombok)
        "annotationProcessor"(rootProject.libs.lombok)
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.springframework.boot:spring-boot-webtestclient")
        "testImplementation"("io.projectreactor:reactor-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    dependencies {
        if (name != "oenexa-common" && name !in listOf("domain", "application", "infrastructure", "api")) {
            "api"(project(":oenexa-common"))
        }
        when (name) {
            "oenexa-common" -> {
                "api"("org.springframework.boot:spring-boot-starter-webflux")
                "api"("org.springframework.boot:spring-boot-starter-validation")
            }
            "oenexa-auth" -> {
                "api"("org.springframework.boot:spring-boot-starter-security")
                "api"(rootProject.libs.jjwt.api)
                "runtimeOnly"(rootProject.libs.jjwt.impl)
                "runtimeOnly"(rootProject.libs.jjwt.jackson)
            }
            "oenexa-chat" -> {
                "api"(project(":oenexa-auth"))
                "api"("org.springframework.ai:spring-ai-client-chat")
            }
            "oenexa-agent", "oenexa-rag" -> "api"(project(":oenexa-auth"))
            "oenexa-core" -> {
                "api"("org.springframework.boot:spring-boot-starter-data-jpa")
                "api"("org.flywaydb:flyway-core")
                "runtimeOnly"("org.flywaydb:flyway-database-postgresql")
                "runtimeOnly"("org.postgresql:postgresql")
            }
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing"))
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.withType<JacocoReport>().configureEach {
        reports { xml.required.set(true); html.required.set(true) }
    }

    // Coverage gate. Stub modules carry no logic and are exempt.
    val coverageExempt = setOf(
        "oenexa-gateway", "oenexa-core",
        "domain", "application", "infrastructure", "api"
    )
    if (name !in coverageExempt) {
        tasks.withType<JacocoCoverageVerification>().configureEach {
            violationRules { rule { limit { minimum = "0.80".toBigDecimal() } } }
        }
        tasks.named("check") { dependsOn("jacocoTestCoverageVerification") }
    }
}
