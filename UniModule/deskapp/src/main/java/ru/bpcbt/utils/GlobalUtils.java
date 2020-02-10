package ru.bpcbt.utils;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.rest.TemplateUploader;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("WeakerAccess")
public class GlobalUtils {

    private GlobalUtils() { //Utils class
    }

    public static void refreshAllFiles() {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().refreshFiles();
        mainFrame.getModulesPanel().refreshFiles();
        mainFrame.getOutputFilesPanel().refreshFiles();
        ReplaceTasksExecutor.refresh();
        TemplateUploader.refresh();
    }

    public static void setNavigatorsFont(Font font) {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().setFontToElements(font);
        mainFrame.getModulesPanel().setFontToElements(font);
        mainFrame.getOutputFilesPanel().setFontToElements(font);
        ReportPane.setFontToReport(font);
    }

    public static void setEnabledToProcessButtons(boolean isEnabled) {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getModulesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getOutputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
    }

    public static String getErrorMessageWithException(String message, Exception e) {
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
        return builder.toString();
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
