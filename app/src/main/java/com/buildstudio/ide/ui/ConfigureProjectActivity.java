package com.buildstudio.ide.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.buildstudio.ide.BuildStudioApp;
import com.buildstudio.ide.R;
import com.buildstudio.ide.generator.ProjectGenerator;
import com.buildstudio.ide.model.Project;
import com.buildstudio.ide.model.TemplateType;
import com.buildstudio.ide.util.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConfigureProjectActivity extends AppCompatActivity {

    private static final int REQ_PICK_ICON = 3001;

    private ImageView ivAppIcon;
    private TextInputLayout tilAppName, tilPkgName;
    private TextInputEditText etAppName, etPkgName, etMinSdk, etTargetSdk, etSaveLocation;
    private TemplateType templateType = TemplateType.NAV_DRAWER;
    private File selectedIconFile = null;
    private boolean isUserEditingPkg = false;

    private static final Pattern PKG_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_project);

        String tName = getIntent().getStringExtra("template");
        if (tName != null) {
            try {
                templateType = TemplateType.valueOf(tName);
            } catch (Exception ignored) {}
        }

        PreferenceManager prefs = new PreferenceManager(this);

        ivAppIcon = findViewById(R.id.iv_app_icon);
        tilAppName = findViewById(R.id.til_app_name);
        tilPkgName = findViewById(R.id.til_pkg_name);
        etAppName = findViewById(R.id.et_app_name);
        etPkgName = findViewById(R.id.et_pkg_name);
        etMinSdk = findViewById(R.id.et_min_sdk);
        etTargetSdk = findViewById(R.id.et_target_sdk);
        etSaveLocation = findViewById(R.id.et_save_location);

        etMinSdk.setText(String.valueOf(prefs.getDefaultMinSdk()));
        etTargetSdk.setText(String.valueOf(prefs.getDefaultTargetSdk()));

        findViewById(R.id.layout_app_icon_selector).setOnClickListener(v -> pickCustomIcon());
        ivAppIcon.setOnClickListener(v -> pickCustomIcon());

        etAppName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilAppName.setError(null);
                String name = s == null ? "" : s.toString().trim();
                String cleanPkg = name.toLowerCase(Locale.US).replaceAll("[^a-z0-9_]", "");
                if (!isUserEditingPkg) {
                    etPkgName.setText("com." + cleanPkg);
                }
                etSaveLocation.setText("/storage/emulated/0/.BUILD STUDIO/" + name);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPkgName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPkgName.setError(null);
                if (etPkgName.hasFocus()) {
                    isUserEditingPkg = true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextView btnPrevious = findViewById(R.id.btn_previous);
        MaterialButton btnDone = findViewById(R.id.btn_done);

        btnPrevious.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> validateAndCreateProject());
    }

    private void pickCustomIcon() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_ICON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_ICON && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    if (bmp != null) {
                        ivAppIcon.setImageBitmap(bmp);
                        selectedIconFile = new File(getCacheDir(), "custom_app_icon.png");
                        try (FileOutputStream fos = new FileOutputStream(selectedIconFile)) {
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load icon: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndCreateProject() {
        String appName = etAppName.getText() == null ? "" : etAppName.getText().toString().trim();
        String pkgName = etPkgName.getText() == null ? "" : etPkgName.getText().toString().trim();

        boolean hasError = false;

        if (appName.isEmpty() || !appName.matches("^[A-Za-z0-9_ -]+$")) {
            tilAppName.setError("Not A Valid AppName");
            hasError = true;
        }

        if (pkgName.isEmpty() || !PKG_PATTERN.matcher(pkgName).matches()) {
            tilPkgName.setError("Not A Valid PackageName");
            hasError = true;
        }

        if (hasError) return;

        int minSdk = 21;
        int targetSdk = 34;
        try {
            minSdk = Integer.parseInt(etMinSdk.getText().toString().trim());
        } catch (Exception ignored) {}
        try {
            targetSdk = Integer.parseInt(etTargetSdk.getText().toString().trim());
        } catch (Exception ignored) {}

        File rootDir = BuildStudioApp.getInstance().getProjectsRootDir();
        File projectDir = new File(rootDir, appName);
        if (projectDir.exists()) {
            Toast.makeText(this, "A project with this name already exists", Toast.LENGTH_LONG).show();
            return;
        }

        Project project = new Project(appName, pkgName, projectDir.getAbsolutePath(), templateType, minSdk, targetSdk);

        try {
            ProjectGenerator.generateProject(project);

            // Copy custom icon if selected
            if (selectedIconFile != null && selectedIconFile.exists()) {
                File resIcon = new File(project.getAppDir(), "src/main/res/drawable-xhdpi/app_icon.png");
                resIcon.getParentFile().mkdirs();
                try (InputStream in = new java.io.FileInputStream(selectedIconFile);
                     FileOutputStream out = new FileOutputStream(resIcon)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
            }

            Intent intent = new Intent(ConfigureProjectActivity.this, EditorActivity.class);
            intent.putExtra("project", project);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Project creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
