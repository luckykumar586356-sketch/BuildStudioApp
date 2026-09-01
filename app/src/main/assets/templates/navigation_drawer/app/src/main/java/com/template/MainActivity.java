package com.template;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ImageButton btnMenu;
    private LinearLayout drawerPane;
    private View drawerOverlay;
    private TextView menuItem1, menuItem2, menuItem3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnMenu = findViewById(R.id.btn_menu);
        drawerPane = findViewById(R.id.drawer_pane);
        drawerOverlay = findViewById(R.id.drawer_overlay);
        menuItem1 = findViewById(R.id.menu_item1);
        menuItem2 = findViewById(R.id.menu_item2);
        menuItem3 = findViewById(R.id.menu_item3);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });

        drawerOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
            }
        });

        menuItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Home Selected", Toast.LENGTH_SHORT).show();
                closeDrawer();
            }
        });

        menuItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Settings Selected", Toast.LENGTH_SHORT).show();
                closeDrawer();
            }
        });

        menuItem3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "About Selected", Toast.LENGTH_SHORT).show();
                closeDrawer();
            }
        });
    }

    private void openDrawer() {
        if (drawerPane != null) drawerPane.setVisibility(View.VISIBLE);
        if (drawerOverlay != null) drawerOverlay.setVisibility(View.VISIBLE);
    }

    private void closeDrawer() {
        if (drawerPane != null) drawerPane.setVisibility(View.GONE);
        if (drawerOverlay != null) drawerOverlay.setVisibility(View.GONE);
    }
}
