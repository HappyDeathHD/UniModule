package ru.bpcbt.navigator;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseNavigatorTreePanel extends JPanel implements SelectableTab {

    private final Settings workingDirType;
    final JTree navigatorTree;
    final ButtonsPanel buttonsPanel;
    private DefaultTreeModel model;

    String workingDir;
    JSplitPane splitPane;

    BaseNavigatorTreePanel(Settings workingDirType) {
        this.workingDirType = workingDirType;
        setLayout(new BorderLayout());

        navigatorTree = new JTree(model);
        navigatorTree.setRootVisible(false);
        navigatorTree.setShowsRootHandles(true);
        navigatorTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
                node.loadChildren(model, Paths.get(workingDir, Arrays.stream(path.getPath()).skip(1).map(Object::toString)
                        .collect(Collectors.joining(File.separator))).toFile());
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) { // No implementation necessary
            }
        });

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
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
    }

    public void refreshFiles() {
        workingDir = Program.getProperties().get(workingDirType);
        if (workingDir == null) {
            Narrator.yell("Необходимо выбрать директорию в настройках");
            Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
        }
        File workingFile = new File(workingDir);
        final LazyTreeNode root = new LazyTreeNode(workingFile);
        model = new DefaultTreeModel(root);
        navigatorTree.setModel(model);
        root.loadChildren(model, workingFile);
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
                final LazyTreeNode lastComponent = (LazyTreeNode) treePath.getLastPathComponent();
                if (lastComponent.isLeaf()) {
                    selectedFiles.add(path.toFile());
                } else {
                    selectedFiles.addAll(FileUtils.getFilesByTypeRecursively(path.toString()));
                }
            }
        }
        return selectedFiles;
    }

    @Override
    public void selectTab() {
        if (navigatorTree.getModel() == null) {
            refreshFiles();
        }
    }

    public void forceUpdateLater() {
        navigatorTree.setModel(null);
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
