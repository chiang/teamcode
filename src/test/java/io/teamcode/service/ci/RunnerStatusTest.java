package io.teamcode.service.ci;

import io.teamcode.domain.entity.ci.Runner;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 4. 23..
 */
public class RunnerStatusTest {

    @Test
    public void isOnline() {
        Assert.assertTrue(DateTime.now().isAfter(DateTime.now().minusMinutes(10)));

        Runner runner = new Runner();
        runner.setContactedAt(DateTime.now().minusMinutes(10).toDate());

        Assert.assertTrue(runner.isOnline());
    }
}
