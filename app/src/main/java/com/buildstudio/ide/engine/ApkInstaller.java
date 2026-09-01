package com.buildstudio.ide.engine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import java.io.File;

public class ApkInstaller {

    public static boolean installApk(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) return false;
        try {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authority = context.getPackageName() + ".fileprovider";
                apkUri = FileProvider.getUriForFile(context, authority, apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setClipData(android.content.ClipData.newRawUri("BuildStudio APK", apkUri));
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
