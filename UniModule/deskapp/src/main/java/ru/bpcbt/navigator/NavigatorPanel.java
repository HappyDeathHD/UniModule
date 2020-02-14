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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;

public class NavigatorPanel extends BaseNavigatorTreePanel {

    private final JTextPane display;
    private File currentFile;
    private String workingDir;
    private boolean isChanged = false;

    public NavigatorPanel(Settings workingDirType) {
        super(workingDirType);
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
        //единение!
        addSplittedScrollAndContent(contentPanel);

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

    public void repaintTextToDisplay() {
        setColoredTextToDisplay(display.getText());
    }

    @Override
    public void setFontToElements(Font font) {
        super.setFontToElements(font);
        display.setFont(font);
    }

    @Override
    public void refreshFiles() {
        super.refreshFiles();
        workingDir = Program.getProperties().get(getWorkingDirType());
    }

    public void saveCurrentFile() {
        if (FileUtils.createFile(currentFile.getPath(), display.getText())) {
            changesDetected(false);
            Narrator.success("Файл \"" + currentFile.getName() + "\" успешно сохранен!");
        } else {
            Narrator.error("Не удалось сохранить файл \"" + currentFile.getName() + "\"");
        }
    }

    private void changesDetected(boolean flag) {
        if (isChanged != flag) {
            isChanged = flag;
            buttonsPanel.setEnabledToSaveButton(flag);
        }
    }
}
