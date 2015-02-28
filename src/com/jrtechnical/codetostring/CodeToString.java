package com.jrtechnical.codetostring;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.TextRange;

import java.io.StringWriter;

public class CodeToString extends EditorAction{

    protected CodeToString() {
        super(new CodeToStringHandler());
    }

    private static class CodeToStringHandler extends EditorActionHandler {
        protected void doExecute(final Editor editor, final Caret caret, final DataContext dataContext) {
            ApplicationManager.getApplication().runWriteAction(new Runnable(){
                @Override
                public void run() {
                    SelectionModel selectionModel = editor.getSelectionModel();

                    if(!selectionModel.hasSelection()){
                        return;
                    }

                    Document document = editor.getDocument();

                    int firstLine = selectionModel.getSelectionStartPosition().getLine();
                    final int lastSelectedLine = selectionModel.getSelectionEndPosition().getLine();
                    final int selectionStart = selectionModel.getSelectionStart();
                    final int endOfFirstSelectedLine = document.getLineEndOffset(firstLine);
                    final int endOffset = document.getLineEndOffset(lastSelectedLine);

                    String selectionOnFirstLine = document.getText(TextRange.create(selectionStart,endOfFirstSelectedLine));
                    if(leadingWhiteSpace(selectionOnFirstLine) == -1) {
                        firstLine++;
                    }
                    if(firstLine > lastSelectedLine) return;

                    int startOffset = document.getLineStartOffset(firstLine);


                    final String text = document.getText(TextRange.create(startOffset,endOffset));
                    final String[] lines = text.split("\\r?\\n");
                    final int lastLine = lastNonWhitespaceLine(lines);
                    if(lastLine == -1) return;
                    final int min = minimumLeadingWhiteSpace(lines);
                    final StringWriter sw = new StringWriter();
                    boolean started = false;
                    int codeVarOffset = -1;
                    for(int i = 0; i <= lastLine; i++){
                        final String line = lines[i];
                        final int sp = leadingWhiteSpace(line);
                        if(started || sp != -1){
                            appendSpaces(sw, started ? min + 2 : min);
                            if(!started){
                                codeVarOffset = startOffset + sw.toString().length() + 4;
                                sw.append("var code = [\n");
                                appendSpaces(sw, min + 2);
                                started = true;
                            }
                        }
                        if(started){
                            sw.append('"');
                            if(sp != -1){
                                escapeJavaStyleString(sw,line.substring(min),false);
                            }
                            sw.append('"');
                            if(i < lastLine) sw.append(",");
                        }
                        sw.append('\n');
                    }
                    appendSpaces(sw,min);
                    sw.append("].join(\"\\n\");");
                    for(int i = lastLine+1; i < lines.length; i++) sw.append('\n');


                    String replacementText = sw.toString();
                    editor.getDocument().replaceString(
                            startOffset,
                            endOffset,
                            replacementText
                    );
                    editor.getCaretModel().moveToOffset(startOffset + replacementText.length());

                    selectionModel.setSelection(codeVarOffset, codeVarOffset + 4);
                }
            });
        }
    }

    private static int lastNonWhitespaceLine(String lines[]){
        for (int i = lines.length - 1; i >= 0; i--) {
            int sp = leadingWhiteSpace(lines[i]);
            if(sp != -1) return i;
        }
        return -1;
    }

    private static void appendSpaces(StringWriter sw, int min){
        for(int i = 0; i < min; i++){
            sw.append(' ');
        }
    }

    private static int minimumLeadingWhiteSpace(String[] lines){
        int min = -1;
        for (String line : lines) {
            int leadingSp = leadingWhiteSpace(line);
            min = min == -1 ? leadingSp : leadingSp == -1 ? min : Math.min(leadingSp, min);
        }
        return min;
    }

    private static int leadingWhiteSpace(String str){
        for(int i = 0; i < str.length(); i++){
            if(!Character.isWhitespace(str.charAt(i))) return i;
        }
        return -1;
    }


    /**
     *
     * Everything below this line is a Modified version of:
     *
     *   https://github.com/krasa/StringManipulation/blob/master/src/osmedile/intellij/stringmanip/utils/StringEscapeUtil.java
     *
     * @param out               write to receieve the escaped string
     * @param str               String to escape values in, may be null
     * @param escapeSingleQuote escapes single quotes if <code>true</code>
     *
     */
    private static void escapeJavaStyleString(StringWriter out, String str, boolean escapeSingleQuote)  {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.write('\\');
                        out.write('b');
                        break;
                    case '\n':
                        out.write('\\');
                        out.write('n');
                        break;
                    case '\t':
                        out.write('\\');
                        out.write('t');
                        break;
                    case '\f':
                        out.write('\\');
                        out.write('f');
                        break;
                    case '\r':
                        out.write('\\');
                        out.write('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            out.write("\\u00" + hex(ch));
                        } else {
                            out.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        if (escapeSingleQuote) {
                            out.write('\\');
                        }
                        out.write('\'');
                        break;
                    case '"':
                        out.write('\\');
                        out.write('"');
                        break;
                    case '\\':
                        out.write('\\');
                        out.write('\\');
                        break;
                    default:
                        out.write(ch);
                        break;
                }
            }
        }
    }

    /**
     * <p>Returns an upper case hexadecimal <code>String</code> for the given
     * character.</p>
     *
     * @param ch The character to convert.
     *
     * @return An upper case hexadecimal <code>String</code>
     */
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }
}
