package ru.bpcbt.navigator;

import ru.bpcbt.Program;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.utils.Style;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class NavigatorPanel extends JPanel {

    private final Settings workingDirType;
    private final JTextPane display;
    private final JTree navigatorTree;
    private File currentFile;
    private boolean isChanged = false;
    private final ButtonsPanel buttonsPanel;

    public NavigatorPanel(Settings workingDirType) {
        this.workingDirType = workingDirType;
        setLayout(new BorderLayout());
        //контент файлов
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        display = new JTextPane();
        display.setEnabled(false);
        display.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changesDetected(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changesDetected(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changesDetected(true);
            }
        });
        final JScrollPane scroll = new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.add(scroll);
        //сами файлы
        final String workingDir = Program.getProperties().get(workingDirType);
        if (FileUtils.isDirExists(workingDir)) {
            navigatorTree = new JTree(addNodes(null, new File(workingDir), Program.getProperties().get(workingDirType)));
        } else {
            navigatorTree = new JTree();
        }
        navigatorTree.setRootVisible(false);
        navigatorTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                final DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) ((JTree) evt.getSource()).getLastSelectedPathComponent();
                if (node != null && evt.getClickCount() == 2 && node.isLeaf()) {
                    boolean confirmed = true;
                    if (isChanged) {
                        confirmed = MiniFrame.askForConfirmation("Все внесенные изменения канут в Лету, пофиг?");
                    }
                    if (confirmed) {
                        currentFile = Paths.get(workingDir,
                                FileUtils.separatePlaceholders(node.toString())).toFile();
                        setColoredTextToDisplay(FileUtils.readFile(currentFile));
                        changesDetected(false);
                        display.setEnabled(true);
                    }
                }
            }
        });
        final JScrollPane scrollPane = new JScrollPane(navigatorTree);
        //единение!
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollPane, contentPanel);
        splitPane.setResizeWeight(0.1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
        buttonsPanel = new ButtonsPanel(this);
        add(buttonsPanel, BorderLayout.PAGE_END);
    }

    public void refreshFiles() {
        final String workingDir = Program.getProperties().get(workingDirType);
        final DefaultMutableTreeNode rootNode = addNodes(null, new File(workingDir),
                Program.getProperties().get(workingDirType));
        final DefaultTreeModel model = (DefaultTreeModel) navigatorTree.getModel();
        model.setRoot(rootNode);
    }

    private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode topNode, File dir, String workingDir) {
        final String curPath = dir.getPath();
        final DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(FileUtils.makeTitleFromFile(dir, workingDir));
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
                addNodes(curDir, file, workingDir);
            } else {
                leafs.add(FileUtils.makeTitleFromFile(file, workingDir));
            }
        }
        for (String leaf : leafs) {
            curDir.add(new DefaultMutableTreeNode(leaf));
        }
        return curDir;
    }

    private void insertString(String message, SimpleAttributeSet simpleAttributeSet) {
        try {
            //костыль, призванный убрать бесконечное дублирование CR+LF и CR. Актуально для винды, не тестилось на других системах.
            final String crlfNormalizedMessage = message.replace(System.lineSeparator(), "\n");
            display.getStyledDocument().insertString(display.getStyledDocument().getLength(),
                    crlfNormalizedMessage, simpleAttributeSet);
        } catch (BadLocationException e) {
            Narrator.error("Не удалось вывести содержимое файла!");
        }
    }

    private void setColoredTextToDisplay(String text) {
        display.setText("");
        final String symbol = Delimiters.START_END.getSymbol();
        boolean isOdd = false;
        for (String pieceOfText : text.split(symbol)) {
            if (isOdd) {
                insertString(symbol + pieceOfText + symbol, Style.getMark());
            } else {
                insertString(pieceOfText, null);
            }
            isOdd = !isOdd;
        }
    }

    void repaintTextToDisplay() {
        setColoredTextToDisplay(display.getText());
    }

    public void setFontToDisplay(Font font) {
        display.setFont(font);
        display.repaint();
    }

    void saveCurrentFile() {
        if (FileUtils.createFile(currentFile.getPath(), display.getText())) {
            changesDetected(false);
            Narrator.success("Файл \"" + currentFile.getName() + "\" успешно сохранен!");
        } else {
            Narrator.error("Не удалось сохранить файл \"" + currentFile.getName() + "\"");
        }
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
                final DefaultMutableTreeNode lastComponent = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                if (lastComponent.isLeaf()) {
                    selectedFiles.add(Paths.get(Program.getProperties().get(workingDirType),
                            FileUtils.separatePlaceholders(lastComponent.toString())).toFile());
                } else {
                    selectedFiles.addAll(FileUtils.getFilesByTypeRecursively(Paths.get(Program.getProperties().get(workingDirType),
                            FileUtils.separatePlaceholders(lastComponent.toString())).toString()));
                }
            }
        }
        return selectedFiles;
    }

    private void changesDetected(boolean flag) {
        if (isChanged != flag) {
            isChanged = flag;
            buttonsPanel.setEnabledToSaveButton(flag);
        }
    }

    /*Getters & Setters*/
    public ButtonsPanel getButtonsPanel() {
        return buttonsPanel;
    }
}
