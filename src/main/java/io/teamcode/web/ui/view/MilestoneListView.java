package io.teamcode.web.ui.view;

import io.teamcode.domain.entity.Milestone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 프로젝트 마일스톤 관리 기능에서 사용하는 뷰
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneListView {

    private Integer allCount;

    private Integer openCount;

    private Integer closedCount;

    private String state;

    private List<Milestone> milestones;
}
