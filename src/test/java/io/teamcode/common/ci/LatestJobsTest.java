package io.teamcode.common.ci;

import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.Pipeline;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

/**
 * 하나의 Job 이 여러 번 실행된 경우 최근 Job 을 계산한다던지 하는 것을 테스트
 *
 *
 */
public class LatestJobsTest {

    @Test
    public void getJobs() {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(1l);
        pipeline.setCommitRevision(new Long(1));

        Job jobA1 = new Job();
        jobA1.setId(1l);
        jobA1.setName("job-a");
        jobA1.setPipeline(pipeline);

        Job jobA2 = new Job();
        jobA2.setId(2l);
        jobA2.setName("job-a");
        jobA2.setPipeline(pipeline);

        Job jobB1 = new Job();
        jobB1.setId(3l);
        jobB1.setName("job-b");
        jobB1.setPipeline(pipeline);

        Job jobB2 = new Job();
        jobB2.setId(4l);
        jobB2.setName("job-b");
        jobB2.setPipeline(pipeline);

        Job jobB3 = new Job();
        jobB3.setId(5l);
        jobB3.setName("job-b");
        jobB3.setPipeline(pipeline);

        List<Job> allJobs = Arrays.asList(jobB2, jobA2, jobB1, jobB3, jobA1);

        Collection<Job> filteredJobs
                = allJobs.stream().collect(groupingBy(j -> j.getName(), collectingAndThen(maxBy(comparingLong(j -> j.getId().longValue())), Optional::get))).values();

        Assert.assertEquals(2, filteredJobs.size());
        Assert.assertTrue(filteredJobs.stream().anyMatch(j -> j.getName().equals("job-a") && j.getId().longValue() == 2));
        Assert.assertTrue(filteredJobs.stream().anyMatch(j -> j.getName().equals("job-b") && j.getId().longValue() == 5));
    }
}
