package ru.bpcbt.utils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.Program;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Scanner;

public class UpdateUtils {

    private static final String UPDATER_NAME = "Updater.jar";
    private static final String COMMIT_CHECK_URL;
    private static final String UPDATER_URL;

    static {
        Properties properties = Program.getUpdateProperties();
        if (properties != null) {
            COMMIT_CHECK_URL = properties.getProperty("update.commit.check");
            UPDATER_URL = properties.getProperty("update.url");
        } else {
            COMMIT_CHECK_URL = null;
            UPDATER_URL = null;
        }
    }

    private UpdateUtils() { // Utils class
    }

    public static void checkForUpdate() {
        new SwingWorker() {
            @Override
            protected Object doInBackground() {
                try (Scanner scanner = new Scanner(new URL(COMMIT_CHECK_URL).openStream(), "UTF-8")) {
                    FileUtils.deleteIfExists(UPDATER_NAME);
                    String rawJson = scanner.useDelimiter("\\A").next();
                    final JsonObject obj = JsonParser.object().from(rawJson.substring(1, rawJson.length() - 1));
                    String message = obj.getObject("commit").getString("message");
                    String penultSha = obj.getArray("parents").getObject(0).getString("sha");
                    if (!penultSha.equals(Program.getSysProperty("git.commit.id"))) {
                        processCommitMessage(message);
                    }
                } catch (IOException | JsonParserException e) {
                    Narrator.yell("Не удалось обновиться :( ", e);
                }
                return null;
            }
        }.execute();
    }

    private static void processCommitMessage(String message) {
        int changelogStart = message.indexOf('<');
        String arguments = message.substring(0, changelogStart);
        String changelog = message.substring(changelogStart);
        boolean isForce = arguments.contains("-f");
        boolean isSilent = arguments.contains("-s");
        if (isSilent) {
            update();
        } else {
            MiniFrame.showUpdateMessage(changelog, isForce);
        }
    }

    static void update() {
        try {
            InputStream in = new URL(UPDATER_URL).openStream();
            Files.copy(in, Paths.get(UPDATER_NAME), StandardCopyOption.REPLACE_EXISTING);
            Runtime.getRuntime().exec("java -jar " + UPDATER_NAME);
            System.exit(10);
        } catch (IOException e) {
            Narrator.yell("Не удалось обновиться :( ", e);
        }
    }
}
