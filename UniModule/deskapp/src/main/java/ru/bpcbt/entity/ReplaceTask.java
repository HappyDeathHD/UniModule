package ru.bpcbt.entity;

import java.util.Map;
import java.util.Objects;

public class ReplaceTask implements Comparable<ReplaceTask> {

    private final String rawPlaceholder;
    private final String content;
    private final Map<String, String> parentVariables;
    private final int priority;

    public ReplaceTask(String rawPlaceholder, String content, Map<String, String> parentVariables) {
        this(rawPlaceholder, content, parentVariables, 0);
    }

    public ReplaceTask(String rawPlaceholder, String content, Map<String, String> parentVariables, int priority) {
        this.rawPlaceholder = rawPlaceholder;
        this.content = content;
        this.parentVariables = parentVariables;
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceTask replaceTask = (ReplaceTask) o;
        return Objects.equals(rawPlaceholder, replaceTask.getRawPlaceholder())
                && Objects.equals(content, replaceTask.getContent())
                && Objects.equals(parentVariables, replaceTask.getParentVariables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawPlaceholder, content, parentVariables);
    }

    @Override
    public int compareTo(ReplaceTask o) {
        return o.getPriority() - this.priority;
    }

    /*Getters & Setters*/
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
}