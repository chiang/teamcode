package io.teamcode.web.api.model.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiang on 2017. 5. 28..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variable {

    private String name;

    private String value;
}
