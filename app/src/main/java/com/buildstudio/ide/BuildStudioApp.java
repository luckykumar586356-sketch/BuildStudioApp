package com.buildstudio.ide;

import android.app.Application;
import com.buildstudio.ide.util.PreferenceManager;
import java.io.File;

public class BuildStudioApp extends Application {

    private static BuildStudioApp instance;
    private PreferenceManager preferenceManager;
    private File projectsRootDir;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferenceManager = new PreferenceManager(this);

        // Android 10+ blocks arbitrary writes to shared storage. Use the app-owned
        // external directory first, then the internal directory as a reliable fallback.
        File appExternal = getExternalFilesDir("projects");
        if (appExternal != null && (appExternal.exists() || appExternal.mkdirs())) {
            projectsRootDir = appExternal;
        } else {
            projectsRootDir = new File(getFilesDir(), "projects");
            projectsRootDir.mkdirs();
        }
    }

    public static BuildStudioApp getInstance() {
        return instance;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public File getProjectsRootDir() {
        return projectsRootDir;
    }
}
