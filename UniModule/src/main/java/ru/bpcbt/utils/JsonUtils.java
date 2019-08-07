package ru.bpcbt.utils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import ru.bpcbt.Program;
import ru.bpcbt.entity.ReplaceJob;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.settings.Settings;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUtils {
    private static Map<File, Map<String, String>> cachedJson = new HashMap<>();
    private static Map<File, Boolean> jsonProcessStatus = new ConcurrentHashMap<>();

    private JsonUtils() { //Utils class
    }

    /**
     * @return мапу вида "@ключ=значение, @массив@массив@ключ2=значение2"
     */
    public static Map<String, String> parseModule(File file) {
        if (!jsonProcessStatus.containsKey(file)) {
            jsonProcessStatus.put(file, false);

            Map<String, String> result = new HashMap<>();
            String jsonContent = FileUtils.readFile(file);
            try {
                JsonObject obj = JsonParser.object().from(jsonContent);
                fillMapFromModuleJson(result, obj, "");
            } catch (JsonParserException e) {
                Program.appendToReport("Ошибка в дочернем json'e " + file.getPath() + " " + e.getMessage(), Style.RED);
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

    public static List<ReplaceJob> parseSkeleton(File file) {
        String jsonContent = FileUtils.readFile(file);
        return parseSkeleton(file, jsonContent);
    }

    public static List<ReplaceJob> parseSkeleton(File file, String jsonContent) {
        try {
            JsonObject obj = JsonParser.object().from(jsonContent);
            return getJobsFromSkeletonJson(obj);
        } catch (Exception e) {
            Program.appendToReport("Ошибка в родительском json'e " + file.getPath() + " " + e.getMessage(), Style.RED);
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

    /**
     * Пример:
     * {"input1.html":{
     * "output1.html":{
     * "var1":1
     * },
     * "output2.html":{
     * "var1":2
     * }
     * }}
     */
    private static List<ReplaceJob> getJobsFromSkeletonJson(JsonObject jsonObject) {
        List<ReplaceJob> replaceJobs = new ArrayList<>();
        for (Map.Entry<String, Object> jInputs : jsonObject.entrySet()) {
            for (Map.Entry<String, Object> jOutputs : ((JsonObject) jInputs.getValue()).entrySet()) {
                HashMap<String, String> variables = new HashMap<>();
                for (Map.Entry<String, Object> jVariables : ((JsonObject) jOutputs.getValue()).entrySet()) {
                    variables.put(jVariables.getKey(), String.valueOf(jVariables.getValue()));
                }
                replaceJobs.add(new ReplaceJob(jOutputs.getKey(),
                        FileUtils.readFile(Paths.get(Program.getProperties().get(Settings.MODULE_DIR), FileUtils.separatePlaceholders(jInputs.getKey())).toFile()),
                        variables));
            }
        }
        return replaceJobs;
    }

    public static void refresh() {
        cachedJson.clear();
        jsonProcessStatus.clear();
    }
}
