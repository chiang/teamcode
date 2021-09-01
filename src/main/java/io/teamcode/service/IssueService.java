package io.teamcode.service;

import io.teamcode.domain.entity.Issue;
import io.teamcode.domain.entity.IssueLabel;
import io.teamcode.domain.entity.IssueState;
import io.teamcode.domain.entity.Project;
import io.teamcode.repository.IssueLabelMapRepository;
import io.teamcode.repository.IssueRepository;
import io.teamcode.web.ui.view.IssueView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * def build_headers(hook_name)
 headers = {
 'Content-Type' => 'application/json',
 'X-Gitlab-Event' => hook_name.singularize.titleize
 }
 headers['X-Gitlab-Token'] = token if token.present?
 headers
 end
 *
 *
 */
@Service
@Transactional(readOnly = true)
public class IssueService {

    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    /*@Autowired
    WebhookService webhookService;

    @Autowired
    SystemAuditNoteService systemAuditNoteService;*/

    @Autowired
    IssueRepository issueRepository;

    @Autowired
    IssueLabelMapRepository issueLabelMapRepository;

    public Issue get(final Long issueId) {
        Issue issue = issueRepository.findOne(issueId);
        if (issue == null)
            throw new ResourceNotFoundException("");

        return issue;
    }

    public Issue get(final String projectPath, final Long internalId) {
        Project project = projectService.getByPath(projectPath);
        Issue issue = issueRepository.findByProjectAndInternalId(project, internalId);
        if (issue == null)
            throw new ResourceNotFoundException("");

        return issue;
    }

    public IssueView getDto(final String repositoryName, final Long internalId) {
        Issue issue = get(repositoryName, internalId);

        IssueView issueDto = new IssueView();
        issueDto.setIssue(issue);
        //issueDto.setAuditNotes(systemAuditNoteService.f);

        return issueDto;
    }

    public List<Issue> list(final String projectPath, final Pageable pageable) {
        //Repository repository = repositoryService.get(repositoryName);
        Project project = projectService.getByPath(projectPath);

        return issueRepository.findByProject(project, pageable);
    }

    @Transactional
    public Issue create(final String projectPath, Issue issue) {
        Project project = projectService.getByPath(projectPath);

        Long maxInternalId = issueRepository.getMaxInternalIdByProject(project);
        if (maxInternalId == null)
            issue.setInternalId(new Long(1));
        else
            issue.setInternalId(new Long(maxInternalId.longValue() + 1));
        issue.setProject(project);
        issue.setAuthor(userService.getCurrentUser());
        issue.setState(IssueState.OPENED);
        issue.setCreatedAt(new Date());


        Issue savedIssue = issueRepository.save(issue);

        return savedIssue;
    }

    @Transactional
    public Issue update(final Issue issueForm) {
        Issue exist = get(issueForm.getId());

        exist.setTitle(issueForm.getTitle());
        exist.setDescription(issueForm.getDescription());
        exist.setMilestone(issueForm.getMilestone());
        exist.setUpdatedBy(userService.getCurrentUser());
        exist.setUpdatedAt(new Date());

        Issue updatedIssue = issueRepository.save(exist);

        handleChanges(issueForm, updatedIssue);

        return updatedIssue;
    }

    @Transactional
    public Issue patch(final Issue issueForm) {
        Issue exist = get(issueForm.getId());

        if (StringUtils.hasText(issueForm.getTitle()))
            exist.setTitle(issueForm.getTitle());

        if (StringUtils.hasText(issueForm.getDescription()))
            exist.setDescription(issueForm.getDescription());

        if (issueForm.getMilestone() != null)
            exist.setMilestone(issueForm.getMilestone());

        exist.setUpdatedBy(userService.getCurrentUser());
        exist.setUpdatedAt(new Date());

        Issue patched = issueRepository.save(exist);

        handleChanges(issueForm, patched);

        return patched;
    }

    private void handleChanges(Issue issueForm, final Issue issueEntity) {
        if (issueForm.getMilestone() != null) {
            //systemAuditNoteService.changeMilestone(issueEntity, issueEntity.getProject(), userService.getCurrentUser(), issueEntity.getMilestone());
        }
    }

    @Transactional
    public void close(final Long issueId) {
        Issue issue = get(issueId);

        issue.setState(IssueState.CLOSED);//굳이 현재 상태를 체크하지는 않는다. 원래 닫힌 것을 다시 닫는다고 문제가 있겠는가?
        issue.setUpdatedAt(new Date());

        issueRepository.save(issue);
        //webhookService.executeHooks(issue);
        //systemAuditNoteService.changeStatus(issue, issue.getProject(), userService.getCurrentUser(), issue.getState().name().toLowerCase());

        logger.info("Issue #{} was closed.", issueId);
    }

    @Transactional
    public void reopen(final Long issueId) {
        Issue issue = get(issueId);

        issue.setState(IssueState.OPENED);
        issue.setUpdatedAt(new Date());

        issueRepository.save(issue);
        //webhookService.executeHooks(issue);
        //systemAuditNoteService.changeStatus(issue, issue.getProject(), userService.getCurrentUser(), "reopened");

        logger.info("Issue #{} was closed.", issueId);

    }

    public Long countByLabelAndState(final Project project, final IssueLabel issueLabel, final IssueState state) {

        return issueLabelMapRepository.countByProjectAndIssueLabel(project, issueLabel, state);
    }

    @Transactional
    public void delete(final String repositoryName, final Long internalId) {
        Issue issue = get(repositoryName, internalId);
        issueRepository.delete(issue);

        logger.info("Issue #{} was deleted.", internalId);
    }

    public Map<String, Long> getCountPerStateMap(final String repositoryName) {
        Map<String, Long> map = new HashMap<>();
        for(IssueState state: IssueState.values()) {
            map.put(state.getDisplayName(), countByState(repositoryName, state));
        }

        map.put("All", map.values().stream().mapToLong(m -> m.longValue()).sum());

        return map;
    }

    public Long countByState(final String projectPath, final IssueState state) {
        Project project = projectService.getByPath(projectPath);

        return issueRepository.countByProjectAndState(project, state);
    }

}
