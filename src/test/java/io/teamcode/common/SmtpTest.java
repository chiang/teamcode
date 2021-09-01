package io.teamcode.common;

import io.teamcode.service.project.integration.EmailsOnCommitService;
import io.teamcode.util.NetworkUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class SmtpTest {

    private static final Logger logger = LoggerFactory.getLogger(SmtpTest.class);

    @Test
    public void send() throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.mailgun.org");
        sender.setUsername("teamcode@mailer.rightstack.net");
        sender.setPassword("_xlazhem");

        Session session = sender.getSession();
        if (logger.isDebugEnabled()) {
            for (Provider provider: session.getProviders()) {
                try {
                    logger.debug("SMTP Provider: {}, isConnected: {}", provider.getProtocol(), session.getTransport(provider.getProtocol()).isConnected());
                } catch (NoSuchProviderException e) {
                    logger.debug("Invalid SMTP provider protocol: {}", provider.getProtocol());
                }
            }
        }
        logger.debug("SMTP 서버가 살아 있는지 체크합니다. Port 는 587, 테스트 서버는 Mailgun Free Plan 을 사용합니다...");
        Assert.assertTrue(NetworkUtils.ping("smtp.mailgun.org", 587, 1000));


        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("chiang@rightstack.net");
        helper.setText("Thank you for ordering!");

        sender.send(message);
        System.out.println(sender.getSession().getTransport("smtp").isConnected());
    }

}
