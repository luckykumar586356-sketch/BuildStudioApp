package com.buildstudio.ide.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "buildstudio_prefs";
    
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_CONFIRM_DELETE = "confirm_before_delete";
    private static final String KEY_FONT_SIZE = "editor_font_size";
    private static final String KEY_TAB_SIZE = "editor_tab_size";
    private static final String KEY_WORD_WRAP = "editor_word_wrap";
    private static final String KEY_LINE_NUMBERS = "editor_line_numbers";
    private static final String KEY_HIGHLIGHT_LINE = "editor_highlight_line";
    private static final String KEY_AUTO_COMPLETE = "editor_auto_complete";
    private static final String KEY_DARK_EDITOR_THEME = "editor_dark_theme";
    private static final String KEY_DEFAULT_MIN_SDK = "default_min_sdk";
    private static final String KEY_DEFAULT_TARGET_SDK = "default_target_sdk";
    private static final String KEY_AUTO_SAVE = "auto_save_before_build";
    private static final String KEY_SHOW_BUILD_LOGS = "show_build_logs";
    private static final String KEY_AI_API_KEY = "ai_api_key";
    private static final String KEY_AI_MODEL = "ai_model";
    private static final String KEY_AI_BASE_URL = "ai_base_url";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean value) {
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply();
    }

    public boolean isConfirmBeforeDelete() {
        return prefs.getBoolean(KEY_CONFIRM_DELETE, true);
    }

    public void setConfirmBeforeDelete(boolean value) {
        prefs.edit().putBoolean(KEY_CONFIRM_DELETE, value).apply();
    }

    public int getEditorFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, 14);
    }

    public void setEditorFontSize(int sp) {
        prefs.edit().putInt(KEY_FONT_SIZE, sp).apply();
    }

    public int getEditorTabSize() {
        return prefs.getInt(KEY_TAB_SIZE, 4);
    }

    public void setEditorTabSize(int spaces) {
        prefs.edit().putInt(KEY_TAB_SIZE, spaces).apply();
    }

    public boolean isWordWrap() {
        return prefs.getBoolean(KEY_WORD_WRAP, false);
    }

    public void setWordWrap(boolean value) {
        prefs.edit().putBoolean(KEY_WORD_WRAP, value).apply();
    }

    public boolean isShowLineNumbers() {
        return prefs.getBoolean(KEY_LINE_NUMBERS, true);
    }

    public void setShowLineNumbers(boolean value) {
        prefs.edit().putBoolean(KEY_LINE_NUMBERS, value).apply();
    }

    public boolean isHighlightCurrentLine() {
        return prefs.getBoolean(KEY_HIGHLIGHT_LINE, true);
    }

    public void setHighlightCurrentLine(boolean value) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_LINE, value).apply();
    }

    public boolean isAutoComplete() {
        return prefs.getBoolean(KEY_AUTO_COMPLETE, true);
    }

    public void setAutoComplete(boolean value) {
        prefs.edit().putBoolean(KEY_AUTO_COMPLETE, value).apply();
    }

    public boolean isDarkEditorTheme() {
        return prefs.getBoolean(KEY_DARK_EDITOR_THEME, false);
    }

    public void setDarkEditorTheme(boolean value) {
        prefs.edit().putBoolean(KEY_DARK_EDITOR_THEME, value).apply();
    }

    public int getDefaultMinSdk() {
        return prefs.getInt(KEY_DEFAULT_MIN_SDK, 21);
    }

    public void setDefaultMinSdk(int sdk) {
        prefs.edit().putInt(KEY_DEFAULT_MIN_SDK, sdk).apply();
    }

    public int getDefaultTargetSdk() {
        return prefs.getInt(KEY_DEFAULT_TARGET_SDK, 34);
    }

    public void setDefaultTargetSdk(int sdk) {
        prefs.edit().putInt(KEY_DEFAULT_TARGET_SDK, sdk).apply();
    }

    public boolean isAutoSaveBeforeBuild() {
        return prefs.getBoolean(KEY_AUTO_SAVE, true);
    }

    public void setAutoSaveBeforeBuild(boolean value) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply();
    }

    public boolean isShowBuildLogs() {
        return prefs.getBoolean(KEY_SHOW_BUILD_LOGS, true);
    }

    public void setShowBuildLogs(boolean value) {
        prefs.edit().putBoolean(KEY_SHOW_BUILD_LOGS, value).apply();
    }

    public String getAiApiKey() {
        return prefs.getString(KEY_AI_API_KEY, "");
    }

    public void setAiApiKey(String key) {
        prefs.edit().putString(KEY_AI_API_KEY, key).apply();
    }

    public String getAiModel() {
        return prefs.getString(KEY_AI_MODEL, "glm-4.6");
    }

    public void setAiModel(String model) {
        prefs.edit().putString(KEY_AI_MODEL, model).apply();
    }

    public String getAiBaseUrl() {
        return prefs.getString(KEY_AI_BASE_URL, "");
    }

    public void setAiBaseUrl(String url) {
        prefs.edit().putString(KEY_AI_BASE_URL, url).apply();
    }
}
