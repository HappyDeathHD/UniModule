package ru.bpcbt.entity;

import java.util.Map;
import java.util.Objects;

public class ReplaceJob implements Comparable<ReplaceJob> {
    private String rawPlaceholder;
    private String content;
    private Map<String, String> parentVariables;
    private int priority;

    public ReplaceJob(String rawPlaceholder, String content, Map<String, String> parentVariables) {
        this(rawPlaceholder, content, parentVariables, 0);
    }

    public ReplaceJob(String rawPlaceholder, String content, Map<String, String> parentVariables, int priority) {
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
        ReplaceJob replaceJob = (ReplaceJob) o;
        return Objects.equals(rawPlaceholder, replaceJob.getRawPlaceholder()) &&
                Objects.equals(content, replaceJob.getContent()) &&
                Objects.equals(parentVariables, replaceJob.getParentVariables());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawPlaceholder, content, parentVariables);
    }

    @Override
    public int compareTo(ReplaceJob o) {
        return o.getPriority() - this.priority;
    }
}