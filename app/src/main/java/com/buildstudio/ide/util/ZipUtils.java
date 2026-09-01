package com.buildstudio.ide.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
        File parent = zipFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFolder(sourceDir, sourceDir.getAbsolutePath(), zos);
        }
    }

    private static void zipFolder(File folder, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;
        byte[] buffer = new byte[8192];
        for (File file : files) {
            if (file.getName().equals(".gradle") || file.getName().equals("build")) {
                continue;
            }
            if (file.isDirectory()) {
                zipFolder(file, basePath, zos);
            } else {
                String entryPath = file.getAbsolutePath().substring(basePath.length() + 1);
                ZipEntry ze = new ZipEntry(entryPath);
                ze.setTime(file.lastModified());
                zos.putNextEntry(ze);
                try (FileInputStream fis = new FileInputStream(file)) {
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    public static void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        byte[] buffer = new byte[8192];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
