package io.teamcode.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chiang on 2017. 6. 1..
 */
@Data
public class IssueLabelPriorityId implements Serializable {

    private Long issueLabelId;

    private Long projectId;

    public boolean equals(Object object) {
        if (object instanceof IssueLabelPriorityId) {
            IssueLabelPriorityId otherId = (IssueLabelPriorityId) object;
            return (otherId.issueLabelId.longValue() == this.issueLabelId.longValue())
                    && (otherId.projectId.longValue() == this.projectId.longValue());
        }

        return false;
    }
}
