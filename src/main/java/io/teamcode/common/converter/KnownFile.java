package io.teamcode.common.converter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiang on 2017. 5. 24..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnownFile {

    private String name;

    private boolean extensionOnly;

    private String mimeType;

}
