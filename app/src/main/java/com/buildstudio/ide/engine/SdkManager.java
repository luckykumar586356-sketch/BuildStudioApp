package com.buildstudio.ide.engine;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SdkManager {
    private final Context context;
    private final File toolchainDir;
    private final File androidJar;
    private final File cpJar;
    private final File aapt2Executable;
    private final File testKeyPk8;
    private final File testKeyCert;

    public SdkManager(Context context) {
        this.context = context.getApplicationContext();
        this.toolchainDir = new File(context.getFilesDir(), "toolchain");
        this.toolchainDir.mkdirs();
        this.androidJar = new File(toolchainDir, "android.jar");
        File compilerCacheDir = new File(context.getCodeCacheDir(), "buildstudio-compiler");
        compilerCacheDir.mkdirs();
        this.cpJar = new File(compilerCacheDir, "cp-android-v6.jar");
        this.aapt2Executable = new File(toolchainDir, "aapt2");
        this.testKeyPk8 = new File(toolchainDir, "testkey.pk8");
        this.testKeyCert = new File(toolchainDir, "testkey.x509.pem");
        extractEssentialToolchain();
    }

    public void extractEssentialToolchain() {
        if (!androidJar.exists() || androidJar.length() == 0) {
            extractAsset("platforms/android-30/android.jar", androidJar);
        }
        if (!cpJar.exists() || cpJar.length() == 0) {
            extractAsset("cp-android-v6.jar", cpJar);
        }
        if (cpJar.exists()) cpJar.setWritable(false, false);
        if (!aapt2Executable.exists() || aapt2Executable.length() == 0) {
            String abi = chooseAapt2Abi();
            extractAsset("toolchain/aapt2/" + abi, aapt2Executable);
            aapt2Executable.setExecutable(true, false);
        }
        // Always ensure modern SHA-256 test key & certificate are updated
        if (!testKeyPk8.exists() || testKeyPk8.length() != 1217) {
            extractAsset("keys/testkey.pk8", testKeyPk8);
        }
        if (!testKeyCert.exists() || testKeyCert.length() != 1367) {
            extractAsset("keys/testkey.x509.pem", testKeyCert);
        }
    }

    private String chooseAapt2Abi() {
        if (Build.SUPPORTED_ABIS != null) {
            for (String abi : Build.SUPPORTED_ABIS) {
                if (abi.equals("arm64-v8a") || abi.equals("armeabi-v7a") || abi.equals("armeabi")
                        || abi.equals("x86_64") || abi.equals("x86")) {
                    return abi;
                }
            }
        }
        return "arm64-v8a";
    }

    public void extractToolchainAssets() {
        extractEssentialToolchain();
    }

    private void extractAsset(String assetPath, File targetFile) {
        try (InputStream input = context.getAssets().open(assetPath);
             FileOutputStream output = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[16 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        } catch (IOException ignored) {
        }
    }

    public File getAndroidJar() { return androidJar; }
    public File getCpJar() { return cpJar; }
    public File getAapt2Executable() {
        File nativeAapt2 = new File(context.getApplicationInfo().nativeLibraryDir, "libaapt2.so");
        if (nativeAapt2.exists() && nativeAapt2.length() > 0) return nativeAapt2;
        return aapt2Executable;
    }
    public File getTestKeyPk8() { return testKeyPk8; }
    public File getTestKeyCert() { return testKeyCert; }
    public File getToolchainDir() { return toolchainDir; }
}
