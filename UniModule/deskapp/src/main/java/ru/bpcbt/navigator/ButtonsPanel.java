package ru.bpcbt.navigator;

import ru.bpcbt.Program;
import ru.bpcbt.misc.HoverButton;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.ReplaceTasksExecutor;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.rest.TemplateWorker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ButtonsPanel extends JPanel {

    private HoverButton saveB;
    private HoverButton processSingleB;
    private HoverButton processAllB;
    private HoverButton uploadSingleB;
    private HoverButton uploadAllB;
    private HoverButton reserveSingleB;
    private HoverButton reserveAllB;

    ButtonsPanel(BaseNavigatorTreePanel parent) {
        try {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            //обновить
            HoverButton refreshB = new HoverButton(getIconFromResource("/images/refresh.png"),
                    "Обновить список файлов");
            refreshB.addActionListener(e -> parent.refreshFiles());
            add(refreshB);
            //сохранить
            saveB = new HoverButton(getIconFromResource("/images/save.png"),
                    "Сохранить открытый файл");
            saveB.addActionListener(e -> parent.saveCurrentFile());
            saveB.setEnabled(false);
            add(saveB);
            //выделить
            HoverButton markB = new HoverButton(getIconFromResource("/images/mark.png"),
                    "Разукрасить. Ну да, пока в ручную");
            markB.addActionListener(e -> parent.repaintTextToDisplay());
            add(markB);
            //открыть в проводнике
            HoverButton openDir = new HoverButton(getIconFromResource("/images/folder.png"),
                    "Открыть текущую папку в проводнике");
            openDir.addActionListener(e -> parent.openCurrentDir());
            add(openDir);
            //заменить для выбранных скелетов
            processSingleB = new HoverButton(getIconFromResource("/images/buildOne.png"),
                    "Сгенерировать файлы с заменой плейсхолдеров для выбранных скелетов");
            processSingleB.addActionListener(e -> {
                Set<File> selectedFiles = Program.getMainFrame().getInputFilesPanel().getSelectedFiles();
                if (selectedFiles.isEmpty()) {
                    MiniFrame.showMessage("Нужно выбрать что собирать." +
                            System.lineSeparator() +
                            "Для этого нужно выделить что-нибудь из вкладки со скелетами.");
                } else {
                    ReplaceTasksExecutor.process(selectedFiles);
                }
            });
            add(processSingleB);
            //заменить для всех скелетов
            processAllB = new HoverButton(getIconFromResource("/images/buildMultiple.png"),
                    "Сгенерировать все файлы с заменой плейсхолдеров для всех скелетов");
            processAllB.addActionListener(e -> ReplaceTasksExecutor.process(
                    FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.INPUT_DIR))));
            add(processAllB);
            //отправить выбранных
            uploadSingleB = new HoverButton(getIconFromResource("/images/uploadOne.png"),
                    "Отправить выбранных во вкладке результатов или резервации на сервер");
            uploadSingleB.addActionListener(e -> {
                Set<File> selectedFiles;
                if (Settings.RESERVE_DIR.equals(parent.getWorkingDirType())) {
                    selectedFiles = Program.getMainFrame().getReserveFilesPanel().getSelectedFiles();
                } else {
                    selectedFiles = Program.getMainFrame().getOutputFilesPanel().getSelectedFiles();
                }
                if (selectedFiles.isEmpty()) {
                    MiniFrame.showMessage("Нужно выбрать что отправлять." +
                            System.lineSeparator() +
                            "Для этого нужно выделить что-нибудь из вкладки с результатами или резервации.");
                } else {
                    TemplateWorker.uploadJob(selectedFiles).execute();
                }
            });
            add(uploadSingleB);
            //отправить всех
            uploadAllB = new HoverButton(getIconFromResource("/images/uploadMultiple.png"),
                    "Отправить всех с вкладки результатов.");
            if (Settings.RESERVE_DIR.equals(parent.getWorkingDirType())) {
                uploadAllB.addActionListener(e ->
                        MiniFrame.showMessage("Воу-воу ты же не хочешь отправить на сервер резервные копии за все время!?" +
                                System.lineSeparator() + "Выбери данные за какую-нибудь дату и грузи их кнопкой левее!"));
            } else {
                uploadAllB.addActionListener(e -> TemplateWorker.uploadJob(
                        FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.OUTPUT_DIR))).execute());
            }
            add(uploadAllB);

            //зарезервировать выбранных
            reserveSingleB = new HoverButton(getIconFromResource("/images/reserveOne.png"),
                    "Выгрузить выбранных во вкладке резервации с сервера");
            reserveSingleB.addActionListener(e -> {
                List<String> selectedTemplates = Program.getMainFrame().getReserveFilesPanel().getSelectedTemplates();
                if (selectedTemplates.isEmpty()) {
                    MiniFrame.showMessage("Нужно выбрать что резервировать." +
                            System.lineSeparator() +
                            "Для этого нужно выделить что-нибудь из вкладки с резервацией.");
                } else {
                    TemplateWorker.downloadJob(selectedTemplates).execute();
                }
            });
            add(reserveSingleB);
            //зарезервировать всех
            reserveAllB = new HoverButton(getIconFromResource("/images/reserveAll.png"),
                    "Выгрузить всё с сервера");
            reserveAllB.addActionListener(e -> {
                Map<String, Long> templateIdMap = TemplateWorker.getTemplateIdMap();
                if (templateIdMap != null) {
                    TemplateWorker.downloadJob(templateIdMap.keySet()).execute();
                }
            });
            add(reserveAllB);
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
        reserveSingleB.setEnabled(isEnabled);
        reserveAllB.setEnabled(isEnabled);
    }

    void setEnabledToSaveButton(boolean enabled) {
        saveB.setEnabled(enabled);
    }
}
