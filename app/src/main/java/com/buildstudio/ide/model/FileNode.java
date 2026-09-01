package com.buildstudio.ide.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileNode implements Comparable<FileNode> {
    private final File file;
    private final int depth;
    private boolean isExpanded;
    private final List<FileNode> children = new ArrayList<>();

    public FileNode(File file, int depth) {
        this.file = file;
        this.depth = depth;
        this.isExpanded = depth == 0 || (depth <= 2 && file.isDirectory() && !file.getName().equals("build") && !file.getName().startsWith("."));
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public int getDepth() {
        return depth;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void loadChildren() {
        children.clear();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(".") || f.getName().equals("build")) {
                        continue;
                    }
                    children.add(new FileNode(f, depth + 1));
                }
                Collections.sort(children);
            }
        }
    }

    @Override
    public int compareTo(FileNode other) {
        if (this.isDirectory() && !other.isDirectory()) {
            return -1;
        } else if (!this.isDirectory() && other.isDirectory()) {
            return 1;
        }
        return this.getName().compareToIgnoreCase(other.getName());
    }
}
