package com.buildstudio.ide.engine;

import com.buildstudio.ide.model.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        callback.log("[ApkBuilder] Packaging project resources and DEX with 4-byte ZipAlign...");
        File intermediateDir = new File(project.getAppDir(), "build/intermediates/apk");
        if (!intermediateDir.exists() && !intermediateDir.mkdirs()) {
            throw new IOException("Cannot create APK output directory");
        }
        File resourcesApk = new File(project.getAppDir(), "build/intermediates/res/resources.ap_");
        if (!resourcesApk.exists()) throw new IOException("Compiled resources package is missing");
        if (classesDex == null || !classesDex.exists()) throw new IOException("Compiled DEX is missing");

        File unsignedApk = new File(intermediateDir, "app-unsigned.apk");
        Set<String> entries = new HashSet<>();

        CountingOutputStream cos = new CountingOutputStream(new FileOutputStream(unsignedApk));
        try (ZipInputStream input = new ZipInputStream(new FileInputStream(resourcesApk));
             ZipOutputStream output = new ZipOutputStream(cos)) {

            ZipEntry entry;
            byte[] buffer = new byte[16 * 1024];

            while ((entry = input.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("META-INF/")) continue;
                if (!entries.add(name)) continue;

                byte[] data = readStream(input, buffer);
                ZipEntry copy = new ZipEntry(name);

                if (mustBeStored(name)) {
                    configureStored(copy, data, cos.getCount());
                } else {
                    copy.setMethod(ZipEntry.DEFLATED);
                }

                output.putNextEntry(copy);
                output.write(data);
                output.closeEntry();
            }

            // Write classes.dex
            byte[] dexData;
            try (FileInputStream dex = new FileInputStream(classesDex)) {
                dexData = readStream(dex, buffer);
            }
            ZipEntry dexEntry = new ZipEntry("classes.dex");
            configureStored(dexEntry, dexData, cos.getCount());
            output.putNextEntry(dexEntry);
            output.write(dexData);
            output.closeEntry();
        }

        callback.log("[ApkBuilder] resources.arsc/classes.dex 4-byte aligned and assembled");
        return unsignedApk;
    }

    private static boolean mustBeStored(String name) {
        return name.equals("resources.arsc") || name.matches("classes\\d*\\.dex") || name.startsWith("lib/");
    }

    private static void configureStored(ZipEntry entry, byte[] data, long currentStreamOffset) {
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        entry.setCrc(crc.getValue());

        // 4-byte ZipAlign boundary calculation for Android OS Package Manager
        byte[] nameBytes = entry.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] existingExtra = entry.getExtra();
        int existingExtraLen = existingExtra != null ? existingExtra.length : 0;

        long dataOffset = currentStreamOffset + 30 + nameBytes.length + existingExtraLen;
        int remainder = (int) (dataOffset % 4);
        if (remainder != 0) {
            int padding = 4 - remainder;
            byte[] newExtra = new byte[existingExtraLen + padding];
            if (existingExtra != null) {
                System.arraycopy(existingExtra, 0, newExtra, 0, existingExtraLen);
            }
            entry.setExtra(newExtra);
        }
    }

    private static byte[] readStream(InputStream input, byte[] buffer) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static class CountingOutputStream extends OutputStream {
        private final OutputStream out;
        private long count = 0;

        CountingOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            count += len;
        }

        public long getCount() {
            return count;
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}
