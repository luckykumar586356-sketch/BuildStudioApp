package com.buildstudio.ide.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buildstudio.ide.BuildStudioApp;
import com.buildstudio.ide.R;
import com.buildstudio.ide.model.Project;
import com.buildstudio.ide.model.TemplateType;
import com.buildstudio.ide.util.FileUtils;
import com.buildstudio.ide.util.PreferenceManager;
import com.buildstudio.ide.util.ZipUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ProjectAdapter.Listener {

    private static final int REQ_IMPORT_PROJECT = 2001;

    private TextView tvNoProjects;
    private RecyclerView rvProjects;
    private LinearLayout layoutSpeedDialItem;
    private FloatingActionButton fabMain;
    private ProjectAdapter projectAdapter;
    private PreferenceManager preferenceManager;
    private boolean isFabExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main_overflow);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (id == R.id.action_about_us) {
                startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                return true;
            } else if (id == R.id.action_import_project) {
                openImportPicker();
                return true;
            }
            return false;
        });

        tvNoProjects = findViewById(R.id.tv_no_projects);
        rvProjects = findViewById(R.id.rv_projects);
        layoutSpeedDialItem = findViewById(R.id.layout_speed_dial_item);
        fabMain = findViewById(R.id.fab_main);
        TextView btnCreateNewProjectPill = findViewById(R.id.btn_create_new_project_pill);

        projectAdapter = new ProjectAdapter(this);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(projectAdapter);

        fabMain.setOnClickListener(v -> toggleFab());
        View.OnClickListener openTemplates = v -> {
            if (isFabExpanded) toggleFab();
            startActivity(new Intent(MainActivity.this, TemplateChooserActivity.class));
        };
        layoutSpeedDialItem.setOnClickListener(openTemplates);
        btnCreateNewProjectPill.setOnClickListener(openTemplates);

        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ZArchiver / external file-manager filesystem synchronization
        loadProjects();
    }

    private void toggleFab() {
        isFabExpanded = !isFabExpanded;
        if (isFabExpanded) {
            layoutSpeedDialItem.setVisibility(View.VISIBLE);
            fabMain.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            fabMain.setRotation(45f);
        } else {
            layoutSpeedDialItem.setVisibility(View.GONE);
            fabMain.setImageResource(android.R.drawable.ic_input_add);
            fabMain.setRotation(0f);
        }
    }

    private void loadProjects() {
        File rootDir = BuildStudioApp.getInstance() == null ? null : BuildStudioApp.getInstance().getProjectsRootDir();
        List<Project> list = new ArrayList<>();
        if (rootDir != null && rootDir.exists()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File folder : files) {
                    if (!folder.isDirectory()) continue;
                    if (folder.getName().startsWith(".")) continue;
                    File appDir = new File(folder, "app");
                    File settings = new File(folder, "settings.gradle");
                    File manifest = new File(appDir, "src/main/AndroidManifest.xml");
                    if (appDir.isDirectory() || settings.exists() || manifest.exists()) {
                        list.add(readProject(folder, manifest));
                    }
                }
            }
        }
        list.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));
        projectAdapter.submitList(list);
        boolean empty = list.isEmpty();
        tvNoProjects.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvProjects.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private Project readProject(File folder, File manifest) {
        String packageName = "com." + folder.getName().toLowerCase(Locale.US).replaceAll("[^a-z0-9_]", "");
        if (packageName.equals("com.")) packageName = "com.buildstudio.project";
        long modified = Math.max(folder.lastModified(), manifest.exists() ? manifest.lastModified() : 0L);
        Project project = new Project(folder.getName(), packageName, folder.getAbsolutePath(), TemplateType.NAV_DRAWER, 21, 34);
        project.setLastModified(modified);
        return project;
    }

    private void openImportPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/zip", "application/x-zip-compressed", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQ_IMPORT_PROJECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMPORT_PROJECT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importProjectFromUri(data.getData());
        }
    }

    private void importProjectFromUri(Uri uri) {
        try {
            File rootDir = BuildStudioApp.getInstance().getProjectsRootDir();
            File tempZip = new File(getCacheDir(), "import_temp.zip");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(tempZip)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            String baseName = "ImportedProject_" + System.currentTimeMillis() % 10000;
            File targetDir = new File(rootDir, baseName);
            targetDir.mkdirs();
            ZipUtils.unzip(tempZip, targetDir);
            tempZip.delete();
            Toast.makeText(this, "Project imported successfully", Toast.LENGTH_SHORT).show();
            loadProjects();
        } catch (Exception e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onOpen(Project project) {
        if (project == null || !project.getRootDir().exists()) {
            Toast.makeText(this, "Project folder not found", Toast.LENGTH_LONG).show();
            loadProjects();
            return;
        }
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("project", project);
        startActivity(intent);
    }

    @Override
    public void onLongPress(Project project) {
        if (project == null) return;
        String[] options = {"OPEN", "CREATE BACKUP", "DELETE"};
        new AlertDialog.Builder(this)
                .setTitle(project.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        onOpen(project);
                    } else if (which == 1) {
                        createBackup(project);
                    } else if (which == 2) {
                        onDelete(project);
                    }
                })
                .show();
    }

    private void createBackup(Project project) {
        try {
            File rootDir = BuildStudioApp.getInstance().getProjectsRootDir();
            File backupsDir = new File(rootDir, "backups");
            backupsDir.mkdirs();
            File backupZip = new File(backupsDir, project.getName() + "_backup_" + System.currentTimeMillis() + ".zip");
            ZipUtils.zipDirectory(project.getRootDir(), backupZip);
            Toast.makeText(this, "Backup created: " + backupZip.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDelete(Project project) {
        if (project == null) return;
        if (preferenceManager.isConfirmBeforeDelete()) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Project?")
                    .setMessage("Delete " + project.getName() + " permanently from storage?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> executeDelete(project))
                    .show();
        } else {
            executeDelete(project);
        }
    }

    private void executeDelete(Project project) {
        if (FileUtils.deleteDirectory(project.getRootDir())) {
            Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Project delete failed", Toast.LENGTH_LONG).show();
        }
        loadProjects();
    }
}
