package io.teamcode.web.project;

import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ProjectAttachment;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.ci.JobService;
import io.teamcode.util.DateUtils;
import io.teamcode.util.UrlUtils;
import io.teamcode.web.DownloadController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * <p>빌드 결과물인 Artifact 를 다운로드 처리해 주는 Controller.</p>
 * <p>
 *     경로는 $TC_HOME/data/ci/artifacts 에서 시작합니다. 그 밑으로 아래와 같이 구성합니다.
 *
 *     |-- 2017 (year)
 *       |-- 01 (month)
 *         |-- 1 (artifact_id % 256)
 *         |-- 1 (artifact_id)
 *       |-- 02
 *       |-- ...
 *       |-- ...
 *       |-- 31
 *
 *     위와 같이 구성한 경우 10년 동안 artifacts 디렉터리에는 120개 디렉터리, 그 하위에는 31개 이하, 그 하위에는 빌드한 개수만큼 디렉터리가 생성이 됩니다.
 *     Private CI System 에서는 하루 빌드가 수천 번 될 리가 없기 때문에 그냥 이렇게 해도 됩니다. 만약 Public 으로 갈 경우 Directory Structure 로
 *     mod(256) 시스템 등을 고려합니다 (TODO 내용 수정 필요)
 * </p>
 * <p>
 *     최종 경로로서 Artifact 의 ID 를 사용하며 유형은 디렉터리입니다. Exploded 형태도 있을 수 있기 때문입니다.
 * </p>
 *
 * TODO 다국어 파일, 디렉터리 명 등은 어찌 하는가?
 * TODO 해당 파일에 실행 권한이 있는 경우, 그리고 이 파일이 Docker 에 의해서 Root 로 구성된 경우는 어떻게 하는가?
 *
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}/jobs/{jobId}/artifacts")
public class ArtifactDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactDownloadController.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    JobService jobService;

    //@GetMapping("/{fileName:.+}")
    @GetMapping("/download")
    public void download(@PathVariable String projectPath, @PathVariable Long jobId, HttpServletResponse response) throws IOException {

        Job job = jobService.getJob(jobId);

        //TODO 경로 규칙을 Runner 와 규칙을 공유하고 공통 모듈로 빼기?
        String[] directoryStructure = DateUtils.dateBasedDirectoryStructure();
        Path file = Paths.get(
                String.format("%s/%s/%s/%s/%s/%s",
                        tcConfig.getArtifactsPath(),
                        directoryStructure[0],
                        directoryStructure[1],
                        (jobId.longValue() % 256),
                        jobId,
                        job.getArtifactsFileName()));
        File f = file.toFile();

        if (Files.exists(file)) {
            //response.setContentType(projectAttachment.getContentType());
            response.setContentLengthLong(f.length());
            response.setHeader("Content-Transfer-Encoding", "binary");
            //FIXME Header 에서 artifacts? 는 무슨 의미?
            response.addHeader("Content-Disposition", "artifacts; filename*=UTF-8''" + UrlUtils.encodeURIComponent(f.getName()));
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
            catch (IOException ex) {
                logger.error("사용자가 다운로드 요청을 하였으나 오류가 발생하여 파일을 전달할 수 없습니다.", ex);
                //ex.printStackTrace();
                //FIXME 이렇게 하면 될까? 사용자가 정확한 이유를 알아야 하나?
                throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
            }
        } else {
            logger.warn("사용자가 다운로드 요청한 파일 '{}' 을 찾을 수 없습니다. 전체 경로: {}", f.getAbsolutePath(), file.toAbsolutePath());

            throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
        }
    }
}
