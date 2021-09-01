package io.teamcode.common;

import io.teamcode.domain.entity.Project;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 5. 30..
 */
public class ProjectTest {

    @Test
    public void getResolvedPipelineConfigPath() {
        Project project = new Project();

        Assert.assertEquals("/trunk", project.getResolvedPipelineConfigPath());

        project.setPipelineConfigPath("branches");
        Assert.assertEquals("/branches", project.getResolvedPipelineConfigPath());

        project.setPipelineConfigPath("tags/");
        Assert.assertEquals("/tags", project.getResolvedPipelineConfigPath());

        project.setPipelineConfigPath("/releases/");
        Assert.assertEquals("/releases", project.getResolvedPipelineConfigPath());
    }
}
