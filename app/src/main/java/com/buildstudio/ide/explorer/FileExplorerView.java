package com.buildstudio.ide.explorer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.buildstudio.ide.model.FileNode;
import java.io.File;

public class FileExplorerView extends LinearLayout {

    private RecyclerView rvFiles;
    private FileTreeAdapter adapter;
    private File currentProjectRoot;

    public FileExplorerView(Context context) {
        super(context);
        init(context);
    }

    public FileExplorerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FileExplorerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFFFFFFFF);
        rvFiles = new RecyclerView(context);
        rvFiles.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        rvFiles.setLayoutManager(new LinearLayoutManager(context));
        adapter = new FileTreeAdapter();
        rvFiles.setAdapter(adapter);
        addView(rvFiles);
    }

    public void setProjectRoot(File rootDir, FileTreeAdapter.OnFileClickListener listener) {
        this.currentProjectRoot = rootDir;
        FileNode rootNode = new FileNode(rootDir, 0);
        rootNode.setExpanded(true);
        rootNode.loadChildren();
        adapter.setRootNode(rootNode);
        adapter.setOnFileClickListener(listener);
    }

    public void refresh() {
        if (currentProjectRoot != null) {
            FileNode rootNode = new FileNode(currentProjectRoot, 0);
            rootNode.setExpanded(true);
            rootNode.loadChildren();
            adapter.setRootNode(rootNode);
        }
    }
}
