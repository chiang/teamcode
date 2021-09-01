package io.teamcode.service.project.integration;

import io.teamcode.common.vcs.svn.callback.SummaryLogMessage;
import io.teamcode.config.TcConfig;
import io.teamcode.config.mail.MailProperties;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceCategory;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import io.teamcode.repository.ProjectIntegrationServiceRepository;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.UserService;
import io.teamcode.worker.EmailsOnCommitWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

/**
 * Created by chiang on 2017. 4. 8..
 */
@Service(EmailsOnCommitService.KEY)
@Transactional(readOnly = true)
public class EmailsOnCommitService extends AbstractProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailsOnCommitService.class);

    public static final String KEY = "emails_on_commit";

    private static final String RECIPIENTS_PROPERTY_NAME = "recipients";

    static {
        PROPS.add(RECIPIENTS_PROPERTY_NAME);
    }

    @Autowired
    TcConfig tcConfig;

    @Autowired
    MailProperties mailProperties;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    EmailsOnCommitWorker emailsOnCommitWorker;

    @Autowired
    @Qualifier("emailTemplateEngine")
    TemplateEngine emailTemplateEngine;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    ProjectIntegrationServiceRepository projectIntegrationServiceRepository;

    @Override
    public String getKey() {

        return KEY;
    }

    @Override
    public String getTitle() {
        //return "Emails on commit";
        return "커밋 시 이메일 발송";
    }

    @Override
    public String getDescription() {
        //return "Email the commits and diff of each push to a list of recipients.";
        return "설정된 이메일 주소로 새로운 내용이 커밋될 때 마다 커밋 내용을 이메일로 전송합니다.";
    }

    @Override
    public String getLogoPath() {
        return "/assets/images/logos/logo-integration-default.svg";
    }

    @Override
    public List<String> getPropertyNames() {

        return PROPS;
    }

    @Override
    public ProjectIntegrationServiceCategory getCategory() {

        return ProjectIntegrationServiceCategory.EMAIL;
    }

    @Override
    @Transactional
    public void doPatch(ProjectIntegrationServiceSettings settings, Map<String, String[]> parameters) {
        //FIXME 설정하면 무조건 커밋 시 동작해야 합니다?
        settings.setCommitEvents(Boolean.TRUE);

        projectIntegrationServiceRepository.save(settings);
    }

    @Override
    public void doBuildProperties(Map<String, Object> serviceProperties, Map<String, String[]> parameters) {
        if (parameters.containsKey(RECIPIENTS_PROPERTY_NAME)) {
            String recipients = parameters.get(RECIPIENTS_PROPERTY_NAME)[0];
            if (StringUtils.hasText(recipients)) {
                //TODO Validate Email Address
                String[] recipientsArray = recipients.split(",");
                List<String> trimedRecipients = new ArrayList<>();
                for (String recipient: recipientsArray) {
                    trimedRecipients.add(recipient.trim());
                }

                serviceProperties.put(RECIPIENTS_PROPERTY_NAME, trimedRecipients);
            }
            else {
                serviceProperties.remove(RECIPIENTS_PROPERTY_NAME);
            }
        }
    }

    @Override
    public void execute(final String projectPath, final ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {
        Assert.isTrue(projectIntegrationServiceSettings.getActive());
        logger.debug("프로젝트 서비스 '{}' 를 실행합니다...", this.getTitle());

        Project project = projectService.getByPath(projectPath);

        if (!StringUtils.hasText(projectIntegrationServiceSettings.getProperties())) {
            logger.warn("서비스 '{}' 에 설정 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle());
            return;
        }

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Map<String, Object> serviceProperties = jsonParser.parseMap(projectIntegrationServiceSettings.getProperties());
        if (!serviceProperties.containsKey(RECIPIENTS_PROPERTY_NAME)) {
            logger.warn("서비스 '{}' 에 '{}' 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle(), RECIPIENTS_PROPERTY_NAME);
            return;
        }

        SummaryLogMessage summaryLogMessage = projectIntegrationServiceSettings.getSummaryLogMessage();

        List<String> recipients = (List<String>)serviceProperties.get(RECIPIENTS_PROPERTY_NAME);

        if (recipients != null && !recipients.isEmpty()) {
            String subject = buildSubject(project.getName(), summaryLogMessage);

            EmailOnCommitObject emailOnCommitObject = new EmailOnCommitObject();
            emailOnCommitObject.setProject(project);
            emailOnCommitObject.setSubject(subject);
            emailOnCommitObject.setNumberOfChangedFiles(summaryLogMessage.getChangedFiles().size());
            try {
                User user = userService.get(summaryLogMessage.getAuthor());
                emailOnCommitObject.setAuthor(user.getFullName());
            } catch (ResourceNotFoundException e) {
                emailOnCommitObject.setAuthor(summaryLogMessage.getAuthor());
            }

            emailOnCommitObject.setCommitMessage(summaryLogMessage.getMessage());
            emailOnCommitObject.setCommitUrl(buildCommitUrl(project, projectIntegrationServiceSettings));
            emailOnCommitObject.setChangedFiles(summaryLogMessage.getChangedFiles());

            send(emailOnCommitObject, recipients);
        } else {
            logger.warn("서비스 '{}' 에 '{}' 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle(), RECIPIENTS_PROPERTY_NAME);
        }
    }

    private final void send(EmailOnCommitObject emailOnCommitObject, List<String> recipients) {
        for (String recipient: recipients) {
            MimeMessagePreparator preparator = mimeMessage -> {
                mimeMessage.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient));
                mimeMessage.setFrom(new InternetAddress(mailProperties.getFrom(), mailProperties.getFromName()));

                //TODO mimeMessage.setReplyTo();
                mimeMessage.setSubject(emailOnCommitObject.getSubject());
                mimeMessage.setContent(buildBody(emailOnCommitObject, recipient), "text/html; charset=utf-8");
            };

            this.emailsOnCommitWorker.send(preparator);
        }
    }

    /**
     *
     GITLAB Style

     subject_text = '[Git]'
     subject_text << "[#{project.path_with_namespace}]"
     subject_text << "[#{ref_name}]" if @action == :push
     subject_text << ' '
     if @action == :push && commits
     if commits.length > 1
     subject_text << "Deleted " if reverse_compare?
     subject_text << "#{commits.length} commits: #{commits.first.title}"
     else
     subject_text << "Deleted 1 commit: " if reverse_compare?
     subject_text << commits.first.title
     end
     else
     subject_action = action_name.dup
     subject_action[0] = subject_action[0].capitalize
     subject_text << "#{subject_action} #{ref_type} #{ref_name}"
     end

     * @param projectName
     * @return
     */
    private String buildSubject(final String projectName, SummaryLogMessage summaryLogMessage) {
        StringBuilder subjectBuilder = new StringBuilder();
        subjectBuilder.append("[팀코드] ");
        subjectBuilder.append("[").append(projectName).append("] ");

        //TODO only trunk 메시지만 포함된 경우만?
        //subjectBuilder.append(" [trunk] ");
        subjectBuilder.append(summaryLogMessage.getCommits()).append(" 개 파일에 대한 변경 사항이 있습니다.");

        return subjectBuilder.toString();
    }

    private String buildBody(EmailOnCommitObject emailOnCommitObject, String recipient) {
        final Context ctx = new Context(Locale.KOREA);//TODO auto
        ctx.setVariable("projectName", emailOnCommitObject.getProject().getName());
        ctx.setVariable("recipient", recipient);
        ctx.setVariable("author", emailOnCommitObject.getAuthor());
        ctx.setVariable("teamCodeUrl", tcConfig.getExternalUrl());
        ctx.setVariable("changedFiles", emailOnCommitObject.getNumberOfChangedFiles());
        ctx.setVariable("message", emailOnCommitObject.getCommitMessage());
        ctx.setVariable("commitUrl", emailOnCommitObject.getCommitUrl());
        ctx.setVariable("commitDetails", emailOnCommitObject.getCommitDetails());

        if (logger.isDebugEnabled()) {
            for (ITemplateResolver resolver: emailTemplateEngine.getTemplateResolvers()) {
                logger.debug("Email template engine's resolver name: {}", resolver.getName());
            }

        }

        //Add <img height="36" src="https://37assets.s3.amazonaws.com/emails/queenbee/product_logos/3/bc3.png" alt="Bc3" />
        return this.emailTemplateEngine.process("commits", ctx);
    }

    private String buildCommitUrl(final Project project, final ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {
        StringBuilder commitUrlBuilder = new StringBuilder();
        commitUrlBuilder.append(tcConfig.getExternalUrl());
        if (!tcConfig.getExternalUrl().endsWith("/")) {
            commitUrlBuilder.append("/");
        }
        commitUrlBuilder.append(project.getGroup().getPath());
        commitUrlBuilder.append("/");
        commitUrlBuilder.append(project.getPath());
        commitUrlBuilder.append("/commits/");
        commitUrlBuilder.append(projectIntegrationServiceSettings.getRevision());

        return commitUrlBuilder.toString();
    }

    /*
    Gitlab 에서는 메일에 아래와 같이 헤더를 넣는다? 이런 것은 왜 사용하는지 잘 모르겠다.

    def add_project_headers
    return unless @project
    headers['X-GitLab-Project'] = @project.name
    headers['X-GitLab-Project-Id'] = @project.id
    headers['X-GitLab-Project-Path'] = @project.path_with_namespace

Additionally, CI emails will have 'X-GitLab-Build-Status' header with either 'fail' or 'success'.

Emails from Email On Push will include 'X-Gitlab-Author' header containing the username of user who did the push.
     */

}
