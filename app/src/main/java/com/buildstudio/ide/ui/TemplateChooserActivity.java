package com.buildstudio.ide.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.buildstudio.ide.R;
import com.buildstudio.ide.model.TemplateType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TemplateChooserActivity extends AppCompatActivity {

    private TemplateType selectedTemplate = TemplateType.NAV_DRAWER;
    private MaterialCardView cardSimple, cardFab, cardDrawer, cardFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_chooser);

        cardSimple = findViewById(R.id.card_simple_app);
        cardFab = findViewById(R.id.card_fab_app);
        cardDrawer = findViewById(R.id.card_drawer_app);
        cardFullscreen = findViewById(R.id.card_fullscreen_app);

        TextView btnExit = findViewById(R.id.btn_exit);
        MaterialButton btnNext = findViewById(R.id.btn_next);

        cardSimple.setOnClickListener(v -> select(TemplateType.SIMPLE_APP));
        cardFab.setOnClickListener(v -> select(TemplateType.FAB_APP));
        cardDrawer.setOnClickListener(v -> select(TemplateType.NAV_DRAWER));
        cardFullscreen.setOnClickListener(v -> select(TemplateType.FULLSCREEN_APP));

        select(TemplateType.NAV_DRAWER); // Default selection

        btnExit.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(TemplateChooserActivity.this, ConfigureProjectActivity.class);
            intent.putExtra("template", selectedTemplate.name());
            startActivity(intent);
        });
    }

    private void select(TemplateType type) {
        selectedTemplate = type;
        int activeBorder = getColor(R.color.primary);
        int inactiveBorder = getColor(R.color.card_border);

        cardSimple.setStrokeColor(type == TemplateType.SIMPLE_APP ? activeBorder : inactiveBorder);
        cardSimple.setStrokeWidth(type == TemplateType.SIMPLE_APP ? 6 : 2);

        cardFab.setStrokeColor(type == TemplateType.FAB_APP ? activeBorder : inactiveBorder);
        cardFab.setStrokeWidth(type == TemplateType.FAB_APP ? 6 : 2);

        cardDrawer.setStrokeColor(type == TemplateType.NAV_DRAWER ? activeBorder : inactiveBorder);
        cardDrawer.setStrokeWidth(type == TemplateType.NAV_DRAWER ? 6 : 2);

        cardFullscreen.setStrokeColor(type == TemplateType.FULLSCREEN_APP ? activeBorder : inactiveBorder);
        cardFullscreen.setStrokeWidth(type == TemplateType.FULLSCREEN_APP ? 6 : 2);
    }
}
