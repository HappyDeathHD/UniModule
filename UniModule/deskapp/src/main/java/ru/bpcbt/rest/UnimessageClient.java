package ru.bpcbt.rest;

import com.grack.nanojson.JsonWriter;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.GlobalUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

class UnimessageClient {

    private static final String API_TEMPLATES = "/api/v1.0/templates/";
    private static final String CRLF = "\r\n";
    private static final String DOUBLE_LINE = "--";

    private final String coreUrl;
    private String token;

    UnimessageClient(String coreUrl, String login, String password) {
        this.coreUrl = coreUrl;
        setNewToken(login, password);
    }

    boolean isAuth() {
        return token != null;
    }

    String templateInfo(long templateId) {
        try {
            final URL uploadUri = new URL(coreUrl + API_TEMPLATES + templateId);

            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            setAuthorizationToConnection(connection);

            final int httpResult = connection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                return GlobalUtils.inputStreamToString(connection.getInputStream());
            } else {
                ReportPane.error("Ошибка при получении информации о шаблоне " + templateId + ": " +
                        GlobalUtils.inputStreamToString(connection.getErrorStream()));
            }
        } catch (Exception e) {
            ReportPane.error("Ошибка при получении информации о шаблоне " + templateId, e);
        }
        return null;
    }

    int uploadScript(File file, long templateId) {
        try {
            final URL uploadUri = new URL(coreUrl + API_TEMPLATES + templateId + "/script");

            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            makeConnectionPostJson(connection);
            connection.setRequestMethod("PUT");
            setAuthorizationToConnection(connection);

            try (final OutputStream os = connection.getOutputStream()) {
                final String params = JsonWriter.string()
                        .object()
                        .value("script", FileUtils.readFile(file))
                        .end().done();
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }
            final int httpResult = connection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                return httpResult;
            } else {
                ReportPane.error("Скрипт " + templateId + " не загрузился:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Ошибка загрузки скрипта " + templateId, e);
        }
        return -1;
    }

    int uploadConfig(File file, long templateId) {
        try {
            final URL uploadUri = new URL(coreUrl + API_TEMPLATES + templateId + "/configuration");

            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            makeConnectionPostJson(connection);
            connection.setRequestMethod("PUT");
            setAuthorizationToConnection(connection);

            try (final OutputStream os = connection.getOutputStream()) {
                final String params = "{\"templateConfiguration\":" + FileUtils.readFile(file) + "}";
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }
            final int httpResult = connection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                return httpResult;
            } else {
                ReportPane.error("Конфиг " + templateId + " не загрузился:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Ошибка загрузки конфига " + templateId, e);
        }
        return -1;
    }

    int uploadFileToTemplate(File file, long templateId, String language, String topic) {
        try {
            HttpURLConnection connection;
            if (topic == null) {
                connection = uploadFile(file, templateId, language);
            } else {
                connection = uploadFile(file, templateId, language, topic);
            }
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

    private HttpURLConnection uploadFile(File file, long templateId, String language) throws IOException {
        final URL uploadUri = new URL(coreUrl + API_TEMPLATES + templateId + "/markups/" + language + "/upload");
        final String boundary = Long.toHexString(System.currentTimeMillis());

        final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
        makeConnectionPostMultipart(connection, boundary);
        setAuthorizationToConnection(connection);

        try (final DataOutputStream os = new DataOutputStream(connection.getOutputStream());
             final PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
            writer.append(DOUBLE_LINE).append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(CRLF)
                    .append("Content-Type: text/html").append(CRLF)
                    .append("Content-Transfer-Encoding: binary").append(CRLF).flush();
            writer.append(CRLF).flush();
            Files.copy(file.toPath(), os);
            os.flush();
            writer.append(CRLF).flush();
            writer.append(DOUBLE_LINE).append(boundary).append(DOUBLE_LINE).append(CRLF).flush();
        }
        return connection;
    }

    private HttpURLConnection uploadFile(File file, long templateId, String language, String topic) throws IOException {
        final URL uploadUri = new URL(coreUrl + API_TEMPLATES + templateId + "/markups/" + language);

        final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
        makeConnectionPostJson(connection);
        setAuthorizationToConnection(connection);

        try (final OutputStream os = connection.getOutputStream()) {
            final String params = JsonWriter.string()
                    .object()
                    .value("body", FileUtils.readFile(file))
                    .value("fileName", topic)
                    .value("language", language)
                    .end().done();
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }
        return connection;
    }

    String getRawTemplates() {
        try {
            final URL uploadUri = new URL(coreUrl + "/api/v1.0/templates/headers");
            final HttpURLConnection connection = (HttpURLConnection) uploadUri.openConnection();
            setAuthorizationToConnection(connection);
            connection.setRequestProperty("Method", "GET");

            final int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                ReportPane.debug("Список схем успешно загрузился");
                return inputStreamToString(connection.getInputStream());
            } else {
                ReportPane.error("Не удалось загрузить список схем:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось загрузить список схем:", e);
        }
        return null;
    }

    String getRawTemplateMarkups(Long templateId) {
        try {
            final URL markupsUri = new URL(coreUrl + API_TEMPLATES + templateId + "/markups");
            final HttpURLConnection connection = (HttpURLConnection) markupsUri.openConnection();
            setAuthorizationToConnection(connection);
            connection.setRequestProperty("Method", "GET");

            final int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                return inputStreamToString(connection.getInputStream());
            } else {
                ReportPane.error("Не удалось загрузить список версток:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось загрузить список версток:", e);
        }
        return null;
    }

    String getRawMarkup(Long templateId, String language) {
        try {
            final URL markupsUri = new URL(coreUrl + API_TEMPLATES + templateId + "/markups/" + language);
            final HttpURLConnection connection = (HttpURLConnection) markupsUri.openConnection();
            setAuthorizationToConnection(connection);
            connection.setRequestProperty("Method", "GET");

            final int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                return inputStreamToString(connection.getInputStream());
            } else {
                ReportPane.error("Не удалось загрузить версту " + templateId + " (" + language + "):" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось загрузить версту " + templateId + " (" + language + "):", e);
        }
        return null;
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void setNewToken(String login, String password) {
        try {
            final URL authUrl = new URL(coreUrl + "/api/v1.0/authenticate");
            final HttpURLConnection connection = (HttpURLConnection) authUrl.openConnection();
            makeConnectionPostJson(connection);

            final OutputStream os = connection.getOutputStream();
            final String params = "{\"username\":\"" + login.trim() + "\",\"password\":\"" + password.trim() + "\"}";
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
                final String rawResponse = sb.substring(sb.toString().indexOf("\"token\":\"")).substring(9);
                token = rawResponse.substring(0, rawResponse.indexOf('\"'));
            } else {
                ReportPane.error("Не удалось получить токен:" + System.lineSeparator() +
                        connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            ReportPane.error("Не удалось получить токен:", e);
        }
    }

    private void makeConnectionPostJson(HttpURLConnection connection) {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json,text/plain");
    }

    private void makeConnectionPostMultipart(HttpURLConnection connection, String boundary) {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

    private void setAuthorizationToConnection(HttpURLConnection connection) {
        connection.setRequestProperty("Authorization", "Bearer " + token);
    }
}
