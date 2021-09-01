package io.teamcode.common.vcs.svn.diff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chiang on 2017. 3. 25..
 */
public enum ParserState {

    /**
     * This is the initial state of the parser.
     */
    INITIAL {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();

            if (matchesIndexPattern(line)) {
                logTransition(line, INITIAL, INDEX);
                return INDEX;
            } else {
                throw new IllegalStateException("A INITIAL state must be directly followed by a INDEX line ('Index: ...')!");
            }

            //original code
            /*if (matchesFromFilePattern(line)) {
                logTransition(line, INITIAL, FROM_FILE);
                return FROM_FILE;
            } else {
                logTransition(line, INITIAL, HEADER);
                return HEADER;
            }*/
        }
    },

    /**
     * The parser is in this state if it is currently parsing a header line.
     */
    INDEX {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            logTransition(line, INDEX, HEADER);
            return HEADER;
        }
    },

    /**
     * The parser is in this state if it is currently parsing a header line.
     */
    HEADER {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesFromFilePattern(line)) {
                logTransition(line, HEADER, FROM_FILE);
                return FROM_FILE;
            }
            else if (matchesCannotDisplayPattern(line)) {
                logTransition(line, HEADER, CANNOT_DISPLAY);
                return CANNOT_DISPLAY;
            }
            else {
                logTransition(line, HEADER, HEADER);
                return HEADER;
            }
        }
    },

    CANNOT_DISPLAY {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesSvnMimeTypePattern(line)) {
                logTransition(line, CANNOT_DISPLAY, SVN_MIME_TYPE);
                return SVN_MIME_TYPE;
            } else {
                throw new IllegalStateException("A CANNOT_DISPLAY line ('Cannot display...') must be directly followed by a SVN_MIME_TYPE line ('svn:mime-type: ...')!");
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing the line containing the "svn:mime-type"
     * <p/>
     * Example line:<br/>
     * {@code svn:mime-type = application/octet-stream}
     */
    SVN_MIME_TYPE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesEndPattern(line, window)) {
                logTransition(line, SVN_MIME_TYPE, END);
                return END;
            } else if (matchesIndexPattern(line)) {
                logTransition(line, SVN_MIME_TYPE, INDEX);
                return INDEX;
            } else {
                throw new IllegalStateException("A SVN_MIME_TYPE line ('svn:mime-type') must be directly followed by a END or INDEX line!");
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing the line containing the "from" file.
     * <p/>
     * Example line:<br/>
     * {@code --- /path/to/file.txt}
     */
    FROM_FILE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesToFilePattern(line)) {
                logTransition(line, FROM_FILE, TO_FILE);
                return TO_FILE;
            } else {
                throw new IllegalStateException("A FROM_FILE line ('---') must be directly followed by a TO_FILE line ('+++')!");
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing the line containing the "to" file.
     * <p/>
     * Example line:<br/>
     * {@code +++ /path/to/file.txt}
     */
    TO_FILE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesHunkStartPattern(line)) {
                logTransition(line, TO_FILE, HUNK_START);
                return HUNK_START;
            } else {
                logTransition(line, TO_FILE, NEUTRAL_LINE);
                return NEUTRAL_LINE;

                //아래 코드는 바이너리 파일인 경우 동작하지 않는다.
                //throw new IllegalStateException("A TO_FILE line ('+++') must be directly followed by a HUNK_START line ('@@')!");
            }
        }
    },

    //Property changes on: trunk/halloween-icons-noga419.sketch
    PROPERTY_CHANGES_ON {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            logTransition(line, PROPERTY_CHANGES_ON, NEUTRAL_LINE);
            return NEUTRAL_LINE;
        }
    },

    /**
     * The parser is in this state if it is currently parsing a line containing the header of a hunk.
     * <p/>
     * Example line:<br/>
     * {@code @@ -1,5 +2,6 @@}
     */
    HUNK_START {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesFromLinePattern(line)) {
                logTransition(line, HUNK_START, FROM_LINE);
                return FROM_LINE;
            } else if (matchesToLinePattern(line)) {
                logTransition(line, HUNK_START, TO_LINE);
                return TO_LINE;
            } else {
                logTransition(line, HUNK_START, NEUTRAL_LINE);
                return NEUTRAL_LINE;
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing a line containing a line that is in the first file,
     * but not the second (a "from" line).
     * <p/>
     * Example line:<br/>
     * {@code - only the dash at the start is important}
     */
    FROM_LINE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesFromLinePattern(line)) {
                logTransition(line, FROM_LINE, FROM_LINE);
                return FROM_LINE;
            } else if (matchesToLinePattern(line)) {
                logTransition(line, FROM_LINE, TO_LINE);
                return TO_LINE;
            } else if (matchesEndPattern(line, window)) {
                logTransition(line, FROM_LINE, END);
                return END;
            } else if (matchesHunkStartPattern(line)) {
                logTransition(line, FROM_LINE, HUNK_START);
                return HUNK_START;
            } else {
                logTransition(line, FROM_LINE, NEUTRAL_LINE);
                return NEUTRAL_LINE;
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing a line containing a line that is in the second file,
     * but not the first (a "to" line).
     * <p/>
     * Example line:<br/>
     * {@code + only the plus at the start is important}
     */
    TO_LINE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesFromLinePattern(line)) {
                logTransition(line, TO_LINE, FROM_LINE);
                return FROM_LINE;
            } else if (matchesToLinePattern(line)) {
                logTransition(line, TO_LINE, TO_LINE);
                return TO_LINE;
            } else if (matchesEndPattern(line, window)) {
                logTransition(line, TO_LINE, END);
                return END;
            } else if (matchesHunkStartPattern(line)) {
                logTransition(line, TO_LINE, HUNK_START);
                return HUNK_START;
            } else if (matchesIndexPattern(line)) {
                logTransition(line, TO_LINE, INDEX);
                return INDEX;
            } else {
                logTransition(line, TO_LINE, NEUTRAL_LINE);
                return NEUTRAL_LINE;
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing a line that is contained in both files (a "neutral" line). This line can
     * contain any string.
     */
    NEUTRAL_LINE {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesFromLinePattern(line)) {
                logTransition(line, NEUTRAL_LINE, FROM_LINE);
                return FROM_LINE;
            } else if (matchesToLinePattern(line)) {
                logTransition(line, NEUTRAL_LINE, TO_LINE);
                return TO_LINE;
            } else if (matchesEndPattern(line, window)) {
                logTransition(line, NEUTRAL_LINE, END);
                return END;
            } else if (matchesHunkStartPattern(line)) {
                logTransition(line, NEUTRAL_LINE, HUNK_START);
                return HUNK_START;
            } else if (matchesPropertyChangesOnPattern(line)) {
                logTransition(line, NEUTRAL_LINE, PROPERTY_CHANGES_ON);
                return PROPERTY_CHANGES_ON;
            } else {
                logTransition(line, NEUTRAL_LINE, NEUTRAL_LINE);
                return NEUTRAL_LINE;
            }
        }
    },

    /**
     * The parser is in this state if it is currently parsing a line that is the delimiter between two Diffs. This line is always a new
     * line.
     */
    END {
        @Override
        public ParserState nextState(ParseWindow window) {
            String line = window.getFocusLine();
            if (matchesIndexPattern(line)) {
                logTransition(line, INITIAL, INDEX);
                return INDEX;
            } else {
                logTransition(line, END, INITIAL);
                return INITIAL;
            }
        }
    };

    //TODO
    protected static Logger logger = LoggerFactory.getLogger(ParserState.class);

    /**
     * Returns the next state of the state machine depending on the current state and the content of a window of lines around the line
     * that is currently being parsed.
     *
     * @param window the window around the line currently being parsed.
     * @return the next state of the state machine.
     */
    public abstract ParserState nextState(ParseWindow window);

    protected void logTransition(String currentLine, ParserState fromState, ParserState toState) {
        //TODO
        logger.trace(String.format("%14s -> %14s: %s", fromState, toState, currentLine));
    }

    protected boolean matchesIndexPattern(String line) {
        return line.startsWith("Index: ");
    }

    protected boolean matchesCannotDisplayPattern(String line) {
        return line.startsWith("Cannot display");
    }

    protected boolean matchesSvnMimeTypePattern(String line) {
        return line.startsWith("svn:mime-type");
    }

    protected boolean matchesPropertyChangesOnPattern(String line) {
        return line.startsWith("Property changes on: ");
    }

    protected boolean matchesFromFilePattern(String line) {
        return line.startsWith("---");
    }

    protected boolean matchesToFilePattern(String line) {
        return line.startsWith("+++");
    }

    protected boolean matchesFromLinePattern(String line) {
        return line.startsWith("-");
    }

    protected boolean matchesToLinePattern(String line) {
        return line.startsWith("+");
    }

    protected boolean matchesHunkStartPattern(String line) {
        return line.startsWith("@@") && line.endsWith("@@");
    }

    protected boolean matchesEndPattern(String line, ParseWindow window) {
        if ("".equals(line.trim())) {
            // We have a newline which might be the delimiter between two diffs. It may just be an empty line in the current diff or it
            // may be the delimiter to the next diff. This has to be disambiguated...
            int i = 1;
            String futureLine;
            while ((futureLine = window.getFutureLine(i)) != null) {
                if (matchesFromFilePattern(futureLine)) {
                    // We found the start of a new diff without another newline in between. That makes the current line the delimiter
                    // between this diff and the next.
                    return true;
                } else if ("".equals(futureLine.trim())) {
                    // We found another newline after the current newline without a start of a new diff in between. That makes the
                    // current line just a newline within the current diff.
                    return false;
                } else {
                    i++;
                }
            }
            // We reached the end of the stream.
            return true;
        } else {
            // some diff tools like "svn diff" do not put an empty line between two diffs
            // we add that empty line and call the method again
            String nextFromFileLine = window.getFutureLine(3);
            if(nextFromFileLine != null && matchesFromFilePattern(nextFromFileLine)){
                window.addLine(1, "");
                return matchesEndPattern(line, window);
            }else{
                return false;
            }
        }
    }
}
