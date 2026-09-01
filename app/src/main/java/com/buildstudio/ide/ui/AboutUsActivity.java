package com.buildstudio.ide.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.buildstudio.ide.R;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
