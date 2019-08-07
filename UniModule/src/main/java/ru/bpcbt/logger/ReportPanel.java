package ru.bpcbt.logger;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

public class ReportPanel extends JPanel {
    private JTextPane report;

    public ReportPanel() {
        setLayout(new BorderLayout());
        report = new JTextPane();
        JScrollPane scroll = new JScrollPane(report);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    public void appendToReport(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyleContext sc = StyleContext.getDefaultStyleContext();
            AttributeSet attributeSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, color);
            int length = report.getDocument().getLength();
            report.setCaretPosition(length);
            report.setCharacterAttributes(attributeSet, false);
            report.replaceSelection(message + System.lineSeparator());
        });
    }

    public void setFontToReport(Font font) {
        report.setFont(font);
        report.repaint();
    }

    public void clearReport() {
        report.setText("");
    }
}
