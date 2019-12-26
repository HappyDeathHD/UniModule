package ru.bpcbt.misc;

public enum Delimiters {
    START_END("##", "Символ, с которого все плэйсхолдеры должны начинаться и заканчиваться"),
    DELIMITER("@", "Разделитель всех сущностей внутри плейсхолдера"),
    VARIABLE_START_END("!", "Символ, которым выделяются переменные с начала и конца"),
    LINK_START_END("~", "Символ, с которого начинаются и заканчиваются значения переменных ссылочного типа");

    private final String symbol;
    private final String description;

    Delimiters(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    /*Getters & Setters*/
    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }
}
