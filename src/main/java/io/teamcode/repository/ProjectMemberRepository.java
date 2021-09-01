package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectMember;
import io.teamcode.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 1. 19..
 */
public interface ProjectMemberRepository extends CrudRepository<ProjectMember, Long> {

    ProjectMember findByProjectAndUser(final Project project, final User user);

    List<ProjectMember> findByProject(final Project project);

    List<ProjectMember> findByUser(final User user);
}
