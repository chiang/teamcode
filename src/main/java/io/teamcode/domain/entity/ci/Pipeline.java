package io.teamcode.domain.entity.ci;

import io.teamcode.common.ci.CiConfigProcessor;
import io.teamcode.common.ci.PipelineDuration;
import io.teamcode.common.ci.config.CiConfig;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 4. 8..
 */
@Data
@ToString(exclude = {"project", "finishedAt"})
@Entity
@Table(name="ci_pipelines")
public class Pipeline implements HasStatus {

    @GenericGenerator(
            name = "pipelinesSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_CI_PIPELINES"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "pipelinesSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_CI_PIPELINES_PROJ"))
    private Project project;

    /**
     * Subversion Commit Revision 정보. 이게 없으면 무슨 빌드를 한다는 말인가?
     */
    @Basic(optional = false)
    @Column(name = "commit_revision", nullable = false, updatable = false)
    private Long commitRevision;

    /**
     * YAML Validation 오류를 저정하는 컬럼.
     */
    @Basic(optional = true)
    @Column(name = "yaml_errors", nullable = true, updatable = true, length = 2000)
    private String yamlErrors;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_status", nullable = false, updatable = true)
    private PipelineStatus status = PipelineStatus.CREATED;

    /**
     * 밀리세컨드 단위.
     *
     */
    @Basic(optional = true)
    @Column(name = "duration", nullable = true, updatable = true)
    private Long duration;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at", nullable = true, updatable = true)
    private Date startedAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at", nullable = true, updatable = true)
    private Date finishedAt;

    /**
     * 커밋한 사람이거나 바로 실행한 사람일 수 있습니다. 만약 <code>null</code> 이면 커밋한 사용자가 데이터베이스에 없는 경우입니다.
     *
     */
    @ManyToOne(optional = true)
    @JoinColumn(name="created_by", foreignKey = @ForeignKey(name = "FK_CI_PIPELINES_CREATOR"))
    private User createdBy;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    private transient List<Job> jobs;

    /**
     * 파이프라인 생성 시 서브버전에 저장된 pipeline 설정 파일 내용입니다.
     */
    @Setter(AccessLevel.NONE)
    private transient String pipelineYml;

    private transient CiConfig ciConfig;

    //pending 상태이고 online runner 가 하나도 없는 경우 이 값을 true 로 설정합니다.
    private transient boolean stuck;

    public boolean isPending() {
        return this.status == PipelineStatus.PENDING;
    }

    @Override
    public boolean isCompleted() {
        switch(this.getStatus()) {
            case SUCCESS:
            case FAILED:
            case CANCELED:
            case SKIPPED:
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean isManual() {
        return this.getStatus() == PipelineStatus.MANUAL;
    }

    public boolean isRetryable() {

        //return this.getJobs().stream().anyMatch(j -> j.isFailedOrCanceled());
        switch(this.getStatus()) {
            case FAILED:
            case CANCELED:
                return true;

            default:
                return false;
        }
    }

    public void processConfig(final String pipelineYml) {
        this.pipelineYml = pipelineYml;

        if (!StringUtils.hasText(this.getPipelineYml())) {
            this.yamlErrors = "teamcode-pipelines.yml 파일 내용이 없습니다. 내용을 작성해 주세요.";

            return;
        }

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(this.getPipelineYml());

        this.ciConfig = processor.getCiConfig();
        if (this.ciConfig.hasErrors()) {
            this.yamlErrors = org.apache.commons.lang3.StringUtils.join(ciConfig.getValidationContext().getValidationError(), ". ");
        }
    }

    public boolean hasYamlErrors() {

        return StringUtils.hasText(this.yamlErrors);
    }

    //TODO 테스트 필요...
    public void updateDuration() {
        if (this.startedAt == null)
            return;

        this.duration = PipelineDuration.fromPipeline(this);
    }

    /**
     * 돌고 있는 Job 들을 취소시킵니다.
     *
     Gitlab::OptimisticLocking.retry_lock(cancelable_statuses) do |cancelable|
     cancelable.find_each do |job|
     yield(job) if block_given?
     job.cancel
     end
     end

     *
     */
    public void cancelRunning() {

        for(Job job: getCancelableJobs()) {
            //job.cancel();
        }
    }

    public List<Job> getJobsByStageName(final String stageName) {
        List<String> stageNames = getJobs().stream().sorted(Comparator.comparing(Job::getStageIndex)).map(j -> j.getStage()).distinct().collect(Collectors.toList());

        if (stageNames.size() == 0) {
            return Collections.emptyList();
        }
        else {
            return getJobs().stream().filter(j -> j.getStage().equals(stageName)).collect(Collectors.toList());
        }
    }

    public List<Stage> getStages(boolean fetchJobs) {
        List<Job> jobs = getJobs();
        if (jobs == null) {
            throw new IllegalStateException("Stage 목록을 조회하려면 Job 목록을 Pipeline 에 미리 설정해야 합니다.");
        }

        List<String> stageNames = jobs.stream().sorted(Comparator.comparing(Job::getStageIndex)).map(j -> j.getStage()).distinct().collect(Collectors.toList());

        if (stageNames.size() == 0) {
            return Collections.emptyList();
        }
        else {
            List<Stage> stages = new ArrayList<>();
            List<Job> jobsInStage;
            Stage stage;
            for (String stageName: stageNames) {
                jobsInStage = getJobs().stream().filter(j -> j.getStage().equals(stageName)).collect(Collectors.toList());

                stage = new Stage();
                stage.setName(stageName);

                if (hasYamlErrors()) {
                    stage.setStatus(PipelineStatus.FAILED);
                }
                else {
                    stage.setStatus(Pipeline.resolveStatus(jobsInStage));
                }

                if (fetchJobs) {
                    stage.setJobs(jobsInStage);
                }

                stages.add(stage);
            }

            return stages;
        }
    }


    /**
     *
     def config_builds_attributes
     return [] unless config_processor

     config_processor.
     builds_for_ref(ref, tag?, trigger_requests.first).
     sort_by { |build| build[:stage_idx] }
     end


     *
     * @return
     */
    //TODO
    public List<JobEntry> getJobEntries() {

        return this.ciConfig.getJobEntries();
    }

    public List<Job> getCreatedJobs() {
        return getJobs().stream().filter(j -> j.getStatus() == PipelineStatus.CREATED).collect(Collectors.toList());
    }

    public List<Job> getCanCancelableJobs() {
        return this.jobs.stream().filter(j -> j.canCanceled()).collect(Collectors.toList());
    }

    public List<Job> getCancelableJobs() {

        return this.jobs.stream().filter(j -> j.isCancelable()).collect(Collectors.toList());
    }

    /**
     * 파아프라인을 Retry 할 때 Retry 가능한 Job 목록을 반환합니다. Job 하나가 Retry 가능한지 여부와는 다릅니다.
     *
     * @return
     */
    public List<Job> getRetryableJobs() {

        //return this.jobs.stream().filter(j -> j.getStatus() == PipelineStatus.FAILED || j.getStatus() == PipelineStatus.CANCELED).collect(Collectors.toList());
        return this.jobs.stream().filter(j -> j.getStatus() != PipelineStatus.SUCCESS).collect(Collectors.toList());
    }

    /**
     * 파이프라인의 상태가 어떤지 확인하고 돌려줍니다.
     *
     GitLab Way
     return 'failed' unless yaml_errors.blank?
     TODO statuses.latest.status || 'skipped'
     *
     * @return
     */
    public PipelineStatus getResolvedStatus() {
        if (this.hasYamlErrors())
            return PipelineStatus.FAILED;

        return resolveStatus(this.jobs);
    }

    /**
     * 반드시 아래 순서대로 체크를 해야 합니다. 그래샤 올바른 상태 파악 가능.
     *
     * 1. Job 개수와 Skipped Job 개수가 같으면 Skipped
     * 2. Job 개수와 Success Job 개수가 같으면 Success
     * 3. Job 개수와 Created Job 개수가 같으면 Created
     * 4. Job 개수와 (Success + Skipped) 개수가 같으면 Success
     * 5. Job 개수와 (Success + Skipped + Pending) 개수가 같으면 Pending
     * 6. Job 개수와 (Success + Skipped + Canceled) 개수가 같으면 Canceled
     * 7. Running Job + Pending Job > 0 이면 Running
     * 8. Manual Job > 0 이면 Manual
     * 9. Created Job > 0 이면 Created
     * 10. Failed Job > 0 이면 Failed
     *
     * @return
     */
    public static final PipelineStatus resolveStatus(final List<Job> jobs) {
        long jobsCount = jobs.size();
        long skippedCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.SKIPPED).count();
        long successCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.SUCCESS).count();
        long createdCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.CREATED).count();
        long pendingCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.PENDING).count();
        long canceledCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.CANCELED).count();
        long runningCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.RUNNING).count();
        long manualCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.MANUAL).count();
        long failedCount = jobs.stream().filter(j -> j.getStatus() == PipelineStatus.FAILED).count();

        if (jobsCount == skippedCount)
            return PipelineStatus.SKIPPED;

        else if (jobsCount == (skippedCount + manualCount))//TODO 이 구문은 PipelineProcessService 의 processJob(216번 라인, Manual Case 의 TODO 와 연결되어 있다).
            return PipelineStatus.SKIPPED;

        else if (jobsCount == successCount)
            return PipelineStatus.SUCCESS;

        else if (jobsCount == createdCount)
            return PipelineStatus.CREATED;

        else if (jobsCount == (successCount + manualCount))
            return PipelineStatus.SUCCESS;

        //GitLab 에서는 pendingCount 와 createdCount 를 계산하지 않는다. 하지만 우리는 이를 계산해서
        //취소 상태인 것으로 간주한다!!!
        //TODO 위의 TODO (else if (jobsCount == (skippedCount + manualCount))) 와 연계해서 고려해야 함.
        else if (jobsCount == (successCount + skippedCount + canceledCount + manualCount))
            return PipelineStatus.CANCELED;

        //else if (jobsCount == manualCount)
        //    return PipelineStatus.SKIPPED;

        else if (runningCount > 0)
            return PipelineStatus.RUNNING;

        else if (pendingCount > 0)
            return PipelineStatus.PENDING;

        else if (failedCount > 0)
            return PipelineStatus.FAILED;

        else
            return PipelineStatus.CREATED;
    }

    public boolean isStuck() {
        //this status is pending && !any_runners_online

        return false;
    }

    public boolean isCancelable() {

        return this.getJobs().stream().anyMatch(j -> j.isCancelable());
    }



}
