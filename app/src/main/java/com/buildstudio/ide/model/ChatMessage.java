package com.buildstudio.ide.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private final String text;
    private final boolean isUser;
    private final String codeSnippet;

    public ChatMessage(String text, boolean isUser, String codeSnippet) {
        this.text = text;
        this.isUser = isUser;
        this.codeSnippet = codeSnippet;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }
}
