package com.buildstudio.ide.engine;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.List;

public class ApkInstaller {

    public static boolean installApk(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            Toast.makeText(context, "APK file not found on storage", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            // Check unknown source install permissions on Android 8.0+ (Oreo+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.getPackageManager().canRequestPackageInstalls()) {
                    Toast.makeText(context, "Please allow permission to install APKs from BuildStudio", Toast.LENGTH_LONG).show();
                    Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse("package:" + context.getPackageName()))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settingsIntent);
                    return false;
                }
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Uri apkUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String[] candidateAuthorities = {
                        context.getPackageName() + ".fileprovider",
                        "com.buildstudio.ide.debug.fileprovider",
                        "com.buildstudio.ide.fileprovider"
                };
                for (String authority : candidateAuthorities) {
                    try {
                        apkUri = FileProvider.getUriForFile(context, authority, apkFile);
                        if (apkUri != null) break;
                    } catch (Exception ignored) {}
                }
                if (apkUri == null) {
                    apkUri = Uri.fromFile(apkFile);
                }
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } else {
                apkUri = Uri.fromFile(apkFile);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            }

            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Install failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
