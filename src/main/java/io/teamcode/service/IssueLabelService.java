package io.teamcode.service;

import io.teamcode.domain.entity.IssueLabel;
import io.teamcode.domain.entity.IssueState;
import io.teamcode.domain.entity.Project;
import io.teamcode.repository.IssueLabelPriorityRepository;
import io.teamcode.repository.IssueLabelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
@Transactional(readOnly = true)
public class IssueLabelService {

    private static final Logger logger = LoggerFactory.getLogger(IssueLabelService.class);

    private static final String DEFAULT_LABEL_COLOR = "#428BCA";

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    @Autowired
    IssueService issueService;

    @Autowired
    IssueLabelRepository issueLabelRepository;

    @Autowired
    private IssueLabelPriorityRepository issueLabelPriorityRepository;

    public List<IssueLabel> list(final String projectPath) {
        Project project = projectService.getByPath(projectPath);
        List<IssueLabel> issueLabels = issueLabelRepository.findByProject(project);
        /*for (IssueLabel issueLabel: issueLabels) {
            issueLabel.setOpenedIssueCount(issueService.countByLabelAndState(project, issueLabel, IssueState.OPENED));
        }*/

        return issueLabels;
    }

    @Transactional
    public void prioritize(final String repositoryName, List<Long> issueLabelIds) {
        /*Repository repository = repositoryService.getDto(repositoryName);
        List<IssueLabelPriority> issueLabelPriorities = issueLabelPriorityRepository.findByIssueLabelId(repository);
        if (issueLabelPriorities.size() > 0) {
            IssueLabelPriority issueLabelPriority;
            for(int i = 0; i < issueLabelIds.size(); i++) {
                issueLabelPriority = issueLabelPriorityRepository.findByIssueLabelId(issueLabelIds.getDto(i));
                issueLabelPriority.setPriority(new Integer(i));
            }
        }*/
    }

    @Transactional
    public IssueLabel create(final String projectPath, IssueLabel issueLabel) {
        Project project = projectService.getByPath(projectPath);

        issueLabel.setProject(project);
        if(!StringUtils.hasText(issueLabel.getColor()))
            issueLabel.setColor(DEFAULT_LABEL_COLOR);

        issueLabel.setCreatedBy(userService.getCurrentUser());
        issueLabel.setCreatedAt(new Date());

        IssueLabel savedIssueLabel = issueLabelRepository.save(issueLabel);

        return savedIssueLabel;
    }

    @Transactional
    public void createDefaultLabels(final String projectPath) {
        //TODO check requery in create method?
        Project project = projectService.getByPath(projectPath);

        if (issueLabelRepository.countByProject(project) == 0) {
            IssueLabel bugLabel = new IssueLabel();
            bugLabel.setTitle("bug");
            bugLabel.setColor("#d9534f");
            create(projectPath, bugLabel);

            IssueLabel criticalLabel = new IssueLabel();
            criticalLabel.setTitle("critical");
            criticalLabel.setColor("#d9534f");
            create(projectPath, criticalLabel);

            IssueLabel discussionLabel = new IssueLabel();
            discussionLabel.setTitle("discussion");
            discussionLabel.setColor("#428bca");
            create(projectPath, discussionLabel);

            IssueLabel enhancementLabel = new IssueLabel();
            enhancementLabel.setTitle("enhancement");
            enhancementLabel.setColor("#5cb85c");
            create(projectPath, enhancementLabel);

            IssueLabel suggestionLabel = new IssueLabel();
            suggestionLabel.setTitle("suggestion");
            suggestionLabel.setColor("#428bca");
            create(projectPath, suggestionLabel);

            IssueLabel supportLabel = new IssueLabel();
            supportLabel.setTitle("support");
            supportLabel.setColor("#f0ad4e");
            create(projectPath, supportLabel);

            logger.info("Default set of labels was created.");
        }
        else {
            logger.warn("Labels of repoitory '{}' exist. Default set of labels creation was skipped.");
        }
    }
}
