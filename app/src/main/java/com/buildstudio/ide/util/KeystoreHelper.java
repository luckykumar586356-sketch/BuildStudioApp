package com.buildstudio.ide.util;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;

public class KeystoreHelper {
    public static final String DEBUG_KEYSTORE_NAME = "debug.keystore";
    public static final char[] DEBUG_PASSWORD = "android".toCharArray();

    public static File getOrCreateDebugKeystore(Context context) {
        File keystoreFile = new File(context.getFilesDir(), DEBUG_KEYSTORE_NAME);
        if (!keystoreFile.exists()) {
            try {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, DEBUG_PASSWORD);
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
                keyPairGen.initialize(2048, new SecureRandom());
                keyPairGen.generateKeyPair();
                try (FileOutputStream fos = new FileOutputStream(keystoreFile)) {
                    ks.store(fos, DEBUG_PASSWORD);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return keystoreFile;
    }
}
