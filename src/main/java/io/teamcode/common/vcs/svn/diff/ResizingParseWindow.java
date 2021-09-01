package io.teamcode.common.vcs.svn.diff;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 3. 25..
 */
public class ResizingParseWindow implements ParseWindow {

    private BufferedReader reader;

    private LinkedList<String> lineQueue = new LinkedList<>();

    private int lineNumber = 0;

    private List<Pattern> ignorePatterns = new ArrayList<>();

    private boolean isEndOfStream = false;

    public ResizingParseWindow(InputStream in) {
        Reader unbufferedReader = new InputStreamReader(in);
        this.reader = new BufferedReader(unbufferedReader);
    }

    public void addIgnorePattern(String ignorePattern) {
        this.ignorePatterns.add(Pattern.compile(ignorePattern));
    }

    @Override
    public String getFutureLine(int distance) {
        try {
            resizeWindowIfNecessary(distance + 1);
            return lineQueue.get(distance);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void addLine(int pos, String line) {
        lineQueue.add(pos, line);
    }

    /**
     * Resizes the sliding window to the given size, if necessary.
     *
     * @param newSize the new size of the window (i.e. the number of lines in the
     *                window).
     */
    private void resizeWindowIfNecessary(int newSize) {
        try {
            int numberOfLinesToLoad = newSize - this.lineQueue.size();
            for (int i = 0; i < numberOfLinesToLoad; i++) {
                String nextLine = getNextLine();
                if (nextLine != null) {
                    lineQueue.addLast(nextLine);
                } else {
                    throw new IndexOutOfBoundsException("End of stream has been reached!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String slideForward() {
        try {
            lineQueue.pollFirst();
            lineNumber++;
            if (lineQueue.isEmpty()) {
                String nextLine = getNextLine();
                if (nextLine != null) {
                    lineQueue.addLast(nextLine);
                }
                return nextLine;
            } else {
                return lineQueue.peekFirst();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextLine() throws IOException {
        String nextLine = reader.readLine();
        while (matchesIgnorePattern(nextLine)) {
            nextLine = reader.readLine();
        }

        return getNextLineOrVirtualBlankLineAtEndOfStream(nextLine);
    }

    /**
     * Guarantees that a virtual blank line is injected at the end of the input
     * stream to ensure the parser attempts to transition to the {@code END}
     * state, if necessary, when the end of stream is reached.
     */
    private String getNextLineOrVirtualBlankLineAtEndOfStream(String nextLine) {
        if ((nextLine == null) && !isEndOfStream) {
            isEndOfStream = true;
            return "";
        }

        return nextLine;
    }

    private boolean matchesIgnorePattern(String line) {
        if (line == null) {
            return false;
        } else {
            for (Pattern pattern : ignorePatterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 현재 위치한 라인을 반환합니다.
     *
     * @return
     */
    @Override
    public String getFocusLine() {
        return lineQueue.element();
    }

    @Override
    public int getFocusLineNumber() {
        return lineNumber;
    }

}
