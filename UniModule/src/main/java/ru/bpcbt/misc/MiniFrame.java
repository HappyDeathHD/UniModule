package ru.bpcbt.misc;

import javax.swing.*;

public class MiniFrame {

    public static boolean confirmUnsavedChanges() {
        return JOptionPane.showConfirmDialog(null, "Все внесенные изменения канут в лету, пофиг?") == 0;//0 — это ДА
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static String askPassword() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Введите пароль:");
        JPasswordField pass = new JPasswordField(20);
        panel.add(label);
        panel.add(pass);
        String[] options = new String[]{"Готово", "Не буду"};
        pass.grabFocus();
        int option = JOptionPane.showOptionDialog(null, panel, "Пароль для доступа к api",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, pass);
        if (option == 0) {
            char[] password = pass.getPassword();
            return new String(password);
        }
        return null;
    }
}
