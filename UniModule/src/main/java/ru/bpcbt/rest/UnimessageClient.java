package ru.bpcbt.rest;

import com.grack.nanojson.JsonWriter;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.Style;
import ru.bpcbt.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UnimessageClient {

    private String coreUrl;
    private String token;

    public UnimessageClient(String coreUrl, String login, String password) {
        this.coreUrl = coreUrl;
        setNewToken(login, password);
    }

    public boolean isAuth() {
        return token != null;
    }

    public int uploadFileToTemplate(File file, long templateId, String language, String topic) {
        try {
            GlobalUtils.appendToReport("Начинаю загрузку " + file.getName() + " в " + templateId + " c языком " + language, Style.GREEN);
            URL uploadUri = new URL(coreUrl + "/api/v1.0/templates/" + templateId + "/markups/" + language);
            HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json,text/plain");
            connection.setRequestProperty("Method", "POST");

            OutputStream os = connection.getOutputStream();
            String params = JsonWriter.string()
                    .object()
                    .value("body", FileUtils.readFile(file))
                    .value("fileName", topic)
                    .value("language", language)
                    .end().done();
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.close();

            int httpResult = connection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                GlobalUtils.appendToReport(file.getName() + " успешно загрузился", Style.GREEN);
                return httpResult;
            } else {
                GlobalUtils.appendToReport(file.getName() + " не загрузился:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage(), Style.RED);
            }
        } catch (Exception e) {
            GlobalUtils.appendToReport("Файл " + file.getName() + " не загрузился:", e);
        }
        return -1;
    }

    private void setNewToken(String login, String password) {
        try {
            URL authUrl = new URL(coreUrl + "/api/v1.0/authenticate");
            HttpURLConnection connection = (HttpURLConnection) authUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json,text/plain");
            connection.setRequestProperty("Method", "POST");

            OutputStream os = connection.getOutputStream();
            String params = "{\"username\":\"" + login + "\",\"password\":\"" + password + "\"}";
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.close();

            StringBuilder sb = new StringBuilder();
            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                GlobalUtils.appendToReport("Получен новый токен", Style.GREEN);
                token = substringBetween(sb.toString(), "\"token\":\"", "\"");
            } else {
                GlobalUtils.appendToReport("Не удалось получить токен:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage(), Style.RED);
            }
        } catch (Exception e) {
            GlobalUtils.appendToReport("Не удалось получить токен:", e);
        }
    }

    public String getRawTemplates() {
        try {
            URL uploadUri = new URL(coreUrl + "/api/v1.0/templates/headers");
            HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Method", "GET");

            StringBuilder sb = new StringBuilder();
            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                GlobalUtils.appendToReport("Список схем успешно загрузился", Style.GREEN);
                return sb.toString();
            } else {
                GlobalUtils.appendToReport("Не удалось загрузить список схем:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage(), Style.RED);
            }
        } catch (Exception e) {
            GlobalUtils.appendToReport("Не удалось загрузить список схем:", e);
        }
        return null;
    }

    private static String substringBetween(String victim, String prefix, String postfix) {
        String withTrash = victim.substring(victim.indexOf(prefix)).substring(prefix.length());
        return withTrash.substring(0, withTrash.indexOf(postfix));
    }
}
