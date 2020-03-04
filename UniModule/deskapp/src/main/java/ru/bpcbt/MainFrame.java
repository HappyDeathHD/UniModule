package ru.bpcbt;

import ru.bpcbt.logger.ReportPanel;
import ru.bpcbt.misc.InfoPanel;
import ru.bpcbt.navigator.NavigatorPanel;
import ru.bpcbt.navigator.ReservePanel;
import ru.bpcbt.navigator.SelectableTab;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.settings.SettingsPanel;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static final int INPUTS_TAB = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int MODULES_TAB = 1;
    public static final int OUTPUTS_TAB = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int RESERVE_TAB = 3;
    public static final int REPORT_TAB = 4;
    public static final int SETTINGS_TAB = 5;
    @SuppressWarnings("WeakerAccess")
    public static final int INFO_TAB = 6;

    private final JTabbedPane tabbedPane;

    private final NavigatorPanel inputFilesPanel;
    private final NavigatorPanel modulesPanel;
    private final NavigatorPanel outputFilesPanel;
    private final ReservePanel reserveFilesPanel;
    private final ReportPanel reportPanel;
    private final SettingsPanel settingsPanel;
    private final InfoPanel infoPanel;

    MainFrame() {
        setTitle("UniModule v" + Program.getSysProperty("version") + " built " + Program.getSysProperty("git.build.time"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Один из логгеров. Инициализируем вне очереди
        reportPanel = new ReportPanel();

        tabbedPane = new JTabbedPane();
        setLayout(new BorderLayout());
        inputFilesPanel = new NavigatorPanel(Settings.INPUT_DIR);
        tabbedPane.addTab("Скелеты", inputFilesPanel);
        modulesPanel = new NavigatorPanel(Settings.MODULE_DIR);
        tabbedPane.addTab("Плоть", modulesPanel);
        outputFilesPanel = new NavigatorPanel(Settings.OUTPUT_DIR);
        tabbedPane.addTab("Результаты", outputFilesPanel);
        reserveFilesPanel = new ReservePanel(Settings.RESERVE_DIR);
        tabbedPane.addTab("Резервация", reserveFilesPanel);
        tabbedPane.addTab("Отчет", reportPanel);
        settingsPanel = new SettingsPanel();
        tabbedPane.addTab("Настройки", settingsPanel);
        infoPanel = new InfoPanel();
        tabbedPane.addTab("Информация", infoPanel);
        add(tabbedPane, BorderLayout.CENTER);
        add(Narrator.getLabel(), BorderLayout.PAGE_END);

        tabbedPane.addChangeListener(e -> getPaneTab(tabbedPane.getSelectedIndex()).selectTab());

        setMinimumSize(new Dimension(625, 700));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void selectPaneTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    private SelectableTab getPaneTab(int index) {
        switch (index) {
            case INPUTS_TAB:
                return inputFilesPanel;
            case MODULES_TAB:
                return modulesPanel;
            case OUTPUTS_TAB:
                return outputFilesPanel;
            case RESERVE_TAB:
                return reserveFilesPanel;
            case REPORT_TAB:
                return reportPanel;
            case SETTINGS_TAB:
                return settingsPanel;
            case INFO_TAB:
                return infoPanel;
            default: // не достижимое состояние
                return settingsPanel;
        }
    }

    /*Getters & Setters*/
    public NavigatorPanel getInputFilesPanel() {
        return inputFilesPanel;
    }

    public NavigatorPanel getModulesPanel() {
        return modulesPanel;
    }

    public NavigatorPanel getOutputFilesPanel() {
        return outputFilesPanel;
    }

    public ReservePanel getReserveFilesPanel() {
        return reserveFilesPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
}
