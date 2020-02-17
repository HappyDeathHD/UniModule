package ru.bpcbt.navigator;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseNavigatorTreePanel extends JPanel {

    private final Settings workingDirType;
    final JTree navigatorTree;
    final ButtonsPanel buttonsPanel;

    String workingDir;
    JSplitPane splitPane;

    BaseNavigatorTreePanel(Settings workingDirType) {
        this.workingDirType = workingDirType;
        setLayout(new BorderLayout());
        navigatorTree = new JTree(new DefaultMutableTreeNode());
        navigatorTree.setRootVisible(false);

        CopyHandlerAndListener copyHandlerAndListener = new CopyHandlerAndListener(navigatorTree);
        navigatorTree.setTransferHandler(copyHandlerAndListener);
        navigatorTree.addKeyListener(copyHandlerAndListener);

        buttonsPanel = new ButtonsPanel(this);
        add(buttonsPanel, BorderLayout.PAGE_END);
    }

    void addSplittedScrollAndContent(JPanel contentPanel) {
        final JScrollPane scrollPane = new JScrollPane(navigatorTree);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollPane, contentPanel);
        splitPane.setResizeWeight(0.1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
    }

    public void refreshFiles() {
        workingDir = Program.getProperties().get(workingDirType);
        if (workingDir == null) {
            Narrator.yell("Необходимо выбрать директорию в настройках");
            Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
            return;
        }
        final DefaultMutableTreeNode rootNode = addNodes(null, new File(workingDir));
        final DefaultTreeModel model = (DefaultTreeModel) navigatorTree.getModel();
        model.setRoot(rootNode);
    }

    private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode topNode, File dir) {
        final String curPath = dir.getPath();
        final DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getName());
        final String[] filesInDir = dir.list();
        List<String> sortedFiles = new ArrayList<>();
        if (filesInDir != null && filesInDir.length != 0) {
            if (topNode != null) {
                topNode.add(curDir);
            }
            sortedFiles = Arrays.asList(filesInDir);
            sortedFiles.sort(String.CASE_INSENSITIVE_ORDER);
        }
        final List<String> leafs = new ArrayList<>();
        for (String fileName : sortedFiles) {
            final File file = curPath.equals(".") ? new File(fileName) : Paths.get(curPath, fileName).toFile();
            if (file.isDirectory()) {
                addNodes(curDir, file);
            } else {
                leafs.add(file.getName());
            }
        }
        for (String leaf : leafs) {
            curDir.add(new DefaultMutableTreeNode(leaf));
        }
        return curDir;
    }

    void setFontToElements(Font font) {
        navigatorTree.setFont(font);
    }

    void openCurrentDir() {
        try {
            Desktop.getDesktop().open(new File(Program.getProperties().get(workingDirType)));
        } catch (IOException e) {
            Narrator.error("Не удалось открыть проводник");
        }
    }

    Set<File> getSelectedFiles() {
        final Set<File> selectedFiles = new HashSet<>();
        final TreePath[] selectionPaths = navigatorTree.getSelectionPaths();
        if (selectionPaths != null) {
            for (TreePath treePath : selectionPaths) {
                final Path path = Paths.get(workingDir, Arrays.stream(treePath.getPath()).skip(1).map(Object::toString)
                        .collect(Collectors.joining(File.separator)));
                final DefaultMutableTreeNode lastComponent = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                if (lastComponent.isLeaf()) {
                    selectedFiles.add(path.toFile());
                } else {
                    selectedFiles.addAll(FileUtils.getFilesByTypeRecursively(path.toString()));
                }
            }
        }
        return selectedFiles;
    }

    public void selectTab() {
        if (((DefaultMutableTreeNode) navigatorTree.getModel().getRoot()).getDepth() == 0) {
            refreshFiles();
        }
    }

    public abstract void saveCurrentFile();

    public abstract void repaintTextToDisplay();

    /*Getters & Setters*/
    public ButtonsPanel getButtonsPanel() {
        return buttonsPanel;
    }

    Settings getWorkingDirType() {
        return workingDirType;
    }
}
