package io.teamcode.web.project;

import io.teamcode.domain.entity.ci.PipelineStatus;
import io.teamcode.model.PipelineConfigurationGuideResponse;
import io.teamcode.service.InsufficientPrivilegeException;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.ci.JobService;
import io.teamcode.service.ci.PipelineService;
import io.teamcode.web.ui.view.ci.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by chiang on 2017. 4. 8..
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}")
public class PipelineController {

    @Autowired
    PipelineService pipelineService;

    @Autowired
    JobService jobService;

    @GetMapping("/pipelines")
    public String list(@PathVariable String groupPath, @PathVariable String projectPath, Model model) {
        PipelineConfigurationGuideResponse pipelineConfigurationGuideResponse = new PipelineConfigurationGuideResponse();
        pipelineConfigurationGuideResponse.setHasPipelineYml(pipelineService.hasPipelineYaml(projectPath));


        Map<String, String> parameters = new HashMap<>();
        parameters.put("groupPath", groupPath);
        parameters.put("projectPath", projectPath);

        pipelineConfigurationGuideResponse.add(linkTo(PipelineController.class, parameters).slash("pipelines").withRel("commit"));

        //pipelineConfigurationGuideResponse.add(linkTo(methodOn(PipelineController.class).commitPipelineConfiguration(projectPath)).withRel("commit"));
        model.addAttribute("pipelineConfigurationGuideResponse", pipelineConfigurationGuideResponse);

        return "projects/pipelines/list";
    }

    @PostMapping("/pipelines")
    @ResponseStatus(HttpStatus.CREATED)
    public void commitPipelineConfiguration(@PathVariable String groupPath, @PathVariable String projectPath, String yaml, String message) {
        pipelineService.commitPipelineYaml(projectPath, yaml, message);
    }

    @GetMapping(value= "/pipelines.json", produces = {"application/json"})
    @ResponseBody
    public PipelinesView getPipelines(@PathVariable String groupPath,
                                      @PathVariable String projectPath,
                                      @RequestParam(required = false, defaultValue = "all") String scope,
                                      @RequestParam(required = false, defaultValue = "1") int page,
                                      HttpServletResponse httpServletResponse) {

        return pipelineService.getPipelinesView(groupPath, projectPath, scope, page, httpServletResponse);
    }

    //https://gitlab.com/baramboy/jandi-connector/pipelines/7820148/stage.json?stage=build
    //http://localhost:8080/projects/example/pipelines/123/stage.json?stage=build
    @GetMapping(value= "/pipelines/{pipelineId}/stage.json", produces = {"application/json"})
    @ResponseBody
    public PipelineStageDetailsView getStage(
            @PathVariable String groupPath,
            @PathVariable String projectPath,
            @PathVariable Long pipelineId,
            @RequestParam String stage) {

        return pipelineService.getPipelineStageDetailsView(groupPath, projectPath, pipelineId, stage);
    }

    @GetMapping("/pipelines/{pipelineId}")
    public String show(@PathVariable String groupPath,
                       @PathVariable String projectPath,
                       @PathVariable Long pipelineId,
                       Model model) {

        model.addAttribute("pipeline", pipelineService.getPipelineView(groupPath, projectPath, pipelineId));

        return "projects/pipelines/show";
    }

    @PostMapping("/pipelines/{pipelineId}/cancel")
    public String cancel(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long pipelineId) {
        pipelineService.cancelRunning(pipelineId);

        return String.format("redirect:/%s/%s/pipelines", groupPath, projectPath);
    }

    @PostMapping("/pipelines/{pipelineId}/retry")
    public String retry(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long pipelineId) {
        pipelineService.retry(pipelineId);

        return String.format("redirect:/%s/%s/pipelines", groupPath, projectPath);
    }

    /**
     * Manual 로 설정한 Job 을 실행합니다. 해당 Job 을 Pending 으로만 해 두면 알아서 Runner 가 가져가서 실행할 겁니다.
     *
     * 이 요청은 화면 측에서 해당 페이지를 Reload 를 하게 됩니다.
     *
     * @param groupPath
     * @param projectPath
     * @param jobId
     * @return
     */
    @PostMapping("/jobs/{jobId}/play")
    public String play(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long jobId) {
        jobService.play(jobId);

        return String.format("redirect:/%s/%s/pipelines", groupPath, projectPath);
    }

    @PostMapping("/jobs/{jobId}/retry")
    public String retryJob(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long jobId) {
        jobService.retry(jobId);

        return String.format("redirect:/%s/%s/pipelines", groupPath, projectPath);
    }

    @PostMapping("/jobs/{jobId}/cancel")
    public String cancelJob(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long jobId) {
        jobService.cancel(jobId);

        return String.format("redirect:/%s/%s/pipelines", groupPath, projectPath);
    }

    @GetMapping("/jobs/{jobId}")
    public String showJob(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long jobId, Model model) {
        model.addAttribute("jobView", jobService.getJobView(groupPath, projectPath, jobId));

        return "projects/jobs/show";
    }

    @GetMapping(value = "/jobs/{jobId}/trace.json", produces = {"application/json"})
    @ResponseBody
    public JobTraceView trace(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long jobId) {

        return jobService.getJobTraceView(jobId);
    }
}
