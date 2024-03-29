package ru.bpcbt.rest;

import com.grack.nanojson.*;
import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.utils.*;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class UnimessageConductor {

    private static UnimessageClient client;
    private static final Map<String, Long> templateIdMap = new HashMap<>();
    private static final Map<String, String> templateTopicMap = new HashMap<>();
    private static final Map<String, String> templateNameMap = new HashMap<>();
    private static int attemptsCount;

    private static boolean isRunning = false;

    private UnimessageConductor() { // Utils class
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean checkAndPrepareConnectionSettings() {
        if (client == null) {
            String coreUrl = Program.getProperties().get(Settings.CORE_URL).trim();
            if (coreUrl.isEmpty()) {
                Narrator.yell("Надо заполнить путь до api!");
                Narrator.error("Надо заполнить путь до api!");
                Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (coreUrl.endsWith("/")) {
                coreUrl = coreUrl.substring(0, coreUrl.length() - 1);
            }
            final String login = Program.getProperties().get(Settings.USERNAME);
            String password = Program.getMainFrame().getSettingsPanel().getPassword();
            if (login == null || login.isEmpty()) {
                Narrator.yell("Нужно заполнить логин/пароль для подключения к api");
                Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
                return false;
            }
            if (password == null || password.isEmpty()) {
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
        if (client.isAuth()) {
            return true;
        } else {
            Narrator.error("Не удалось получить токен, проверь данные для подключения");
            return false;
        }
    }

    public static void refresh() {
        client = null;
    }

    public static SwingWorker uploadJob(Set<File> files) {
        ReportPane.clearReport();
        attemptsCount = 0;
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                GlobalUtils.setEnabledToProcessButtons(false);
                isRunning = true;
                Program.getMainFrame().selectPaneTab(MainFrame.REPORT_TAB);
                if (files.isEmpty()) {
                    ReportPane.warn("Нечего грузить!");
                    Narrator.warn("Нечего грузить!");
                    GlobalUtils.setEnabledToProcessButtons(true);
                    return null;
                }
                if (!checkAndPrepareConnectionSettings()) {
                    Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
                    GlobalUtils.setEnabledToProcessButtons(true);
                    return null;
                }
                int errorsCount = 0;
                fillTemplates();
                for (File file : files) {
                    if (isRunning) {
                        String templateName = getTemplateName(file);
                        if (templateNameMap.containsKey(templateName)) {
                            templateName = templateNameMap.get(templateName);
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
                }
                if (errorsCount == 0) {
                    ReportPane.success("Все схемы загружены!");
                    Narrator.success("Все схемы загружены!");
                } else {
                    String errorMessage = "Не удалось загрузить " + errorsCount + " из " + files.size() + " схем!";
                    ReportPane.error(errorMessage);
                    Narrator.error(errorMessage);
                }
                GlobalUtils.setEnabledToProcessButtons(true);
                isRunning = false;
                return null;
            }
        };
    }

    public static SwingWorker downloadJob(Collection<String> templates) {
        ReportPane.clearReport();
        attemptsCount = 0;
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                GlobalUtils.setEnabledToProcessButtons(false);
                isRunning = true;
                Program.getMainFrame().selectPaneTab(MainFrame.REPORT_TAB);
                if (templates.isEmpty()) {
                    ReportPane.warn("Нечего бэкапить!");
                    Narrator.warn("Нечего бэкапить!");
                    GlobalUtils.setEnabledToProcessButtons(true);
                    return null;
                }
                if (!checkAndPrepareConnectionSettings()) {
                    Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
                    GlobalUtils.setEnabledToProcessButtons(true);
                    return null;
                }
                final String reserveDir = Program.getProperties().get(Settings.RESERVE_DIR);
                final SimpleDateFormat osDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss");
                int errorCount = 0;
                long start = System.currentTimeMillis();
                final Date startDate = new Date(start);
                ReportPane.normal("Начало выгрузки: " + startDate);
                Narrator.normal("Создаю бэкап...");
                try {
                    for (String templateName : templates) {
                        // Список языков версток шаблона
                        final List<String> languages = new ArrayList<>();
                        final Long templateId = templateIdMap.get(templateName);
                        final String rawTemplatesJson = client.getRawTemplateMarkups(templateId);
                        JsonObject obj = JsonParser.object().from("{\"key\":" + rawTemplatesJson + "}");
                        JsonArray templateArray = obj.getArray("key");
                        for (Object templateObj : templateArray) {
                            languages.add(((JsonObject) templateObj).get("language").toString().toUpperCase());
                        }

                        for (String language : languages) {
                            if (isRunning) {
                                final String rawMarkupsJson = client.getRawMarkup(templateId, language);
                                if (rawMarkupsJson == null) {
                                    errorCount++;
                                    continue;
                                }
                                final String markup = GlobalUtils.getJsonValue(rawMarkupsJson, "body");
                                if (markup == null) {
                                    ReportPane.error("Верстка шаблона " + templateName + " с языком " + language + " имеет неожиданный формат!");
                                    ReportPane.debug(rawMarkupsJson);
                                    errorCount++;
                                    continue;
                                }
                                FileUtils.writeToPath(
                                        Paths.get(reserveDir, osDateFormat.format(startDate), templateName,
                                                templateName + "_" + language.toLowerCase() + ".html"),
                                        GistUtils.unescapeJavaString(markup));
                                ReportPane.success("Верстка шаблона " + templateName + " с языком " + language + " успешно сохранена!");
                            }
                        }
                        downloadTemplateData(
                                Paths.get(reserveDir, osDateFormat.format(startDate), templateName),
                                templateId, templateName);
                    }
                    if (errorCount == 0) {
                        String conclusionSuccess = "Создан бэкап!";
                        ReportPane.success(conclusionSuccess);
                        Narrator.success(conclusionSuccess);
                    } else {
                        String conclusionWithErrors = "Во время бэкапа были ошибки! (" + errorCount + " шт.)";
                        ReportPane.warn(conclusionWithErrors);
                        Narrator.warn(conclusionWithErrors);
                    }
                    Program.getMainFrame().getReserveFilesPanel().refreshFiles();
                } catch (JsonParserException e) {
                    ReportPane.error("Ошибка в полученном json'e со схемами" + e.getMessage());
                }
                GlobalUtils.setEnabledToProcessButtons(true);
                isRunning = false;
                return null;
            }
        };
    }

    public static boolean stop() {
        if (isRunning) {
            isRunning = false;
            return true;
        }
        return false;
    }

    private static void downloadTemplateData(Path path, Long templateId, String templateName) throws JsonParserException {
        JsonObject templateInfo = JsonParser.object().from(client.templateInfo(templateId));
        if (templateInfo == null) {
            return;
        }
        Object preBuildScript = templateInfo.get("preBuildScript");
        if (preBuildScript != null) {
            FileUtils.writeToPath(Paths.get(path.toString(), "script.groovy")
                    , (String) preBuildScript);
            ReportPane.success("Скрипт " + templateName + " успешно сохранен!");
        }
        Object templateConfiguration = templateInfo.get("templateConfiguration");
        if (templateConfiguration != null) {
            FileUtils.writeToPath(Paths.get(path.toString(), "config.json")
                    , JsonWriter.string(templateConfiguration));
            ReportPane.success("Конфиг " + templateName + " успешно сохранен!");
        }
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
        final String inputDir = Program.getProperties().get(Settings.INPUT_DIR);
        final File mappingFile = Paths.get(inputDir, FileUtils.TEMPLATE_MAPPING_FILE).toFile();
        if (mappingFile.exists()) {
            try {
                final JsonObject mainNode = JsonParser.object().from(FileUtils.readFile(mappingFile));
                for (Map.Entry<String, Object> folderMap : mainNode.entrySet()) {
                    final String folderName = folderMap.getKey();
                    final JsonObject properties = (JsonObject) folderMap.getValue();
                    final String templateName = properties.get("name").toString();
                    templateNameMap.put(folderName, templateName);
                    final JsonObject topics = (JsonObject) properties.get("topics");
                    if (topics != null) {
                        for (Map.Entry<String, Object> topic : topics.entrySet()) {
                            templateTopicMap.put(templateName + topic.getKey().toUpperCase(), topic.getValue().toString());
                        }
                    }
                }
            } catch (JsonParserException e) {
                ReportPane.error(FileUtils.TEMPLATE_MAPPING_FILE + " содержит ошибку: " + e.getMessage());
            }
        } else {
            ReportPane.debug(FileUtils.TEMPLATE_MAPPING_FILE + " не найден.");
        }
    }

    private static boolean upload(String templateName, File file) {
        if (file.getName().contains(".groovy")) {
            return uploadGroovy(templateName, file);
        }
        if (file.getName().equals("config.json")) {
            return uploadConfig(templateName, file);
        }
        final String language = getLanguage(file.getName());
        if (language != null) {
            return uploadMakeup(templateName, file, language);
        }
        ReportPane.warn(file.getName() + " имеет неопознанный тип");
        return false;
    }

    private static boolean uploadMakeup(String templateName, File file, String language) {
        final long templateId = templateIdMap.get(templateName);
        String topic = templateTopicMap.get(templateName + language.toUpperCase());
        ReportPane.normal("Начинаю загрузку " + file.getName() + " в " + templateName + "(" + templateId + ") c языком " + language);
        int httpResult = client.uploadFileToTemplate(file, templateId, language, topic);
        if (httpResult == HttpURLConnection.HTTP_OK) {
            ReportPane.success(file.getName() + " успешно загрузился");
            return true;
        } else if (isNeedRetry(httpResult)) {
            return uploadMakeup(templateName, file, language);
        }
        return false;
    }

    private static boolean uploadGroovy(String templateName, File file) {
        final long templateId = templateIdMap.get(templateName);
        int httpResult = client.uploadScript(file, templateId);
        if (httpResult == HttpURLConnection.HTTP_OK) {
            ReportPane.success("Скрипт " + templateName + " успешно загрузился");
            return true;
        } else if (isNeedRetry(httpResult)) {
            return uploadGroovy(templateName, file);
        }
        return true;
    }

    private static boolean uploadConfig(String templateName, File file) {
        final long templateId = templateIdMap.get(templateName);
        int httpResult = client.uploadConfig(file, templateId);
        if (httpResult == HttpURLConnection.HTTP_OK) {
            ReportPane.success("Конфиг " + templateName + " успешно загрузился");
            return true;
        } else if (isNeedRetry(httpResult)) {
            return uploadConfig(templateName, file);
        }
        return true;
    }

    private static boolean isNeedRetry(int httpResult) {
        if (httpResult == HttpURLConnection.HTTP_FORBIDDEN || httpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (++attemptsCount <= 1) {
                ReportPane.warn("Похоже что-то с токеном, попробую обновить");
                return checkAndPrepareConnectionSettings();
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
            final String language = FileUtils.getLanguage(fileName);
            if (language != null) {
                return language.toUpperCase();
            }
        } catch (Exception ignored) {
        }
        ReportPane.error("Не удалось вытащить язык из файла " + fileName + ", название должно бьть в формате имя_язык.тип:");
        return null;
    }

    private static String getTemplateName(File file) {
        return file.getParentFile().getName();
    }

    public static Map<String, Long> getTemplateIdMap() {
        if (!checkAndPrepareConnectionSettings()) {
            Program.getMainFrame().selectPaneTab(MainFrame.SETTINGS_TAB);
            return null;
        }
        fillTemplates();
        return templateIdMap;
    }
}
