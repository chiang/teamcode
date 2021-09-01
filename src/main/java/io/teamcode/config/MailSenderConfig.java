package io.teamcode.config;

import io.teamcode.config.mail.MailProperties;
import io.teamcode.model.ApplicationState;
import io.teamcode.model.ApplicationStates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailSenderConfig {

    @Autowired
    private MailProperties properties;

    @Autowired
    ApplicationStates applicationStates;

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        if (properties.isEnabled()) {
            applyProperties(sender);
        }
        else {
            applicationStates.setSmtpState(ApplicationState.OFF);
        }

        return sender;
    }

    private void applyProperties(JavaMailSenderImpl sender) {
        sender.setHost(this.properties.getHost());
        if (this.properties.getPort() != null) {
            sender.setPort(this.properties.getPort());
        }
        sender.setUsername(this.properties.getUsername());
        sender.setPassword(this.properties.getPassword());
        sender.setProtocol(this.properties.getProtocol());
        if (this.properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(this.properties.getDefaultEncoding().name());
        }
        if (!this.properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(this.properties.getProperties()));
        }
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
