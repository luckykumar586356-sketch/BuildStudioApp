package com.buildstudio.ide.model;

import com.buildstudio.ide.R;

public enum TemplateType {
    SIMPLE_APP("Simple App", R.drawable.ic_template_simple_preview),
    FAB_APP("FAB App", R.drawable.ic_template_fab_preview),
    NAV_DRAWER("Navigation Drawer", R.drawable.ic_template_drawer_preview),
    FULLSCREEN_APP("Fullscreen App", R.drawable.ic_template_fullscreen_preview);

    private final String title;
    private final int previewResId;

    TemplateType(String title, int previewResId) {
        this.title = title;
        this.previewResId = previewResId;
    }

    public String getTitle() {
        return title;
    }

    public int getPreviewResId() {
        return previewResId;
    }
}
