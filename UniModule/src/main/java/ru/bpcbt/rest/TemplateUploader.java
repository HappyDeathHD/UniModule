package ru.bpcbt.rest;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.utils.*;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.*;

public class TemplateUploader {
    private static UnimessageClient client;
    private static Map<String, Long> templateIdMap = new HashMap<>();
    private static Map<String, String> templateTopicMap = new HashMap<>();
    private static Map<String, String> templateNameMap = new HashMap<>();
    private static int attemptsCount;

    private TemplateUploader() { // Utils class
    }

    private static boolean checkAndPrepareConnectionSettings() {
        if (client == null) {
            String coreUrl = GlobalUtils.getProperties().get(Settings.CORE_URL).trim();
            if (coreUrl.isEmpty()) {
                Narrator.yell("Надо заполнить путь до api!");
                Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (coreUrl.endsWith("/")) {
                coreUrl = coreUrl.substring(0, coreUrl.length() - 1);
            }
            final String login = GlobalUtils.getProperties().get(Settings.USERNAME).trim();
            String password = Program.getMainFrame().getSettingsPanel().getPassword().trim();
            if (login.isEmpty()) {
                Narrator.yell("Нужно заполнить логин/пароль для подключения к api");
                Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (password.isEmpty()) {
                final String newPassword = MiniFrame.askPassword();
                if (newPassword == null) {
                    Narrator.error("Без пароля ничего делать не буду!");
                    return false;
                } else {
                    Program.getMainFrame().getSettingsPanel().setPassword(newPassword);
                    password = newPassword;
                }
            }
            client = new UnimessageClient(coreUrl, login, password);
        }
        return client.isAuth();
    }

    public static void refresh() {
        client = null;
    }

    public static SwingWorker uploadJob(List<File> files) {
        ReportPane.clearReport();
        attemptsCount = 0;
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
                if (files.isEmpty()) {
                    ReportPane.warning("Нечего грузить! Проверь вкладку результатов!");
                    Narrator.warn("Нечего грузить!");
                    return null;
                }
                if (!checkAndPrepareConnectionSettings()) {
                    Narrator.error("Не удалось получить токен, проверь данные для подключения");
                    return null;
                }
                int errorsCount = 0;
                fillTemplates();
                for (File file : files) {
                    String templateName = getTemplateName(file);
                    if (templateNameMap.containsKey(templateName)) {
                        templateName = templateNameMap.get(getTemplateName(file));
                    }
                    if (templateIdMap.containsKey(templateName)) {
                        if (!upload(templateName, file)) {
                            errorsCount++;
                        }
                    } else {
                        ReportPane.error("Схема " + templateName + " (" + file.getName() + ")" + " не найдена");
                        errorsCount++;
                    }
                }
                if (errorsCount == 0) {
                    ReportPane.success("Все схемы загружены!");
                    Narrator.success("Все схемы загружены!");
                } else {
                    String errorMessage = "Не удалось загрузить " + errorsCount + " из " + files.size() + " схем!";
                    ReportPane.error(errorMessage);
                    Narrator.error(errorMessage);
                }
                return null;
            }
        };
    }

    private static void fillTemplates() {
        final String rawJson = client.getRawTemplates();
        if (rawJson == null) {
            templateIdMap.clear();
            return;
        }
        try {
            final JsonObject obj = JsonParser.object().from("{\"key\":" + rawJson + "}");
            final JsonArray templateArray = obj.getArray("key");
            for (Object o : templateArray) {
                final JsonObject templateObj = (JsonObject) o;
                templateIdMap.put(templateObj.get("code").toString().toUpperCase(), Long.parseLong(templateObj.get("id").toString()));
            }
        } catch (JsonParserException e) {
            ReportPane.error("Ошибка в полученном json'e со схемами" + e.getMessage());
        }
        fillThemes();
    }

    private static void fillThemes() {
        final String inputDir = GlobalUtils.getProperties().get(Settings.INPUT_DIR);
        final File mappingFile = Paths.get(inputDir, Const.TEMPLATE_MAPPING_FILE).toFile();
        if (mappingFile.exists()) {
            try {
                final JsonObject mainNode = JsonParser.object().from(FileUtils.readFile(mappingFile));
                for (Map.Entry<String, Object> folderMap : mainNode.entrySet()) {
                    final String folderName = folderMap.getKey();
                    final JsonObject properties = (JsonObject) folderMap.getValue();
                    final String templateName = properties.get("name").toString();
                    templateNameMap.put(folderName, templateName);
                    final JsonObject topics = (JsonObject) properties.get("topics");
                    for (Map.Entry<String, Object> topic : topics.entrySet()) {
                        templateTopicMap.put(templateName + topic.getKey().toUpperCase(), topic.getValue().toString());
                    }
                }
            } catch (JsonParserException e) {
                ReportPane.error(Const.TEMPLATE_MAPPING_FILE + " содержит ошибку: " + e.getMessage());
            }
        } else {
            ReportPane.warning(Const.TEMPLATE_MAPPING_FILE + " не найден.");
        }
    }

    private static boolean upload(String templateName, File file) {
        final String language = getLanguage(file.getName());
        if (language == null) {
            return false;
        }
        final long templateId = templateIdMap.get(templateName);
        String topic = templateTopicMap.get(templateName + language.toUpperCase());
        if(topic == null){
            topic = file.getName();
        }

        final int httpResult = client.uploadFileToTemplate(file, templateId, language, topic);
        if (httpResult == HttpURLConnection.HTTP_OK) {
            return true;
        } else if (httpResult == HttpURLConnection.HTTP_FORBIDDEN || httpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (++attemptsCount <= 1) {
                ReportPane.warning("Похоже что-то с токеном, попробую обновить");
                if (!checkAndPrepareConnectionSettings()) {
                    return false;
                } else {
                    return upload(templateName, file);
                }
            } else {
                ReportPane.error("Идея с обновлением токена не помогла");
                return false;
            }
        } else {
            return false;
        }
    }

    private static String getLanguage(String fileName) {
        try {
            final String[] split = fileName.split("_");
            final String language = split[split.length - 1].split("\\.")[0].toUpperCase();
            if (language.length() != 2) {
                ReportPane.error("Не удалось вытащить язык из файла " + fileName + ", название должно бьть в формате имя_ru.тип:");
                return null;
            }
            return language;
        } catch (Exception e) {
            ReportPane.error("Не удалось вытащить язык из файла " + fileName + ", название должно бьть в формате имя_ru.тип:", e);
        }
        return null;
    }

    private static String getTemplateName(File file) {
        return file.getParentFile().getName().toUpperCase();
    }
}
