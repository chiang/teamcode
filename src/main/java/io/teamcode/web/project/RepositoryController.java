package io.teamcode.web.project;

import io.teamcode.common.Strings;
import io.teamcode.common.vcs.RepositoryBrowseContent;
import io.teamcode.common.vcs.svn.Commit;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import io.teamcode.model.rest.CommitsResponse;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.project.integration.CustomIssueTracker;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import io.teamcode.service.vcs.svn.RepositoryBrowseService;
import io.teamcode.service.vcs.svn.CommitService;
import io.teamcode.util.FileSystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by chiang on 2017. 2. 27..
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}")
public class RepositoryController {

    @Autowired
    TcConfig tcConfig;

    @Autowired
    RepositoryBrowseService repositoryBrowseService;

    @Autowired
    ProjectIntegrationServiceManager projectIntegrationServiceManager;

    @Autowired
    CommitService commitService;

    @GetMapping(value = "files")
    public String files(@PathVariable String groupPath,
                        @PathVariable String projectPath,
                        @RequestParam(required = false) String path,
                        @RequestParam(required = false, defaultValue = "-1") long revision,
                        HttpServletRequest request,
                        Model model) {

        RepositoryBrowseContent repositoryBrowseContent = repositoryBrowseService.getRepositoryBrowseContent(request.getContextPath(), groupPath, projectPath, path, revision);
        model.addAttribute("repositoryBrowseContent", repositoryBrowseContent);

        if (repositoryBrowseContent.isFileContent()) {
            return "projects/repository/content";
        }
        else {
            return "projects/repository/files";
        }
    }

    @DeleteMapping(value = "/files")
    public String commitDeletion(@PathVariable String groupPath,
                                 @PathVariable String projectPath,
                                 @RequestParam(required = false) String path,
                                 String message) {

        long deletionRevision = commitService.commitDeletion(projectPath, path, message);

        return String.format("redirect:/%s/%s/commits/%s", groupPath, projectPath, deletionRevision);
    }



    @GetMapping("commits")
    public String commits(@PathVariable String projectPath,
                          @RequestParam(required = false, defaultValue = "-1") long offset,
                          @RequestParam(required = false, defaultValue = "-1") long limit,
                          Model model) {
        Page<Commit> repositoryHistories = commitService.list(projectPath, offset, limit);
        model.addAttribute("commits", repositoryHistories);
        model.addAttribute("prevOffset", offset + 10);
        if (repositoryHistories.hasContent())
            model.addAttribute("offset", repositoryHistories.getContent().get(repositoryHistories.getNumberOfElements() - 1).getRevision());


        return "projects/repository/commits";
    }

    @GetMapping(value = "commits.json", produces = {"application/json"})
    @ResponseBody
    public CommitsResponse getCommits(@PathVariable String projectPath,
                                      @RequestParam(required = false, defaultValue = "-1") long offset,
                                      @RequestParam(required = false, defaultValue = "-1") long limit) {
        CommitsResponse commitsResponse = new CommitsResponse();

        Page<Commit> commits = commitService.list(projectPath, offset, limit);
        commitsResponse.setCommits(commits);
        commitsResponse.setPrevOffset(offset + 10);
        commitsResponse.setOffset(commits.getContent().get(commits.getNumberOfElements() - 1).getRevision());

        //FIXME 다른 것들과 충돌은?
        try {
            ProjectIntegrationServiceSettings settings = projectIntegrationServiceManager.getSettings(projectPath, CustomIssueTracker.KEY);
            if (settings.getActive().booleanValue()) {
                Map<String, Object> properties = settings.getPropertyMap();
                if (!properties.isEmpty() && properties.containsKey(CustomIssueTracker.PROPS_LINK_ENABLED)) {
                    commitsResponse.setIssueLinkEnabled(Boolean.valueOf((String)properties.get(CustomIssueTracker.PROPS_LINK_ENABLED)));

                    if (!properties.isEmpty() && properties.containsKey(CustomIssueTracker.PROPS_URL)) {
                        commitsResponse.setIssueLinkUrl((String)properties.get(CustomIssueTracker.PROPS_URL));
                    }

                    if (!properties.isEmpty() && properties.containsKey(CustomIssueTracker.PROPS_REGEXP)) {
                        commitsResponse.setRegexp((String)properties.get(CustomIssueTracker.PROPS_REGEXP));
                    }
                }

            }
        } catch (ResourceNotFoundException e) {
            //do nothing
        }

        return commitsResponse;
    }

    @GetMapping("/commits/{revision}")
    public String showCommit(@PathVariable String projectPath,
                          @PathVariable("revision") long revisionNumber,
                          Model model) {
        model.addAttribute("commitDetails", commitService.getCommitDetails(projectPath, revisionNumber));

        return "projects/repository/commit";
    }

    /*@GetMapping("/commits/r{revision}.diff")
    @ResponseStatus(HttpStatus.OK)
    public void downloadDiff(@PathVariable String projectPath,
                             @PathVariable("revision") long revisionNumber,
                             Model model) {
        model.addAttribute("commitDetails", commitService.getCommitDetails(projectPath, revisionNumber));

        return "projects/repository/commit";
    }*/

    //TODO 화면에서 보여줄 수 있는 것은 보여주도록 content-disposition 을 제외해야 한다.
    @GetMapping("/raw")
    @ResponseStatus(HttpStatus.OK)
    public void raw(@PathVariable String projectPath, @RequestParam("path") String path, HttpServletResponse response) throws IOException {
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, path);
        String fileName = Strings.getFileNameFromPath(path);

        final String encodedFileName = FileSystemUtils.encodeFilename(fileName);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        response.setContentType(String.format("%s;charset=UTF-8", FileSystemUtils.detectMimeType(path)));
        repositoryBrowseService.populateContentAsStream(repositoryUri, response.getOutputStream());
    }
}
