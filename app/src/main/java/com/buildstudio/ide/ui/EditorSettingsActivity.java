package com.buildstudio.ide.ui;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;
import com.buildstudio.ide.util.PreferenceManager;

public class EditorSettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_settings);

        preferenceManager = new PreferenceManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_editor_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvFontSizeVal = findViewById(R.id.tv_font_size_val);
        SeekBar seekFontSize = findViewById(R.id.seek_font_size);
        int currentFont = preferenceManager.getEditorFontSize();
        tvFontSizeVal.setText(currentFont + "sp");
        seekFontSize.setProgress(Math.max(0, Math.min(14, currentFont - 10)));
        seekFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int sp = 10 + progress;
                tvFontSizeVal.setText(sp + "sp");
                preferenceManager.setEditorFontSize(sp);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        TextView tvTabSizeVal = findViewById(R.id.tv_tab_size_val);
        SeekBar seekTabSize = findViewById(R.id.seek_tab_size);
        int currentTab = preferenceManager.getEditorTabSize();
        tvTabSizeVal.setText(currentTab + " spaces");
        seekTabSize.setProgress(Math.max(0, Math.min(6, currentTab - 2)));
        seekTabSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int spaces = 2 + progress;
                tvTabSizeVal.setText(spaces + " spaces");
                preferenceManager.setEditorTabSize(spaces);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SwitchCompat switchWordWrap = findViewById(R.id.switch_word_wrap);
        switchWordWrap.setChecked(preferenceManager.isWordWrap());
        switchWordWrap.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setWordWrap(isChecked));

        SwitchCompat switchLineNumbers = findViewById(R.id.switch_line_numbers);
        switchLineNumbers.setChecked(preferenceManager.isShowLineNumbers());
        switchLineNumbers.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setShowLineNumbers(isChecked));

        SwitchCompat switchHighlightLine = findViewById(R.id.switch_highlight_line);
        switchHighlightLine.setChecked(preferenceManager.isHighlightCurrentLine());
        switchHighlightLine.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setHighlightCurrentLine(isChecked));

        SwitchCompat switchAutoComplete = findViewById(R.id.switch_auto_complete);
        switchAutoComplete.setChecked(preferenceManager.isAutoComplete());
        switchAutoComplete.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setAutoComplete(isChecked));

        SwitchCompat switchDarkEditorTheme = findViewById(R.id.switch_dark_editor_theme);
        switchDarkEditorTheme.setChecked(preferenceManager.isDarkEditorTheme());
        switchDarkEditorTheme.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setDarkEditorTheme(isChecked));
    }
}
