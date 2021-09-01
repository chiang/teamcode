package io.teamcode.config;

import io.teamcode.model.ApplicationStates;
import io.teamcode.repository.UserRepository;
import io.teamcode.service.GroupService;
import io.teamcode.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import java.util.Enumeration;

/**
 * TODO 나중에는 제거해야 할테다. 테스트 용도의 데이터들이므로.
 *
 * Created by chiang on 16. 5. 27..
 */
@Configuration
public class DefaultSystemConfig {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSystemConfig.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    ApplicationStates applicationStates;

    @PostConstruct
    public void init() {


        /*for(IssueTypes type: IssueTypes.values()) {
            if (issueTypeRepository.findByName(type.name()) == null) {
                logger.info("기본 이슈 타입 '{}' 이 존재하지 않아 새로 생성합니다...", type.name());

                issueTypeRepository.save(new IssueType(type));
            }
        }*/
    }

}
