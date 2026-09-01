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
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {

    private static final int REQ_IMPORT_LIB = 4001;

    private Project currentProject;
    private DrawerLayout drawerLayout;
    private CodeEditorView codeEditor;
    private FileExplorerView fileExplorerView;
    private TextView tvProjectTitle;
    private TextView tvActiveTab;
    private View btnFloatingSave;
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
        codeEditor = findViewById(R.id.code_editor);
        fileExplorerView = findViewById(R.id.file_explorer_view);
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvActiveTab = findViewById(R.id.tv_active_tab);
        btnFloatingSave = findViewById(R.id.layout_floating_save);

        tvProjectTitle.setText(currentProject.getName());

        codeEditor.applyPreferences(preferenceManager);
        codeEditor.setOnModifiedListener(isModified -> {
            if (btnFloatingSave != null) {
                btnFloatingSave.setVisibility(isModified ? View.VISIBLE : View.GONE);
            }
        });

        if (btnFloatingSave != null) {
            btnFloatingSave.setOnClickListener(v -> {
                if (codeEditor.saveCurrentFile()) {
                    Toast.makeText(EditorActivity.this, "File saved", Toast.LENGTH_SHORT).show();
                    btnFloatingSave.setVisibility(View.GONE);
                }
            });
        }

        ImageButton btnHamburger = findViewById(R.id.btn_hamburger);
        ImageButton btnUndo = findViewById(R.id.btn_undo);
        ImageButton btnRedo = findViewById(R.id.btn_redo);
        ImageButton btnFolder = findViewById(R.id.btn_folder);
        MaterialButton btnRun = findViewById(R.id.btn_run);
        ImageButton btnOverflow = findViewById(R.id.btn_overflow_menu);

        btnHamburger.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnUndo.setOnClickListener(v -> codeEditor.undo());
        btnRedo.setOnClickListener(v -> codeEditor.redo());

        btnFolder.setOnClickListener(this::showFolderPopup);
        btnOverflow.setOnClickListener(this::showOverflowMenu);

        btnRun.setOnClickListener(v -> runBuild());

        fileExplorerView.setProjectRoot(currentProject.getRootDir(), file -> {
            codeEditor.openFile(file);
            tvActiveTab.setText(file.getName().toUpperCase());
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        File mainActivity = currentProject.getMainActivityFile();
        if (mainActivity.exists()) {
            codeEditor.openFile(mainActivity);
            tvActiveTab.setText("MAINACTIVITY.JAVA");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (codeEditor != null && preferenceManager != null) {
            codeEditor.applyPreferences(preferenceManager);
        }
    }

    private void showFolderPopup(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_editor_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(16);

        popupView.findViewById(R.id.item_java_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            promptCreateJavaFile();
        });

        popupView.findViewById(R.id.item_res_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            promptCreateResourceFile();
        });

        popupView.findViewById(R.id.item_assets_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            promptCreateAssetFile();
        });

        popupView.findViewById(R.id.item_lib_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            openLibPicker();
        });

        popupView.findViewById(R.id.item_jni_file).setOnClickListener(v -> {
            popupWindow.dismiss();
            promptCreateJniFile();
        });

        popupView.findViewById(R.id.item_local_lib).setOnClickListener(v -> {
            popupWindow.dismiss();
            Toast.makeText(this, "Local Library manager", Toast.LENGTH_SHORT).show();
        });

        popupView.findViewById(R.id.item_build_ai).setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(this, BuildAIActivity.class);
            intent.putExtra("project", currentProject);
            startActivity(intent);
        });

        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.START);
    }

    private void showOverflowMenu(View anchor) {
        String[] items = {"Save File", "Close Project", "Project Settings"};
        new AlertDialog.Builder(this)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        codeEditor.saveCurrentFile();
                        Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
                        if (btnFloatingSave != null) btnFloatingSave.setVisibility(View.GONE);
                    } else if (which == 1) {
                        finish();
                    } else if (which == 2) {
                        startActivity(new Intent(this, SettingsActivity.class));
                    }
                })
                .show();
    }

    private void promptCreateJavaFile() {
        EditText input = new EditText(this);
        input.setHint("ClassName (e.g. SecondActivity)");
        new AlertDialog.Builder(this)
                .setTitle("Create Java File")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (!name.endsWith(".java")) name += ".java";
                        File javaDir = new File(currentProject.getAppDir(), "src/main/java/" + currentProject.getPackageName().replace('.', '/'));
                        javaDir.mkdirs();
                        File newFile = new File(javaDir, name);
                        String content = "package " + currentProject.getPackageName() + ";\n\nimport android.os.Bundle;\nimport androidx.appcompat.app.AppCompatActivity;\n\npublic class " + name.replace(".java", "") + " extends AppCompatActivity {\n    @Override\n    protected void onCreate(Bundle savedInstanceState) {\n        super.onCreate(savedInstanceState);\n    }\n}\n";
                        try {
                            FileUtils.writeStringToFile(newFile, content);
                            codeEditor.openFile(newFile);
                            tvActiveTab.setText(name.toUpperCase());
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
                if (success && apkFile != null && apkFile.exists()) {
                    btnInstall.setVisibility(View.VISIBLE);
                    btnInstall.setOnClickListener(v -> ApkInstaller.installApk(EditorActivity.this, apkFile));
                    ApkInstaller.installApk(EditorActivity.this, apkFile);
                }
            }
        });
    }
}
