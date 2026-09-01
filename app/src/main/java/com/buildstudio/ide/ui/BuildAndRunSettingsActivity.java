package com.buildstudio.ide.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;
import com.buildstudio.ide.util.PreferenceManager;

public class BuildAndRunSettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private TextView tvMinSdkVal, tvTargetSdkVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_and_run_settings);

        preferenceManager = new PreferenceManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_build_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvMinSdkVal = findViewById(R.id.tv_min_sdk_val);
        tvTargetSdkVal = findViewById(R.id.tv_target_sdk_val);

        tvMinSdkVal.setText(String.valueOf(preferenceManager.getDefaultMinSdk()));
        tvTargetSdkVal.setText(String.valueOf(preferenceManager.getDefaultTargetSdk()));

        findViewById(R.id.layout_min_sdk).setOnClickListener(v -> showMinSdkDialog());
        findViewById(R.id.layout_target_sdk).setOnClickListener(v -> showTargetSdkDialog());

        SwitchCompat switchAutoSave = findViewById(R.id.switch_auto_save);
        switchAutoSave.setChecked(preferenceManager.isAutoSaveBeforeBuild());
        switchAutoSave.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setAutoSaveBeforeBuild(isChecked));

        SwitchCompat switchShowLogs = findViewById(R.id.switch_show_logs);
        switchShowLogs.setChecked(preferenceManager.isShowBuildLogs());
        switchShowLogs.setOnCheckedChangeListener((v, isChecked) -> preferenceManager.setShowBuildLogs(isChecked));
    }

    private void showMinSdkDialog() {
        String[] sdks = {"16", "19", "21", "23", "24", "26", "28", "29", "30", "31", "33", "34"};
        int current = preferenceManager.getDefaultMinSdk();
        int selectedIndex = 2;
        for (int i = 0; i < sdks.length; i++) {
            if (sdks[i].equals(String.valueOf(current))) {
                selectedIndex = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Minimum SDK")
                .setSingleChoiceItems(sdks, selectedIndex, (dialog, which) -> {
                    int val = Integer.parseInt(sdks[which]);
                    preferenceManager.setDefaultMinSdk(val);
                    tvMinSdkVal.setText(String.valueOf(val));
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTargetSdkDialog() {
        String[] sdks = new String[19];
        for (int i = 0; i < 19; i++) {
            sdks[i] = String.valueOf(16 + i);
        }
        int current = preferenceManager.getDefaultTargetSdk();
        int selectedIndex = 18;
        for (int i = 0; i < sdks.length; i++) {
            if (sdks[i].equals(String.valueOf(current))) {
                selectedIndex = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Target SDK")
                .setSingleChoiceItems(sdks, selectedIndex, (dialog, which) -> {
                    int val = Integer.parseInt(sdks[which]);
                    preferenceManager.setDefaultTargetSdk(val);
                    tvTargetSdkVal.setText(String.valueOf(val));
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
