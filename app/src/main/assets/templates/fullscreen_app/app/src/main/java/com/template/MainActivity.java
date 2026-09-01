package com.template;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean isImmersive = true;
    private View decorView;
    private Button btnToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        decorView = getWindow().getDecorView();
        applyFullscreen();

        btnToggle = findViewById(R.id.btn_toggle);
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImmersive = !isImmersive;
                if (isImmersive) {
                    applyFullscreen();
                    Toast.makeText(MainActivity.this, "Immersive View On", Toast.LENGTH_SHORT).show();
                } else {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    Toast.makeText(MainActivity.this, "Standard View On", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFullscreen() {
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
