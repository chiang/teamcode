package io.teamcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by chiang on 2017. 1. 18..
 */
@ComponentScan("io.teamcode")
@EntityScan("io.teamcode")
@EnableCaching
@EnableAsync
@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
public class TeamCodeServer {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(TeamCodeServer.class);
        app.run(args);
    }
}
