package ru.bpcbt.logger;

import ru.bpcbt.utils.Style;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ReportPane extends JTextPane {
    private static final ReportPane INSTANCE = new ReportPane();
    private static final StyledDocument doc = INSTANCE.getStyledDocument();

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
        final StringBuilder builder = new StringBuilder(message);
        if (e != null) {
            builder.append(System.lineSeparator()).append(e.getClass().getCanonicalName());
            if (e.getMessage() != null) {
                builder.append(System.lineSeparator()).append(e.getMessage());
            }
            if (e.getStackTrace().length != 0) {
                builder.append(System.lineSeparator()).append(e.getStackTrace()[0]);
            }
        }
        ReportPane.error(System.lineSeparator() + builder.toString());
    }

    private static void insertString(String message, SimpleAttributeSet simpleAttributeSet) {
        try {
            doc.insertString(doc.getLength(), message + System.lineSeparator(), simpleAttributeSet);
        } catch (BadLocationException e) {
            Narrator.error("Не удалось добавить текст в отчет!");
        }
    }

    public static void setFontToReport(Font font) {
        INSTANCE.setFont(font);
        INSTANCE.repaint();
    }

    public static void clearReport() {
        INSTANCE.setText("");
    }
}
