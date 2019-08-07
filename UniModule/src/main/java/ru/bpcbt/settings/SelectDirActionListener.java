package ru.bpcbt.settings;

import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

public class SelectDirActionListener implements ActionListener {

    private Component parent;
    private JTextField selectedDirTF;
    private Settings property;

    SelectDirActionListener(Component parent, JTextField selectedDirTF, Settings property) {
        this.parent = parent;
        this.selectedDirTF = selectedDirTF;
        this.property = property;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setLocale(Locale.getDefault());
        chooser.updateUI();
        String workingDir = selectedDirTF.getText().trim();
        if (FileUtils.isDirExists(workingDir)) {
            chooser.setCurrentDirectory(new File(workingDir));
        } else {
            chooser.setCurrentDirectory(new File("."));
        }
        chooser.setDialogTitle(property.getDescription());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String newWorkingDir = String.valueOf(chooser.getSelectedFile());
            selectedDirTF.setText(newWorkingDir);
        } else {
            Narrator.normal("Для смены дирекории надо нажать на \"Выбрать\"");
        }
    }
}
