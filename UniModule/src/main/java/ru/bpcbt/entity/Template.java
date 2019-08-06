package ru.bpcbt.entity;

import java.util.Objects;

public class Template {
    private String name;
    private String language;

    public Template(String name, String language) {
        this.name = name;
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(name, template.name) &&
                Objects.equals(language, template.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, language);
    }
}
