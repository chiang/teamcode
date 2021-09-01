package io.teamcode.domain.entity;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by chiang on 2017. 5. 22..
 */
public class ProgrammingLanguageTest {

    @Test
    public void populars() {
        List<ProgrammingLanguage> populars = ProgrammingLanguage.populars();
        Assert.assertEquals(ProgrammingLanguage.C, populars.get(0));
        Assert.assertEquals(ProgrammingLanguage.SWIFT, populars.get(populars.size() - 1));

        List<ProgrammingLanguage> others = ProgrammingLanguage.others();
        Assert.assertEquals(ProgrammingLanguage.ABAP, others.get(0));
        Assert.assertEquals(ProgrammingLanguage.ZSH, others.get(others.size() - 1));
    }
}
