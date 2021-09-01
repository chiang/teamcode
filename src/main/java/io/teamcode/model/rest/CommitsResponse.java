package io.teamcode.model.rest;

import io.teamcode.common.vcs.svn.Commit;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;

@Data
public class CommitsResponse extends ResourceSupport {

    private Page<Commit> commits;

    private long prevOffset;

    private long offset;

    private boolean issueLinkEnabled;

    private String issueLinkUrl;

    private String regexp;

}
