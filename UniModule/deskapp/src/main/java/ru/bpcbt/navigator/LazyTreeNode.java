package ru.bpcbt.navigator;

import ru.bpcbt.logger.ReportPane;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LazyTreeNode extends DefaultMutableTreeNode {

    private boolean loaded = false;

    /**
     * Конструктор для папок
     *
     * @param dir папка
     */
    LazyTreeNode(File dir) {
        setUserObject(dir.getName());
        add(new LazyTreeNode("Сек..."));
    }

    /**
     * Конструктор для файлов
     *
     * @param leafName название файла
     */
    private LazyTreeNode(String leafName) {
        super(leafName);
    }

    void loadChildren(final DefaultTreeModel model, File dir) {
        if (loaded) {
            return;
        }
        SwingWorker<List<LazyTreeNode>, Void> worker = new SwingWorker<List<LazyTreeNode>, Void>() {
            @Override
            protected List<LazyTreeNode> doInBackground() {
                final String curPath = dir.getPath();
                final String[] filesInDir = dir.list();
                List<String> sortedFiles = new ArrayList<>();
                if (filesInDir != null && filesInDir.length != 0) {
                    sortedFiles = Arrays.asList(filesInDir);
                    sortedFiles.sort(String.CASE_INSENSITIVE_ORDER);
                }
                final List<LazyTreeNode> children = new ArrayList<>();
                final List<LazyTreeNode> leafs = new ArrayList<>(); // Для красоты. Папки сверху, файлы снизу
                for (String fileName : sortedFiles) {
                    final File file = curPath.equals(".") ? new File(fileName) : Paths.get(curPath, fileName).toFile();
                    if (file.isDirectory()) {
                        children.add(new LazyTreeNode(file));
                    } else {
                        leafs.add(new LazyTreeNode(file.getName()));
                    }
                }
                children.addAll(leafs);
                return children;
            }

            @Override
            protected void done() {
                try {
                    setChildren(get());
                    model.nodeStructureChanged(LazyTreeNode.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    ReportPane.error("Ошибка при генерации деревьев", e);
                }
                super.done();
            }
        };
        worker.execute();
    }

    private void setChildren(List<LazyTreeNode> children) {
        removeAllChildren();
        setAllowsChildren(children.size() > 0);
        for (MutableTreeNode node : children) {
            add(node);
        }
        loaded = true;
    }
}
