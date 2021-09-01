package io.teamcode.common;

/**
 * Created by chiang on 2017. 2. 28..
 */
public class TeamcodeConstants {

    public final static String DEFAULT_AUTHZ_ADMIN = "system";

    public static final String DEFAULT_PIPELINE_CONFIG_PATH = "/trunk";

    //200KB
    public static final long MAX_UPLOAD_AVATAR_SIZE = 1024 * 200;

    public static final String MAX_UPLOAD_AVATAR_ERROR_MESSAGE = "첨부된 파일이 200KB를 넘었습니다. 다른 파일을 선택해 주세요.";

    public static final String DEFAULT_PIPELINE_YAML_FILE_NAME = ".teamcode-pipelines.yml";

    public static final String DEFAULT_SKIP_CI_REGEXP = "\\[(ci[ _-]skip|skip[ _-]ci)\\]";

}
