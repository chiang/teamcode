package io.teamcode.worker;

import io.teamcode.domain.entity.Project;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Commit 시 이메일 발송 시 지연이 발생하므로 이렇게 처리합니다. TODO MQ 를 써야 하나?
 *
 * Created by chiang on 2017. 4. 8..
 */
@Component
public class EmailsOnCommitWorker {

    private static final Logger logger = LoggerFactory.getLogger(EmailsOnCommitWorker.class);

    @Autowired
    JavaMailSender mailSender;

    @Async
    public void send(MimeMessagePreparator preparator) {
        this.mailSender.send(preparator);
        logger.debug("메일 발송을 완료했습니다.");
    }

    private final void sendEmail() {

    }
}
