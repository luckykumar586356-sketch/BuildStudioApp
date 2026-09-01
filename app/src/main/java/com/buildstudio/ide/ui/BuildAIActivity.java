package com.buildstudio.ide.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buildstudio.ide.R;
import com.buildstudio.ide.model.ChatMessage;
import com.buildstudio.ide.model.Project;
import com.buildstudio.ide.util.FileUtils;
import com.buildstudio.ide.util.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildAIActivity extends AppCompatActivity {

    private Project currentProject;
    private PreferenceManager preferenceManager;
    private RecyclerView rvChat;
    private EditText etInput;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_ai);

        currentProject = (Project) getIntent().getSerializableExtra("project");
        preferenceManager = new PreferenceManager(this);

        TextView tvSubtitle = findViewById(R.id.tv_ai_subtitle);
        tvSubtitle.setText(preferenceManager.getAiModel());

        findViewById(R.id.btn_back_ai).setOnClickListener(v -> finish());
        findViewById(R.id.btn_clear_chat).setOnClickListener(v -> {
            messages.clear();
            chatAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
        });

        rvChat = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_chat_input);
        ImageButton btnSend = findViewById(R.id.btn_send_message);

        chatAdapter = new ChatAdapter();
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        messages.add(new ChatMessage("Hello! I am Build AI (" + preferenceManager.getAiModel() + "). How can I help you build or customize your Android app today?", false, null));
        chatAdapter.notifyDataSetChanged();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String prompt = etInput.getText().toString().trim();
        if (prompt.isEmpty()) return;

        messages.add(new ChatMessage(prompt, true, null));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        etInput.setText("");

        mainHandler.postDelayed(() -> {
            String lower = prompt.toLowerCase();
            String reply;
            String snippet = null;

            if (lower.contains("button") || lower.contains("click")) {
                reply = "Here is a code snippet to handle Button clicks:";
                snippet = "Button myButton = findViewById(R.id.btn_menu);\nmyButton.setOnClickListener(v -> {\n    Toast.makeText(this, \"Button clicked!\", Toast.LENGTH_SHORT).show();\n});";
            } else if (lower.contains("toast") || lower.contains("message")) {
                reply = "You can display a Toast message using:";
                snippet = "Toast.makeText(this, \"Hello from Build Studio!\", Toast.LENGTH_LONG).show();";
            } else if (lower.contains("dark mode") || lower.contains("theme")) {
                reply = "To toggle dark theme programmatically in Android:";
                snippet = "AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);";
            } else if (lower.contains("activity") || lower.contains("screen")) {
                reply = "To navigate to another Activity:";
                snippet = "Intent intent = new Intent(this, SecondActivity.class);\nstartActivity(intent);";
            } else {
                reply = "I analyzed your request for project  + (currentProject != null ? currentProject.getName() : app) + . I can write Java logic, design XML layouts, or fix compiler issues. What would you like to build?";
            }

            messages.add(new ChatMessage(reply, false, snippet));
            chatAdapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);
        }, 500);
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ChatViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            if (msg.isUser()) {
                holder.layoutUser.setVisibility(View.VISIBLE);
                holder.layoutAi.setVisibility(View.GONE);
                holder.tvUser.setText(msg.getText());
            } else {
                holder.layoutUser.setVisibility(View.GONE);
                holder.layoutAi.setVisibility(View.VISIBLE);
                holder.tvAi.setText(msg.getText());

                if (msg.getCodeSnippet() != null && currentProject != null) {
                    holder.btnApply.setVisibility(View.VISIBLE);
                    holder.btnApply.setOnClickListener(v -> applyCodeToProject(msg.getCodeSnippet()));
                } else {
                    holder.btnApply.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            View layoutUser, layoutAi;
            TextView tvUser, tvAi, btnApply;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutUser = itemView.findViewById(R.id.layout_user_msg);
                layoutAi = itemView.findViewById(R.id.layout_ai_msg);
                tvUser = itemView.findViewById(R.id.tv_user_text);
                tvAi = itemView.findViewById(R.id.tv_ai_text);
                btnApply = itemView.findViewById(R.id.btn_apply_code);
            }
        }
    }

    private void applyCodeToProject(String snippet) {
        if (currentProject == null) return;
        File mainActivity = currentProject.getMainActivityFile();
        if (mainActivity.exists()) {
            try {
                String existing = FileUtils.readFileToString(mainActivity);
                if (existing.contains("super.onCreate(savedInstanceState);")) {
                    String updated = existing.replace("super.onCreate(savedInstanceState);", "super.onCreate(savedInstanceState);\n        // Added by Build AI\n        " + snippet);
                    FileUtils.writeStringToFile(mainActivity, updated);
                    Toast.makeText(this, "Code applied to MainActivity.java", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to apply code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
