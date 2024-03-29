package ru.bpcbt.utils;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.rest.UnimessageConductor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GlobalUtils {

    private GlobalUtils() { // Utils class
    }

    public static void refreshAllFiles() {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().forceUpdateLater();
        mainFrame.getModulesPanel().forceUpdateLater();
        mainFrame.getOutputFilesPanel().forceUpdateLater();
        mainFrame.getReserveFilesPanel().forceUpdateLater();
        ReplaceTasksExecutor.refresh();
        UnimessageConductor.refresh();
    }

    public static void setNavigatorsFont(Font font) {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().setFontToElements(font);
        mainFrame.getModulesPanel().setFontToElements(font);
        mainFrame.getOutputFilesPanel().setFontToElements(font);
        mainFrame.getReserveFilesPanel().setFontToElements(font);
        ReportPane.setFontToReport(font);
    }

    public static void setEnabledToProcessButtons(boolean isEnabled) {
        MainFrame mainFrame = Program.getMainFrame();
        mainFrame.getInputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getModulesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getOutputFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getReserveFilesPanel().getButtonsPanel().setEnabledToProcessButtons(isEnabled);
        mainFrame.getReportPanel().setEnabledToStopButton(!isEnabled);
    }

    public static void emergencyBrake() {
        if (ReplaceTasksExecutor.stop()) {
            ReportPane.error("Сборка была прервана!");
        } else if (UnimessageConductor.stop()) {
            ReportPane.error("Взаимодействие с сервером было прервано!");
        } else {
            ReportPane.error("Ничего не было прервано!");
        }
        setEnabledToProcessButtons(true);
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

    public static String inputStreamToString(InputStream is) {
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();
        } catch (IOException e) {
            ReportPane.debug("Не удалось прочитать стрим", e);
        }
        return "";
    }

    public static String getJsonValue(String json, String key) {
        String valueStart = json.substring(json.indexOf("\"" + key + "\"")).substring(key.length() + 2);
        valueStart = valueStart.substring(valueStart.indexOf("\"") + 1);
        byte[] bytes = valueStart.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] != '\\' && bytes[i + 1] == '"') {
                return new String(bytes, 0, i + 1);
            }
        }
        return null;
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
