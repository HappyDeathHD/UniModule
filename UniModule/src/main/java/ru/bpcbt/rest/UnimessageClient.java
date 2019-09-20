package ru.bpcbt.rest;

import com.grack.nanojson.JsonWriter;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class UnimessageClient {

    private String coreUrl;
    private String token;

    UnimessageClient(String coreUrl, String login, String password) {
        this.coreUrl = coreUrl;
        setNewToken(login, password);
    }

    boolean isAuth() {
        return token != null;
    }

    int uploadFileToTemplate(File file, long templateId, String language, String topic) {
        try {
            final URL uploadUri = new URL(coreUrl + "/api/v1.0/templates/" + templateId + "/markups/" + language);
            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json,text/plain");
            connection.setRequestProperty("Method", "POST");

            final OutputStream os = connection.getOutputStream();
            final String params = JsonWriter.string()
                    .object()
                    .value("body", FileUtils.readFile(file))
                    .value("fileName", topic)
                    .value("language", language)
                    .end().done();
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.close();

            final int httpResult = connection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                return httpResult;
            } else {
                ReportPane.error(file.getName() + " не загрузился:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Файл " + file.getName() + " не загрузился:", e);
        }
        return -1;
    }

    private void setNewToken(String login, String password) {
        try {
            final URL authUrl = new URL(coreUrl + "/api/v1.0/authenticate");
            final HttpURLConnection connection = (HttpURLConnection) authUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json,text/plain");
            connection.setRequestProperty("Method", "POST");

            final OutputStream os = connection.getOutputStream();
            final String params = "{\"username\":\"" + login + "\",\"password\":\"" + password + "\"}";
            os.write(params.getBytes(StandardCharsets.UTF_8));
            os.close();

            final StringBuilder sb = new StringBuilder();
            final int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                ReportPane.fine("Получен новый токен");
                final String rawResponse = sb.toString().substring(sb.toString().indexOf("\"token\":\"")).substring(9);
                token =  rawResponse.substring(0, rawResponse.indexOf("\""));
            } else {
                ReportPane.error("Не удалось получить токен:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось получить токен:", e);
        }
    }

    String getRawTemplates() {
        try {
            final URL uploadUri = new URL(coreUrl + "/api/v1.0/templates/headers");
            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Method", "GET");

            final StringBuilder sb = new StringBuilder();
            final int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                ReportPane.fine("Список схем успешно загрузился");
                return sb.toString();
            } else {
                ReportPane.error("Не удалось загрузить список схем:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось загрузить список схем:", e);
        }
        return null;
    }
}
