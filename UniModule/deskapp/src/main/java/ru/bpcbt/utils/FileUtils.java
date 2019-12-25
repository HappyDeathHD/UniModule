package ru.bpcbt.utils;

import ru.bpcbt.logger.Narrator;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.settings.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils {
    private static Map<File, String> cachedFiles = new HashMap<>();
    private static Map<File, Boolean> fileProcessStatus = new ConcurrentHashMap<>();

    private static final String CONFIGURATION_DIR = System.getProperty("user.home") + File.separator + "UniModule";
    private static final String CONFIGURATION_FILE = CONFIGURATION_DIR + File.separator + "configurations.xml";

    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    private FileUtils() {
        // Utils class
    }

    public static boolean isFileExists(String pathToFile) {
        if (pathToFile == null || pathToFile.trim().isEmpty()) {
            return false;
        }
        final File file = new File(pathToFile);
        return file.exists() && !file.isDirectory();
    }

    public static boolean isFileExists(File file) {
        if (file == null) {
            return false;
        }
        return file.exists() && !file.isDirectory();
    }

    public static boolean isDirExists(String pathToDir) {
        if (pathToDir == null || pathToDir.trim().isEmpty()) {
            return false;
        }
        final File file = new File(pathToDir);
        return file.exists() && file.isDirectory();
    }

    public static void setProperties(Map<Settings, String> properties) {
        final Properties configFile = loadConfigFile();
        properties.forEach((k, v) -> configFile.setProperty(k.name(), v));
        mkDir(new File(CONFIGURATION_DIR));
        try (OutputStream os = new FileOutputStream(CONFIGURATION_FILE)) {
            configFile.storeToXML(os, "Конфиги");
        } catch (Exception e) {
            Narrator.yell("Не смог записать конфиги", e);
        }
    }

    public static Map<Settings, String> getProperties() {
        Properties configFile = loadConfigFile();
        return Arrays.stream(Settings.values()).filter(s -> configFile.containsKey(s.name()))
                .collect(Collectors.toMap(s -> s, s -> configFile.getProperty(s.name())));
    }

    public static List<File> getFilesByTypeRecursively(String workingDir) {
        final List<File> neededFiles = new ArrayList<>();
        if (isDirExists(workingDir)) {
            File dir = new File(workingDir);
            for (File file : dir.listFiles()) {
                if (file.isFile() && !Const.TEMPLATE_MAPPING_FILE.equals(file.getName())) {
                    neededFiles.add(file);
                } else if (file.isDirectory() && !file.getName().startsWith(".")) {
                    neededFiles.addAll(getFilesByTypeRecursively(file.getPath()));
                }
            }
        }
        return neededFiles;
    }

    public static String readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ReportPane.error("Пытался прочитать " + file.getName() + ". Не повезло", e);
        }
        return "";
    }

    public static String readAndCacheFileContent(File file) {
        if (cachedFiles.containsKey(file)) {
            return cachedFiles.get(file);
        }
        if (!fileProcessStatus.containsKey(file)) {
            fileProcessStatus.put(file, false);
            String content = readFile(file);
            cachedFiles.put(file, content);
            fileProcessStatus.put(file, true);
            return content;
        } else {
            while (!fileProcessStatus.get(file)) {
            }
            return cachedFiles.get(file);
        }
    }

    public static boolean createFile(String fileName, String content) {
        try (OutputStreamWriter writer =
                     new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            ReportPane.error("Пытался создать " + fileName + ". Не повезло", e);
        }
        return false;
    }

    public static void mkDir(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                if (!file.exists()) {
                    ReportPane.error("Не удалось создать директорию " + file.getPath());
                }
            }
        }
    }

    public static String makeTitleFromFile(File file, String dirPath) {
        if (file.getPath().contains(dirPath)) {
            final String desiredPath = file.getPath().replace(dirPath, "")
                    .replace(SEPARATOR, Delimiters.DELIMITER.getSymbol());
            return desiredPath.startsWith(Delimiters.DELIMITER.getSymbol())
                    ? desiredPath.replaceFirst(Delimiters.DELIMITER.getSymbol(), "")
                    : desiredPath;
        }
        return file.getPath();
    }

    public static String[] separatePath(String path) {
        return path.split(Pattern.quote(SEPARATOR));
    }

    public static String[] separatePlaceholders(String path) {
        return path.split(Pattern.quote(Delimiters.DELIMITER.getSymbol()));
    }

    public static void writeResultFile(String fileName, String newFileContent) {
        final String outputDir = GlobalUtils.getProperties().get(Settings.OUTPUT_DIR);
        final String[] separatedPath = FileUtils.separatePlaceholders(fileName);
        if (GlobalUtils.getProperties().get(Settings.INPUT_DIR).equals(outputDir)) {
            separatedPath[0] = Const.CONFLICT_PREFIX + separatedPath[0];
        }
        final Path newPath = Paths.get(outputDir, separatedPath);
        mkDir(newPath.toFile().getParentFile());
        createFile(newPath.toString(), newFileContent);
    }

    public static String getLanguage(String fileName) {
        try {
            final String[] split = fileName.split("_");
            final String language = split[split.length - 1].split("\\.")[0];
            if (language.length() == 2) {
                return language;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static HashMap<String, String> getVariableMapWithLocale(File file) {
        String language = FileUtils.getLanguage(file.getName());
        HashMap<String, String> variables = new HashMap<>();
        if (language != null) {
            variables.put("locale", language);
        }
        return variables;
    }

    public static void deleteIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            ReportPane.error("Не удалось удалить  " + path);
        }
    }

    public static void refresh() {
        cachedFiles.clear();
        fileProcessStatus.clear();
    }

    private static Properties loadConfigFile() {
        final Properties configFile = new Properties();
        if (isFileExists(CONFIGURATION_FILE)) {
            try (InputStream is = new FileInputStream(CONFIGURATION_FILE)) {
                configFile.loadFromXML(is);
            } catch (Exception e) {
                Narrator.yell("Не смог прочитать конфиги", e);
            }
        }
        return configFile;
    }
}
