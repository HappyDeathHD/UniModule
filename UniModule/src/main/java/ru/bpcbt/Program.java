package ru.bpcbt;

import ru.bpcbt.utils.ReplaceTasksExecutor;
import ru.bpcbt.utils.Style;
import ru.bpcbt.rest.TemplateUploader;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Program {
    private static MainFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                makeSovietRussiaButtons();
                mainFrame = new MainFrame();
                mainFrame.getSettingsPanel().loadConfigurations();
                refreshAllFiles();
            } catch (Exception e) {
                Narrator.yell("Вот это поворот!", e);
            }
        });
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    public static void refreshAllFiles() {
        mainFrame.getInputFilesPanel().refreshFiles();
        mainFrame.getModulesPanel().refreshFiles();
        mainFrame.getOutputFilesPanel().refreshFiles();
        ReplaceTasksExecutor.refresh();
        TemplateUploader.refresh();
    }

    public static Map<Settings, String> getProperties() {
        return mainFrame.getSettingsPanel().getProperties();
    }

    public static void appendToReport(String message, Color color) {
        Program.getMainFrame().getReportPanel().appendToReport(message, color);
    }

    public static void appendToReport(String message, Exception e) {
        StringBuilder builder = new StringBuilder(message);
        if (e != null) {
            builder.append(System.lineSeparator()).append(e.getClass().getCanonicalName());
            if (e.getMessage() != null) {
                builder.append(System.lineSeparator()).append(e.getMessage());
            }
            if (e.getStackTrace().length != 0) {
                builder.append(System.lineSeparator()).append(e.getStackTrace()[0]);
            }
        }
        Program.getMainFrame().getReportPanel().appendToReport(System.lineSeparator() + builder.toString(), Style.RED);
    }

    public static void clearReport() {
        Program.getMainFrame().getReportPanel().clearReport();
    }

    public static void setNavigatorsFont(Font font) {
        mainFrame.getInputFilesPanel().setFontToDisplay(font);
        mainFrame.getModulesPanel().setFontToDisplay(font);
        mainFrame.getOutputFilesPanel().setFontToDisplay(font);
        mainFrame.getReportPanel().setFontToReport(font);
    }

    public static void setEnabledToProcessButtons(boolean isEnabled) {
        mainFrame.getInputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getModulesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getOutputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
    }

    private static void makeSovietRussiaButtons() {
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.noButtonText", "Никак нет");
        UIManager.put("OptionPane.okButtonText", "Принял к сведению");
        UIManager.put("OptionPane.yesButtonText", "Так точно");

        UIManager.put("FileChooser.openButtonText", "Это мой Выбор");
        UIManager.put("FileChooser.cancelButtonText", "Я передумал");
        UIManager.put("FileChooser.lookInLabelText", "Смотреть в");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Рудимент");
        UIManager.put("FileChooser.upFolderToolTipText", "На один уровень вверх");
        UIManager.put("FileChooser.newFolderToolTipText", "Создать новую папку");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблица");
    }
}
