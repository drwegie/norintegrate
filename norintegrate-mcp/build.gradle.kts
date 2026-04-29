plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":norintegrate-common"))
    testImplementation(testFixtures(project(":norintegrate-common")))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:9.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.ai:spring-ai-starter-mcp-client-webflux")
}
