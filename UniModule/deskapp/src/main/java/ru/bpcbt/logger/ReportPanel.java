package ru.bpcbt.logger;

import ru.bpcbt.Program;
import ru.bpcbt.misc.HoverButton;
import ru.bpcbt.navigator.SelectableTab;
import ru.bpcbt.utils.GlobalUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.io.IOException;

public class ReportPanel extends JPanel implements SelectableTab {

    private HoverButton stopB = null;

    public ReportPanel() {
        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);
        try {
            stopB = new HoverButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/stop.png"))), "Стоп-кран");
            stopB.setEnabled(false);
            stopB.addActionListener(e -> GlobalUtils.emergencyBrake());
        } catch (IOException e) {
            Narrator.yell("Что-то пошло не так при отрисовке кнопки стоп-крана:", e);
        }
        springLayout.putConstraint(SpringLayout.SOUTH, stopB, -30, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.EAST, stopB, -30, SpringLayout.EAST, this);
        add(stopB);

        final JScrollPane scroll = new JScrollPane(ReportPane.getReportPane());
        springLayout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, scroll, 0, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.EAST, scroll, 0, SpringLayout.EAST, this);
        final DefaultCaret caret = (DefaultCaret) ReportPane.getReportPane().getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll);
    }

    public void setEnabledToStopButton(boolean isEnabled) {
        stopB.setEnabled(isEnabled);
    }

    @Override
    public void selectTab() { // No implementation necessary
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }
}
