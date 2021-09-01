package io.teamcode.common.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represent HTTP Content-Range Header Value
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRange {

    private long offset;

    private long limit;

    public static ContentRange build(String contentRangeHeaderValue) {
        String[] contentRangeStringArray = contentRangeHeaderValue.split("-");
        if (contentRangeStringArray.length != 2)
            throw new IllegalArgumentException("Content-Range header value must contains '-'");

        ContentRange contentRange = new ContentRange();
        try {
            contentRange.setOffset(Long.parseLong(contentRangeStringArray[0]));
            contentRange.setLimit(Long.parseLong(contentRangeStringArray[1]));
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Content-Range header value must contains number");
        }

        return contentRange;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(offset).append("-");
        builder.append(limit);

        return builder.toString();
    }
}
