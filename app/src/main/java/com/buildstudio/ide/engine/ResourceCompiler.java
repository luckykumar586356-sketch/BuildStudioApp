package com.buildstudio.ide.engine;

import com.buildstudio.ide.model.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceCompiler {
    public interface LogCallback {
        void log(String message);
    }

    public static File compileResources(Project project, File androidJar, File aapt2, LogCallback callback) throws Exception {
        callback.log("[AAPT2] Compiling project resources...");
        File appDir = project.getAppDir();
        File resDir = new File(appDir, "src/main/res");
        File manifest = project.getManifestFile();
        migrateLegacyIconConflict(resDir, manifest, callback);
        File buildDir = new File(appDir, "build/intermediates/res");
        File compiledDir = new File(buildDir, "compiled");
        File javaDir = new File(appDir, "build/generated/source/r/debug");
        if (!compiledDir.exists() && !compiledDir.mkdirs()) throw new IOException("Cannot create AAPT2 directory");
        if (javaDir.exists()) clearDirectory(javaDir);
        if (!javaDir.exists() && !javaDir.mkdirs()) throw new IOException("Cannot create R.java directory");
        File resourcesApk = new File(buildDir, "resources.ap_");
        File compiledZip = new File(compiledDir, "resources.zip");

        if (aapt2 == null || !aapt2.exists() || aapt2.length() == 0) throw new IOException("AAPT2 executable is missing");
        aapt2.setExecutable(true, false);

        run(aapt2, callback, "compile", "--dir", resDir.getAbsolutePath(), "-o", compiledZip.getAbsolutePath());
        callback.log("[AAPT2] Linking resources...");
        run(aapt2, callback,
                "link",
                "-o", resourcesApk.getAbsolutePath(),
                "--manifest", manifest.getAbsolutePath(),
                "-I", androidJar.getAbsolutePath(),
                "--min-sdk-version", String.valueOf(project.getMinSdk()),
                "--target-sdk-version", String.valueOf(project.getTargetSdk()),
                "--java", javaDir.getAbsolutePath(),
                "--auto-add-overlay",
                compiledZip.getAbsolutePath());

        File rJavaFile = findGeneratedRJava(javaDir);
        if (rJavaFile == null) {
            String manifestText = readUtf8(manifest);
            callback.log("[AAPT2][ERROR][R_GENERATION] R.java was not found after a successful link");
            callback.log("[AAPT2][CONTEXT] manifest=" + manifest.getAbsolutePath());
            callback.log("[AAPT2][CONTEXT] manifestPackage=" + extractManifestPackage(manifestText));
            callback.log("[AAPT2][CONTEXT] expectedJavaOutput=" + javaDir.getAbsolutePath());
            callback.log("[AAPT2][CONTEXT] resourceDirectory=" + resDir.getAbsolutePath());
            callback.log("[AAPT2][ACTION] Check that res/ contains valid XML/resources and that manifest package matches Java package");
            throw new IOException("AAPT2 did not generate R.java; see [AAPT2][ERROR][R_GENERATION] details");
        }
        callback.log("[AAPT2] Generated R.java: " + rJavaFile.getAbsolutePath());
        if (!validateJavaResourceIds(appDir, rJavaFile, callback)) {
            throw new IOException("Resource validation failed; no APK was produced");
        }
        return rJavaFile;
    }

    private static File findGeneratedRJava(File dir) {
        if (dir == null || !dir.exists()) return null;
        File[] children = dir.listFiles();
        if (children == null) return null;
        for (File child : children) {
            if (child.isDirectory()) {
                File found = findGeneratedRJava(child);
                if (found != null) return found;
            } else if ("R.java".equals(child.getName()) && child.length() > 0) {
                return child;
            }
        }
        return null;
    }

    private static String extractManifestPackage(String manifestText) {
        Matcher matcher = Pattern.compile("package=\\\"([^\\\"]+)\\\"").matcher(manifestText == null ? "" : manifestText);
        return matcher.find() ? matcher.group(1) : "<missing>";
    }

    private static void clearDirectory(File dir) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                clearDirectory(child);
                child.delete();
            } else {
                child.delete();
            }
        }
    }

    private static boolean validateJavaResourceIds(File appDir, File rJavaFile, LogCallback callback) throws IOException {
        String rText = readUtf8(rJavaFile);
        Set<String> generatedIds = new HashSet<>();
        Matcher generated = Pattern.compile("public static (?:final )?int ([A-Za-z0-9_]+)\\s*=").matcher(rText);
        while (generated.find()) generatedIds.add(generated.group(1));
        Pattern referencePattern = Pattern.compile("R\\.id\\.([A-Za-z0-9_]+)");
        File sourceDir = new File(appDir, "src/main/java");
        List<File> sources = new ArrayList<>();
        collectFiles(sourceDir, sources, ".java");
        int missing = 0;
        Set<String> reportedMissing = new HashSet<>();
        for (File source : sources) {
            String text = readUtf8(source);
            Matcher references = referencePattern.matcher(text);
            while (references.find()) {
                String id = references.group(1);
                if (!generatedIds.contains(id) && reportedMissing.add(id)) {
                    missing++;
                    callback.log("[AAPT2][ERROR][RESOURCE_ID] " + source.getAbsolutePath() + " references R.id." + id + " but no matching @+id/" + id + " or @id/" + id + " exists in res XML");
                    callback.log("[AAPT2][FIX] Add android:id=\"@+id/" + id + "\" to the matching XML view, or change Java to one of the generated IDs: " + generatedIds);
                }
            }
        }
        if (missing > 0) {
            callback.log("[AAPT2][SUMMARY] Resource validation found " + missing + " unique missing ID reference(s); build stopped before ECJ and no APK was produced.");
            return false;
        }
        callback.log("[AAPT2] Resource ID validation passed");
        return true;
    }

    private static String readUtf8(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream input = new FileInputStream(file)) {
            int offset = 0;
            int count;
            while (offset < data.length && (count = input.read(data, offset, data.length - offset)) > 0) offset += count;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private static void collectFiles(File dir, List<File> result, String suffix) {
        if (dir == null || !dir.exists()) return;
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) collectFiles(child, result, suffix);
            else if (child.getName().endsWith(suffix)) result.add(child);
        }
    }

    private static void migrateLegacyIconConflict(File resDir, File manifest, LogCallback callback) throws IOException {
        File drawableDir = new File(resDir, "drawable");
        File legacyPng = new File(drawableDir, "app_icon.png");
        File selectedPng = new File(drawableDir, "app_icon_selected.png");
        File vectorIcon = new File(drawableDir, "app_icon.xml");
        if (legacyPng.exists() && vectorIcon.exists() && !selectedPng.exists()) {
            if (!legacyPng.renameTo(selectedPng)) throw new IOException("Cannot migrate legacy app_icon.png");
            callback.log("[AAPT2] Migrated legacy app_icon.png to app_icon_selected.png");
        }
        if (selectedPng.exists() && manifest.exists()) {
            byte[] data = new byte[(int) manifest.length()];
            try (FileInputStream input = new FileInputStream(manifest)) {
                int offset = 0;
                int count;
                while (offset < data.length && (count = input.read(data, offset, data.length - offset)) > 0) offset += count;
                String text = new String(data, StandardCharsets.UTF_8);
                if (text.contains("@drawable/app_icon\"")) {
                    text = text.replace("@drawable/app_icon\"", "@drawable/app_icon_selected\"");
                    try (FileOutputStream output = new FileOutputStream(manifest)) {
                        output.write(text.getBytes(StandardCharsets.UTF_8));
                    }
                    callback.log("[AAPT2] Updated manifest to use selected app icon");
                }
            }
        }
    }

    private static void run(File executable, LogCallback callback, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(executable.getAbsolutePath());
        for (String arg : args) command.add(arg);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) callback.log("[AAPT2] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new IOException("AAPT2 failed with exit code " + exitCode);
    }
}
