package io.teamcode.common.ci;

import io.teamcode.domain.entity.ci.Job;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * Created by chiang on 2017. 5. 11..
 */
public abstract class JobHelper {

    public static final String generateJobToken(final Job job) {
        return DigestUtils.sha1Hex(String.format("%s%s", job.getId(), job.getName())).substring(0, 20);
    }
}
