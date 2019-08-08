package ru.bpcbt.entity;

import java.util.Map;
import java.util.Objects;

public class ReplaceTask implements Comparable<ReplaceTask> {
    private String rawPlaceholder;
    private String content;
    private Map<String, String> parentVariables;
    private int priority;

    public ReplaceTask(String rawPlaceholder, String content, Map<String, String> parentVariables) {
        this(rawPlaceholder, content, parentVariables, 0);
    }

    public ReplaceTask(String rawPlaceholder, String content, Map<String, String> parentVariables, int priority) {
        this.rawPlaceholder = rawPlaceholder;
        this.content = content;
        this.parentVariables = parentVariables;
        this.priority = priority;
    }

    public String getRawPlaceholder() {
        return rawPlaceholder;
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> getParentVariables() {
        return parentVariables;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceTask replaceTask = (ReplaceTask) o;
        return Objects.equals(rawPlaceholder, replaceTask.getRawPlaceholder()) &&
                Objects.equals(content, replaceTask.getContent()) &&
                Objects.equals(parentVariables, replaceTask.getParentVariables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawPlaceholder, content, parentVariables);
    }

    @Override
    public int compareTo(ReplaceTask o) {
        return o.getPriority() - this.priority;
    }
}