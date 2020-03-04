package ru.bpcbt.logger;

import ru.bpcbt.Program;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.Style;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ReportPane extends JTextPane {

    private static final ReportPane INSTANCE = new ReportPane(); //ленивая инициализация не имеет смысла
    private static final StyledDocument DOC = INSTANCE.getStyledDocument();

    private ReportPane() { //Singleton
    }

    static ReportPane getReportPane() {
        return INSTANCE;
    }

    public static void success(String message) {
        insertString(message, Style.getSuccess());
    }

    public static void fine(String message) {
        insertString(message, Style.getFine());
    }

    public static void normal(String message) {
        insertString(message, null);
    }

    public static void warning(String message) {
        insertString(message, Style.getWarning());
    }

    public static void error(String message) {
        insertString(message, Style.getError());
    }

    public static void error(String message, Exception e) {
        error(System.lineSeparator() + GlobalUtils.getErrorMessageWithException(message, e));
    }

    public static void debug(String message) {
        if (Program.getMainFrame().getSettingsPanel().isDebug()) {
            insertString(message, Style.getMark());
        }
    }

    public static void debug(String message, Exception e) {
        if (Program.getMainFrame().getSettingsPanel().isDebug()) {
            debug(System.lineSeparator() + GlobalUtils.getErrorMessageWithException(message, e));
        }
    }

    private static void insertString(String message, SimpleAttributeSet simpleAttributeSet) {
        try {
            DOC.insertString(DOC.getLength(), message + System.lineSeparator(), simpleAttributeSet);
        } catch (BadLocationException e) {
            Narrator.error("Не удалось добавить текст в отчет!");
        }
    }

    public static void setFontToReport(Font font) {
        getReportPane().setFont(font);
        getReportPane().repaint();
    }

    public static void clearReport() {
        getReportPane().setText("");
    }
}
