package io.teamcode.common.vcs;

import io.teamcode.common.vcs.svn.Diff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 25..
 */
public interface DiffParser {

    /**
     * Constructs a list of Diffs from a textual InputStream.
     *
     * @param in the input stream to parse
     * @return list of Diff objects parsed from the InputStream.
     */
    List<Diff> parse(InputStream in);

    /**
     * Constructs a list of Diffs from a textual byte array.
     *
     * @param bytes the byte array to parse
     * @return list of Diff objects parsed from the byte array.
     */
    List<Diff> parse(byte[] bytes);

    /**
     * Constructs a list of Diffs from a textual File
     *
     * @param file the file to parse
     * @return list of Diff objects parsed from the File.
     */
    List<Diff> parse(File file) throws IOException;

}
