package ru.bpcbt;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Program {

    public static void main(String[] args) {
        downloadUniModule();
    }

    private static void downloadUniModule() {
        try {
            final String url = "https://github.com/HappyDeathHD/UniModule/raw/master/UniModule/UniMain/target/UniModule.jar";
            InputStream in = new URL(url).openStream();
            Files.copy(in, Paths.get("UniModule.jar"), StandardCopyOption.REPLACE_EXISTING);
            Runtime.getRuntime().exec("java -jar UniModule.jar");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() + System.lineSeparator() + "Попробуйте в ручную запустить Updater.jar",
                    "Не удалось обновиться :(", JOptionPane.WARNING_MESSAGE);
        }
    }
}
