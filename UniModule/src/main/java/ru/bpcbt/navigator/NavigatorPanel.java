package ru.bpcbt.navigator;

import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.utils.Style;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class NavigatorPanel extends JPanel {

    private Settings workingDirType;
    private JTextPane display;
    private JList<String> navigatorList;
    private List<File> fileList;
    private File currentFile;
    private boolean isChanged = false;
    private ButtonsPanel buttonsPanel;

    public NavigatorPanel(Settings workingDirType) {
        this.workingDirType = workingDirType;
        setLayout(new BorderLayout());
        //контент файлов
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        display = new JTextPane();
        display.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isChanged = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isChanged = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isChanged = true;
            }
        });
        final JScrollPane scroll = new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.add(scroll);
        //сами файлы
        navigatorList = new JList<>();
        navigatorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        navigatorList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    boolean confirmed = true;
                    if (isChanged) {
                        confirmed = MiniFrame.confirmUnsavedChanges();
                    }
                    if (confirmed) {
                        int index = list.locationToIndex(evt.getPoint());
                        currentFile = fileList.get(index);
                        buttonsPanel.getSaveB().setEnabled(true);
                        setColoredTextToDisplay(FileUtils.readFile(currentFile));
                        isChanged = false;
                    }
                }
            }
        });
        final JScrollPane scrollPane = new JScrollPane(navigatorList);
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
        final String workingDir = GlobalUtils.getProperties().get(workingDirType);
        fileList = FileUtils.getFilesByTypeRecursively(workingDir);
        final Vector<String> htmlFilesVector = fileList.stream().map(file -> FileUtils.makeTitleFromFile(file, workingDir))
                .collect(Collectors.toCollection(Vector::new));
        navigatorList.setListData(htmlFilesVector);
    }

    private void insertString(String message, SimpleAttributeSet simpleAttributeSet) {
        try {
            display.getStyledDocument().insertString(display.getStyledDocument().getLength(),
                    message + System.lineSeparator(), simpleAttributeSet);
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
            isChanged = false;
            Narrator.success("Файл \"" + currentFile.getName() + "\" успешно сохранен!");
        } else {
            Narrator.error("Не удалось сохранить файл \"" + currentFile.getName() + "\"");
        }
    }

    void openCurrentDir() {
        try {
            Desktop.getDesktop().open(new File(GlobalUtils.getProperties().get(workingDirType)));
        } catch (IOException e) {
            Narrator.error("Не удалось открыть проводник");
        }
    }

    List<File> getSelectedFiles() {
        final List<File> selectedFiles = new ArrayList<>();
        Arrays.stream(navigatorList.getSelectedIndices()).forEach(index -> selectedFiles.add(fileList.get(index)));
        return selectedFiles;
    }

    List<File> getFileList() {
        return fileList;
    }

    public String getText() {
        return display.getText();
    }

    public ButtonsPanel getButtonsPanel() {
        return buttonsPanel;
    }
}
