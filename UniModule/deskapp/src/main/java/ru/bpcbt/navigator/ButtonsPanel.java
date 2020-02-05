package ru.bpcbt.navigator;

import ru.bpcbt.Program;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.ReplaceTasksExecutor;
import ru.bpcbt.misc.ColoredButton;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.utils.Style;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.rest.TemplateUploader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ButtonsPanel extends JPanel {

    private ColoredButton saveB;
    private ColoredButton processSingleB;
    private ColoredButton processAllB;
    private ColoredButton uploadSingleB;
    private ColoredButton uploadAllB;

    ButtonsPanel(NavigatorPanel parent) {
        try {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            //обновить
            ColoredButton refreshB = new ColoredButton(getIconFromResource("/images/refresh.png"),
                    "Обновить список файлов",
                    Style.GREEN, Style.GREEN_B, Style.YELLOW);
            refreshB.addActionListener(e -> parent.refreshFiles());
            add(refreshB);
            //сохранить
            saveB = new ColoredButton(getIconFromResource("/images/save.png"),
                    "Сохранить открытый файл",
                    Style.GREEN, Style.GREEN_B, Style.YELLOW);
            saveB.addActionListener(e -> parent.saveCurrentFile());
            saveB.setEnabled(false);
            add(saveB);
            //выделить
            ColoredButton markB = new ColoredButton(getIconFromResource("/images/mark.png"),
                    "Разукрасить. Ну да, пока в ручную",
                    Style.YELLOW, Style.RED, Style.BLUE);
            markB.addActionListener(e -> parent.repaintTextToDisplay());
            add(markB);
            //открыть в проводнике
            ColoredButton openDir = new ColoredButton(getIconFromResource("/images/folder.png"),
                    "Открыть текущую папку в проводнике",
                    Style.BLUE, Style.BLUE_B, Style.YELLOW);
            openDir.addActionListener(e -> parent.openCurrentDir());
            add(openDir);
            //заменить для выбранного скелета
            processSingleB = new ColoredButton(getIconFromResource("/images/buildOne.png"),
                    "Сгенерировать файлы с заменой плейсхолдеров для выбранных скелетов",
                    Style.GREEN, Style.GREEN_B, Style.RED);
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
            processAllB = new ColoredButton(getIconFromResource("/images/buildMultiple.png"),
                    "Сгенерировать все файлы с заменой плейсхолдеров для всех скелетов",
                    Style.GREEN, Style.GREEN_B, Style.RED);
            processAllB.addActionListener(e -> ReplaceTasksExecutor.process(
                    FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.INPUT_DIR))));
            add(processAllB);
            //отправить одного
            uploadSingleB = new ColoredButton(getIconFromResource("/images/uploadOne.png"),
                    "Отправить выбранных во вкладке результатов на сервер",
                    Style.YELLOW, Style.YELLOW_B, Style.YELLOW);
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
            uploadAllB = new ColoredButton(getIconFromResource("/images/uploadMultiple.png"),
                    "Отправить всех с вкладки результатов на сервер",
                    Style.YELLOW, Style.YELLOW_B, Style.YELLOW);
            uploadAllB.addActionListener(e -> TemplateUploader.uploadJob(
                    FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.OUTPUT_DIR))).execute());
            add(uploadAllB);
        } catch (Exception e) {
            Narrator.yell("Что-то пошло не так при отрисовке кнопок: ", e);
        }
    }

    private ImageIcon getIconFromResource(String path) throws IOException {
        return new ImageIcon(ImageIO.read(Program.class.getResourceAsStream(path)));
    }

    public void setEnabledToProcessButtons(boolean isEnabled) {
        processSingleB.setEnabled(isEnabled);
        processAllB.setEnabled(isEnabled);
        uploadSingleB.setEnabled(isEnabled);
        uploadAllB.setEnabled(isEnabled);
    }

    void setEnabledToSaveButton(boolean enabled) {
        saveB.setEnabled(enabled);
    }
}
