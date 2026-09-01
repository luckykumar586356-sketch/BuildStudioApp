package com.buildstudio.ide.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.Button;
import androidx.drawerlayout.widget.DrawerLayout;
import com.buildstudio.ide.R;
import com.buildstudio.ide.editor.CodeEditorView;
import com.buildstudio.ide.engine.AndroidBuilder;
import com.buildstudio.ide.engine.ApkInstaller;
import com.buildstudio.ide.explorer.FileExplorerView;
import com.buildstudio.ide.model.Project;
import com.buildstudio.ide.util.FileUtils;
import com.buildstudio.ide.util.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {

    private static final int REQ_IMPORT_LIB = 1002;

    private DrawerLayout drawerLayout;
    private FileExplorerView fileExplorerView;
    private CodeEditorView codeEditor;
    private TextView tvProjectTitle;
    private TextView tvActiveTab;
    private LinearLayout btnFloatingSave;
    private MaterialButton btnInstallTop;
    private LinearLayout layoutSymbolsContainer;

    private Project currentProject;
    private AndroidBuilder androidBuilder;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentProject = (Project) getIntent().getSerializableExtra("project");
        if (currentProject == null) {
            finish();
            return;
        }

        preferenceManager = new PreferenceManager(this);
        androidBuilder = new AndroidBuilder(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        fileExplorerView = findViewById(R.id.file_explorer_view);
        codeEditor = findViewById(R.id.code_editor);
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvActiveTab = findViewById(R.id.tv_active_tab);
        btnFloatingSave = findViewById(R.id.layout_floating_save);
        btnInstallTop = findViewById(R.id.btn_install_top);
        layoutSymbolsContainer = findViewById(R.id.layout_symbols_container);

        tvProjectTitle.setText(currentProject.getName());

        codeEditor.applyPreferences(preferenceManager);

        codeEditor.setOnModifiedListener(isModified -> {
            if (btnFloatingSave != null) {
                btnFloatingSave.setVisibility(isModified ? View.VISIBLE : View.GONE);
            }
        });

        btnFloatingSave.setOnClickListener(v -> {
            if (codeEditor.saveCurrentFile()) {
                btnFloatingSave.setVisibility(View.GONE);
                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
            }
        });

        if (btnInstallTop != null) {
            btnInstallTop.setOnClickListener(v -> {
                File debugApk = currentProject.getDebugApkFile();
                if (debugApk != null && debugApk.exists()) {
                    ApkInstaller.installApk(EditorActivity.this, debugApk);
                } else {
                    Toast.makeText(EditorActivity.this, "Compiled APK not found. Tap BUILD APK to compile.", Toast.LENGTH_SHORT).show();
                    refreshApkInstallButton();
                }
            });
        }

        ImageButton btnHamburger = findViewById(R.id.btn_hamburger);
        btnHamburger.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.btn_undo).setOnClickListener(v -> codeEditor.undo());
        findViewById(R.id.btn_redo).setOnClickListener(v -> codeEditor.redo());

        MaterialButton btnRun = findViewById(R.id.btn_run);
        btnRun.setOnClickListener(v -> runBuild());

        ImageButton btnOverflow = findViewById(R.id.btn_overflow_menu);
        btnOverflow.setOnClickListener(this::showPopupMenu);

        fileExplorerView.setProjectRoot(currentProject.getRootDir(), file -> {
            if (file.isFile()) {
                codeEditor.openFile(file);
                tvActiveTab.setText(file.getName().toUpperCase());
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        setupQuickSymbolsBar();
        openDefaultMainFile();
        refreshApkInstallButton();
    }

    private void setupQuickSymbolsBar() {
        if (layoutSymbolsContainer == null) return;
        layoutSymbolsContainer.removeAllViews();
        String[] symbols = {"TAB", "{", "}", "(", ")", "[", "]", ";", "\"", "'", "=", ".", ",", "<", ">", "/", "_", ":", "+", "-"};
        for (String sym : symbols) {
            Button btn = new Button(this, null, android.R.attr.borderlessButtonStyle);
            btn.setText(sym);
            btn.setTextSize(13f);
            btn.setTypeface(Typeface.MONOSPACE);
            btn.setTextColor(Color.parseColor("#374151"));
            btn.setPadding(16, 4, 16, 4);
            btn.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(2, 2, 2, 2);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                if (sym.equals("TAB")) {
                    codeEditor.insertSymbol("    ");
                } else {
                    codeEditor.insertSymbol(sym);
                }
            });
            layoutSymbolsContainer.addView(btn);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeEditor.applyPreferences(preferenceManager);
        refreshApkInstallButton();
    }

    private void refreshApkInstallButton() {
        if (btnInstallTop != null && currentProject != null) {
            File debugApk = currentProject.getDebugApkFile();
            boolean apkExists = debugApk != null && debugApk.exists() && debugApk.length() > 0;
            btnInstallTop.setVisibility(apkExists ? View.VISIBLE : View.GONE);
        }
    }

    private void openDefaultMainFile() {
        File mainActivity = new File(currentProject.getAppDir(), "src/main/java/" + currentProject.getPackageName().replace('.', '/') + "/MainActivity.java");
        if (mainActivity.exists()) {
            codeEditor.openFile(mainActivity);
            tvActiveTab.setText("MAINACTIVITY.JAVA");
        }
    }

    private void showPopupMenu(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_editor_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setElevation(12f);

        popupView.findViewById(R.id.item_build_ai).setOnClickListener(v -> {
            Intent intent = new Intent(this, BuildAIActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_java_file).setOnClickListener(v -> {
            promptCreateJavaClass();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_res_file).setOnClickListener(v -> {
            promptCreateResourceFile();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_assets_file).setOnClickListener(v -> {
            promptCreateAssetFile();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_jni_file).setOnClickListener(v -> {
            promptCreateJniFile();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_lib_file).setOnClickListener(v -> {
            openLibPicker();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.item_local_lib).setOnClickListener(v -> {
            openLibPicker();
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -100, 0, Gravity.END);
    }

    private void promptCreateJavaClass() {
        EditText input = new EditText(this);
        input.setHint("ClassName.java");
        new AlertDialog.Builder(this)
                .setTitle("Create Java Class")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (!name.endsWith(".java")) name += ".java";
                        File javaDir = new File(currentProject.getAppDir(), "src/main/java/" + currentProject.getPackageName().replace('.', '/'));
                        javaDir.mkdirs();
                        File newFile = new File(javaDir, name);
                        String className = name.substring(0, name.length() - 5);
                        String content = "package " + currentProject.getPackageName() + ";\n\npublic class " + className + " {\n\n}\n";
                        try {
                            FileUtils.writeStringToFile(newFile, content);
                            codeEditor.openFile(newFile);
                            tvActiveTab.setText(name.toUpperCase());
                            fileExplorerView.refresh();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error creating file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void promptCreateResourceFile() {
        EditText input = new EditText(this);
        input.setHint("layout_name.xml");
        new AlertDialog.Builder(this)
                .setTitle("Create Resource File")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (!name.endsWith(".xml")) name += ".xml";
                        File resDir = new File(currentProject.getAppDir(), "src/main/res/layout");
                        resDir.mkdirs();
                        File newFile = new File(resDir, name);
                        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:orientation=\"vertical\">\n\n</LinearLayout>\n";
                        try {
                            FileUtils.writeStringToFile(newFile, content);
                            codeEditor.openFile(newFile);
                            tvActiveTab.setText(name.toUpperCase());
                            fileExplorerView.refresh();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error creating file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void promptCreateAssetFile() {
        EditText input = new EditText(this);
        input.setHint("filename.ext");
        new AlertDialog.Builder(this)
                .setTitle("Create Asset File")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        File assetDir = new File(currentProject.getAppDir(), "src/main/assets");
                        assetDir.mkdirs();
                        File newFile = new File(assetDir, name);
                        try {
                            FileUtils.writeStringToFile(newFile, "");
                            codeEditor.openFile(newFile);
                            tvActiveTab.setText(name.toUpperCase());
                            fileExplorerView.refresh();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void promptCreateJniFile() {
        EditText input = new EditText(this);
        input.setHint("native-lib.cpp");
        new AlertDialog.Builder(this)
                .setTitle("Create JNI File")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        File jniDir = new File(currentProject.getAppDir(), "src/main/cpp");
                        jniDir.mkdirs();
                        File newFile = new File(jniDir, name);
                        try {
                            FileUtils.writeStringToFile(newFile, "#include <jni.h>\n");
                            codeEditor.openFile(newFile);
                            tvActiveTab.setText(name.toUpperCase());
                            fileExplorerView.refresh();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void openLibPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_IMPORT_LIB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMPORT_LIB && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                File libsDir = new File(currentProject.getAppDir(), "libs");
                libsDir.mkdirs();
                File target = new File(libsDir, "imported_lib_" + System.currentTimeMillis() + ".jar");
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
                Toast.makeText(this, "Library added to libs/", Toast.LENGTH_SHORT).show();
                fileExplorerView.refresh();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to import library: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void runBuild() {
        if (preferenceManager.isAutoSaveBeforeBuild()) {
            codeEditor.saveCurrentFile();
            if (btnFloatingSave != null) btnFloatingSave.setVisibility(View.GONE);
        }

        Dialog dialog = new Dialog(this, R.style.BuildStudioDialogTheme);
        dialog.setContentView(R.layout.dialog_build_log);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvOutput = dialog.findViewById(R.id.tv_terminal_output);
        ScrollView scrollLogs = dialog.findViewById(R.id.scroll_logs);
        MaterialButton btnInstall = dialog.findViewById(R.id.btn_install_apk);
        MaterialButton btnCopy = dialog.findViewById(R.id.btn_copy_logs);
        MaterialButton btnDone = dialog.findViewById(R.id.btn_dialog_done);

        StringBuilder logBuilder = new StringBuilder();

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData cd = ClipData.newPlainText("Build Logs", logBuilder.toString());
            cm.setPrimaryClip(cd);
            Toast.makeText(EditorActivity.this, "Logs copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnDone.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> refreshApkInstallButton());

        dialog.show();

        androidBuilder.buildAsync(currentProject, new AndroidBuilder.BuildCallback() {
            @Override
            public void onLog(String log) {
                logBuilder.append(log).append("\n");
                tvOutput.setText(logBuilder.toString());
                if (scrollLogs != null) {
                    scrollLogs.post(() -> scrollLogs.fullScroll(View.FOCUS_DOWN));
                }
            }

            @Override
            public void onComplete(boolean success, File apkFile) {
                refreshApkInstallButton();
                if (success && apkFile != null && apkFile.exists()) {
                    btnInstall.setVisibility(View.VISIBLE);
                    btnInstall.setOnClickListener(v -> ApkInstaller.installApk(EditorActivity.this, apkFile));
                    ApkInstaller.installApk(EditorActivity.this, apkFile);
                }
            }
        });
    }
}
