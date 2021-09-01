package io.teamcode.core.actuate.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class MailHealthIndicator extends AbstractHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(MailHealthIndicator.class);

    private final JavaMailSenderImpl mailSender;

    public MailHealthIndicator(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        logger.debug("Checking mail health... host: {}", mailSender.getHost());

        builder.withDetail("location",
                this.mailSender.getHost() + ":" + this.mailSender.getPort());
        this.mailSender.testConnection();

        logger.debug("Mail health ok!");
        builder.up();
    }
}
