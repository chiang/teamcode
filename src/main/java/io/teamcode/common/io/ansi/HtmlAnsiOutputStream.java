package io.teamcode.common.io.ansi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://code.dblock.org">Daniel Doubrovkine</a>
 */
public class HtmlAnsiOutputStream extends AnsiOutputStream {

    private boolean concealOn = false;

    @Override
    public void close() throws IOException {
        closeAttributes();
        super.close();
    }

    private static final String[] ANSI_COLOR_MAP = {"black", "term-fg-l-red term-bold",
            "term-fg-l-green term-bold", "yellow", "blue", "magenta", "cyan", "white",};

    private static final byte[] BYTES_QUOT = "&quot;".getBytes();
    private static final byte[] BYTES_AMP = "&amp;".getBytes();
    private static final byte[] BYTES_LT = "&lt;".getBytes();
    private static final byte[] BYTES_GT = "&gt;".getBytes();

    public HtmlAnsiOutputStream(OutputStream os) {
        super(os);
    }

    private final List<String> closingAttributes = new ArrayList<String>();

    private void write(String s) throws IOException {
        super.out.write(s.getBytes());
    }

    private void writeAttribute(String s) throws IOException {
        write("<" + s + ">");
        closingAttributes.add(0, s.split(" ", 2)[0]);
    }

    private void closeAttributes() throws IOException {
        for (String attr : closingAttributes) {
            write("</" + attr + ">");
        }
        closingAttributes.clear();
    }

    public void write(int data) throws IOException {
        switch (data) {
            case 34: // "
                out.write(BYTES_QUOT);
                break;
            case 38: // &
                out.write(BYTES_AMP);
                break;
            case 60: // <
                out.write(BYTES_LT);
                break;
            case 62: // >
                out.write(BYTES_GT);
                break;
            default:
                super.write(data);
        }
    }

    public void writeLine(byte[] buf, int offset, int len) throws IOException {
        write(buf, offset, len);
        closeAttributes();
    }

    @Override
    protected void processSetAttribute(int attribute) throws IOException {
        switch (attribute) {
            case ATTRIBUTE_CONCEAL_ON:
                write("\u001B[8m");
                concealOn = true;
                break;
            case ATTRIBUTE_INTENSITY_BOLD:
                writeAttribute("b");
                break;
            case ATTRIBUTE_INTENSITY_NORMAL:
                closeAttributes();
                break;
            case ATTRIBUTE_UNDERLINE:
                writeAttribute("u");
                break;
            case ATTRIBUTE_UNDERLINE_OFF:
                closeAttributes();
                break;
            case ATTRIBUTE_NEGATIVE_ON:
                break;
            case ATTRIBUTE_NEGATIVE_OFF:
                break;
            default:
                break;
        }
    }

    @Override
    protected void processAttributeRest() throws IOException {
        if (concealOn) {
            write("\u001B[0m");
            concealOn = false;
        }
        closeAttributes();
    }

    @Override
    protected void processSetForegroundColor(int color, boolean bright) throws IOException {
        writeAttribute("span class=\"" + ANSI_COLOR_MAP[color] + "\"");
    }

    @Override
    protected void processSetBackgroundColor(int color, boolean bright) throws IOException {
        writeAttribute("span style=\"background-color: " + ANSI_COLOR_MAP[color] + ";\"");
    }
}
