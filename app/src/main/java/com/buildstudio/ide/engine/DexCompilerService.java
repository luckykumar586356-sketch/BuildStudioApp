package com.buildstudio.ide.engine;

import dalvik.system.DexClassLoader;

import com.buildstudio.ide.model.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DexCompilerService {
    public interface LogCallback {
        void log(String message);
    }

    public static File compileDex(Project project, File cpJar, LogCallback callback) {
        callback.log("[D8] Converting edited bytecode to classes.dex...");
        File classesDir = new File(project.getAppDir(), "build/intermediates/classes/debug");
        File dexOutDir = new File(project.getAppDir(), "build/intermediates/dex/debug");
        if (!dexOutDir.exists()) dexOutDir.mkdirs();
        File classesDexFile = new File(dexOutDir, "classes.dex");
        if (cpJar == null || !cpJar.exists()) {
            callback.log("[D8] Compiler file is missing");
            return null;
        }

        List<String> classFiles = new ArrayList<>();
        collectClassFiles(classesDir, classFiles);
        if (classFiles.isEmpty()) {
            callback.log("[D8] No compiled class files found");
            return null;
        }

        try {
            File optimizedDir = new File(cpJar.getParentFile(), "d8-dex");
            optimizedDir.mkdirs();
            DexClassLoader loader = new DexClassLoader(
                    cpJar.getAbsolutePath(), optimizedDir.getAbsolutePath(), null,
                    DexCompilerService.class.getClassLoader());
            Class<?> d8 = Class.forName("com.android.tools.r8.D8", true, loader);
            List<String> args = new ArrayList<>();
            args.add("--output");
            args.add(dexOutDir.getAbsolutePath());
            args.add("--min-api");
            args.add(String.valueOf(Math.max(21, project.getMinSdk())));
            args.addAll(classFiles);
            d8.getMethod("main", String[].class).invoke(null, (Object) args.toArray(new String[0]));
            if (!classesDexFile.exists() || classesDexFile.length() < 100) {
                callback.log("[D8] D8 did not produce classes.dex");
                return null;
            }
            callback.log("[D8] DEX compilation successful");
            return classesDexFile;
        } catch (Exception e) {
            callback.log("[D8] Compiler error: " + e.getMessage());
            return null;
        }
    }

    private static void collectClassFiles(File dir, List<String> list) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) collectClassFiles(file, list);
            else if (file.getName().endsWith(".class")) list.add(file.getAbsolutePath());
        }
    }
}
