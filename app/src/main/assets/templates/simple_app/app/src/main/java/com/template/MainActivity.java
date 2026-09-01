package com.template;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private int counter = 0;
    private TextView tvTitle;
    private TextView tvCounter;
    private Button btnIncrement;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvTitle = findViewById(R.id.tv_title);
        tvCounter = findViewById(R.id.tv_counter);
        btnIncrement = findViewById(R.id.btn_increment);
        btnReset = findViewById(R.id.btn_reset);

        btnIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                tvCounter.setText("Clicks: " + counter);
                Toast.makeText(MainActivity.this, "Counter: " + counter, Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter = 0;
                tvCounter.setText("Clicks: 0");
                Toast.makeText(MainActivity.this, "Reset Done", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
