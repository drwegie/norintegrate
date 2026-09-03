plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.9.0" apply false
    kotlin("jvm") version "2.4.10" apply false
    kotlin("plugin.spring") version "2.4.10" apply false
}

allprojects {
    group = "com.norintegrate"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "jacoco")

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.14"
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required = true
            html.required = true
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.35.0")
            formatAnnotations()
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
            mavenBom("org.testcontainers:testcontainers-bom:2.0.5")
            mavenBom("org.springframework.ai:spring-ai-bom:2.0.0")
        }
        dependencies {
            dependency("org.postgresql:postgresql:42.7.13")
            dependency("org.apache.tomcat.embed:tomcat-embed-core:11.0.25")
            dependency("org.apache.tomcat.embed:tomcat-embed-el:11.0.25")
            dependency("org.apache.tomcat.embed:tomcat-embed-websocket:11.0.25")
            // Jackson 3 (tools.jackson) still compiles against the 2.x annotations
            // artifact, so both families move together. Importing the Jackson BOMs
            // does not work here — the Spring Boot plugin's own managed versions
            // outrank any imported bom, and only these explicit entries override
            // them. Keep core/databind/annotations at the versions the matching
            // jackson-bom declares; a partial bump resolves fine and then fails at
            // runtime with NoClassDefFoundError.
            // Boot 4.1.0 manages Log4j at 2.25.4, which carries CVE-2026-49844
            // (MapMessage JSON serialization of non-finite floats). Only
            // log4j-api is flagged, but api and to-slf4j ship as one release
            // train, so both move together for the same reason Jackson does.
            dependencySet("org.apache.logging.log4j:2.25.5") {
                entry("log4j-api")
                entry("log4j-to-slf4j")
            }
            dependency("com.fasterxml.jackson.core:jackson-annotations:2.22")
            dependencySet("com.fasterxml.jackson.core:2.22.1") {
                entry("jackson-core")
                entry("jackson-databind")
            }
            dependencySet("tools.jackson.core:3.2.1") {
                entry("jackson-core")
                entry("jackson-databind")
            }
        }
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
        val envFile = project.file(".env.local")
        if (envFile.exists()) {
            envFile.readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { it.split("=", limit = 2) }
                .filter { it.size == 2 }
                .forEach { (key, value) -> environment(key.trim(), value.trim()) }
        }
    }
}
