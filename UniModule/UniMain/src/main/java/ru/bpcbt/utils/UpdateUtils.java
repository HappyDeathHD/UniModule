package ru.bpcbt.utils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.Program;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Scanner;

public class UpdateUtils {

    private UpdateUtils() { // Utils class
    }

    public static void update() {
        try {
            FileUtils.deleteIfExists("Updater.jar");
            if (isUpdateNeeded()) {
                final String updaterUrl = "https://github.com/HappyDeathHD/UniModule/raw/master/UniModule/Updater/target/Updater.jar";
                InputStream in = new URL(updaterUrl).openStream();
                Files.copy(in, Paths.get("Updater.jar"), StandardCopyOption.REPLACE_EXISTING);
                Runtime.getRuntime().exec("java -jar Updater.jar");
                System.exit(10);
            }
        } catch (IOException | JsonParserException e) {
            Narrator.yell("Не удалось обновиться :( ", e);
        }
    }

    private static boolean isUpdateNeeded() throws IOException, JsonParserException {
        final String commitCheckUrl = "https://api.github.com/repos/HappyDeathHD/UniModule/commits?path=UniModule/UniMain/target/UniModule.jar&page=1&per_page=1";
        String rawJson = new Scanner(new URL(commitCheckUrl).openStream(), "UTF-8").useDelimiter("\\A").next();

        final JsonObject obj = JsonParser.object().from(rawJson.substring(1, rawJson.length() - 1));
        String message = obj.getObject("commit").getString("message");
        String sha = obj.getString("sha");

        Map<Settings, String> properties = Program.getProperties();
        if (!properties.containsKey(Settings.LAST_SHA)) {
            properties.put(Settings.LAST_SHA, sha);
            FileUtils.setProperties(properties);
        }

        if (!sha.equals(properties.get(Settings.LAST_SHA))) {
            properties.put(Settings.LAST_SHA, sha);
            FileUtils.setProperties(properties);
            return MiniFrame.askForConfirmation("Появилась новая версия с фиксом старых/добавлением новых багов!"
                    + System.lineSeparator() + message + System.lineSeparator() + "Обновляемся?");
        } else {
            return false;
        }
    }
}
