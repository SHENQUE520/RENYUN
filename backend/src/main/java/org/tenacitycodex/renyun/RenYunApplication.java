package org.tenacitycodex.renyun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RenYunApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenYunApplication.class, args);
    }

}
