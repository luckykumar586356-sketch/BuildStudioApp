package com.buildstudio.ide.engine;

import dalvik.system.DexClassLoader;

import com.buildstudio.ide.model.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaCompilerService {
    public interface LogCallback {
        void log(String message);
    }

    public static boolean compileJava(Project project, File androidJar, File cpJar, LogCallback callback) {
        callback.log("[ECJ] Compiling edited Java source...");
        File javaSrcDir = new File(project.getAppDir(), "src/main/java");
        List<File> javaFiles = new ArrayList<>();
        // AAPT2 generates the package-specific R.java used by edited sources.
        File generatedRDir = new File(project.getAppDir(), "build/generated/source/r/debug");
        collectJavaFiles(generatedRDir, javaFiles);
        collectJavaFiles(javaSrcDir, javaFiles);
        callback.log("[ECJ] Java inputs: " + javaFiles.size() + " source file(s), including generated R.java");
        if (javaFiles.isEmpty()) {
            callback.log("[ECJ] No Java source files found");
            return false;
        }

        File classesOutDir = new File(project.getAppDir(), "build/intermediates/classes/debug");
        deleteContents(classesOutDir);
        if (!classesOutDir.exists()) classesOutDir.mkdirs();

        List<String> args = new ArrayList<>();
        args.add("-1.8");
        args.add("-nowarn");
        // Annotation processors require desktop-only javax.annotation.processing APIs.
        // Normal Android projects do not use processors, so keep ECJ on its batch path.
        args.add("-proc:none");
        args.add("-d");
        args.add(classesOutDir.getAbsolutePath());
        if (androidJar != null && androidJar.exists()) {
            args.add("-cp");
            args.add(androidJar.getAbsolutePath());
        }
        for (File file : javaFiles) args.add(file.getAbsolutePath());

        if (cpJar == null || !cpJar.exists()) {
            callback.log("[ECJ] Compiler file is missing");
            return false;
        }

        try {
            File optimizedDir = new File(cpJar.getParentFile(), "ecj-dex");
            optimizedDir.mkdirs();
            DexClassLoader loader = new DexClassLoader(
                    cpJar.getAbsolutePath(), optimizedDir.getAbsolutePath(), null,
                    JavaCompilerService.class.getClassLoader());
            Class<?> ecjClass = Class.forName("org.eclipse.jdt.internal.compiler.batch.Main", true, loader);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            PrintWriter outWriter = new PrintWriter(output);
            PrintWriter errorWriter = new PrintWriter(errors);
            Method compile = ecjClass.getMethod("compile", String[].class, PrintWriter.class, PrintWriter.class, Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress", true, loader));
            Object result = compile.invoke(null, args.toArray(new String[0]), outWriter, errorWriter, null);
            outWriter.flush();
            errorWriter.flush();
            String stdout = output.toString();
            String stderr = errors.toString();
            if (!stdout.trim().isEmpty()) logDiagnostics(callback, stdout);
            if (!stderr.trim().isEmpty()) logDiagnostics(callback, stderr);
            boolean success = !(result instanceof Boolean) || (Boolean) result;
            int errorCount = countOccurrences(stdout + "\n" + stderr, "ERROR in ");
            callback.log("[ECJ] Diagnostic summary: " + errorCount + " error(s), source path and line/column details shown above");
            callback.log(success ? "[ECJ] Java compilation successful" : "[ECJ] Java compilation failed");
            return success;
        } catch (Exception e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            callback.log("[ECJ] Compiler error: " + cause.getClass().getName() + ": " + (cause.getMessage() == null ? "no details" : cause.getMessage()));
            return false;
        }
    }

    private static void logDiagnostics(LogCallback callback, String text) {
        for (String line : text.split("\\r?\\n")) {
            if (!line.trim().isEmpty()) callback.log("[ECJ] " + line);
        }
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static void collectJavaFiles(File dir, List<File> list) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) collectJavaFiles(file, list);
            else if (file.getName().endsWith(".java")) list.add(file);
        }
    }

    private static void deleteContents(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
                file.delete();
            } else {
                file.delete();
            }
        }
    }
}
