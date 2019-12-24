package ru.bpcbt.logger;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    public ReportPanel() {
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(ReportPane.getReportPane());
        DefaultCaret caret = (DefaultCaret)ReportPane.getReportPane().getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }
}