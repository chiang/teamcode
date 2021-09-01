package io.teamcode.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by chiang on 2016. 12. 18..
 */
public abstract class StorageUtils {

    public static final void moveStagedToTicket(final File resourceRoot, String stagedPath, String ticketPath) throws IOException {
        File stagedFile = new File(resourceRoot, stagedPath);
        FileUtils.copyFile(stagedFile, new File(resourceRoot, ticketPath));
        if (!stagedFile.delete())
            throw new IOException(String.format("Cannot delete file '%s'.", stagedFile.getAbsolutePath()));
    }


    /**
     *
     * 임시로 올리는 경로. mod 16 시스템 사용. 그렇게 많을 것 같지는 않으니까.
     *
     * ./data/attachments/projects/{project-id % 16}/{project-id}/_staged_/{hashed-file-name-first-char}/{hashed-file-name-second-char}
     *
     * @param projectId
     * @param key
     * @return
     */
    public static final String buildStagedHierarchyStructure(final Long projectId, final String key) {
        StringBuilder builder = new StringBuilder();

        builder.append("projects").append(File.separator);
        builder.append(projectId % 16).append(File.separator).append(File.separator);
        builder.append(projectId).append(File.separator);
        builder.append("_staged_").append(File.separator);
        builder.append(key.charAt(0)).append(File.separator);
        builder.append(key.charAt(1));

        return builder.toString();
    }

    /**
     *
     * 실제로 올리는 경로. mod 16 시스템 사용. 그렇게 많을 것 같지는 않으니까.
     *
     * ./data/attachments/projects/{project-id % 16}/{project-id}/downloads/{hashed-file-name-first-char}/{hashed-file-name-second-char}
     *
     * @param projectId
     * @param key
     * @return
     */
    public static final String buildProjectAttachmentsHierarchyStructure(final Long projectId, final String key) {
        StringBuilder builder = new StringBuilder();

        builder.append("projects").append(File.separator);
        builder.append(projectId % 16).append(File.separator).append(File.separator);
        builder.append(projectId).append(File.separator);
        builder.append("downloads").append(File.separator);
        builder.append(key.charAt(0)).append(File.separator);
        builder.append(key.charAt(1));

        return builder.toString();
    }

    public static final String buildProjectDirHierarchyStructure(final Long projectId) {
        StringBuilder builder = new StringBuilder();

        builder.append("projects").append(File.separator);
        builder.append(projectId % 16).append(File.separator).append(File.separator);
        builder.append(projectId);

        return builder.toString();
    }

    /**
     * :root/{last-3-digit-brand-id % 250}/{next-3-digit-brand-id % 250}/{brand-id}/tickets/
     *
     *
     * @param brandId
     * @param ticketId
     * @param key UUID (containing dash) x- file name
     * @return
     */
    public static final String buildTicketHierarchyStructure(final Long brandId, final Long ticketId, final String key) {
        StringBuilder builder = new StringBuilder();
        long last3DigitBrand = brandId % 1000;
        long nextLast3DigitBrand = (brandId - last3DigitBrand) / 1000;

        builder.append(last3DigitBrand % 250).append(File.separator).append(nextLast3DigitBrand % 250).append(File.separator);
        builder.append(brandId).append(File.separator).append("tickets").append(File.separator);

        long last3Digit = ticketId % 1000;
        long nextLast3Digit = (ticketId - last3Digit) / 1000;

        builder.append(last3Digit % 250).append(File.separator).append(nextLast3Digit % 250).append(File.separator);
        builder.append(ticketId).append(File.separator);
        builder.append(key.charAt(0));

        return builder.toString();
    }
}
