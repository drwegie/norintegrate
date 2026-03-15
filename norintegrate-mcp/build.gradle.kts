plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":norintegrate-common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // TODO: add spring-ai-mcp-server-spring-boot-starter when Spring AI 2.x GA is released

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
