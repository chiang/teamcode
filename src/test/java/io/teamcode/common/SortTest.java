package io.teamcode.common;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.web.api.model.ci.Step;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 4. 16..
 */
public class SortTest {

    @Test
    public void sortShort() {
        List<Short> shorts = Arrays.asList(new Short((short)2), new Short((short)1), new Short((short)0), new Short((short)10));
        List<Short> expected = Arrays.asList(new Short((short)0), new Short((short)1), new Short((short)2), new Short((short)10));

        List<Short> sorted = shorts.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        Assert.assertArrayEquals(expected.toArray(), sorted.toArray());
    }

    @Test
    public void nullTest() {
        List<String> list = Arrays.asList("afafafaf".split("\n"));

        Integer buildTimeout = null;
        Step.builder().timeout(buildTimeout).build();
    }
}
