package ru.bpcbt.logger;

import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.Style;

import javax.swing.*;

public class Narrator {

    private static final JLabel label;

    static {
        label = new JLabel("Добро пожаловать! При первом запуске нужно указать три папки в настройках");
        label.setOpaque(true);
    }

    private Narrator() {//Utils class
    }

    public static JLabel getLabel() {
        return label;
    }

    public static void yell(String message, Exception e) {
        yell(GlobalUtils.getErrorMessageWithException(message, e));
    }

    public static void yell(String message) {
        JOptionPane.showMessageDialog(null, message, "Обнаружена новая фича!", JOptionPane.WARNING_MESSAGE);
    }

    public static void normal(String message) {
        label.setBackground(Style.GRAY);
        label.setText(message);
    }

    public static void success(String message) {
        label.setBackground(Style.GREEN);
        label.setText(message);
    }

    public static void warn(String message) {
        label.setBackground(Style.YELLOW);
        label.setText(message);
    }

    public static void error(String message) {
        label.setBackground(Style.RED);
        label.setText(message);
    }
}
