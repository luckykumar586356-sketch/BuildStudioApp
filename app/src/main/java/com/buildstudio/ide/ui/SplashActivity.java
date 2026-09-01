package com.buildstudio.ide.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.buildstudio.ide.R;

public class SplashActivity extends AppCompatActivity {

    private static final int REQ_MANAGE_STORAGE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkPermissionsAndProceed, 800);
    }

    private void checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQ_MANAGE_STORAGE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQ_MANAGE_STORAGE);
                }
                return;
            }
        }
        proceedToMain();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MANAGE_STORAGE) {
            proceedToMain();
        }
    }

    private void proceedToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
