package com.norintegrate.mcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.norintegrate"])
@EntityScan("com.norintegrate.common")
@EnableJpaRepositories("com.norintegrate.common")
class NorIntegrateMcpApplication

fun main(args: Array<String>) {
    runApplication<NorIntegrateMcpApplication>(*args)
}
