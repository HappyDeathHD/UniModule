package ru.bpcbt;

import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.utils.Style;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static ru.bpcbt.settings.Settings.STYLE;

public class Program {
    private static MainFrame mainFrame;
    private static Map<Settings, String> properties;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            update();
            try {
                properties = FileUtils.getProperties();
                if (properties.containsKey(STYLE)) {
                    try {
                        int lafIndex = Integer.parseInt(properties.get(STYLE));
                        UIManager.setLookAndFeel(Style.getLafs()[lafIndex].getClassName());
                    } catch (Exception e) {
                        Narrator.yell("Ошибка при применении стиля", e);
                    }
                }
                GlobalUtils.makeSovietRussiaButtons();
                mainFrame = new MainFrame();
                mainFrame.getSettingsPanel().loadConfigurations();
                GlobalUtils.refreshAllFiles();
            } catch (Exception e) {
                Narrator.yell("Ошибка:", e);
            }
        });
    }

    private static void update() {
        try {
            final String url = "https://github.com/HappyDeathHD/UniModule/raw/master/UniModule/target/UniModule.jar";
            InputStream in = new URL(url).openStream();
            Files.copy(in, Paths.get("UniModule.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Narrator.yell("Не удалось обновиться :( ", e);
        }
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    public static Map<Settings, String> getProperties() {
        return properties;
    }
}
