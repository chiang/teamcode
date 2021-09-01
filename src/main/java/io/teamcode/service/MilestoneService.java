package io.teamcode.service;

import io.teamcode.domain.entity.Milestone;
import io.teamcode.domain.entity.MilestoneState;
import io.teamcode.domain.entity.Project;
import io.teamcode.repository.MilestoneRepository;
import io.teamcode.util.DateUtils;
import io.teamcode.web.ui.view.MilestoneListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
@Transactional(readOnly = true)
public class MilestoneService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private MilestoneRepository milestoneRepository;

    public Milestone get(final Long milestoneId) {

        return this.milestoneRepository.findOne(milestoneId);
    }

    public MilestoneListView list(final String projectPath, final String state) {
        Project project = projectService.getByPath(projectPath);

        List<Milestone> milestones;
        if (!StringUtils.hasText(state) || "all".equals(state))
            milestones = milestoneRepository.findByProject(project);
        else {
            MilestoneState milestoneState = MilestoneState.valueOf(state.toUpperCase());

            milestones = milestoneRepository.findByProjectAndState(project, milestoneState);
        }

        MilestoneListView milestoneListView = new MilestoneListView();
        milestoneListView.setAllCount(milestoneRepository.countByProject(project));
        milestoneListView.setOpenCount(milestoneRepository.countByProjectAndState(project, MilestoneState.OPEN));
        milestoneListView.setClosedCount(milestoneRepository.countByProjectAndState(project, MilestoneState.CLOSED));
        milestoneListView.setState(state);
        milestoneListView.setMilestones(milestones);

        return milestoneListView;
    }

    @Transactional
    public Milestone create(final String projectPath, Milestone milestone) {
        Project project = projectService.getByPath(projectPath);

        try {
            if (StringUtils.hasText(milestone.getStartDateStr()))
                milestone.setStartDate(DateUtils.parseMilestoneDateString(milestone.getStartDateStr()));
            else
                milestone.setDueDate(DateUtils.parseMilestoneDateString(milestone.getDueDateStr()));
        } catch (ParseException e) {
            throw new EntityValidationException("잘못된 날짜 포맷입니다. 날짜는 2017-07-07 와 같은 형식이어야 합니다.");
        }

        //TODO 시작일이 종료일보다 이전인지 확인
        milestone.setState(MilestoneState.OPEN);
        milestone.setProject(project);
        milestone.setCreatedAt(new Date());
        milestone.setCreatedBy(userService.getCurrentUser());

        Milestone savedMilestone = milestoneRepository.save(milestone);

        return savedMilestone;
    }
}
