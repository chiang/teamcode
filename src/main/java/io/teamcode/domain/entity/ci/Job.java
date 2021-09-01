package io.teamcode.domain.entity.ci;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamcode.common.ci.ContentRange;
import io.teamcode.common.ci.config.entry.ArtifactsEntry;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.web.api.model.ci.Artifact;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.*;

/**
 * Created by chiang on 2017. 4. 13..
 */
@Data
@ToString(exclude = {"pipeline"})
@Entity
@Table(name="ci_jobs", indexes = {@Index(name="IDX_CI_JOBS_UNIQUE", columnList = "id, name", unique = true)})//TODO add index to schema?
public class Job implements HasStatus {

    @GenericGenerator(
            name = "jobsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_CI_JOBS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "jobsSequenceGenerator")
    private Long id;

    //TODO updatable true?
    @Basic(optional = false)
    @Column(name = "name", nullable = false, updatable = true)
    private String name;

    /**
     * API 를 통해서 통신할 때 사용하는 Token. GitLab 에서는 원래 Unique 한 것을 사용하는 모양이지만, 여기서는 그렇게 하지 않는다.
     *
     */
    @Size(min = 20, max = 20)
    @Basic(optional = true)
    @Column(name = "token", nullable = true, updatable = true, length = 20)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pipeline_id", foreignKey = @ForeignKey(name = "FK_JOB_PIPELINE"))
    private Pipeline pipeline;

    /**
     * 어떤 Runner 가 이 Job 을 실행했는지 (혹은 실행하고 있는지) 를 표시합니다. Runner 가 하나인 경우는 관계 없으나
     * Multiple 인 경우 이 값은 여러 값이 들어갈 수 있습니다 (물론 각 Job 마다 하나씩 One-To-One 관계).
     *
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "runner_id", foreignKey = @ForeignKey(name = "FK_JOB_RUNNER"))
    private Runner runner;

    @Basic(optional = false)
    @Column(name = "stage_name", nullable = false, updatable = true)
    private String stage;

    /**
     * 0 부터 시작하는 Stage Index
     *
     */
    @Basic(optional = false)
    @Column(name = "stage_idx", nullable = false, updatable = true)
    private Short stageIndex;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false, updatable = true)
    private PipelineStatus status;

    /**
     * 줄바꿈을 기준으로 하는 명령어 문자열. '\n' 으로 Split 처리해서 사용합니다.
     *
     * //TODO length 조정 필요
     */
    @Basic(optional = true)
    @Column(name = "commands", nullable = true, updatable = true, length=10485760)
    private String commands;

    /**
     * 저장된 Artifacts 파일 이름. 실제 경로 정보가 저장됩니다.
     */
    @Basic(optional = true)
    @Column(name = "artifacts_file", nullable = true, updatable = true, length=10485760)
    private String artifactsFile;

    /**
     * Artifacts 를 삭제하는 날짜입니다. Scheduler 등에서 이 값이 현재 날짜 (정확히는 Scheduler 가 동작하는 날짜) 보다 이전 날짜이면
     * Artifacts 를 모두 삭제합니다.
     *
     */
    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "artifacts_expire_at", nullable = true, updatable = true)
    private Date artifactsExpireAt;

    /**
     * JSON 유형.
     */
    @Basic(optional = true)
    @Column(name = "options", nullable = true, updatable = true, length=10485760)
    private String options;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "job_when", nullable = false, updatable = true)
    private JobWhen when = JobWhen.ON_SUCCESS;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "queued_at", nullable = true, updatable = true)
    private Date queuedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at", nullable = true, updatable = true)
    private Date startedAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at", nullable = true, updatable = true)
    private Date finishedAt;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    private transient ContentRange currentContentRange;

    /**
     * 로그 파일의 크기
     */
    private transient long traceLength;

    public static final Artifact buildArtifact(final Job job) {
        Map<String, Object> serializedOptions = job.getSerializedOptions();
        if (!serializedOptions.isEmpty() && serializedOptions.containsKey(ArtifactsEntry.KEY)) {
            ArtifactsEntry artifactsEntry = JobEntry.buildArtifactsEntry((Map<String, Object>)serializedOptions.get(ArtifactsEntry.KEY));

            Artifact artifact = new Artifact();
            artifact.setPaths(artifactsEntry.getPaths());

            if (StringUtils.hasText(artifactsEntry.getName()))
                artifact.setName(artifactsEntry.getName());
            else
                artifact.setName(job.getName());

            return artifact;
        }

        return null;
    }

    public boolean hasArtifacts() {
        return StringUtils.hasText(getArtifactsFileName());
    }

    //FIXME 너무 반복적으로 buildArtifact 를 호출한다. 낭비다.
    public String getArtifactsFileName() {
        Artifact artifact = Job.buildArtifact(this);
        if (artifact != null) {
            if (StringUtils.hasText(artifact.getName()))
                return String.format("%s.zip", artifact.getName());
            else
                return String.format("%s.zip", this.getName());
        }

        return null;
    }

    public Map<String, Object> getSerializedOptions() {
        if (StringUtils.hasText(this.options)) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef
                    = new TypeReference<HashMap<String, Object>>() {
            };

            try {
                return mapper.readValue(this.options, typeRef);
            } catch (IOException e) {
                //TODO e.printStackTrace();
                return Collections.emptyMap();
            }
        }
        else {
            return Collections.emptyMap();
        }
    }

    public boolean isPending() {
        return this.status == PipelineStatus.PENDING;
    }

    /**
     * Pending 이거나 Running 인 경우 <code>true</code> 를 반환합니다.
     *
     * @return
     */
    public boolean isActive() {
        switch(this.status) {
            case PENDING:
            case RUNNING:
                return true;

            default:
                return false;
        }
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

    public boolean isAction() {

        return this.when == JobWhen.MANUAL;
    }

    public boolean isPlayable() {

        return isAction() && isManual();
    }

    public boolean isCancelable() {

        return isActive();
    }

    /**
     * <code>isCancelable</code> 은 사용자가 취소를 클릭할 수 있는지 여부라면 이 메소드는 취소 상태로 만들 수 있는 상태인지 여부를
     * 반환합니다.
     *
     * @return
     */
    public boolean canCanceled() {
        switch(this.getStatus()) {
            case RUNNING:
            case PENDING:
            case CREATED:
            case MANUAL:
                return true;

            default:
                return false;
        }
    }

    public boolean isFailedOrCanceled() {
        return this.getStatus() == PipelineStatus.FAILED || this.getStatus() == PipelineStatus.CANCELED;
    }

    public String resolveDuration() {
        Long duration = duration();
        if (duration != null) {
            if (duration.longValue() < 1000 * 60 * 60)
                return DurationFormatUtils.formatDuration(duration.longValue(), "mm:ss", true);
            else
                return DurationFormatUtils.formatDuration(duration.longValue(), "HH:mm:ss", true);
        }
        else {
            return null;
        }
    }

    public Long duration() {
        if (this.startedAt != null && this.finishedAt != null) {
            return new Long(this.finishedAt.getTime() - this.startedAt.getTime());
        }
        else if (this.startedAt != null) {
            return new Long(new Date().getTime() - this.startedAt.getTime());
        }
        else {
            return null;
        }
    }

    /*
    TODO
    pending? && !any_runners_online?
    public boolean isStuck() {
        return this.isPending() &&
    }*/

}
