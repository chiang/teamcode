package io.teamcode.domain.entity.project.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.teamcode.common.vcs.svn.callback.SummaryLogMessage;
import io.teamcode.domain.entity.Project;
import io.teamcode.service.project.integration.CustomIssueTracker;
import io.teamcode.service.project.integration.EmailsOnCommitService;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 8..
 */
@Data
@Entity
@Table(name="project_services", uniqueConstraints = {
        @UniqueConstraint(name = "UK_PROJ_SERVICES", columnNames = {"project_id", "service_key"})})
public class ProjectIntegrationServiceSettings {

    @GenericGenerator(
            name = "projectServiceSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_PROJ_SERVICES"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "projectServiceSequenceGenerator")
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = true)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_PROJ_SERVICES_PROJ"))
    private Project project;

    /**
     * 서비스는 키로 등록이 되며
     *
     */
    @Basic(optional = false)
    @Column(name = "service_key", nullable = false, updatable = false)
    private String key;

    @Basic(optional = false)
    @Column(name = "is_active", nullable = false, updatable = true)
    private Boolean active = Boolean.FALSE;

    @Basic(optional = false)
    @Column(name = "service_category", nullable = false, updatable = true)
    private ProjectIntegrationServiceCategory category = ProjectIntegrationServiceCategory.COMMON;

    @Basic(optional = false)
    @Column(name = "title", nullable = false, updatable = true)
    private String title;

    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true)
    private String description;

    @Basic(optional = true)
    @Column(name = "is_commit_events", nullable = true, updatable = true)
    private Boolean commitEvents = Boolean.TRUE;


    /**
     * JSON Type 속성 값입니다. 자유롭게 다루기 위해서죠. text type 으로 관리합니다.
     *
     */
    @JsonRawValue
    @Basic(optional = true)
    @Column(name = "properties", nullable = true, updatable = true, length = 2000)
    private String properties;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    private transient SummaryLogMessage summaryLogMessage;

    private transient Long revision;

    private transient String logoPath;

    public Map<String, Object> getPropertyMap() {
        Map<String, Object> serviceProperties;
        if (StringUtils.hasText(this.getProperties())) {
            JacksonJsonParser jsonParser = new JacksonJsonParser();
            serviceProperties = jsonParser.parseMap(this.getProperties());
        }
        else {
            serviceProperties = new HashMap<>();
        }

        return serviceProperties;
    }


    /*t.string "type", ExternalWikiService, GitlabIssueTrackerService
    t.boolean "template", default: false
    t.boolean "issues_events", default: true
    t.boolean "merge_requests_events", default: true
    t.boolean "tag_push_events", default: true
    t.boolean "note_events", default: true, null: false
    t.boolean "build_events", default: false, null: false
    t.boolean "default", default: false
    t.boolean "wiki_page_events", default: true
    t.boolean "pipeline_events", default: false, null: false
    t.boolean "confidential_issues_events", default: true, null: false*/
}
