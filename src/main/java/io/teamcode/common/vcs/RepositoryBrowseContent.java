package io.teamcode.common.vcs;

import io.teamcode.common.converter.KnownFile;
import io.teamcode.common.io.KnownFilesResolver;
import io.teamcode.common.vcs.svn.RepositoryEntry;
import io.teamcode.common.vcs.svn.RepositoryEntryType;
import io.teamcode.service.vcs.svn.RepositoryBrowseService;
import io.teamcode.util.FileSystemUtils;
import io.teamcode.util.MimeTypeUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 화면에 표시한 Repository Contents 정보. Breadcrumb 정보를 기본으로 전달한다.
 *
 * 만약, 목록이 비어 있다면 빈 디렉터링다.
 * 목록이 하나이고 타입이 파일이면 파일 정보를 보내줘야 한다. 물론 읽을 수 있는 혹은 없는 지 여부도 여기서 판단해서 사용자가 알 수 있게 한다.
 */
@Data
public class RepositoryBrowseContent {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryBrowseContent.class);

    private KnownFilesResolver knownFilesResolver;

    private static List<String> displayableMimeTypes = new ArrayList<>();

    static {
        displayableMimeTypes.add("application/x-sh");
        displayableMimeTypes.add("application/javascript");
        displayableMimeTypes.add("application/xml");
    }

    private static final long MB_50 = 1024 * 1024 * 50;

    private List<SourceBrowserBreadcrumb> breadcrumbs;

    private List<RepositoryEntry> entries;

    /**
     * 만약 현재 Content 가 파일이고 웹 브라우저에서 읽을 수 있는 포맷인 경우 이 값이 설정됩니다. 이미지는 별도로 처리를 합니다.
     */
    @Setter(AccessLevel.NONE)
    private String content;

    //FIXME naming
    private String notDisplayableReason;

    private long revision;

    //markdown format
    private boolean wiki;

    public RepositoryBrowseContent(final KnownFilesResolver knownFilesResolver) {
        this.knownFilesResolver = knownFilesResolver;
    }

    public void setContent(String content, boolean wiki) {
        this.content = content;
        this.wiki = wiki;

        //code 란 말이다!
        if (!wiki) {
            Scanner scanner = new Scanner(this.content);

            StringBuilder lineNumberBuilder = new StringBuilder();
            StringBuilder builder = new StringBuilder();
            String line;
            int lineNumber = 0;

            lineNumberBuilder.append("<div class=\"line-numbers\">");
            builder.append("<div class=\"blob-content\" data-blob-id=\"452c74337c019336b01a1de74df78d87982bde9d\">");
            builder.append("<pre class=\"code highlight\"><code>");
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNumberBuilder.append("<a class=\"diff-line-num\" data-line-number=\"");
                lineNumberBuilder.append(++lineNumber);
                lineNumberBuilder.append("\" href=\"#L").append(lineNumber);
                lineNumberBuilder.append("\" id=\"L").append(lineNumber);
                lineNumberBuilder.append("\"><i class=\"fa fa-link\"></i>").append(lineNumber);
                lineNumberBuilder.append("</a>");

                builder.append("<span id=\"LC").append(lineNumber).append("\" class=\"line\">").append(line).append("</span>");
            }
            lineNumberBuilder.append("</div>");
            builder.append("</code></pre></div>");

            this.content = lineNumberBuilder.append(builder).toString();
        }
    }

    public boolean isRoot() {

        return this.isDirectory() && this.entries.stream().anyMatch(e -> e.getPath().equals("/") && e.getType() == RepositoryEntryType.DIRECTORY);
    }

    public boolean isEmptyDirectory() {

        return entries == null || entries.isEmpty() || (entries.size() == 1 && entries.get(0).getType() == RepositoryEntryType.UP_LINK);
    }

    /**
     * 현재 Content 가 디렉터리인지 여부를 반환합니다. Subversion 에서 빈 디렉터리가 올 수도 있는 점도 감안합니다.
     * @return
     */
    public boolean isDirectory() {

        return !isFileContent();
    }

    public boolean isFileContent() {

        return entries != null && entries.size() == 1 && entries.get(0).getType() == RepositoryEntryType.FILE;

        /*if (entries == null || entries.size() == 0 || entries.size() > 1)
            return false;
        else {
            return entries.get(0).getType() == RepositoryEntryType.FILE;
        }*/
    }

    public boolean isDisplayableIfFileContent() {
        if (isFileContent()) {
            if (entries.get(0).getSize() > MB_50) {
                this.notDisplayableReason = "파일 크기가 너무 커서 표시할 수 없습니다.";
                return false;
            }
            else {
                if (knownFilesResolver.match(entries.get(0).getName())) {
                    return true;
                }
                else {
                    String mimeType = FileSystemUtils.detectMimeType(entries.get(0).getName());
                    logger.debug("Detected mime type for file name ({}): {}", entries.get(0).getName(), mimeType);
                    if (StringUtils.hasText(mimeType)) {
                        if (MimeTypeUtils.isImage(mimeType) || mimeType.startsWith("text")) {
                            return true;
                        } else if (displayableMimeTypes.contains(mimeType)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    public RepositoryEntry getFileEntry() {
        if (isFileContent())
            return this.entries.get(0);
        else
            return null;
    }

    //@see getFileEntry
    @Deprecated
    public RepositoryEntry getOneEntryIfFileContent() {
        if (isFileContent())
            return this.entries.get(0);
        else
            return null;
    }

    public RepositoryEntry getOneEntryIfDirectoryContent() {
        if (isDirectory())
            return this.entries.get(0);
        else
            return null;
    }
}
