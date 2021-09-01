package io.teamcode.common.vcs.svn.diff;

import io.teamcode.common.vcs.DiffParser;
import io.teamcode.common.vcs.svn.Diff;
import io.teamcode.util.FileSystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 3. 25..
 */
public class SvnDiffParser implements DiffParser {

    @Override
    public List<Diff> parse(InputStream in) {
        ResizingParseWindow window = new ResizingParseWindow(in);
        ParserState state = ParserState.INITIAL;
        List<Diff> parsedDiffs = new ArrayList<>();
        Diff currentDiff = null;//new Diff();
        String currentLine;
        while ((currentLine = window.slideForward()) != null) {
            state = state.nextState(window);
            switch (state) {
                case INITIAL:
                    // nothing to do
                    break;
                case INDEX:
                    currentDiff = new Diff();
                    parsedDiffs.add(currentDiff);
                    parseIndex(currentDiff, currentLine);
                    break;
                case HEADER:
                    parseHeader(currentDiff, currentLine);
                    break;
                case CANNOT_DISPLAY:
                    parseCannotDisplay(currentDiff, currentLine);
                    break;
                case SVN_MIME_TYPE:
                    parseSvnMimeType(currentDiff, currentLine);
                    break;
                case PROPERTY_CHANGES_ON:
                    parsePropertyChangesOn(currentDiff);
                    break;
                case FROM_FILE:
                    parseFromFile(currentDiff, currentLine);
                    break;
                case TO_FILE:
                    parseToFile(currentDiff, currentLine);
                    break;
                case HUNK_START:
                    parseHunkStart(currentDiff, currentLine);
                    break;
                case FROM_LINE:
                    parseFromLine(currentDiff, currentLine);
                    break;
                case TO_LINE:
                    parseToLine(currentDiff, currentLine);
                    break;
                case NEUTRAL_LINE:
                    parseNeutralLine(currentDiff, currentLine);
                    break;
                case END:
                    //original code
                    //parsedDiffs.add(currentDiff);
                    //currentDiff = new Diff();
                    break;
                default:
                    throw new IllegalStateException(String.format("Illegal parser state '%s'", state));
            }
        }

        parsedDiffs.removeIf(d -> d.isPropertyChanges());

        return parsedDiffs;
    }

    private void parseNeutralLine(Diff currentDiff, String currentLine) {
        Line line;
        if (currentLine.length() > 1 && currentLine.startsWith(" "))
            line = new Line(Line.LineType.NEUTRAL, currentLine.substring(1));
        else
            line = new Line(Line.LineType.NEUTRAL, currentLine);
        if (currentDiff.getLatestHunk() != null) {
            currentDiff.getLatestHunk().getLines().add(line);
        }
        //binary 파일의 경우 빈 라인 다음에 "Property changes on: trunk/halloween-icons-noga419.sketch" 이 오는 경우도 있으므로
        //Hunk 가 없다. 그래서 이렇게 처리한다.
        else {
            //do nothing...
        }
    }

    private void parseToLine(Diff currentDiff, String currentLine) {
        if (currentDiff.isNotPropertyChangesOn()) {
            Line toLine = new Line(Line.LineType.TO, currentLine.substring(1));
            currentDiff.getLatestHunk().getLines().add(toLine);

            currentDiff.plusAdditions();
        }
    }

    private void parseFromLine(Diff currentDiff, String currentLine) {
        if (currentDiff.isNotPropertyChangesOn()) {
            Line fromLine = new Line(Line.LineType.FROM, currentLine.substring(1));
            currentDiff.getLatestHunk().getLines().add(fromLine);

            currentDiff.plusDeletions();
        }
    }

    private void parseHunkStart(Diff currentDiff, String currentLine) {
        Pattern pattern = Pattern.compile("^.*-([0-9]+)(?:,([0-9]+))? \\+([0-9]+)(?:,([0-9]+))?.*$");
        Matcher matcher = pattern.matcher(currentLine);
        if (matcher.matches()) {
            String range1Start = matcher.group(1);
            String range1Count = (matcher.group(2) != null) ? matcher.group(2) : "1";
            Range fromRange = new Range(Integer.valueOf(range1Start), Integer.valueOf(range1Count));

            String range2Start = matcher.group(3);
            String range2Count = (matcher.group(4) != null) ? matcher.group(4) : "1";
            Range toRange = new Range(Integer.valueOf(range2Start), Integer.valueOf(range2Count));

            Hunk hunk = new Hunk(currentLine);
            hunk.setFromFileRange(fromRange);
            hunk.setToFileRange(toRange);
            currentDiff.getHunks().add(hunk);
        } else {
            throw new IllegalStateException(String.format("No line ranges found in the following hunk start line: '%s'. Expected something " +
                    "like '-1,5 +3,5'.", currentLine));
        }
    }

    private void parseToFile(Diff currentDiff, String currentLine) {
        currentDiff.setToFileName(cutAfterTab(currentLine.substring(4)));
    }

    private void parseFromFile(Diff currentDiff, String currentLine) {
        currentDiff.setFromFileName(cutAfterTab(currentLine.substring(4)));
    }

    /**
     * Cuts a TAB and all following characters from a String.
     */
    private String cutAfterTab(String line) {
        Pattern p = Pattern.compile("^(.*)\\t.*$");
        Matcher matcher = p.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return line;
        }
    }

    private void parseIndex(Diff currentDiff, String currentLine) {
        currentDiff.setPath(cutAfterTab(currentLine.substring(7)));
        currentDiff.getHeaderLines().add(currentLine);
    }

    private void parseHeader(Diff currentDiff, String currentLine) {
        currentDiff.getHeaderLines().add(currentLine);
    }

    private void parseCannotDisplay(Diff currentDiff, String currentLine) {
        currentDiff.setCannotDisplay(true);
    }

    private void parseSvnMimeType(Diff currentDiff, String currentLine) {
        String[] tuple = currentLine.split("=");
        currentDiff.setMimeType(tuple[1].trim());

        //이미지 같은 것들도 모두 octet-stream 으로 나오니까 좀 그렇다. 그래서 아래와 같이 처리.
        String detectedMimeType = FileSystemUtils.detectMimeType(currentDiff.getPath());
        currentDiff.setMimeType(detectedMimeType);
    }

    private void parsePropertyChangesOn(Diff currentDiff) {
        currentDiff.setPropertyChanges(true);
    }

    @Override
    public List<Diff> parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes));
    }

    @Override
    public List<Diff> parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

}
