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
import java.util.Scanner;

public class UpdateUtils {

    private static final String UPDATER_URL = "https://github.com/HappyDeathHD/UniModule/raw/master/UniModule/updater/target/Updater.jar";
    private static final String UPDATER_NAME = "Updater.jar";
    private static final String COMMIT_CHECK_URL = "https://api.github.com/repos/HappyDeathHD/UniModule/commits?path=UniModule/deskapp/target/UniModule.jar&page=1&per_page=1";

    private UpdateUtils() { // Utils class
    }

    public static void update() {
        SwingUtilities.invokeLater(() -> {
            try {
                FileUtils.deleteIfExists(UPDATER_NAME);
                if (isUpdateNeeded()) {
                    InputStream in = new URL(UPDATER_URL).openStream();
                    Files.copy(in, Paths.get(UPDATER_NAME), StandardCopyOption.REPLACE_EXISTING);
                    Runtime.getRuntime().exec("java -jar " + UPDATER_NAME);
                    System.exit(10);
                }
            } catch (IOException | JsonParserException e) {
                Narrator.yell("Не удалось обновиться :( ", e);
            }
        });
    }

    private static boolean isUpdateNeeded() throws IOException, JsonParserException {
        String rawJson = new Scanner(new URL(COMMIT_CHECK_URL).openStream(), "UTF-8").useDelimiter("\\A").next();

        final JsonObject obj = JsonParser.object().from(rawJson.substring(1, rawJson.length() - 1));
        String message = obj.getObject("commit").getString("message");
        String penultSha = obj.getArray("parents").getObject(0).getString("sha");

        return !penultSha.equals(Program.getSysProperty("git.commit.id"))
                && MiniFrame.askForConfirmation("Появилась новая версия с фиксом старых/добавлением новых багов:"
                + System.lineSeparator() + message + System.lineSeparator() + "Обновляемся?");
    }
}
