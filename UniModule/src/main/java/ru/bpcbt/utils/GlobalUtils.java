package ru.bpcbt.utils;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.rest.TemplateUploader;
import ru.bpcbt.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GlobalUtils {

    private GlobalUtils() { //Utils class
    }

    public static MainFrame getMainFrame() {
        return Program.getMainFrame();
    }

    public static void refreshAllFiles() {
        getMainFrame().getInputFilesPanel().refreshFiles();
        getMainFrame().getModulesPanel().refreshFiles();
        getMainFrame().getOutputFilesPanel().refreshFiles();
        ReplaceTasksExecutor.refresh();
        TemplateUploader.refresh();
    }

    public static Map<Settings, String> getProperties() {
        return Program.getProperties();
    }

    public static void appendToReport(String message, Color color) {
        Program.getMainFrame().getReportPanel().appendToReport(message, color);
    }

    public static void appendToReport(String message, Exception e) {
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
        Program.getMainFrame().getReportPanel().appendToReport(System.lineSeparator() + builder.toString(), Style.RED);
    }

    public static void clearReport() {
        Program.getMainFrame().getReportPanel().clearReport();
    }

    public static void setNavigatorsFont(Font font) {
        getMainFrame().getInputFilesPanel().setFontToDisplay(font);
        getMainFrame().getModulesPanel().setFontToDisplay(font);
        getMainFrame().getOutputFilesPanel().setFontToDisplay(font);
        getMainFrame().getReportPanel().setFontToReport(font);
    }

    public static void setEnabledToProcessButtons(boolean isEnabled) {
        getMainFrame().getInputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        getMainFrame().getModulesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        getMainFrame().getOutputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
    }

    public static void makeSovietRussiaButtons() {
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
