package io.teamcode.service.vcs.svn;

import io.teamcode.TeamcodeException;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.common.vcs.svn.SvnExceptionHandler;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.common.vcs.svn.callback.SvnLogMessageCallback;
import io.teamcode.config.TcConfig;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Service
public class SvnRepositoryImportService {

    private static final Logger logger = LoggerFactory.getLogger(SvnRepositoryImportService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    SvnClientFactory svnClientFactory;

    public int importRepository(final String repositoryName) {
        long start = System.currentTimeMillis();

        ISVNClient client = svnClientFactory.createLocalClient();

        SvnLogMessageCallback callback = new SvnLogMessageCallback();
        try {
            client.logMessages(SvnUtils.buildAbsoluteUrl(tcConfig.getRepositoryRootPath(), repositoryName), null,
                    SvnUtils.defaultRevisionRanges(), false, true, false, SvnUtils.defaultRevisionProps(),
                    0L, callback);
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);
            throw new TeamcodeException(e.getMessage(), e);
        }

        /*List<ChangeSet> changeSets = callback.getChangeSets();
        int total = changeSets.size();
        //this.changeSetRepository.save(callback.getChangeSets());

        changeSets.clear();

        client.dispose();
        long end = System.currentTimeMillis();
        logger.info("repository ({}) imported. elapsed time: [{}]", repositoryName, (new TimeValue(end - start)).toString());

        return total;*/

        return 0;
    }
}
