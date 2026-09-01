package com.buildstudio.ide.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;
import com.buildstudio.ide.util.PreferenceManager;

public class BuildAiSettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private EditText etApiKey, etModel, etBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_ai_settings);

        preferenceManager = new PreferenceManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar_ai_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        etApiKey = findViewById(R.id.et_ai_api_key);
        etModel = findViewById(R.id.et_ai_model);
        etBaseUrl = findViewById(R.id.et_ai_base_url);

        etApiKey.setText(preferenceManager.getAiApiKey());
        etModel.setText(preferenceManager.getAiModel());
        etBaseUrl.setText(preferenceManager.getAiBaseUrl());

        findViewById(R.id.btn_save_ai_settings).setOnClickListener(v -> {
            preferenceManager.setAiApiKey(etApiKey.getText().toString().trim());
            String m = etModel.getText().toString().trim();
            if (m.isEmpty()) m = "glm-4.6";
            preferenceManager.setAiModel(m);
            preferenceManager.setAiBaseUrl(etBaseUrl.getText().toString().trim());
            Toast.makeText(BuildAiSettingsActivity.this, "Build AI settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
