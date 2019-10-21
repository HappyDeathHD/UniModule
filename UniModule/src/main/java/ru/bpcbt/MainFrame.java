package ru.bpcbt;

import ru.bpcbt.logger.ReportPanel;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.navigator.NavigatorPanel;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.settings.SettingsPanel;
import ru.bpcbt.utils.Const;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public static final int INPUTS_TAB = 0;
    public static final int MODULES_TAB = 1;
    public static final int OUTPUTS_TAB = 2;
    public static final int REPORT_TAB = 3;
    public static final int SETTINGS_TAB = 4;

    private JTabbedPane tabbedPane;
    private NavigatorPanel inputFilesPanel;
    private NavigatorPanel modulesPanel;
    private NavigatorPanel outputFilesPanel;
    private SettingsPanel settingsPanel;

    MainFrame() {
        setTitle("UniModule 0.0.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tabbedPane = new JTabbedPane();
        setLayout(new BorderLayout());

        inputFilesPanel = new NavigatorPanel(Settings.INPUT_DIR);
        tabbedPane.addTab("Скелеты", inputFilesPanel);
        modulesPanel = new NavigatorPanel(Settings.MODULE_DIR);
        tabbedPane.addTab("Плоть", modulesPanel);
        outputFilesPanel = new NavigatorPanel(Settings.OUTPUT_DIR);
        tabbedPane.addTab("Результаты", outputFilesPanel);
        ReportPanel reportPanel = new ReportPanel();
        tabbedPane.addTab("Отчет", reportPanel);
        settingsPanel = new SettingsPanel();
        tabbedPane.addTab("Настройки", settingsPanel);
        tabbedPane.addTab("Информация", getInfoPanel());
        add(tabbedPane, BorderLayout.CENTER);
        add(Narrator.getLabel(), BorderLayout.PAGE_END);
        setMinimumSize(new Dimension(600, 600));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    /*Getters & Setters*/
    public void setPaneTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    public NavigatorPanel getInputFilesPanel() {
        return inputFilesPanel;
    }

    public NavigatorPanel getModulesPanel() {
        return modulesPanel;
    }

    public NavigatorPanel getOutputFilesPanel() {
        return outputFilesPanel;
    }

    private JPanel getInfoPanel() {
        JPanel hintsPanel = new JPanel();
        final StringBuilder hints = new StringBuilder("<html><h1>Разделители</h1><table>");
        for (Delimiters delimiter : Delimiters.values()) {
            hints.append("<tr><td>").append(delimiter.getSymbol()).append("</td><td>").append(delimiter.getDescription()).append("</td></tr>");
        }
        hints.append("</table><h1>Маппинг названий шаблонов/схем/заголовков писем</h1>")
                .append("В корневой папке со скелетами может быть файл ").append(Const.TEMPLATE_MAPPING_FILE).append(" со структурой:")
                .append("<pre>{<br/> \"НАЗВАНИЕ_ПАПКИ\": {<br/>  \"name\":\"НАЗВАНИЕ_ОБЩЕЙ_СХЕМЫ\",<br/>")
                .append("  \"topics\": {<br/>   \"ЯЗЫК(ru/en/...)\":\"ТЕМА_ПИСЬМА\"<br/>  }<br/> }<br/>}</pre></html>");
        hintsPanel.add(new JLabel(hints.toString()));
        return hintsPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
}
