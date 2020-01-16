package ru.bpcbt.utils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.Program;
import ru.bpcbt.entity.ReplaceTask;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.settings.Settings;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class JsonUtils {
    private static final Map<File, Map<String, String>> cachedJson = new HashMap<>();
    private static final Map<File, Boolean> jsonProcessStatus = new ConcurrentHashMap<>();

    private JsonUtils() { //Utils class
    }

    /**
     * @return мапу вида "@ключ=значение, @массив@массив@ключ2=значение2"
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static Map<String, String> parseModule(File file) {
        if (!jsonProcessStatus.containsKey(file)) {
            jsonProcessStatus.put(file, false);

            final Map<String, String> result = new HashMap<>();
            final String jsonContent = FileUtils.readFile(file);
            try {
                final JsonObject obj = JsonParser.object().from(jsonContent);
                fillMapFromModuleJson(result, obj, "");
            } catch (JsonParserException e) {
                ReportPane.error("Ошибка в дочернем json'e " + file.getPath() + " " + e.getMessage());
            }
            cachedJson.put(file, result);
            jsonProcessStatus.put(file, true);
            return result;
        } else {
            while (!jsonProcessStatus.get(file)) {
            }
            return cachedJson.get(file);
        }
    }

    /**
     * Парсит json скелет скелетов вида:
     * <pre>
     * {
     *   "путь@к@основному@скелету.html": {
     *     "путь@для@результов@результат1.html": {
     *       "переменная1": "значение1",
     *       "переменная2": "значение2"
     *     },
     *     "путь@для@результов@результат2.html": {
     *       "переменная1": "значение1",
     *       "переменная2": "значение2"
     *     }
     *   }
     * }
     * </pre>
     * Создает и возвращает лист из ReplaceTask
     *
     * @param file файл скелета скелетов
     * @return лист ReplaceTask
     * @see ReplaceTask
     */
    public static List<ReplaceTask> parseSkeleton(File file) {
        final String jsonContent = FileUtils.readFile(file);
        return parseSkeleton(file, jsonContent);
    }

    private static List<ReplaceTask> parseSkeleton(File file, String jsonContent) {
        try {
            final JsonObject obj = JsonParser.object().from(jsonContent);
            return getJobsFromSkeletonJson(obj);
        } catch (Exception e) {
            ReportPane.error("Ошибка в родительском json'e " + file.getPath() + " " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void fillMapFromModuleJson(Map<String, String> result, JsonObject jsonObject, String key) {
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (entry.getValue().getClass() == JsonObject.class) {
                fillMapFromModuleJson(result, (JsonObject) entry.getValue(), key + Delimiters.DELIMITER.getSymbol() + entry.getKey());
            } else {
                result.put(key + Delimiters.DELIMITER.getSymbol() + entry.getKey(), entry.getValue().toString());
            }
        }
    }

    private static List<ReplaceTask> getJobsFromSkeletonJson(JsonObject jsonObject) {
        final List<ReplaceTask> replaceTasks = new ArrayList<>();
        for (Map.Entry<String, Object> jInputs : jsonObject.entrySet()) {
            for (Map.Entry<String, Object> jOutputs : ((JsonObject) jInputs.getValue()).entrySet()) {
                File file = Paths.get(Program.getProperties().get(Settings.MODULE_DIR), FileUtils.separatePlaceholders(jInputs.getKey())).toFile();
                final HashMap<String, String> variables = FileUtils.getVariableMapWithLocale(jOutputs.getKey());
                for (Map.Entry<String, Object> jVariables : ((JsonObject) jOutputs.getValue()).entrySet()) {
                    variables.put(jVariables.getKey(), String.valueOf(jVariables.getValue()));
                }
                replaceTasks.add(new ReplaceTask(jOutputs.getKey(),
                        FileUtils.readFile(file),
                        variables));
            }
        }
        return replaceTasks;
    }

    public static void refresh() {
        cachedJson.clear();
        jsonProcessStatus.clear();
    }
}
