package ru.bpcbt.utils;

import ru.bpcbt.Program;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.misc.ColoredButton;

import javax.swing.*;
import java.awt.*;

public class MiniFrame {

    private MiniFrame() { // Utils class
    }

    public static boolean askForConfirmation(String message) {
        return JOptionPane.showConfirmDialog(null, message) == 0; // 0 — это ДА
    }

    static void showUpdateMessage(String changelog, boolean isForce) {
        Narrator.warn("Версия приложения устарела!");
        Program.getMainFrame().setTitle(Program.getMainFrame().getTitle() + " [Устаревшая версия]");
        JLabel textArea = new JLabel("<html><body style='height:380px'>"
                + "<h2>Появилась новая версия<br/>с фиксом старых/добавлением новых багов:</h2><br/>"
                + "<div style='width:380px;margin:0 auto;'>" + changelog + "</div></body></html>");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);
        JOptionPane op = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = op.createDialog("Обновление!");
        dialog.setAlwaysOnTop(false);
        dialog.setModal(isForce);
        if (isForce) {
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        } else {
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        ColoredButton confirmButton = new ColoredButton("Обновляемся!", Style.GREEN);
        confirmButton.addActionListener(e -> UpdateUtils.update());
        Object[] buttons = {confirmButton};
        op.setOptions(buttons);
        dialog.setVisible(true);
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
