package io.teamcode.common;

import io.teamcode.domain.entity.ci.Job;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by chiang on 2017. 4. 25..
 */
public class DurationTest {

    @Test
    public void duration() {
        Job job = new Job();
        job.setStartedAt(new Date());
        job.setFinishedAt(DateTime.now().plusMinutes(12).plusSeconds(31).toDate());

        Long duration = job.duration();
        Assert.assertNotNull(duration);
        Assert.assertTrue(job.resolveDuration().length() == 5);

        job.setFinishedAt(DateTime.now().plusHours(1).toDate());
        duration = job.duration();
        Assert.assertNotNull(duration);
        Assert.assertTrue(job.resolveDuration().length() == 8);
    }
}
