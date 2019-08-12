package ru.bpcbt;

import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;

public class Program {
    private static MainFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                GlobalUtils.makeSovietRussiaButtons();
                mainFrame = new MainFrame();
                mainFrame.getSettingsPanel().loadConfigurations();
                GlobalUtils.refreshAllFiles();
            } catch (Exception e) {
                Narrator.yell("Вот это поворот!", e);
            }
        });
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }
}
