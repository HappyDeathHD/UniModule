package ru.bpcbt.rest;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.utils.Style;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.Const;
import ru.bpcbt.utils.FileUtils;
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
            String coreUrl = Program.getProperties().get(Settings.CORE_URL).trim();
            if (coreUrl.isEmpty()) {
                Narrator.yell("Надо заполнить путь до api!");
                Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (coreUrl.endsWith("/")) {
                coreUrl = coreUrl.substring(0, coreUrl.length() - 1);
            }
            String login = Program.getProperties().get(Settings.USERNAME).trim();
            String password = Program.getMainFrame().getSettingsPanel().getPassword().trim();
            if (login.isEmpty()) {
                Narrator.yell("Нужно заполнить логин/пароль для подключения к api");
                Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (password.isEmpty()) {
                String newPassword = MiniFrame.askPassword();
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
        Program.clearReport();
        attemptsCount = 0;
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
                if (files.isEmpty()) {
                    Program.appendToReport("Нечего грузить! Проверь вкладку результатов!", Style.YELLOW);
                    Narrator.warn("Нечего грузить!");
                    return null;
                }
                int errorsCount = 0;
                if (!checkAndPrepareConnectionSettings()) {
                    Narrator.error("Не удалось получить токен, проверь данные для подключения");
                    return null;
                }
                fillTemplates();
                for (File file : files) {
                    String templateName = templateNameMap.get(getTemplateName(file));
                    if (templateName != null && templateIdMap.containsKey(templateName)) {
                        if (!upload(templateName, file)) {
                            errorsCount++;
                        }
                    } else {
                        Program.appendToReport("Схема " + templateName + " не найдена", Style.RED);
                    }
                }
                if (errorsCount == 0) {
                    Program.appendToReport("Все схемы загружены!", Style.GREEN);
                    Narrator.success("Все схемы загружены!");
                } else {
                    String errorMessage = "Не удалось загрузить " + errorsCount + " из " + files.size() + " схем!";
                    Program.appendToReport(errorMessage, Style.RED);
                    Narrator.error(errorMessage);
                }
                return null;
            }
        };
    }

    private static void fillTemplates() {
        String rawJson = client.getRawTemplates();
        if (rawJson == null) {
            templateIdMap.clear();
            return;
        }
        try {
            JsonObject obj = JsonParser.object().from("{\"key\":" + rawJson + "}");
            JsonArray templateArray = obj.getArray("key");
            for (Object o : templateArray) {
                JsonObject templateObj = (JsonObject) o;
                templateIdMap.put(templateObj.get("code").toString(), Long.parseLong(templateObj.get("id").toString()));
            }
        } catch (JsonParserException e) {
            Program.appendToReport("Ошибка в полученном json'e со схемами" + e.getMessage(), Style.RED);
        }
        fillThemes();
    }

    private static void fillThemes() {
        String inputDir = Program.getProperties().get(Settings.INPUT_DIR);
        File mappingFile = Paths.get(inputDir, Const.TEMPLATE_MAPPING_FILE).toFile();
        if (mappingFile.exists()) {
            try {
                JsonObject mainNode = JsonParser.object().from(FileUtils.readFile(mappingFile));
                for (Map.Entry<String, Object> folderMap : mainNode.entrySet()) {
                    String folderName = folderMap.getKey();
                    JsonObject properties = (JsonObject) folderMap.getValue();
                    String templateName = properties.get("name").toString();
                    templateNameMap.put(folderName, templateName);
                    JsonObject topics = (JsonObject) properties.get("topics");
                    for (Map.Entry<String, Object> topic : topics.entrySet()) {
                        templateTopicMap.put(templateName + topic.getKey().toUpperCase(), topic.getValue().toString());
                    }
                }
            } catch (JsonParserException e) {
                Program.appendToReport(Const.TEMPLATE_MAPPING_FILE + " содержит ошибку: " + e.getMessage(), Style.RED);
            }
        } else {
            Program.appendToReport(Const.TEMPLATE_MAPPING_FILE + " не найден.", Style.YELLOW);
        }
    }

    private static boolean upload(String templateName, File file) {
        String language = getLanguage(file.getName());
        if (language == null) {
            return false;
        }
        long templateId = templateIdMap.get(templateName);
        String topic = templateTopicMap.get(templateName + language.toUpperCase());
        int httpResult = client.uploadFileToTemplate(file, templateId, language, topic);

        if (httpResult == HttpURLConnection.HTTP_OK) {
            return true;
        } else if (httpResult == HttpURLConnection.HTTP_FORBIDDEN || httpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (++attemptsCount <= 1) {
                Program.appendToReport("Похоже что-то с токеном, попробую обновить", Style.YELLOW_B);
                if (!checkAndPrepareConnectionSettings()) {
                    return false;
                } else {
                    return upload(templateName, file);
                }
            } else {
                Program.appendToReport("Идея с обновлением токена не помогла", Style.RED_B);
                return false;
            }
        } else {
            return false;
        }
    }

    private static String getLanguage(String fileName) {
        try {
            String[] split = fileName.split("_");
            String language = split[split.length - 1].split("\\.")[0].toUpperCase();
            if (language.length() != 2) {
                Program.appendToReport("Не удалось вытащить язык из файла " + fileName + ", название должно бьть в формате имя_ru.тип:", Style.RED);
                return null;
            }
            return language;
        } catch (Exception e) {
            Program.appendToReport("Не удалось вытащить язык из файла " + fileName + ", название должно бьть в формате имя_ru.тип:", e);
        }
        return null;
    }

    private static String getTemplateName(File file) {
        return file.getParentFile().getName();
    }
}
