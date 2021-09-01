package io.teamcode.service.ci;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.common.ci.config.entry.StageEntry;
import io.teamcode.common.ci.config.entry.StagesEntry;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.web.api.model.ci.Dependency;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

/**
 * Created by chiang on 2017. 7. 31..
 */
public class JobServiceTest {

    @Test
    public void getDependencies() throws JsonProcessingException {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("stage", "qa");

        StagesEntry stagesEntry = Mockito.mock(StagesEntry.class);
        Mockito.when(stagesEntry.getStageEntries()).thenReturn(Arrays.asList(new StageEntry("build")));

        String options = new JobEntry(stagesEntry, "test-job", jobMap).toJson();

        Job job = new Job();
        job.setId(10l);
        job.setOptions(options);
        job.setName("test-job");

        JobService jobService = Mockito.spy(JobService.class);
        List<Job> dependsOn = new ArrayList<>();
        Job j1 = new Job();
        j1.setId(1l);
        j1.setName("job-a");
        dependsOn.add(j1);

        Job j2 = new Job();
        j2.setId(2l);
        j2.setName("job-b");
        dependsOn.add(j2);

        Mockito.doReturn(dependsOn).when(jobService).dependsOnBuilds(job);

        List<Dependency> dependencies = jobService.getDependencies(job);
        Assert.assertEquals(dependsOn.size(), dependencies.size());

        jobMap.put("dependencies", Arrays.asList("job-a"));
        options = new JobEntry(stagesEntry, "test-job", jobMap).toJson();
        job.setOptions(options);

        dependencies = jobService.getDependencies(job);
        Assert.assertEquals(1, dependencies.size());

        //YAML 에서는 dependencies: (Colon 뒤에 빈 값) 혹은 dependencies: [] 와 같이 쓰면 됩니다. 그러면 빈 List 가 됩니다.
        jobMap.put("dependencies", Collections.emptyList());
        options = new JobEntry(stagesEntry, "test-job", jobMap).toJson();
        job.setOptions(options);

        dependencies = jobService.getDependencies(job);
        Assert.assertEquals(0, dependencies.size());
    }
}
