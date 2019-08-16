package ru.bpcbt.entity;

import javafx.util.Pair;
import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.Style;
import ru.bpcbt.settings.Settings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Плейсхолдер — ссылка до текста, которым он сам должен быть заменен.
 * <p/>
 * <ul>
 * <li>Все плейсхолдеры начинаются и оканчиваются на {@link Delimiters#START_END}</li>
 * <li>Корневая директория определяется {@link Settings#MODULE_DIR}</li>
 * <li>Плейсхолдер может содержать один путь разделенный при помощи {@link Delimiters#DELIMITER} и любое количество переменных. Переменные выделяются {@link Delimiters#VARIABLE_START_END}</li>
 * <li>Переменные бывают 3 видов:<ul>
 * <li>"название=значение" — объявление переменной</li>
 * <li>только название — место для подстановки значение объявленной ранее переменной</li>
 * <li>"название={@link Delimiters#LINK_START_END}ссылка{@link Delimiters#LINK_START_END}" — объявление переменной плейсхолдером без использования {@link Delimiters#START_END}</li>
 * </ul>
 * </li>
 * <li>Текст для замены берется в корневой директории с добавлением личного пути плейсхолдера</li>
 * <li>В найденном тексте переменные состоящие только из названия заменятся значениями</li>
 * </ul>
 */
public class Placeholder {
    private String rawPH;
    private String body;
    private Map<String, String> variables;
    private Map<String, Placeholder> links;

    public Placeholder(String placeholder) {
        rawPH = placeholder;
        variables = new HashMap<>();
        links = new HashMap<>();
        try {
            setVariables(placeholder.trim());
        } catch (Exception e) {
            ReportPane.error("Плохой плейсхолдер " + placeholder);
            body = placeholder;
        }
    }

    private void setVariables(String placeholder) {
        String varDelimiter = Delimiters.VARIABLE_START_END.getSymbol();
        String linkDelimiter = Delimiters.LINK_START_END.getSymbol();
        if (placeholder.endsWith(varDelimiter) && placeholder.contains("=")) {
            String subPH = placeholder.substring(0, placeholder.length() - varDelimiter.length());
            if (subPH.endsWith(linkDelimiter)) { //это ссылка
                subPH = placeholder.substring(0, subPH.length() - linkDelimiter.length());
                final int startValue = subPH.lastIndexOf(linkDelimiter);
                final String linkValue = subPH.substring(startValue + linkDelimiter.length());
                final int startKey = subPH.substring(0, startValue).lastIndexOf(varDelimiter);
                final String keyValue = subPH.substring(startKey + varDelimiter.length()).split("=")[0];
                links.put(keyValue, new Placeholder(linkValue));
                setVariables(subPH.substring(0, startKey));
            } else { //это обычная переменная
                final int start = subPH.lastIndexOf(varDelimiter);
                final String rawVariable = subPH.substring(start + varDelimiter.length());
                final String[] split = rawVariable.split("=");
                if (split.length == 2) {
                    variables.put(split[0].trim(), split[1].trim());
                }
                setVariables(subPH.substring(0, start));
            }
        } else {
            body = placeholder;
        }
    }

    private String getWithReplaces() {
        String result = body;
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            result = result.replace(wrapVar(variable.getKey()), variable.getValue());
        }
        return result;
    }

    public String getVariableWithReplaces() {
        final String result = getWithReplaces();
        if (result.equals(body)) {
            return wrapPH(result);
        }
        return result;
    }

    private String wrapVar(String variable) {
        return Delimiters.VARIABLE_START_END.getSymbol() + variable + Delimiters.VARIABLE_START_END.getSymbol();
    }

    public String wrapPH() {
        return Delimiters.START_END.getSymbol() + rawPH + Delimiters.START_END.getSymbol();
    }

    private static String wrapPH(String placeholder) {
        return Delimiters.START_END.getSymbol() + placeholder + Delimiters.START_END.getSymbol();
    }

    public boolean isJson() {
        return getWithReplaces().contains(".json");
    }

    /**
     * @return пара путь/значение, вида docs@data.json/@var1@var2
     */
    public Pair<String, String> getJsonAndInnerPH() {
        final String[] untilAndAfterJson = getWithReplaces().split(".json");
        if (untilAndAfterJson.length != 2) {
            return null;
        }
        return new Pair<>(untilAndAfterJson[0] + ".json", untilAndAfterJson[1]);
    }

    public File getFile() {
        if (isJson()) {
            return getFile(getJsonAndInnerPH().getKey());
        } else {
            return getFile(getWithReplaces());
        }
    }

    private File getFile(String pathStr) {
        final String[] pathPieces = pathStr.split(Delimiters.DELIMITER.getSymbol());
        final Path fullPath = Paths.get(GlobalUtils.getProperties().get(Settings.MODULE_DIR), pathPieces);
        return fullPath.toFile();
    }

    /**
     * Объединяет объявленные переменные плейсхолдера с переменными, определенные его прородителем.<br/>
     * Значения не перетирают значений данного плейсхолдера.
     *
     * @param parentVariables результат объединения
     */
    public void mergeVariables(Map<String, String> parentVariables) {
        for (Map.Entry<String, String> variable : parentVariables.entrySet()) {
            if (!variables.containsKey(variable.getKey())) {
                variables.put(variable.getKey(), variable.getValue());
            }
        }
    }

    public boolean isVariable() {
        return rawPH.startsWith(Delimiters.VARIABLE_START_END.getSymbol()) && rawPH.endsWith(Delimiters.VARIABLE_START_END.getSymbol());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(body);
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            sb.append(Delimiters.VARIABLE_START_END.getSymbol())
                    .append(variable.getKey()).append("=").append(variable.getValue())
                    .append(Delimiters.VARIABLE_START_END.getSymbol());
        }
        sb.append(" ~").append(links);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placeholder that = (Placeholder) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, variables);
    }

    /*Getters & Setters*/
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getRawPH() {
        return rawPH;
    }

    public Map<String, Placeholder> getLinks() {
        return links;
    }

}
