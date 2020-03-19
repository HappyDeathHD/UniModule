package ru.bpcbt.navigator;

import ru.bpcbt.Program;
import ru.bpcbt.misc.HoverButton;
import ru.bpcbt.settings.Settings;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.ReplaceTasksExecutor;
import ru.bpcbt.utils.MiniFrame;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.rest.UnimessageConductor;

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
    private HoverButton processSelectedB;
    private HoverButton processAllB;
    private HoverButton uploadSelectedB;
    private HoverButton uploadAllB;
    private HoverButton reserveSelectedB;
    private HoverButton reserveAllB;

    ButtonsPanel(BaseNavigatorTreePanel parent) {
        try {
            setLayout(new FlowLayout(FlowLayout.LEFT));

            addRefreshButton(parent);
            addSaveButton(parent);
            addMarkButton(parent);
            addExplorerButton(parent);
            addProcessSelectedButton();
            addProcessAllButton();
            addUploadSelectedButton(parent);
            addUploadAllButton(parent);
            addReserveSelectedButton();
            addReserveAllButton();
        } catch (Exception e) {
            Narrator.yell("Что-то пошло не так при отрисовке кнопок:", e);
        }
    }

    public void setEnabledToProcessButtons(boolean isEnabled) {
        processSelectedB.setEnabled(isEnabled);
        processAllB.setEnabled(isEnabled);
        uploadSelectedB.setEnabled(isEnabled);
        uploadAllB.setEnabled(isEnabled);
        reserveSelectedB.setEnabled(isEnabled);
        reserveAllB.setEnabled(isEnabled);
    }

    void setEnabledToSaveButton(boolean enabled) {
        saveB.setEnabled(enabled);
    }

    private ImageIcon getIconFromResource(String path) throws IOException {
        return new ImageIcon(ImageIO.read(Program.class.getResourceAsStream(path)));
    }

    private void addRefreshButton(BaseNavigatorTreePanel parent) throws IOException {
        HoverButton refreshB = new HoverButton(getIconFromResource("/images/refresh.png"),
                "Обновить список файлов");
        refreshB.addActionListener(e -> parent.refreshFiles());
        add(refreshB);
    }

    private void addSaveButton(BaseNavigatorTreePanel parent) throws IOException {
        saveB = new HoverButton(getIconFromResource("/images/save.png"),
                "Сохранить открытый файл");
        saveB.addActionListener(e -> parent.saveCurrentFile());
        saveB.setEnabled(false);
        add(saveB);
    }

    private void addMarkButton(BaseNavigatorTreePanel parent) throws IOException {
        HoverButton markB = new HoverButton(getIconFromResource("/images/mark.png"),
                "Разукрасить. Ну да, пока в ручную");
        markB.addActionListener(e -> parent.repaintTextToDisplay());
        add(markB);
    }

    private void addExplorerButton(BaseNavigatorTreePanel parent) throws IOException {
        HoverButton openDir = new HoverButton(getIconFromResource("/images/folder.png"),
                "Открыть текущую папку в проводнике");
        openDir.addActionListener(e -> parent.openCurrentDir());
        add(openDir);
    }

    private void addProcessSelectedButton() throws IOException {
        processSelectedB = new HoverButton(getIconFromResource("/images/buildOne.png"),
                "Сгенерировать файлы с заменой плейсхолдеров для выбранных скелетов");
        processSelectedB.addActionListener(e -> {
            Set<File> selectedFiles = Program.getMainFrame().getInputFilesPanel().getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                MiniFrame.showMessage("Нужно выбрать что собирать." + System.lineSeparator()
                        + "Для этого нужно выделить что-нибудь из вкладки со скелетами.");
            } else {
                ReplaceTasksExecutor.process(selectedFiles);
            }
        });
        add(processSelectedB);
    }

    private void addProcessAllButton() throws IOException {
        processAllB = new HoverButton(getIconFromResource("/images/buildMultiple.png"),
                "Сгенерировать все файлы с заменой плейсхолдеров для всех скелетов");
        processAllB.addActionListener(e -> ReplaceTasksExecutor.process(
                FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.INPUT_DIR))));
        add(processAllB);
    }

    private void addUploadSelectedButton(BaseNavigatorTreePanel parent) throws IOException {
        uploadSelectedB = new HoverButton(getIconFromResource("/images/uploadOne.png"),
                "Отправить выбранных во вкладке результатов или резервации на сервер");
        uploadSelectedB.addActionListener(e -> {
            Set<File> selectedFiles;
            if (Settings.RESERVE_DIR.equals(parent.getWorkingDirType())) {
                selectedFiles = Program.getMainFrame().getReserveFilesPanel().getSelectedFiles();
            } else {
                selectedFiles = Program.getMainFrame().getOutputFilesPanel().getSelectedFiles();
            }
            if (selectedFiles.isEmpty()) {
                MiniFrame.showMessage("Нужно выбрать что отправлять."
                        + System.lineSeparator()
                        + "Для этого нужно выделить что-нибудь из вкладки с результатами или резервации.");
            } else {
                UnimessageConductor.uploadJob(selectedFiles).execute();
            }
        });
        add(uploadSelectedB);
    }

    private void addUploadAllButton(BaseNavigatorTreePanel parent) throws IOException {
        uploadAllB = new HoverButton(getIconFromResource("/images/uploadMultiple.png"),
                "Отправить всех с вкладки результатов.");
        if (Settings.RESERVE_DIR.equals(parent.getWorkingDirType())) {
            uploadAllB.addActionListener(e ->
                    MiniFrame.showMessage("Воу-воу ты же не хочешь отправить на сервер резервные копии за все время!?"
                            + System.lineSeparator()
                            + "Выбери данные за какую-нибудь дату и грузи их кнопкой левее!"));
        } else {
            uploadAllB.addActionListener(e -> {
                if (MiniFrame.askForConfirmation("Уверен, что хочешь отправить на сервер всё что есть?")) {
                    UnimessageConductor.uploadJob(FileUtils.getFilesByTypeRecursively(Program.getProperties().get(Settings.OUTPUT_DIR)))
                            .execute();
                }
            });
        }
        add(uploadAllB);
    }

    private void addReserveSelectedButton() throws IOException {
        reserveSelectedB = new HoverButton(getIconFromResource("/images/reserveOne.png"),
                "Выгрузить выбранных во вкладке резервации с сервера");
        reserveSelectedB.addActionListener(e -> {
            List<String> selectedTemplates = Program.getMainFrame().getReserveFilesPanel().getSelectedTemplates();
            if (selectedTemplates.isEmpty()) {
                MiniFrame.showMessage("Нужно выбрать что резервировать."
                        + System.lineSeparator()
                        + "Для этого нужно выделить что-нибудь из вкладки с резервацией.");
            } else {
                UnimessageConductor.downloadJob(selectedTemplates).execute();
            }
        });
        add(reserveSelectedB);
    }

    private void addReserveAllButton() throws IOException {
        reserveAllB = new HoverButton(getIconFromResource("/images/reserveAll.png"),
                "Выгрузить всё с сервера");
        reserveAllB.addActionListener(e -> {
            Map<String, Long> templateIdMap = UnimessageConductor.getTemplateIdMap();
            if (templateIdMap != null) {
                UnimessageConductor.downloadJob(templateIdMap.keySet()).execute();
            }
        });
        add(reserveAllB);
    }
}
