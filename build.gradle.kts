plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.8.0" apply false
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
            dependency("org.postgresql:postgresql:42.7.11")
            dependency("org.apache.tomcat.embed:tomcat-embed-core:11.0.23")
            dependency("org.apache.tomcat.embed:tomcat-embed-el:11.0.23")
            dependency("org.apache.tomcat.embed:tomcat-embed-websocket:11.0.23")
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
