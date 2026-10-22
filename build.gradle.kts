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
    apply {
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
        "testImplementation"("org.junit.platform:junit-platform-suite")
        "testImplementation"(rootProject.libs.cucumber.java)
        "testImplementation"(rootProject.libs.cucumber.junit.platform.engine)
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

    val testSourceSet = extensions.getByType<SourceSetContainer>()["test"]

    tasks.register<Test>("testUnit") {
        group = "verification"
        description = "Runs unit tests across the module (excluding BDD and TDD suites)."
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath
        useJUnitPlatform {
            excludeTags("bdd", "tdd")
            excludeEngines("junit-platform-suite", "cucumber")
        }
        filter {
            isFailOnNoMatchingTests = false
            excludeTestsMatching("*Cucumber*")
            excludeTestsMatching("*Bdd*")
            excludeTestsMatching("*Tdd*")
        }
    }

    tasks.register<Test>("testBdd") {
        group = "verification"
        description = "Runs Behavior-Driven Development (BDD) Cucumber scenarios."
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
            includeTestsMatching("*Cucumber*")
            includeTestsMatching("*Bdd*")
        }
    }

    tasks.register<Test>("testTdd") {
        group = "verification"
        description = "Runs Test-Driven Development (TDD) invariant specifications."
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath
        useJUnitPlatform {
            includeTags("tdd")
            excludeEngines("junit-platform-suite", "cucumber")
        }
        filter {
            isFailOnNoMatchingTests = false
            includeTestsMatching("*Tdd*")
        }
    }

    tasks.withType<JacocoReport>().configureEach {
        reports { xml.required.set(true); html.required.set(true) }
    }

    // Coverage gate. All 4 hexagonal modules enforce JaCoCo verification.
    val coverageExempt = emptySet<String>()
    if (name !in coverageExempt) {
        tasks.withType<JacocoCoverageVerification>().configureEach {
            violationRules { rule { limit { minimum = "0.80".toBigDecimal() } } }
        }
        tasks.named("check") { dependsOn("jacocoTestCoverageVerification") }
    }
}

tasks.register("testUnit") {
    group = "verification"
    description = "Executes unit tests across all hexagonal modules (excluding BDD and TDD)."
    dependsOn(subprojects.map { "${it.path}:testUnit" })
}

tasks.register("testBdd") {
    group = "verification"
    description = "Executes BDD Cucumber feature scenarios across all modules."
    dependsOn(subprojects.map { "${it.path}:testBdd" })
}

tasks.register("testTdd") {
    group = "verification"
    description = "Executes TDD contract and invariant specifications across all modules."
    dependsOn(subprojects.map { "${it.path}:testTdd" })
}
