package ru.bpcbt.logger;

import ru.bpcbt.navigator.SelectableTab;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ReportPanel extends JPanel implements SelectableTab {

    public ReportPanel() {
        setLayout(new BorderLayout());
        final JScrollPane scroll = new JScrollPane(ReportPane.getReportPane());
        final DefaultCaret caret = (DefaultCaret) ReportPane.getReportPane().getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void selectTab() { // No implementation necessary
    }
}
