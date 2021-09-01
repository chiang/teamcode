package io.teamcode.config;

import io.teamcode.common.thymeleaf.AppearancesDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chiang on 2017. 4. 25..
 */
@Configuration
public class ThymeleafConfig {

    @Bean
    public AppearancesDialect appearancesDialect() {
        return new AppearancesDialect();
    }
}

