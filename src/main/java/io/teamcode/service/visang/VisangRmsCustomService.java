package io.teamcode.service.visang;

import io.teamcode.TeamcodeException;
import io.teamcode.common.vcs.svn.ChangedFile;
import io.teamcode.common.vcs.svn.Commit;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.common.vcs.svn.callback.SvnLogMessageCallback;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.api.ChangeSet;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.visang.VisangRmsRecord;
import io.teamcode.domain.entity.visang.VisangRmsRecordDetails;
import io.teamcode.repository.visang.VisangRmsRecordDetailsRepository;
import io.teamcode.repository.visang.VisangRmsRecordRepository;
import io.teamcode.service.ProjectService;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by chiang on 2017. 4. 3..
 */
@Service
@Transactional(readOnly = true)
public class VisangRmsCustomService {

    private static final Logger logger = LoggerFactory.getLogger(VisangRmsCustomService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    VisangRmsRecordRepository visangRmsRecordRepository;

    @Autowired
    VisangRmsRecordDetailsRepository visangRmsRecordDetailsRepository;

    @Autowired
    SvnClientFactory svnClientFactory;

    public ChangeSet getByRmsId(final long rmsId) {
        List<VisangRmsRecord> visangRmsRecords = visangRmsRecordRepository.findByRmsId(String.valueOf(rmsId));
        ChangeSet changeSet = new ChangeSet();
        if (visangRmsRecords.size() == 0) {
            return changeSet;
        }
        else {
            //FIXME 동일한 파일을 여러 사람이 수정했다면 한 사람만 표시가 된다. 근데 이 표시되는 사용자가 누가 될지는 순서가 정확하지 않다.
            List<VisangRmsRecordDetails> visangRmsRecordDetailses;

            //Key 는 파일 path (trunk 등에서부터 시작하는)
            Map<String, io.teamcode.domain.api.ChangedFile> changedFileMap = new LinkedHashMap<>();
            for (VisangRmsRecord record: visangRmsRecords) {
                visangRmsRecordDetailses = visangRmsRecordDetailsRepository.findByVisangRmsRecord(record);
                if (visangRmsRecordDetailses.size() > 0) {
                    for (VisangRmsRecordDetails details: visangRmsRecordDetailses) {
                        if (!changedFileMap.containsKey(details.getFilePath())) {
                            changedFileMap.put(details.getFilePath(),
                                    io.teamcode.domain.api.ChangedFile.builder().author(record.getAuthorId())
                                            .lastModifiedAt(record.getLastModifiedAt())
                                            .path(details.getFilePath()).build());
                        }
                    }
                }
            }

            changeSet.setChangedFiles(new ArrayList<>(changedFileMap.values()));

            return changeSet;
        }
    }

    @Transactional
    public void addRecord(final String repositoryName, final Long revision) {
        Project project = projectService.getByPath(repositoryName);
        VisangRmsRecord visangRmsRecord = visangRmsRecordRepository.findByProjectAndRevision(project, revision);
        if (visangRmsRecord != null) {
            logger.warn("이미 데이터베이스에 저장된 RMS 레코드 대한 리비전 추가를 요청하였습니다. 이 요청은 잘못된 요청으로 무시합니다.");
        }
        else {
            logger.debug("저장소 '{}' 의 리비전 '{}' 은 데이터베이스에 존재하지 않습니다. 리비전 로그를 조회 후 새로운 ChangeSet 저장을 시작합니다...", project.getPath(), revision);

            ISVNClient client = null;
            try {
                client = svnClientFactory.createLocalClient();
                String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), project.getPath(), "");

                Set<String> revisionProps = SvnUtils.defaultRevisionProps();
                revisionProps.add("rms:id");
                SvnLogMessageCallback callback = new SvnLogMessageCallback(revisionProps);

                client.logMessages(repositoryUri, null,
                        SvnUtils.oneRevisionRanages(Long.valueOf(revision)), false, true, false, revisionProps,
                        0L, callback);

                List<Commit> histories = callback.getRepositoryHistories();
                if (histories.size() == 1) {
                    //TODO check exist revision and then throw error
                    Commit commit = histories.get(0);
                    visangRmsRecord = new VisangRmsRecord();
                    visangRmsRecord.setAuthorId(commit.getAuthor());
                    visangRmsRecord.setLastModifiedAt(commit.getCreatedAt());
                    visangRmsRecord.setProject(project);
                    visangRmsRecord.setRevision(revision);
                    visangRmsRecord.setRmsId(commit.getAdditionalRevisionProps().get("rms:id"));

                    visangRmsRecord = visangRmsRecordRepository.save(visangRmsRecord);

                    List<VisangRmsRecordDetails> visangRmsRecordDetailses = new ArrayList<>();
                    VisangRmsRecordDetails visangRmsRecordDetails;
                    for (ChangedFile changedFile: commit.getChangedFiles()) {
                        visangRmsRecordDetails = new VisangRmsRecordDetails();
                        visangRmsRecordDetails.setVisangRmsRecord(visangRmsRecord);
                        visangRmsRecordDetails.setFilePath(changedFile.getPath());

                        visangRmsRecordDetailses.add(visangRmsRecordDetails);
                    }

                    visangRmsRecordDetailsRepository.save(visangRmsRecordDetailses);

                    //TODO 원래 여기서 통계를 처리했으나 통계는 Daily 로 집계하는 것이 좋겠다.
                    //notificationService.sendCommitNotification(authorizationService.currentUser(), changeSet);
                }
                else {
                    logger.warn("received commit hook but cannot find changeset");
                    //TODO check size 1 or error
                }
            } catch (ClientException e) {
                //비동기이므로 에러가 나도 그냥 넘긴다.
                //FIXME 무조건 Commit은 되게 해야 겠지만, 그래도 에러를 추적해야 하지 않나?
                logger.error("cannot add changeset by commit hook", e);
                throw new TeamcodeException(e);
            } finally {
                if (client != null)
                    client.dispose();
            }
        }
    }
}
