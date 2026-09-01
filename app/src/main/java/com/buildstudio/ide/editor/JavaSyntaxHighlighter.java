package com.buildstudio.ide.editor;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight, high-performance Java syntax highlighting for embedded code editor. */
public final class JavaSyntaxHighlighter {
    private static final int KEYWORD = Color.parseColor("#7C3AED");
    private static final int STRING = Color.parseColor("#059669");
    private static final int COMMENT = Color.parseColor("#6B7280");
    private static final int NUMBER = Color.parseColor("#D97706");

    private static final Pattern KEYWORDS = Pattern.compile(
            "\\b(?:package|import|public|private|protected|static|final|class|interface|enum|extends|implements|new|return|if|else|for|while|do|switch|case|break|continue|try|catch|finally|throw|throws|void|boolean|byte|char|short|int|long|float|double|true|false|null|this|super|abstract|synchronized|volatile|instanceof)\\b");
    private static final Pattern STRINGS = Pattern.compile("(?:\"(?:\\\\.|[^\"])*\"|'(?:\\\\.|[^'])*')");
    private static final Pattern COMMENTS = Pattern.compile("//[^\\n]*|/\\*[\\s\\S]*?\\*/");
    private static final Pattern NUMBERS = Pattern.compile("\\b(?:0[xX][0-9a-fA-F]+|\\d+(?:\\.\\d+)?[fFdDlL]?)\\b");

    private JavaSyntaxHighlighter() {
    }

    public static void highlight(Editable editable) {
        if (editable == null || editable.length() == 0) return;
        
        // Remove existing custom formatting spans safely
        ForegroundColorSpan[] colorSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : colorSpans) {
            editable.removeSpan(span);
        }
        StyleSpan[] styleSpans = editable.getSpans(0, editable.length(), StyleSpan.class);
        for (StyleSpan span : styleSpans) {
            editable.removeSpan(span);
        }

        apply(editable, COMMENTS, COMMENT, Typeface.ITALIC);
        apply(editable, STRINGS, STRING, Typeface.NORMAL);
        apply(editable, KEYWORDS, KEYWORD, Typeface.BOLD);
        apply(editable, NUMBERS, NUMBER, Typeface.NORMAL);
    }

    private static void apply(Editable editable, Pattern pattern, int color, int style) {
        Matcher matcher = pattern.matcher(editable);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (style != Typeface.NORMAL) {
                editable.setSpan(new StyleSpan(style), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
