package ru.bpcbt;

import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.utils.Style;
import ru.bpcbt.utils.UpdateUtils;

import javax.swing.*;
import java.util.Map;
import java.util.Properties;

import static ru.bpcbt.settings.Settings.STYLE;

public class Program {
    private static MainFrame mainFrame;
    private static Map<Settings, String> properties;
    private static Properties sysProperties;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                properties = FileUtils.getProperties();
                if (properties.containsKey(STYLE)) {
                    try {
                        UIManager.setLookAndFeel(Style.getLafs()[Integer.parseInt(properties.get(STYLE))].getClassName());
                    } catch (Exception e) {
                        Narrator.yell("Ошибка при применении стиля", e);
                    }
                }
                GlobalUtils.makeSovietRussiaButtons();
                mainFrame = new MainFrame();
                mainFrame.getSettingsPanel().loadConfigurations();
                GlobalUtils.refreshAllFiles();
                UpdateUtils.update();
            } catch (Exception e) {
                Narrator.yell("Ошибка:", e);
            }
        });
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    public static Map<Settings, String> getProperties() {
        return properties;
    }

    public static String getSysProperty(String key) {
        if (sysProperties != null) {
            return sysProperties.getProperty(key);
        }
        try {
            sysProperties = new Properties();
            sysProperties.load(Program.class.getClassLoader().getResourceAsStream(".properties"));
            return sysProperties.getProperty(key);
        } catch (Exception ignored) {
        }
        return null;
    }
}
