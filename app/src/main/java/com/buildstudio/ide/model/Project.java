package com.buildstudio.ide.model;

import java.io.File;
import java.io.Serializable;

public class Project implements Serializable {
    private String name;
    private String packageName;
    private String rootPath;
    private TemplateType templateType;
    private int minSdk = 21;
    private int targetSdk = 35;
    private long lastModified;

    public Project() {
    }

    public Project(String name, String packageName, String rootPath, TemplateType templateType, int minSdk, int targetSdk) {
        this.name = name;
        this.packageName = packageName;
        this.rootPath = rootPath;
        this.templateType = templateType;
        this.minSdk = minSdk;
        this.targetSdk = targetSdk;
        this.lastModified = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public File getRootDir() {
        return new File(rootPath);
    }

    public File getAppDir() {
        return new File(rootPath, "app");
    }

    public File getMainJavaDir() {
        String packagePath = packageName.replace('.', File.separatorChar);
        return new File(rootPath, "app/src/main/java/" + packagePath);
    }

    public File getMainActivityFile() {
        return new File(getMainJavaDir(), "MainActivity.java");
    }

    public File getManifestFile() {
        return new File(rootPath, "app/src/main/AndroidManifest.xml");
    }

    public File getOutputsApkDir() {
        return new File(rootPath, "app/build/outputs/apk/debug");
    }

    public File getDebugApkFile() {
        return new File(getOutputsApkDir(), "app-debug.apk");
    }
}
