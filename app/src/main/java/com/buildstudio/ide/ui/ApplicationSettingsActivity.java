package com.buildstudio.ide.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;
import com.buildstudio.ide.util.FileUtils;
import com.buildstudio.ide.util.PreferenceManager;
import java.io.File;

public class ApplicationSettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private TextView tvCacheSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_settings);

        preferenceManager = new PreferenceManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_app_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchCompat switchDarkMode = findViewById(R.id.switch_dark_mode);
        SwitchCompat switchConfirmDelete = findViewById(R.id.switch_confirm_delete);
        tvCacheSize = findViewById(R.id.tv_cache_size);

        switchDarkMode.setChecked(preferenceManager.isDarkMode());
        switchConfirmDelete.setChecked(preferenceManager.isConfirmBeforeDelete());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkMode(isChecked);
            Toast.makeText(ApplicationSettingsActivity.this, "Restart the app to apply theme changes", Toast.LENGTH_SHORT).show();
        });

        switchConfirmDelete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setConfirmBeforeDelete(isChecked);
        });

        updateCacheSize();

        findViewById(R.id.layout_clear_cache).setOnClickListener(v -> {
            clearAppCache();
            updateCacheSize();
            Toast.makeText(ApplicationSettingsActivity.this, "App cache cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCacheSize() {
        long size = getDirSize(getCacheDir()) + getDirSize(getCodeCacheDir());
        tvCacheSize.setText("Free up temporary build files (" + formatSize(size) + ")");
    }

    private void clearAppCache() {
        FileUtils.deleteDirectory(getCacheDir());
        FileUtils.deleteDirectory(getCodeCacheDir());
    }

    private long getDirSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                size += f.isDirectory() ? getDirSize(f) : f.length();
            }
        }
        return size;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(java.util.Locale.US, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
