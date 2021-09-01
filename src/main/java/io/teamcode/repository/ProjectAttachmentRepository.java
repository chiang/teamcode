package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectAttachment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 3. 27..
 */
public interface ProjectAttachmentRepository extends CrudRepository<ProjectAttachment, Long> {

    ProjectAttachment findByProjectAndOriginalFileName(final Project project, final String originalFileName);

    List<ProjectAttachment> findByProject(final Project project);
}
