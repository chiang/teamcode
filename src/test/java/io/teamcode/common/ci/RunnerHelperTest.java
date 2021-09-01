package io.teamcode.common.ci;

import org.junit.Test;

/**
 * Created by chiang on 2017. 4. 11..
 */
public class RunnerHelperTest {

    @Test
    public void generateRunnersRegistrationToken() {
        String token = RunnerHelper.generateRunnersRegistrationToken();
        System.out.println("--> " + token);
    }
}
