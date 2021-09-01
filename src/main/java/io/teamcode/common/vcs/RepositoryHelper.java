package io.teamcode.common.vcs;


import io.teamcode.TeamcodeException;
import io.teamcode.common.io.ClasspathResourceReader;
import io.teamcode.common.io.TeamcodePermissionHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

/**
 * 디렉터리 조사 등 저장소 파일 시스템 (혹은 데이터베이스)을 관리하는 도구
 *

 * Created by chiang on 2015. 10. 30..
 */
public abstract class RepositoryHelper {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RepositoryHelper.class);

    //TODO api version 은 자동으로 가져오도록...
    //TODO 오류 메시지도 기록할 수 있으면...
    private static final String POST_COMMIT_HOOK_URL = "curl --insecure --data \"path=$REPOS&rev=$REV\" %s/api/v1/hooks/post-commit";

    public static final String buildHookEndpoint(String consoleEndpoint, String repositoryName, String tcHomeAbsolutePath) {
        return String.format(POST_COMMIT_HOOK_URL, consoleEndpoint, repositoryName, tcHomeAbsolutePath);
    }


    /**
     * Repository 의 Post Commit Hook 내용을 체크해 보고 문제가 있으면 원복합니다.
     *
     * @param repositoryHome
     * @param curl 콘솔 서버를 호출하는 URL (curl command)
     */
    public static void recoveryIfInvalidPostCommitHook(String repositoryName, File repositoryHome, String curl, boolean syncPermission) {
        Assert.notNull(repositoryHome);
        Assert.notNull(curl);
        Assert.isTrue(repositoryHome.exists());

        try {
            File repositoryDir = new File(repositoryHome, repositoryName);
            if (!repositoryDir.exists())
                throw new TeamcodeException(String.format("저장소 '%s'의 디렉터리 '%s'가 존재하지 않습니다.", repositoryName, repositoryDir.getAbsolutePath()));  //TODO 가이드 필요.

            File hookScriptDir = new File(repositoryDir, "hooks");
            if (hookScriptDir.exists()) {
                File postCommitHookScript = new File(hookScriptDir, "post-commit");
                if (postCommitHookScript.exists()) {
                    String postCommitHookScriptString = FileUtils.readFileToString(postCommitHookScript, Charset.defaultCharset());
                    String rightString = buildPostCommitHookScript(curl);
                    if (!postCommitHookScriptString.equals(rightString)) {
                        logger.warn("저장소 '{}' 의 post-commit 스크립트 내용이 잘못 되었습니다. 내용을 복구합니다.", repositoryName);
                        FileUtils.write(postCommitHookScript, buildPostCommitHookScript(curl), false);
                    } else {
                        logger.debug("저장소 '{}' 의 post-commit 스크립트 내용이 정상입니다. 스크립트 검증에 성공하였습니다.", repositoryName);
                    }

                    if (syncPermission) {
                        logger.debug("서브버전 저장소 Hook Scripts 소유자 권한 동기화를 시작합니다...");
                        TeamcodePermissionHelper.setDefaultOwner(postCommitHookScript);
                    }
                    else {
                        logger.debug("서브버전 저장소 Hook Scripts 소유자 권한 동기화 기능이 Off 상태입니다. 권한 동기화 작업을 건너뜁니다.");
                    }

                } else {
                    logger.warn("post-commit 스크립트가 존재하지 않습니다. 새로 생성합니다.");

                    FileUtils.write(postCommitHookScript, buildPostCommitHookScript(curl), Charset.defaultCharset());
                }

                if (!postCommitHookScript.canExecute()) {
                    //logger.warn("스크립트 파일 '{}'은 실행 권한을 가지고 있지 않습니다. 사용자 필드에 실행 권한을 부여합니다.", postCommitHookScript.getAbsolutePath());
                    //postCommitHookScript.setExecutable(true, true);

                    //전체에 실행 권한을 줘야 Apache HTTPD 프로세스가 이 파일을 실행할 수 있다.
                    logger.warn("스크립트 파일 '{}'은 실행 권한을 가지고 있지 않습니다. 실행 권한을 부여합니다.", postCommitHookScript.getAbsolutePath());
                    postCommitHookScript.setExecutable(true, false);
                }
            }
            else {
                logger.error("저장소 '{}'의 hooks 디렉터리 '{}' 가 존재하지 않습니다.", repositoryName, hookScriptDir.getAbsolutePath());

                //TODO 어차피 새로 초기화할 수 있으므로 여기서 초기화하고 넘어가기.
            }
        } catch (IOException e) {
            throw new TeamcodeException(String.format("저장소 '%s'의 post-commit 스크립트 내용을 점검하던 중 오류가 발생하였습니다.", repositoryName), e);
        }
    }

    private static String buildPostCommitHookScript(String curl) {
        StringBuilder builder = new StringBuilder();
        builder.append("#!/bin/sh").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("REPOS=\"$1\"").append(System.lineSeparator());
        builder.append("REV=\"$2\"").append(System.lineSeparator());
        builder.append("TXN=\"$3\"").append(System.lineSeparator()).append(System.lineSeparator());
        //builder.append("UUID=`svnlook uuid $REPOS`").append(System.lineSeparator()).append(System.lineSeparator());

        builder.append(curl);

        return builder.toString();
    }

    public static final void syncPreRevpropChangeScript(File repositoryDir, boolean syncPermission) {
        try {
            if (!repositoryDir.exists())
                throw new TeamcodeException(String.format("저장소 '%s'의 디렉터리 '%s'가 존재하지 않습니다.", repositoryDir.getName(), repositoryDir.getAbsolutePath()));  //TODO 가이드 필요.

            File hookScriptDir = new File(repositoryDir, "hooks");
            if (hookScriptDir.exists()) {

                File preRevpropChangeScript = new File(hookScriptDir, "pre-revprop-change");
                String prevRevpropChangeScriptSource = ClasspathResourceReader.readAsString("META-INF/hook-scripts/pre-revprop-change");


                FileUtils.write(preRevpropChangeScript, prevRevpropChangeScriptSource, Charset.defaultCharset());

                if (!preRevpropChangeScript.canExecute()) {
                    logger.warn("스크립트 파일 '{}'은 실행 권한을 가지고 있지 않습니다. 실행 권한을 부여합니다.", preRevpropChangeScript.getAbsolutePath());
                    preRevpropChangeScript.setExecutable(true, false);
                }

                if (syncPermission) {
                    logger.debug("서브버전 저장소 Hook Scripts 소유자 권한 동기화를 시작합니다...");
                    TeamcodePermissionHelper.setDefaultOwner(preRevpropChangeScript);
                }
                else {
                    logger.debug("서브버전 저장소 Hook Scripts 소유자 권한 동기화 기능이 Off 상태입니다. 권한 동기화 작업을 건너뜁니다.");
                }
            }
            else {
                logger.error("저장소 '{}'의 hooks 디렉터리 '{}' 가 존재하지 않습니다.", repositoryDir.getName(), hookScriptDir.getAbsolutePath());

                //TODO 어차피 새로 초기화할 수 있으므로 여기서 초기화하고 넘어가기.
            }
        } catch (IOException e) {
            throw new TeamcodeException(String.format("저장소 '%s'의 pre-revprop-change 스크립트를 동기화하던 중 오류가 발생하였습니다.", repositoryDir.getName()), e);
        }
    }
}
