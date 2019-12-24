package ru.bpcbt.utils;

import javax.swing.*;

public class MiniFrame {

    private MiniFrame() {// Utils class
    }

    public static boolean askForConfirmation(String message) {
        return JOptionPane.showConfirmDialog(null, message) == 0;//0 — это ДА
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static String askPassword() {
        final JPanel panel = new JPanel();
        final JLabel label = new JLabel("Введите пароль:");
        final JPasswordField pass = new JPasswordField(20);
        panel.add(label);
        panel.add(pass);
        final String[] options = new String[]{"Готово", "Не буду"};
        pass.grabFocus();
        final int option = JOptionPane.showOptionDialog(null, panel, "Пароль для доступа к api",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, pass);
        if (option == 0) {
            final char[] password = pass.getPassword();
            return new String(password);
        }
        return null;
    }
}
