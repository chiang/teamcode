package io.teamcode.common.ci;

import io.teamcode.domain.entity.ci.Job;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 13..
 */
public class PipelineDurationTest {

    /**
     *          -10 -09 -08 -07 -06 -05 -04 -03 -02 -01 00 01 02 03 04 5 6 7 8 9
     * JobEntry A:                A   A   A   A   A   A   A   A
     * JobEntry B:     A  A   A   A   A   A   A   A   A   A   A
     * JobEntry C:                                    A   A   A  A  A
     *
     */
    @Test
    public void fromJobs() {
        List<Job> jobs = new ArrayList<>();
        Job jobA = new Job();
        jobA.setStartedAt(DateTime.now().minusSeconds(7).toDate());
        jobA.setFinishedAt(DateTime.now().toDate());

        Job jobB = new Job();
        jobB.setStartedAt(DateTime.now().minusSeconds(10).toDate());

        Job jobC = new Job();
        jobC.setStartedAt(DateTime.now().minusSeconds(2).toDate());
        jobC.setFinishedAt(DateTime.now().plusSeconds(2).toDate());

        jobs.add(jobA);
        jobs.add(jobB);
        jobs.add(jobC);

        long duration = PipelineDuration.fromJobs(jobs);

        Assert.assertEquals(12, (duration/1000));
    }

    @Test
    public void merge() {
        Interval previous = new Interval(DateTime.now().minusSeconds(20), DateTime.now());
        Interval current = new Interval(DateTime.now().minusSeconds(10), DateTime.now().plusSeconds(20));

        Interval merged = PipelineDuration.merge(previous, current);
        int seconds = merged.toPeriod().getSeconds();
        Assert.assertEquals(40, seconds);

        previous = new Interval(DateTime.now().minusSeconds(20), DateTime.now().plusSeconds(21));
        current = new Interval(DateTime.now().minusSeconds(10), DateTime.now().plusSeconds(20));

        merged = PipelineDuration.merge(previous, current);
        seconds = merged.toPeriod().getSeconds();
        Assert.assertEquals(41, seconds);
    }

}
