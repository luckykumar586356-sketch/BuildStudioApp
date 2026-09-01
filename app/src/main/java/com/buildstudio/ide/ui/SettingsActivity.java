package com.buildstudio.ide.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.item_setting_application).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, ApplicationSettingsActivity.class)));

        findViewById(R.id.item_setting_editor).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, EditorSettingsActivity.class)));

        findViewById(R.id.item_setting_build_run).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, BuildAndRunSettingsActivity.class)));

        findViewById(R.id.item_setting_about_us).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, AboutUsActivity.class)));

        findViewById(R.id.item_setting_build_ai).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, BuildAiSettingsActivity.class)));
    }
}
