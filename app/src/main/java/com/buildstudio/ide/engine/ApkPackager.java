package com.buildstudio.ide.engine;

import com.buildstudio.ide.model.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ApkPackager {
    public interface LogCallback {
        void log(String message);
    }

    public static File packageUnsignedApk(Project project, File classesDex, LogCallback callback) throws IOException {
        callback.log("[ApkBuilder] Packaging project resources and DEX...");
        File intermediateDir = new File(project.getAppDir(), "build/intermediates/apk");
        if (!intermediateDir.exists() && !intermediateDir.mkdirs()) {
            throw new IOException("Cannot create APK output directory");
        }
        File resourcesApk = new File(project.getAppDir(), "build/intermediates/res/resources.ap_");
        if (!resourcesApk.exists()) throw new IOException("Compiled resources package is missing");
        if (classesDex == null || !classesDex.exists()) throw new IOException("Compiled DEX is missing");

        File rawUnsignedApk = new File(intermediateDir, "app-unsigned-raw.apk");
        Set<String> entries = new HashSet<>();
        try (ZipInputStream input = new ZipInputStream(new FileInputStream(resourcesApk));
             ZipOutputStream output = new ZipOutputStream(new FileOutputStream(rawUnsignedApk))) {
            ZipEntry entry;
            byte[] buffer = new byte[16 * 1024];
            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().startsWith("META-INF/")) continue;
                if (!entries.add(entry.getName())) continue;
                byte[] data = readStream(input, buffer);
                ZipEntry copy = new ZipEntry(entry.getName());
                if (mustBeStored(entry.getName())) configureStored(copy, data);
                output.putNextEntry(copy);
                output.write(data);
                output.closeEntry();
            }
            byte[] dexData;
            try (FileInputStream dex = new FileInputStream(classesDex)) {
                dexData = readStream(dex, buffer);
            }
            ZipEntry dexEntry = new ZipEntry("classes.dex");
            configureStored(dexEntry, dexData);
            output.putNextEntry(dexEntry);
            output.write(dexData);
            output.closeEntry();
        }

        callback.log("[ApkBuilder] Aligning APK entries (4-byte ZipAlign for Android OS compatibility)...");
        File alignedUnsignedApk = new File(intermediateDir, "app-unsigned-aligned.apk");
        try {
            ZipAligner.align(rawUnsignedApk, alignedUnsignedApk);
            rawUnsignedApk.delete();
            callback.log("[ApkBuilder] ZipAlign 4-byte boundary verified");
            return alignedUnsignedApk;
        } catch (Exception e) {
            callback.log("[ApkBuilder] ZipAlign warning: " + e.getMessage() + " (falling back to raw package)");
            return rawUnsignedApk;
        }
    }

    private static boolean mustBeStored(String name) {
        return name.equals("resources.arsc") || name.matches("classes\\d*\\.dex") || name.startsWith("lib/");
    }

    private static void configureStored(ZipEntry entry, byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        entry.setCrc(crc.getValue());
    }

    private static byte[] readStream(java.io.InputStream input, byte[] buffer) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }
}
