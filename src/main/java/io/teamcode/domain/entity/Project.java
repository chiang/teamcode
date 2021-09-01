package io.teamcode.domain.entity;

import io.teamcode.common.Strings;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.vcs.svn.Commit;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

/**
 * 
 */
@Data
@Entity
@Table(name="projects", indexes = {
        @Index(columnList = "name", name = "IDX_PROJECTS_NAME")})
public class Project {

    @GenericGenerator(
            name = "projectsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_PROJECTS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "projectsSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="group_id", foreignKey = @ForeignKey(name = "FK_PROJECTS_GROUP"))
    private Group group;

    /**
     * TODO 규칙 만들기. 일단 최소 2자에서 32자 이내. 특수 문자는 제외한다. 근데 이걸 Validation 할 수 없다. 왜냐면 최초 생성할 때는 Name 없이 Path 값을 복사해서 사용하기 때문이다.
     */
    @Basic(optional = false)
    @Column(name = "name", nullable = false, updatable = true)
    private String name;

    /**
     * TODO 규칙 만들기. 일단 최소 2자에서 32자 이내. 한글 허용 안함.
     */
    @Pattern(regexp = "^[\\p{Alnum}\\-]{2,32}$")
    @Basic(optional = false)
    @Column(name = "path", nullable = false, updatable = true)
    private String path;

    //text
    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name="creator_id", foreignKey = @ForeignKey(name = "FK_PROJECT_CREATOR"))
    private User creator;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_level", nullable = false, updatable = true)
    private Visibility visibility = Visibility.INTERNAL;

    @Basic(optional = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "programming_language", nullable = true, updatable = true)
    private ProgrammingLanguage programmingLanguage;

    @Basic(optional = true)
    @Column(name = "star_count", nullable = true, updatable = true)
    private Integer starCount = new Integer(0);

    /**
     * 아바타를 설정한 경우 경로 정보를 저장합니다. 서버 상의 경로 밑 상대 경로로 갑니다. 주로 data/attachments 밑이 됩니다.
     *
     */
    @Basic(optional = true)
    @Column(name = "avatar_path", nullable = true, updatable = true)
    private String avatarPath;

    /**
     * 빌드 타임아웃. 초 단위. 기본 값은 3,600 초 (1시간).
     */
    @Basic(optional = false)
    @Column(name = "build_timeout", nullable = false, updatable = true)
    private Integer buildTimeout = 3600;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "attachments_visibility", nullable = false, updatable = true)
    private ProjectMenuVisibility attachmentsVisibility = ProjectMenuVisibility.ENABLED;

    /**
     * 프로젝트가 파이프라인을 사용할 수 있는지 여부를 설정합니다. 기본은 OFF 입니다.
     */
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_visibility", nullable = false, updatable = true)
    private ProjectMenuVisibility pipelineVisibility = ProjectMenuVisibility.DISABLED;

    /**
     * 파이프라인 설정 파일 위치 정보. 값이 없으면 REPO_ROOT/trunk/.teamcode-pipelines.yml 입니다. 경로만 설정할 수 있으며 파일 이름은
     * 설정할 수 없습니다.
     *
     * trunk 는 가장 기본적으로 사용하는 경로이므로 이렇게 처리합니다.
     */
    @Basic(optional = true)
    @Column(name = "pipeline_config_path", nullable = true, updatable = true)
    private String pipelineConfigPath;

    /**
     * 프로젝트가 아카이브 되었는지 여부를 확인합니다. 프로젝트는 바로 삭제할 수 없고 먼저 아카이브됩니다. 아카이브된 프로젝트는 저장소에서도 분리됩니다.
     *
     */
    @Basic(optional = false)
    @Column(name = "archived", nullable = false, updatable = true)
    private Boolean archived = Boolean.FALSE;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "archived_at", nullable = true, updatable = true)
    private Date archivedAt;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_activity_at", nullable = false, updatable = false)
    private Date lastActivityAt;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    /**
     * 이 프로젝트의 마지막 커밋 정보
     *
     */
    private transient Commit lastCommit;

    /**
     * 이 프로젝트의 멤버 목록
     */
    private transient List<User> members;

    public String getAvatarFileName() {
        if (StringUtils.hasText(this.avatarPath)) {
            return Strings.getFileNameFromPath(this.avatarPath);
        }

        return null;
    }

    public String getProgrammingLanguageIcon() {
        if (this.programmingLanguage == null)
            return "default.svg";

        //TODO 아이콘을 지원하지 않는 타입은 그냥 default 로 나오게 해야겠다.
        return this.programmingLanguage.name().toLowerCase();
    }

    public String getResolvedPipelineConfigPath() {

        if (!StringUtils.hasText(this.pipelineConfigPath)) {
            return TeamcodeConstants.DEFAULT_PIPELINE_CONFIG_PATH;
        }
        else {
            StringBuilder builder = new StringBuilder();
            if (!this.pipelineConfigPath.startsWith("/")) {
                builder.append("/");
            }
            if (this.pipelineConfigPath.endsWith("/")) {
                builder.append(this.pipelineConfigPath.substring(0, this.pipelineConfigPath.length() - 1));
            }
            else {
                builder.append(this.pipelineConfigPath);
            }

            return builder.toString();
        }
    }

    /**
     * allowed_colors = {
     red: 'FFEBEE',
     purple: 'F3E5F5',
     indigo: 'E8EAF6',
     blue: 'E3F2FD',
     teal: 'E0F2F1',
     orange: 'FBE9E7',
     gray: 'EEEEEE'
     }
     */
    private transient String[] allowedColors = {"FFEBEE", "F3E5F5", "E8EAF6", "E3F2FD", "E0F2F1", "FBE9E7", "EEEEEE"};

    public char getIdenticonChar() {

        return Character.toUpperCase(getName().charAt(0));
    }

    public String getIdenticonStyle() {
        int bgIndex = (int)(this.id % 7);
        return new StringBuilder("background-color: #").append(allowedColors[bgIndex]).append("; color: #555;").toString();
    }

    //t.integer "namespace_id"
    //t.string "import_url"

    //t.string "avatar"
    //t.string "import_status"
    //t.string "import_type"
    //t.string "import_source"
    //t.text "import_error"
    //t.integer "ci_id"
    //t.boolean "shared_runners_enabled", default: true, null: false
    //t.string "runners_token"
    //t.string "build_coverage_regex"
    //t.boolean "build_allow_git_fetch", default: true, null: false
    //t.integer "build_timeout", default: 3600, null: false
    //t.boolean "pending_delete", default: false
    //t.boolean "public_builds", default: true, null: false
    //t.boolean "last_repository_check_failed"
    //t.datetime "last_repository_check_at"
    //t.boolean "container_registry_enabled"
    //t.boolean "only_allow_merge_if_build_succeeds", default: false, null: false
    //t.boolean "has_external_issue_tracker"
    //t.string "repository_storage", default: "default", null: false
    //t.boolean "request_access_enabled", default: false, null: false
    //t.boolean "has_external_wiki"
    //t.boolean "lfs_enabled"
    //t.text "description_html"
    //t.boolean "only_allow_merge_if_all_discussions_are_resolved"
}
