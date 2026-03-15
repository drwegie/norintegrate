package com.norintegrate.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.norintegrate")
@EntityScan("com.norintegrate.common")
@EnableJpaRepositories("com.norintegrate.common")
public class NorIntegrateMcpApplication {

  public static void main(String[] args) {
    SpringApplication.run(NorIntegrateMcpApplication.class, args);
  }
}
