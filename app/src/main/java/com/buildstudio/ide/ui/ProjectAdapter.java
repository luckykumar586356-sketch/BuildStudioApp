package com.buildstudio.ide.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.buildstudio.ide.R;
import com.buildstudio.ide.model.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface Listener {
        void onOpen(Project project);
        void onLongPress(Project project);
        void onDelete(Project project);
    }

    private final List<Project> list = new ArrayList<>();
    private final Listener listener;

    public ProjectAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Project> projects) {
        list.clear();
        if (projects != null) list.addAll(projects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = list.get(position);
        holder.tvName.setText(project.getName());
        holder.tvPath.setText("Internal Storage/.BUILD STUDIO/" + project.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(project);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongPress(project);
            return true;
        });

        if (holder.btnOpen != null) {
            holder.btnOpen.setOnClickListener(v -> {
                if (listener != null) listener.onOpen(project);
            });
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(project);
            });
        }

        if (holder.btnRename != null) {
            holder.btnRename.setOnClickListener(v -> {
                if (listener != null) listener.onLongPress(project);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPath;
        Button btnOpen, btnRename, btnDelete;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_project_name);
            tvPath = itemView.findViewById(R.id.tv_project_path);
            btnOpen = itemView.findViewById(R.id.btn_open_project);
            btnRename = itemView.findViewById(R.id.btn_rename_project);
            btnDelete = itemView.findViewById(R.id.btn_delete_project);
        }
    }
}
