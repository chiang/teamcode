package io.teamcode.service;

import io.teamcode.domain.entity.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;

/**
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@Transactional
public class ProjectServiceTest {

    @Autowired
    ProjectService projectService;

    @WithUserDetails("chiang")
    @Test(expected = InsufficientPrivilegeException.class)
    public void create() {
        Project project = new Project();
        project.setName("petclinic");

        projectService.create("projects", project);
    }

    @WithUserDetails("admin")
    @Test(expected = ConstraintViolationException.class)
    public void createInvalid() {
        Project project = new Project();
        project.setName("petcli  nic");

        projectService.create("projects", project);
    }
}
