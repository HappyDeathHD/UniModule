package ru.bpcbt.settings;

public enum Settings {
    INPUT_DIR("Директория содержащая файлы с плейсхолдерами"),
    MODULE_DIR("Корневая директория с модулями"),
    OUTPUT_DIR("Директория для сохранения результатов подстановок"),
    RESERVE_DIR("Директория для сохранения резервных копий шаблонов"),
    FONT_NAME("Шрифт, использующийся в текстовых окнах"),
    STYLE("Стиль"),
    FONT_SIZE("Размер шрифта"),
    CORE_URL("Адрес до api"),
    USERNAME("Логин"),
    PASSWORD("Пароль (не сохраняется)"),
    DEBUG("Выводить в отчет отладочную информацию (Замедляет процесс сборки)");

    private final String description;

    Settings(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
