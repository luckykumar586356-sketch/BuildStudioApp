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
        callback.log("[ApkBuilder] Packaging the edited project resources and DEX...");
        File intermediateDir = new File(project.getAppDir(), "build/intermediates/apk");
        if (!intermediateDir.exists() && !intermediateDir.mkdirs()) {
            throw new IOException("Cannot create APK output directory");
        }
        File resourcesApk = new File(project.getAppDir(), "build/intermediates/res/resources.ap_");
        if (!resourcesApk.exists()) throw new IOException("Compiled resources package is missing");
        if (classesDex == null || !classesDex.exists()) throw new IOException("Compiled DEX is missing");

        File unsignedApk = new File(intermediateDir, "app-unsigned.apk");
        Set<String> entries = new HashSet<>();
        try (ZipInputStream input = new ZipInputStream(new FileInputStream(resourcesApk));
             ZipOutputStream output = new ZipOutputStream(new FileOutputStream(unsignedApk))) {
            ZipEntry entry;
            byte[] buffer = new byte[16 * 1024];
            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().startsWith("META-INF/")) continue;
                if (!entries.add(entry.getName())) continue;
                byte[] data = readEntry(input, buffer);
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
        callback.log("[ApkBuilder] resources.arsc/classes.dex stored uncompressed for Android compatibility");
        callback.log("[ApkBuilder] Unsigned project APK assembled");
        return unsignedApk;
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

    private static byte[] readEntry(ZipInputStream input, byte[] buffer) throws IOException {
        return readStream(input, buffer);
    }

    private static byte[] readStream(java.io.InputStream input, byte[] buffer) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }
}
