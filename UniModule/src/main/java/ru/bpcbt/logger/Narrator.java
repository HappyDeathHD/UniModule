package ru.bpcbt.logger;

import ru.bpcbt.utils.Style;

import javax.swing.*;

public class Narrator {

    private static JLabel label;

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
        final StringBuilder builder = new StringBuilder(message);
        if (e != null) {
            builder.append(System.lineSeparator()).append(e.getClass().getCanonicalName());
            if (e.getMessage() != null) {
                builder.append(System.lineSeparator()).append(e.getMessage());
            }
            if (e.getStackTrace().length != 0) {
                builder.append(System.lineSeparator()).append(e.getStackTrace()[0]);
            }
        }
        yell(builder.toString());
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

    public static void blue(String message) {
        label.setBackground(Style.BLUE);
        label.setText(message);
    }
}
