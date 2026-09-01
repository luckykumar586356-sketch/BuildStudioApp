package com.buildstudio.ide.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatEditText;

import com.buildstudio.ide.util.FileUtils;
import com.buildstudio.ide.util.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class CodeEditorView extends AppCompatEditText {

    public interface OnModifiedListener {
        void onModifiedChanged(boolean isModified);
    }

    private final Rect rect = new Rect();
    private final Paint linePaint = new Paint();
    private final Paint gutterPaint = new Paint();
    private final Paint lineSeparatorPaint = new Paint();
    private final Paint activeLinePaint = new Paint();

    private File currentFile;
    private boolean isModified = false;
    private final Handler syntaxHandler = new Handler(Looper.getMainLooper());
    private boolean isHighlighting = false;
    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private boolean isRestoring = false;
    private OnModifiedListener modifiedListener;
    private boolean showLineNumbers = true;
    private boolean highlightCurrentLine = true;

    public CodeEditorView(Context context) {
        super(context);
        init();
    }

    public CodeEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(Typeface.MONOSPACE);
        setTextSize(14f);
        setTextColor(Color.parseColor("#1F2937"));
        setBackgroundColor(Color.parseColor("#FFFFFF"));
        setGravity(Gravity.TOP | Gravity.START);
        setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        setHorizontallyScrolling(true);

        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.parseColor("#9CA3AF"));
        linePaint.setTextSize(getTextSize() * 0.85f);
        linePaint.setTypeface(Typeface.MONOSPACE);
        linePaint.setTextAlign(Paint.Align.RIGHT);

        gutterPaint.setStyle(Paint.Style.FILL);
        gutterPaint.setColor(Color.parseColor("#FAFAFC"));

        lineSeparatorPaint.setStyle(Paint.Style.FILL);
        lineSeparatorPaint.setColor(Color.parseColor("#E5E7EB"));

        activeLinePaint.setStyle(Paint.Style.FILL);
        activeLinePaint.setColor(Color.parseColor("#F3F4F6"));

        setPadding(96, 16, 24, 48);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isRestoring && s != null) {
                    undoStack.push(s.toString());
                    redoStack.clear();
                    while (undoStack.size() > 50) {
                        undoStack.removeLast();
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isRestoring) {
                    isModified = true;
                    if (modifiedListener != null) modifiedListener.onModifiedChanged(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isHighlighting) return;
                triggerSyntaxHighlight();
            }
        });
    }

    public void setOnModifiedListener(OnModifiedListener listener) {
        this.modifiedListener = listener;
    }

    public void applyPreferences(PreferenceManager prefs) {
        if (prefs == null) return;
        setTextSize(prefs.getEditorFontSize());
        linePaint.setTextSize(getTextSize() * 0.85f);
        showLineNumbers = prefs.isShowLineNumbers();
        highlightCurrentLine = prefs.isHighlightCurrentLine();
        setHorizontallyScrolling(!prefs.isWordWrap());

        if (prefs.isDarkEditorTheme()) {
            setBackgroundColor(Color.parseColor("#1E1E1E"));
            setTextColor(Color.parseColor("#D4D4D4"));
            gutterPaint.setColor(Color.parseColor("#252526"));
            lineSeparatorPaint.setColor(Color.parseColor("#333333"));
            linePaint.setColor(Color.parseColor("#858585"));
            activeLinePaint.setColor(Color.parseColor("#2A2D2E"));
        } else {
            setBackgroundColor(Color.parseColor("#FFFFFF"));
            setTextColor(Color.parseColor("#1F2937"));
            gutterPaint.setColor(Color.parseColor("#FAFAFC"));
            lineSeparatorPaint.setColor(Color.parseColor("#E5E7EB"));
            linePaint.setColor(Color.parseColor("#9CA3AF"));
            activeLinePaint.setColor(Color.parseColor("#F3F4F6"));
        }
        setPadding(showLineNumbers ? 96 : 24, 16, 24, 48);
        invalidate();
    }

    public void triggerSyntaxHighlight() {
        syntaxHandler.removeCallbacksAndMessages(null);
        syntaxHandler.postDelayed(() -> {
            isHighlighting = true;
            Editable editable = getText();
            if (editable != null) {
                JavaSyntaxHighlighter.highlight(editable);
            }
            isHighlighting = false;
        }, 120);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (showLineNumbers) {
            int count = getLineCount();
            int gutterWidth = 80;

            canvas.drawRect(getScrollX(), 0, getScrollX() + gutterWidth, getHeight() + getScrollY(), gutterPaint);
            canvas.drawRect(getScrollX() + gutterWidth, 0, getScrollX() + gutterWidth + 1, getHeight() + getScrollY(), lineSeparatorPaint);

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, rect);
                canvas.drawText(String.valueOf(i + 1), getScrollX() + gutterWidth - 10, baseline, linePaint);
            }
        }

        super.onDraw(canvas);
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        String current = getText() == null ? "" : getText().toString();
        String previous = undoStack.pop();
        redoStack.push(current);
        restoreText(previous);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        String current = getText() == null ? "" : getText().toString();
        String next = redoStack.pop();
        undoStack.push(current);
        restoreText(next);
    }

    private void restoreText(String content) {
        isRestoring = true;
        setText(content);
        setSelection(Math.min(content.length(), content.length()));
        isRestoring = false;
        isModified = true;
        if (modifiedListener != null) modifiedListener.onModifiedChanged(true);
        triggerSyntaxHighlight();
    }

    public void openFile(File file) {
        this.currentFile = file;
        try {
            String content = FileUtils.readFileToString(file);
            setText(content);
            isModified = false;
            if (modifiedListener != null) modifiedListener.onModifiedChanged(false);
            undoStack.clear();
            redoStack.clear();
            triggerSyntaxHighlight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean saveCurrentFile() {
        if (currentFile != null) {
            try {
                FileUtils.writeStringToFile(currentFile, getText().toString());
                isModified = false;
                if (modifiedListener != null) modifiedListener.onModifiedChanged(false);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public boolean isModified() {
        return isModified;
    }
}
