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

    // Core Hexagonal dependencies are defined directly within each module build.gradle.kts

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

    // Coverage gate. All 23 modules enforce JaCoCo verification.
    val coverageExempt = emptySet<String>()
    if (name !in coverageExempt) {
        tasks.withType<JacocoCoverageVerification>().configureEach {
            violationRules { rule { limit { minimum = "0.80".toBigDecimal() } } }
        }
        tasks.named("check") { dependsOn("jacocoTestCoverageVerification") }
    }
}
