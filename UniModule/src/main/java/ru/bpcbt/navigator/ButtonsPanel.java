package ru.bpcbt.navigator;

import ru.bpcbt.Program;
import ru.bpcbt.utils.ReplaceTasksExecutor;
import ru.bpcbt.misc.ColoredButton;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.utils.Style;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.rest.TemplateUploader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class ButtonsPanel extends JPanel {

    private ColoredButton refreshB;
    private ColoredButton saveB;
    private ColoredButton markB;
    private ColoredButton openDir;
    private ColoredButton processSingleB;
    private ColoredButton processAllB;
    private ColoredButton uploadSingleB;
    private ColoredButton uploadAllB;

    public ButtonsPanel(NavigatorPanel parent) {
        try {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            //обновить
            refreshB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/refresh.png"))));
            refreshB.setToolTipText("Обновить список файлов");
            refreshB.setBackground(Style.GREEN);
            refreshB.setHoverBackgroundColor(Style.GREEN_B);
            refreshB.setPressedBackgroundColor(Style.YELLOW);
            refreshB.addActionListener(e -> parent.refreshFiles());
            add(refreshB);
            //сохранить
            saveB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/save.png"))));
            saveB.setToolTipText("Сохранить открытый файл");
            saveB.addActionListener(e -> parent.saveCurrentFile());
            saveB.setBackground(Style.GREEN);
            saveB.setHoverBackgroundColor(Style.GREEN_B);
            saveB.setPressedBackgroundColor(Style.YELLOW);
            saveB.setEnabled(false);
            add(saveB);
            //выделить
            markB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/mark.png"))));
            markB.setToolTipText("Разукрасить. Ну да, пока в ручную");
            markB.setBackground(Style.YELLOW);
            markB.setHoverBackgroundColor(Style.RED);
            markB.setPressedBackgroundColor(Style.BLUE);
            markB.addActionListener(e -> parent.repaintTextToDisplay());
            add(markB);
            //открыть в проводнике
            openDir = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/folder.png"))));
            openDir.setToolTipText("Открыть текущую папку в проводнике");
            openDir.setBackground(Style.BLUE);
            openDir.setHoverBackgroundColor(Style.BLUE_B);
            openDir.setPressedBackgroundColor(Style.YELLOW);
            openDir.addActionListener(e -> parent.openCurrentDir());
            add(openDir);
            //заменить для выбранного скелета
            processSingleB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/buildOne.png"))));
            processSingleB.setToolTipText("Сгенерировать файлы с заменой плейсхолдеров для выбранных скелетов");
            processSingleB.setBackground(Style.GREEN);
            processSingleB.setHoverBackgroundColor(Style.GREEN_B);
            processSingleB.setPressedBackgroundColor(Style.RED);
            processSingleB.addActionListener(e -> {
                NavigatorPanel inputPanel = Program.getMainFrame().getInputFilesPanel();
                if (inputPanel.getSelectedFiles().isEmpty()) {
                    MiniFrame.showMessage("Нужно выбрать что собирать." +
                            System.lineSeparator() +
                            "Для этого нужно выделить что-нибудь из вкладки со скелетами.");
                } else {
                    ReplaceTasksExecutor.process(inputPanel.getSelectedFiles());
                }
            });
            add(processSingleB);
            //заменить для всех скелетов
            processAllB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/buildMultiple.png"))));
            processAllB.setToolTipText("Сгенерировать все файлы с заменой плейсхолдеров для всех скелетов");
            processAllB.setBackground(Style.GREEN);
            processAllB.setHoverBackgroundColor(Style.GREEN_B);
            processAllB.setPressedBackgroundColor(Style.RED);
            processAllB.addActionListener(e -> {
                NavigatorPanel inputPanel = Program.getMainFrame().getInputFilesPanel();
                ReplaceTasksExecutor.process(inputPanel.getFileList());
            });
            add(processAllB);
            //отправить одного
            uploadSingleB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/uploadOne.png"))));
            uploadSingleB.setToolTipText("Отправить выбранных во вкладке результатов на сервер");
            uploadSingleB.setBackground(Style.YELLOW);
            uploadSingleB.setHoverBackgroundColor(Style.YELLOW_B);
            uploadSingleB.setPressedBackgroundColor(Style.YELLOW);
            uploadSingleB.addActionListener(e -> {
                NavigatorPanel outputPanel = Program.getMainFrame().getOutputFilesPanel();
                if (outputPanel.getSelectedFiles().isEmpty()) {
                    MiniFrame.showMessage("Нужно выбрать что отправлять." +
                            System.lineSeparator() +
                            "Для этого нужно выделить что-нибудь из вкладки с результатами.");
                } else {
                    TemplateUploader.uploadJob(outputPanel.getSelectedFiles()).execute();
                }
            });
            add(uploadSingleB);
            //отправить всех
            uploadAllB = new ColoredButton(new ImageIcon(ImageIO.read(Program.class.getResourceAsStream("/images/uploadMultiple.png"))));
            uploadAllB.setToolTipText("Отправить всех с вкладки результатов на сервер");
            uploadAllB.setBackground(Style.YELLOW);
            uploadAllB.setHoverBackgroundColor(Style.YELLOW_B);
            uploadAllB.setPressedBackgroundColor(Style.YELLOW);
            uploadAllB.addActionListener(e -> {
                NavigatorPanel outputPanel = Program.getMainFrame().getOutputFilesPanel();
                TemplateUploader.uploadJob(outputPanel.getFileList()).execute();
            });
            add(uploadAllB);
        } catch (Exception e) {
            Narrator.yell("Что-то пошло не так при отрисовке кнопок: ", e);
        }
    }

    public ColoredButton getSaveB() {
        return saveB;
    }

    public void setEnabledToProcessButtons(boolean isEnabled) {
        processSingleB.setEnabled(isEnabled);
        processAllB.setEnabled(isEnabled);
        uploadSingleB.setEnabled(isEnabled);
        uploadAllB.setEnabled(isEnabled);
    }
}
