package io.teamcode.common;

import io.teamcode.common.io.ClasspathResourceReader;
import io.teamcode.common.vcs.RepositoryHelper;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by chiang on 2017. 4. 25..
 */
public class AppearancesHelper {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AppearancesHelper.class);

    /**
     *
     * @param iconName PipelineStatus.icon 값을 전달합니다.
     * @return
     */
    public final String renderIcon(final String iconName) {
        String iconFileClassPath = String.format("public/assets/images/icons/_%s.svg", iconName);
        try {

            return ClasspathResourceReader.readAsString(iconFileClassPath);
        } catch (IOException e) {
            logger.error("클래스패스에서 아이콘 파일 '{}' 을 읽을 수 없습니다. 빈 파일을 반환합니다...", iconFileClassPath);

            return "";
        }
    }

}
