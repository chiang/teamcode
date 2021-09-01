package io.teamcode.domain.entity;

import io.teamcode.common.IconsHelper;
import io.teamcode.common.vcs.svn.RepositoryEntryType;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.File;
import java.util.Date;

/**
 * 팀코드에서 제공하는 파일 관리 기능은 매우 기본적인 기능만 제공합니다. 주요 기능이 아닌 거지요. 그래서 파일 이름은 동일한 것은 없습니다.
 * 프로젝트 별로 한 개만 있을 수 있습니다.
 *
 * <p><code>project</code>는 <code>null</code>을 허용합니다. <code>null</code>인 경우는, 이 Entity 가 Staged 상태임을 의미합니다.
 * 파일을 업로드 중 어떤 문제가 발행하였음을 의미하며 이 Entity 는 나중에 주기적으로 삭제를 해 줘야 합니다.</p>
 *
 * <p>Staged 상태를 관리하기 위해서 project 와 originalFileName 을 기반으로 한 UK 를 만들지는 않습니다.
 * Staged 상태에서는 동일 파일 이름이 여러 번 저장될 가능성이 있기 때문입니다. UK가 <code>null</code>은 체크하지 않는다는
 * 확신이 있을 경우 UK를 만들면 되겠습니다.</p>
 */
@Data
@ToString(exclude = "project")
@Entity
@Table(name = "project_attachments")
public class ProjectAttachment {

    @GenericGenerator(
            name = "projectAttachmentSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_PROJ_ATTACHMENTS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "projectAttachmentSequenceGenerator")
    private Long id;

    /**
     * Pre-upload (FIXME 무슨 용어가 있을텐데) 할 때는 이 값이 비어 있고 Transaction 이 완료되면 들어간다.
     *
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_PROJ_ATTACH_PROJ"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "FK_PROJ_ATTACH_AUTHOR"))
    private User author;

    @Basic(optional = false)
    @Column(name = "file_name", nullable = false, updatable = true)
    private String fileName;

    @Basic(optional = false)
    @Column(name = "byte_size", nullable = false, updatable = true)
    private Long size;

    @Basic(optional = false)
    @Column(name = "content_type", nullable = false, updatable = true)
    private String contentType;

    @Basic(optional = false)
    @Column(name = "original_file_name", nullable = false, updatable = true)
    private String originalFileName;

    /**
     * 파일 확장자는 없는 경우도 있으므로 Nullable 처리합니다.
     *
     */
    @Basic(optional = true)
    @Column(name = "extension", nullable = true, updatable = true)
    private String extension;

    @Basic(optional = false)
    @Column(name = "path", nullable = false, updatable = true)
    private String path;

    /**
     * 파일 다운로드 횟수
     */
    @Basic(optional = false)
    @Column(name = "downloads", nullable = false, updatable = true)
    private int downloads = 0;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public String getIconClass() {
        return IconsHelper.fileTypeIconClass(RepositoryEntryType.FILE, originalFileName);
    }


    /*
     * 아래 코드는 사용하지 않습니다. project 가 null 인 경우를 staged 로 판단합니다. 불필요한 정보를 더 관리하지 않습니다.
     *
     * <code>true</code> 이면 아직 임시 저장 중, <code>false</code> 이면 Database Entity 와 Reference 가 맺어진 상태
     * //TODOs json ignoring...
     *
     */
    /*@Basic(optional = false)
    @Column(name = "is_staged", nullable = false, updatable = true)
    private Boolean staged = Boolean.TRUE;*/

    private transient File file;

    public void plusDownloads() {
        this.downloads++;
    }

    public String getHumanReadableSize() {
        if (this.size != null)
            return FileUtils.byteCountToDisplaySize(this.size.longValue());
        else
            return "0 B";
    }

}
