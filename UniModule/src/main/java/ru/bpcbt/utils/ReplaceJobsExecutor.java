package ru.bpcbt.utils;

import javafx.util.Pair;
import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.entity.ReplaceJob;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.misc.Delimiters;
import ru.bpcbt.entity.Placeholder;
import ru.bpcbt.settings.Settings;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ReplaceJobsExecutor {
    private static final int MAX_WORKER_THREAD = 9; // одновременно могут работать 10 свинка-воркеров, но один из них менеджер среднего звена
    private static AtomicInteger workersCount = new AtomicInteger(0);

    private static PriorityBlockingQueue<ReplaceJob> jobs = new PriorityBlockingQueue<>();
    private static Map<Placeholder, String> foundReplacements = new ConcurrentHashMap<>();
    private static Queue<Placeholder> notFoundReplacements = new ConcurrentLinkedQueue<>();

    private static int mainJobsCount;
    private static AtomicInteger mainJobsDone = new AtomicInteger(0);

    private ReplaceJobsExecutor() { // Utils class
    }

    public static void process(List<File> files) {
        Program.clearReport();
        Program.setEnabledToProcessButtons(false);
        Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
        //не json
        List<File> commonInputFiles = files.stream()
                .filter(f -> !f.getPath().contains(Const.CONFLICT_PREFIX) && !f.getPath().contains(".json"))
                .collect(Collectors.toList());
        commonInputFiles.forEach(f -> jobs.add(new ReplaceJob(cutThePath(f), FileUtils.readFile(f), new HashMap<>())));
        //json
        files.stream().filter(f -> f.getPath().contains(".json")).forEach(file -> jobs.addAll(JsonUtils.parseSkeleton(file)));
        mainJobsCount = ReplaceJobsExecutor.jobs.size();
        Narrator.normal("Сейчас мы соберем " + mainJobsCount + " файл(-ов)");
        createWorkersManager().execute();
    }

    private static SwingWorker createWorkersManager() {
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                workersCount.set(0);
                while (mainJobsCount > mainJobsDone.get()) {
                    if (workersCount.get() < MAX_WORKER_THREAD && !jobs.isEmpty()) {
                        workersCount.incrementAndGet();
                        takeOneJob(jobs.poll()).execute();
                    }
                }
                makeConclusion();
                return null;
            }
        };
    }

    private static String cutThePath(File file) {
        return file.getPath().replace(Program.getProperties().get(Settings.INPUT_DIR), "").substring(1);
    }

    private static SwingWorker takeOneJob(ReplaceJob job) {
        return new SwingWorker() {
            private boolean isModule() {
                return job.getPriority() > 0;
            }

            @Override
            protected Object doInBackground() {
                try {
                    String newFileContent = job.getContent();
                    Set<Placeholder> allPlaceholders = getAllPlaceholders(newFileContent);
                    boolean jobDone = true;
                    if (!allPlaceholders.isEmpty()) {
                        for (Placeholder placeholder : allPlaceholders) {
                            placeholder.mergeVariables(job.getParentVariables());
                            if (isLinksDone(placeholder, job.getPriority())) {
                                String phValue = getContentForPlaceholder(placeholder, job.getPriority());
                                if (phValue != null) {
                                    newFileContent = newFileContent.replace(placeholder.wrapPH(), phValue);
                                } else {
                                    jobDone = false;
                                }
                            }
                        }
                    }
                    if (jobDone) {
                        if (!isModule()) {
                            FileUtils.writeResultFile(job.getRawPlaceholder(), newFileContent);
                            mainJobsDone.incrementAndGet();
                            Program.appendToReport("Файл " + job.getRawPlaceholder() + " успешно сгенерирован!", Style.GREEN);
                        } else {
                            Placeholder placeholder = new Placeholder(job.getRawPlaceholder());
                            placeholder.mergeVariables(job.getParentVariables());
                            foundReplacements.put(placeholder, newFileContent);
                            Program.appendToReport("Плейсхолдер " + placeholder + " успешно собран", Style.GREEN_B);
                        }
                    } else {
                        jobs.add(new ReplaceJob(job.getRawPlaceholder(), newFileContent, job.getParentVariables(), job.getPriority()));
                    }
                } catch (Exception e) {
                    Program.appendToReport("Ошибка в потоке, обрабатывающем файл " + job.getRawPlaceholder(), e);
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
                workersCount.getAndDecrement();
            }
        };
    }

    private static boolean isLinksDone(Placeholder placeholder, int priority) {
        boolean linksDone = true;
        for (Map.Entry<String, Placeholder> link : placeholder.getLinks().entrySet()) {
            link.getValue().mergeVariables(placeholder.getVariables());
            Map<String, String> linkVariables = new HashMap<>();
            String linkValue = getContentForPlaceholder(link.getValue(), priority + 1);
            if (linkValue != null) {
                linkVariables.put(link.getKey(), linkValue);
                placeholder.mergeVariables(linkVariables);
            } else {
                linksDone = false;
            }
        }
        return linksDone;
    }

    private static String getContentForPlaceholder(Placeholder placeholder, int priority) {
        if (placeholder.isVariable()) {
            String variable = placeholder.getVariableWithReplaces();
            if (variable.startsWith(Delimiters.START_END.getSymbol())) { // не все переменные были определены
                notFoundReplacements.add(placeholder);
                Program.appendToReport("Не все переменные плейсхолдера " + placeholder + " были определены", Style.RED_B);
            }
            return variable;
        }
        if (foundReplacements.containsKey(placeholder)) {
            return foundReplacements.get(placeholder);
        } else if (notFoundReplacements.contains(placeholder)) {
            return placeholder.wrapPH();
        }
        if (FileUtils.isFileExists(placeholder.getFile())) {
            String content;
            if (placeholder.isJson()) {
                Pair<String, String> jsonAndInnerPH = placeholder.getJsonAndInnerPH();
                Map<String, String> parsedJson = JsonUtils.parseModule(placeholder.getFile());
                if (parsedJson.containsKey(jsonAndInnerPH.getValue())) {
                    content = parsedJson.get(jsonAndInnerPH.getValue());
                    jobs.add(new ReplaceJob(placeholder.getRawPH(), content, placeholder.getVariables(), priority + 1));
                } else {
                    notFoundReplacements.add(placeholder);
                    Program.appendToReport("Плейсхолдер " + placeholder + " не был найден в json", Style.RED_B);
                    return placeholder.wrapPH();
                }
            } else {
                content = FileUtils.readAndCacheFileContent(placeholder.getFile());
                if (content.isEmpty()) {
                    notFoundReplacements.add(placeholder);
                    Program.appendToReport("Плейсхолдер " + placeholder + " не был найден в файле", Style.RED_B);
                    return placeholder.wrapPH();
                } else {
                    jobs.add(new ReplaceJob(placeholder.getRawPH(), content, placeholder.getVariables(), priority + 1));
                }
            }
            return null;
        } else {
            notFoundReplacements.add(placeholder);
            Program.appendToReport("Плейсхолдер " + placeholder + " ведет к несуществующему файлу", Style.RED_B);
            return placeholder.wrapPH();
        }
    }

    private static Set<Placeholder> getAllPlaceholders(String text) {
        Set<Placeholder> placeholders = new HashSet<>();
        String sym = Delimiters.START_END.getSymbol();
        boolean isOdd = false;
        for (String pieceOfText : text.split(sym)) {
            if (isOdd) {
                placeholders.add(new Placeholder(pieceOfText));
            }
            isOdd = !isOdd;
        }
        return placeholders;
    }

    private static void makeConclusion() {
        Program.getMainFrame().getOutputFilesPanel().refreshFiles();
        if (notFoundReplacements.isEmpty()) {
            if (foundReplacements.isEmpty()) {
                Program.appendToReport(System.lineSeparator() + "Не было ни одного плейсхолдера!", Style.YELLOW);
                Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
                Narrator.warn("Не было ни одного плейсхолдера!");
            } else {
                Program.appendToReport(System.lineSeparator() + "Все необходимые модули были найдены (" + foundReplacements.size() + " шт.)",
                        Style.GREEN);
                Program.getMainFrame().setPaneTab(MainFrame.OUTPUTS_TAB);
                Narrator.success("Все готово без ошибок!");
            }
        } else {
            Program.appendToReport(System.lineSeparator() +
                            "Есть не найденные модули (" + notFoundReplacements.size() + " шт) (" + foundReplacements.size() + " найдено):" +
                            System.lineSeparator() + notFoundReplacements.stream().map(Placeholder::toString)
                            .collect(Collectors.joining(System.lineSeparator())),
                    Style.RED);
            Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
            Narrator.error("Все прошло не очень гладко!");
        }
        refresh();
    }

    public static void refresh() {
        foundReplacements.clear();
        notFoundReplacements.clear();
        jobs.clear();
        workersCount.set(0);
        mainJobsDone.set(0);
        JsonUtils.refresh();
        FileUtils.refresh();
        Program.setEnabledToProcessButtons(true);
    }
}
