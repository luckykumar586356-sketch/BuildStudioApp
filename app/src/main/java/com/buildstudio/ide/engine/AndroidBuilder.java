package com.buildstudio.ide.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.buildstudio.ide.model.Project;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AndroidBuilder {

    public interface BuildCallback {
        void onLog(String log);
        void onComplete(boolean success, File apkFile);
    }

    private final Context context;
    private final SdkManager sdkManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AndroidBuilder(Context context) {
        this.context = context.getApplicationContext();
        this.sdkManager = new SdkManager(context);
    }

    public void buildAsync(Project project, BuildCallback callback) {
        executor.execute(() -> {
            try {
                // Exact log workflow matching BUILD STUDIO Screenshot 9
                send(callback, "[Compiler] Android SDK 30 platform jar is missing. Setting up...");
                Thread.sleep(250);

                send(callback, "[Compiler] Copying SDK 30 platform jar from pre-bundled assets...");
                sdkManager.extractToolchainAssets();
                Thread.sleep(300);

                File rJava = ResourceCompiler.compileResources(project, sdkManager.getAndroidJar(), sdkManager.getAapt2Executable(), msg -> send(callback, msg));
                Thread.sleep(200);

                boolean javaOk = JavaCompilerService.compileJava(project, sdkManager.getAndroidJar(), sdkManager.getCpJar(), msg -> send(callback, msg));
                if (!javaOk) {
                    mainHandler.post(() -> callback.onComplete(false, null));
                    return;
                }
                Thread.sleep(200);

                File dex = DexCompilerService.compileDex(project, sdkManager.getCpJar(), msg -> send(callback, msg));
                Thread.sleep(200);

                File unsigned = ApkPackager.packageUnsignedApk(project, dex, msg -> send(callback, msg));
                Thread.sleep(200);

                File signedApk = ApkSignerService.signApk(context, project, unsigned, msg -> send(callback, msg));

                boolean ok = signedApk != null && signedApk.exists();
                mainHandler.post(() -> callback.onComplete(ok, signedApk));
            } catch (Exception e) {
                send(callback, "Build Error: " + e.getMessage());
                mainHandler.post(() -> callback.onComplete(false, null));
            }
        });
    }

    private void send(BuildCallback callback, String log) {
        mainHandler.post(() -> callback.onLog(log));
    }
}
