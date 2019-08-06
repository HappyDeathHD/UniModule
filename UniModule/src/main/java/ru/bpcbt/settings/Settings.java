package ru.bpcbt.settings;

public enum Settings {
    INPUT_DIR("Директория содержащая файлы с плейсхолдерами"),
    MODULE_DIR("Корневая директория с модулями"),
    OUTPUT_DIR("Директория для сохранения результатов подстановок"),
    FONT_NAME("Шрифт, использующийся в текстовых окнах"),
    FONT_SIZE("Размер шрифта"),
    CORE_URL("Адрес до api"),
    USERNAME("Логин"),
    PASSWORD("Пароль (не сохраняется)");

    private String description;

    Settings(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
