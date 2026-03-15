plugins {
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.3.0" apply false
}

allprojects {
    group = "com.norintegrate"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

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
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.3")
            mavenBom("org.testcontainers:testcontainers-bom:2.0.3")
            // TODO: add Spring AI BOM when 2.x GA is released (Spring Boot 4 compatible)
        }
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
