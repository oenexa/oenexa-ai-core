dependencies {
    implementation(project(":domain"))
    implementation("org.springframework:spring-context")
    implementation(rootProject.libs.jjwt.api)
    runtimeOnly(rootProject.libs.jjwt.impl)
    runtimeOnly(rootProject.libs.jjwt.jackson)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation(rootProject.libs.cucumber.java)
    testImplementation(rootProject.libs.cucumber.junit.platform.engine)
}
