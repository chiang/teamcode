package io.teamcode.service.vcs.svn;

import io.teamcode.TeamcodeException;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.common.vcs.svn.SvnCommandException;
import io.teamcode.common.vcs.svn.SvnExceptionHandler;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.common.vcs.svn.callback.SimpleLogMessageCallback;
import io.teamcode.common.vcs.svn.callback.SummaryLogMessage;
import io.teamcode.common.vcs.svn.callback.SummaryLogMessageCallback;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ci.PipelineService;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import io.teamcode.service.visang.VisangRmsCustomService;
import org.apache.commons.io.FilenameUtils;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.callback.LogMessageCallback;
import org.apache.subversion.javahl.types.ChangePath;
import org.apache.subversion.javahl.types.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 3. 23..
 */
@Component
public class RepositoryHookService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryHookService.class);

    //@Value("teamcode.ticket.pattern")
    private String ticketPattern = "\\[RMS-(\\d+)\\]";

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectIntegrationServiceManager projectIntegrationServiceManager;

    @Autowired
    PipelineService pipelineService;

    @Autowired
    VisangRmsCustomService visangRmsCustomService;

    @Autowired
    SvnClientFactory svnClientFactory;

    /**
     *
     * @param path 저장소 경로.
     * @param revisionNumber
     */
    @Async
    public void onPostCommit(final String path, final long revisionNumber) {
        logger.debug("r{} 에 대한 Commit Hook 을 실행합니다...", revisionNumber);

        String repositoryName;
        if (path.contains("/")) {
            File repositoryPath = new File(path);

            //TC 에서 받는 위치는 HTTPD 에서 보내는 위치와 다르다. 그래서 이름만 추출한다.
            repositoryName = repositoryPath.getName();
        }
        else {
            repositoryName = path;
        }

        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), repositoryName, "");
        SummaryLogMessage summaryLogMessage = getSummaryLogMessage(repositoryUri, revisionNumber);

        if (StringUtils.hasText(summaryLogMessage.getMessage())) {
            try {
                boolean rmsIdFounded = executeVisangRmsFeature(repositoryUri, revisionNumber, summaryLogMessage);

                if (rmsIdFounded) {
                    visangRmsCustomService.addRecord(repositoryName, revisionNumber);
                    logger.debug("RMS 레코드를 저장했습니다.");
                } else {
                    logger.debug("RMS ID 가 로그 메시지에 없어 RMS 레코드를 저장하지 않습니다.");
                }
            } catch (ClientException e) {
                //TODO hook exception?
                throw new TeamcodeException("오류가 발생하여 RMS 요청 번호를 확인 및 처리할 수 없습니다.");
            }
        }
        else {
            logger.debug("r{} 에 대한 커밋 메시지가 없어 RMS 레코드를 처리하지 않습니다.", revisionNumber);
        }

        logger.debug("CI 파이프라인 설정을 체크 후 시작합니다...");
        pipelineService.createPipeline(repositoryName, revisionNumber, summaryLogMessage, summaryLogMessage.isSkipCi());

        logger.debug("등록된 프로젝트 연동 서비스를 실행합니다...");
        projectIntegrationServiceManager.executeServices(repositoryName, summaryLogMessage, revisionNumber);
    }

    private final SummaryLogMessage getSummaryLogMessage(final String repositoryUri, final long revisionNumber) {
        //ensureLocalRequest();

        ISVNClient isvnClient = null;
        try {
            isvnClient = svnClientFactory.createLocalClient();


            Revision revision = Revision.getInstance(revisionNumber);

            SummaryLogMessageCallback callback = new SummaryLogMessageCallback();
            isvnClient.logMessages(repositoryUri, revision,
                    SvnUtils.oneRevisionRanages(revisionNumber), false, true, false, SvnUtils.defaultRevisionProps(),
                    0L, callback);

            return callback.getSummaryLogMessage();
        } catch (ClientException e) {
            //TODO custom...
            //비동기이므로 에러가 나도 그냥 넘긴다.
            //FIXME 무조건 Commit은 되게 해야 겠지만, 그래도 에러를 추적해야 하지 않나?
            //TODO 에러를 기록하고 나중에 탐지 가능하도록 하자.
            //TODO 주기적으로 체크해서 빈 것을 채워주자.
            logger.error("cannot add changeset by commit hook", e);

            SvnExceptionHandler.throwDetailSvnException(e);

            //TODO hook 에레로 던져야 한다.
            throw new SvnCommandException();
        } finally {
            if (isvnClient != null)
                isvnClient.dispose();
        }
    }

    //TODO 나중에 관리형 기능으로 대체
    private boolean executeVisangRmsFeature(final String repositoryUri, final long revisionNumber, final SummaryLogMessage summaryLogMessage) throws ClientException {
        try {
            Revision revision = Revision.getInstance(revisionNumber);

            return extractThenPropertySet(repositoryUri, revision, summaryLogMessage.getMessage());
        } catch (ClientException e) {
            //TODO custom...
            //비동기이므로 에러가 나도 그냥 넘긴다.
            //FIXME 무조건 Commit은 되게 해야 겠지만, 그래도 에러를 추적해야 하지 않나?
            //TODO 에러를 기록하고 나중에 탐지 가능하도록 하자.
            //TODO 주기적으로 체크해서 빈 것을 채워주자.

            throw e;
        }
    }

    /**
     * 커밋 메시지에서 ticketPattern 을 찾으면 빼서 이를 Revision Property 에 저장합니다.
     * @param message
     */
    private boolean extractThenPropertySet(final String repositoryPath, final Revision revision, String message) throws ClientException {
        //logger.trace("CSR 해석 여부: {}", repository.isWithCsr());
        //if (repository.isWithCsr()) {
        ISVNClient isvnClient = svnClientFactory.createLocalClient();
        if (StringUtils.hasText(ticketPattern)) {
            Pattern p = Pattern.compile(ticketPattern);
            Matcher m = p.matcher(message);
            if (m.find()) {
                if (logger.isDebugEnabled()) {
                    logger.trace("Ticket 정보를 찾았습니다. Ticket ID 추출을 시작합니다...");
                    logger.trace("매칭된 Ticket 결과의 그룹 수: {}", m.groupCount());
                    if (m.groupCount() > 1) {
                        logger.warn("커밋 메시지에 여러 Ticket ID 를 입력했습니다. 처음 Ticket ID 만 저장합니다...");
                    }
                    logger.debug("Ticket ID 를 rms:id Revision Property 에 저장합니다. '{}'", m.group(1));
                }
                boolean force = true;
                isvnClient.setRevProperty(repositoryPath, "rms:id", revision, m.group(1), null, force);

                return true;
            }
            else {
                logger.debug("Ticket 정보를 찾도록 설정되어 있으나 메시지에 포함되어 있지 않습니다.");
            }
            //1. get csrRegex from repository entity
            //2. match
            //3. get csr id
            //4.
        }
        else {
            //logger.debug("CSR ID를 연동하도록 설정되어 있으나 CSR ID 해석을 위한 정규식 정보를 찾을 수 없습니다. 저장소 설정애서 설정이 필요합니다.");
        }

        return false;
    }

    //TODO 일단 WebSecurityConfig 에서 막아 놓았으나...
    //TODO 원격일지라도 IP Address 방식으로???
    private void ensureLocalRequest() {
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest().getRemoteAddr();
        // TODO check ip address (설정 파일의 정보 확인도 중요?)
    }

}
