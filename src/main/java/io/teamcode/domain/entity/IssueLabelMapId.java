package io.teamcode.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class IssueLabelMapId implements Serializable {

    private Long issueId;

    private Long issueLabelId;

    private Long projectId;

    public boolean equals(Object object) {
        if (object instanceof IssueLabelMapId) {
            IssueLabelMapId otherId = (IssueLabelMapId) object;
            return (otherId.issueId.longValue() == this.issueId.longValue())
                    && (otherId.issueLabelId.longValue() == this.issueLabelId.longValue())
                    && (otherId.projectId.longValue() == this.projectId.longValue());
        }

        return false;
    }
}
