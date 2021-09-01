package io.teamcode.web.ui.view;

import io.teamcode.domain.entity.ProjectLink;
import io.teamcode.domain.entity.Tag;
import io.teamcode.domain.entity.User;
import lombok.Data;

import java.util.List;

/**
 * Project Overview 화면에서 사용하는 View
 */
@Data
public class ProjectOverviewView {

    private List<User> members;

    private int totalMembers;

    private List<ProjectLink> links;

    private List<Tag> tags;

    public boolean isMoreMembers() {

        return totalMembers > members.size();
    }

    public int getMoreMembersCount() {

        return totalMembers - members.size();
    }
}
