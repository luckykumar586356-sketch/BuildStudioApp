package com.buildstudio.ide.engine;

import android.content.Context;

import com.android.apksig.ApkSigner;
import com.buildstudio.ide.model.Project;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;

public class ApkSignerService {
    public interface LogCallback {
        void log(String message);
    }

    public static File signApk(Context context, Project project, File unsignedApk, LogCallback callback) {
        callback.log("[ApkSigner] Signing APK with SHA-256 Android test key (V1+V2+V3 signatures)...");
        File outputDir = project.getOutputsApkDir();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            callback.log("Sign error: Cannot create APK output directory");
            return null;
        }
        File signedApk = project.getDebugApkFile();
        try {
            SdkManager sdk = new SdkManager(context);
            PrivateKey privateKey = readPrivateKey(sdk.getTestKeyPk8());
            X509Certificate certificate = readCertificate(sdk.getTestKeyCert());
            ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder(
                    "BUILD_STUDIO_KEY", privateKey, Collections.singletonList(certificate)).build();

            int minSdk = Math.max(21, project.getMinSdk());
            ApkSigner signer = new ApkSigner.Builder(Collections.singletonList(signerConfig))
                    .setInputApk(unsignedApk)
                    .setOutputApk(signedApk)
                    .setMinSdkVersion(minSdk)
                    .setV1SigningEnabled(true)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true)
                    .setV4SigningEnabled(false)
                    .setDebuggableApkPermitted(true)
                    .setCreatedBy("BUILD STUDIO IDE")
                    .build();
            signer.sign();

            if (signedApk.exists() && signedApk.length() > 0) {
                callback.log("[ApkSigner] APK signed successfully: " + signedApk.getName() + " (" + (signedApk.length() / 1024) + " KB)");
                callback.log("BUILD SUCCESSFUL");
                return signedApk;
            } else {
                callback.log("Sign error: Output APK file was not created");
                return null;
            }
        } catch (Exception e) {
            callback.log("Sign error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static PrivateKey readPrivateKey(File file) throws Exception {
        byte[] bytes = readAll(file);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static X509Certificate readCertificate(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
        }
    }

    private static byte[] readAll(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file);
             java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[16 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }
}
