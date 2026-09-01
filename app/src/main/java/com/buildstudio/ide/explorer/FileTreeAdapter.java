package com.buildstudio.ide.explorer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.buildstudio.ide.R;
import com.buildstudio.ide.model.FileNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.ViewHolder> {

    public interface OnFileClickListener {
        void onFileClick(File file);
    }

    private final List<FileNode> displayList = new ArrayList<>();
    private FileNode rootNode;
    private OnFileClickListener listener;

    public void setRootNode(FileNode rootNode) {
        this.rootNode = rootNode;
        refreshList();
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.listener = listener;
    }

    public void refreshList() {
        displayList.clear();
        if (rootNode != null) {
            flattenTree(rootNode, displayList);
        }
        notifyDataSetChanged();
    }

    private void flattenTree(FileNode node, List<FileNode> list) {
        list.add(node);
        if (node.isDirectory() && node.isExpanded()) {
            if (node.getChildren().isEmpty()) {
                node.loadChildren();
            }
            for (FileNode child : node.getChildren()) {
                flattenTree(child, list);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_tree_node, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileNode node = displayList.get(position);

        int indentPixels = node.getDepth() * 24;
        ViewGroup.LayoutParams params = holder.viewIndent.getLayoutParams();
        params.width = indentPixels;
        holder.viewIndent.setLayoutParams(params);

        holder.tvFilename.setText(node.getName());

        if (node.isDirectory()) {
            holder.ivArrow.setVisibility(View.VISIBLE);
            holder.ivArrow.setRotation(node.isExpanded() ? 90f : 0f);
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_more);
        } else {
            holder.ivArrow.setVisibility(View.INVISIBLE);
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_edit);
        }

        holder.itemView.setOnClickListener(v -> {
            if (node.isDirectory()) {
                node.toggleExpanded();
                refreshList();
            } else if (listener != null) {
                listener.onFileClick(node.getFile());
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIndent;
        ImageView ivArrow;
        ImageView ivIcon;
        TextView tvFilename;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIndent = itemView.findViewById(R.id.view_indent);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvFilename = itemView.findViewById(R.id.tv_filename);
        }
    }
}
