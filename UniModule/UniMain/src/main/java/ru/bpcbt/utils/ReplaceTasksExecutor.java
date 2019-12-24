package ru.bpcbt.utils;

import javafx.util.Pair;
import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.entity.ReplaceTask;
import ru.bpcbt.logger.Narrator;
import ru.bpcbt.logger.ReportPane;
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

public class ReplaceTasksExecutor {
    /**
     * Одновременно могут работать 10 свинка-воркеров, но один из них менеджер среднего звена
     */
    private static final int MAX_WORKER_THREAD = 9;
    private static final AtomicInteger workersCount = new AtomicInteger(0);

    private static final PriorityBlockingQueue<ReplaceTask> tasks = new PriorityBlockingQueue<>();
    private static final Map<Placeholder, String> foundReplacements = new ConcurrentHashMap<>();
    private static final Queue<Placeholder> notFoundReplacements = new ConcurrentLinkedQueue<>();

    private static int mainJobsCount;
    private static final AtomicInteger mainJobsDone = new AtomicInteger(0);

    private ReplaceTasksExecutor() { // Utils class
    }

    public static void process(List<File> files) {
        ReportPane.clearReport();
        GlobalUtils.setEnabledToProcessButtons(false);
        Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
        //не json
        final List<File> commonInputFiles = files.stream()
                .filter(f -> !f.getPath().contains(Const.CONFLICT_PREFIX) && !f.getPath().contains(".json"))
                .collect(Collectors.toList());
        commonInputFiles.stream().map(file -> new ReplaceTask(cutThePath(file), FileUtils.readFile(file), FileUtils.getVariableMapWithLocale(file)))
                .forEach(tasks::add);
        //json
        files.stream().filter(f -> f.getPath().contains(".json")).forEach(file -> tasks.addAll(JsonUtils.parseSkeleton(file)));
        mainJobsCount = ReplaceTasksExecutor.tasks.size();
        Narrator.normal("Сейчас мы соберем " + mainJobsCount + " файл(-ов)");
        createWorkersManager().execute();
    }

    private static SwingWorker createWorkersManager() {
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                int maxThreadsCount = Runtime.getRuntime().availableProcessors(); //кол-во ядер (x2 при поддержке гиперпоточности)
                final long start = System.currentTimeMillis();
                ReportPane.normal("Начало собрки: " + new Date(start) +
                        System.lineSeparator() + "Количество ядер процессора: " + maxThreadsCount);
                if (maxThreadsCount > MAX_WORKER_THREAD) {
                    maxThreadsCount = MAX_WORKER_THREAD;
                }
                ReportPane.normal("Количество потоков: " + maxThreadsCount +
                        System.lineSeparator() + "Количество файлов для сборки: " + mainJobsCount);
                workersCount.set(0);
                while (mainJobsCount > mainJobsDone.get()) {
                    if (workersCount.get() < maxThreadsCount && !tasks.isEmpty()) {
                        workersCount.incrementAndGet();
                        getSwingWorkerWithTask(tasks.poll()).execute();
                    }
                }
                ReportPane.normal("На сборку ушло " + (System.currentTimeMillis() - start) + "мс.");
                makeConclusion();
                return null;
            }
        };
    }

    private static String cutThePath(File file) {
        return file.getPath().replace(GlobalUtils.getProperties().get(Settings.INPUT_DIR), "").substring(1);
    }

    private static boolean isLinksDone(Placeholder placeholder, int priority) {
        boolean linksDone = true;
        for (Map.Entry<String, Placeholder> link : placeholder.getLinks().entrySet()) {
            link.getValue().mergeVariables(placeholder.getVariables());
            final Map<String, String> linkVariables = new HashMap<>();
            final String linkValue = getContentForPlaceholder(link.getValue(), priority + 1);
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
            final String variable = placeholder.getVariableWithReplaces();
            if (variable.startsWith(Delimiters.START_END.getSymbol())) { // не все переменные были определены
                notFoundReplacements.add(placeholder);
                ReportPane.warning("Не все переменные плейсхолдера " + placeholder + " были определены");
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
                final Pair<String, String> jsonAndInnerPH = placeholder.getJsonAndInnerPH();
                final Map<String, String> parsedJson = JsonUtils.parseModule(placeholder.getFile());
                if (parsedJson.containsKey(jsonAndInnerPH.getValue())) {
                    content = parsedJson.get(jsonAndInnerPH.getValue());
                    tasks.add(new ReplaceTask(placeholder.getRawPH(), content, placeholder.getVariables(), priority + 1));
                } else {
                    notFoundReplacements.add(placeholder);
                    ReportPane.error("Плейсхолдер " + placeholder + " не был найден в json");
                    return placeholder.wrapPH();
                }
            } else {
                content = FileUtils.readAndCacheFileContent(placeholder.getFile());
                if (content.isEmpty()) {
                    notFoundReplacements.add(placeholder);
                    ReportPane.error("Плейсхолдер " + placeholder + " не был найден в файле");
                    return placeholder.wrapPH();
                } else {
                    tasks.add(new ReplaceTask(placeholder.getRawPH(), content, placeholder.getVariables(), priority + 1));
                }
            }
            return null;
        } else {
            notFoundReplacements.add(placeholder);
            ReportPane.error("Плейсхолдер " + placeholder + " ведет к несуществующему файлу");
            return placeholder.wrapPH();
        }
    }

    private static Set<Placeholder> getAllPlaceholders(String text) {
        final Set<Placeholder> placeholders = new HashSet<>();
        final String sym = Delimiters.START_END.getSymbol();
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
                ReportPane.warning(System.lineSeparator() + "Не было ни одного плейсхолдера!");
                Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
                Narrator.warn("Не было ни одного плейсхолдера!");
            } else {
                ReportPane.success(System.lineSeparator() + "Все необходимые модули были найдены (" + foundReplacements.size() + " шт.)");
                Program.getMainFrame().setPaneTab(MainFrame.OUTPUTS_TAB);
                Narrator.success("Все готово без ошибок!");
            }
        } else {
            ReportPane.error(System.lineSeparator() +
                    "Есть не найденные модули (" + notFoundReplacements.size() + " шт) (" + foundReplacements.size() + " найдено):" +
                    System.lineSeparator() + notFoundReplacements.stream().map(Placeholder::toString)
                    .collect(Collectors.joining(System.lineSeparator())));
            Program.getMainFrame().setPaneTab(MainFrame.REPORT_TAB);
            Narrator.error("Все прошло не очень гладко!");
        }
        refresh();
    }

    static void refresh() {
        foundReplacements.clear();
        notFoundReplacements.clear();
        tasks.clear();
        workersCount.set(0);
        mainJobsDone.set(0);
        JsonUtils.refresh();
        FileUtils.refresh();
        GlobalUtils.setEnabledToProcessButtons(true);
    }

    private static SwingWorker getSwingWorkerWithTask(ReplaceTask task) {
        return new SwingWorker() {
            @Override
            protected Object doInBackground() {
                try {
                    String newFileContent = task.getContent();
                    final Set<Placeholder> allPlaceholders = getAllPlaceholders(newFileContent);
                    boolean jobDone = true;
                    if (!allPlaceholders.isEmpty()) {
                        for (Placeholder placeholder : allPlaceholders) {
                            placeholder.mergeVariables(task.getParentVariables());
                            if (isLinksDone(placeholder, task.getPriority())) {
                                final String phValue = getContentForPlaceholder(placeholder, task.getPriority());
                                if (phValue != null) {
                                    newFileContent = newFileContent.replace(placeholder.wrapPH(), phValue);
                                } else {
                                    jobDone = false;
                                }
                            } else {
                                jobDone = false;
                            }
                        }
                    }
                    if (jobDone) {
                        if (!isModule()) {
                            FileUtils.writeResultFile(task.getRawPlaceholder(), newFileContent);
                            mainJobsDone.incrementAndGet();
                            ReportPane.success("Файл " + task.getRawPlaceholder() + " успешно сгенерирован!");
                        } else {
                            final Placeholder placeholder = new Placeholder(task.getRawPlaceholder());
                            placeholder.mergeVariables(task.getParentVariables());
                            foundReplacements.put(placeholder, newFileContent);
                            ReportPane.debug("Плейсхолдер " + placeholder + " успешно собран"); //узкое место
                        }
                    } else {
                        tasks.add(new ReplaceTask(task.getRawPlaceholder(), newFileContent, task.getParentVariables(), task.getPriority()));
                    }
                } catch (Exception e) {
                    ReportPane.error("Ошибка в потоке, обрабатывающем файл " + task.getRawPlaceholder(), e);
                } finally {
                    workersCount.getAndDecrement();
                }
                return null;
            }

            private boolean isModule() {
                return task.getPriority() > 0;
            }
        };
    }
}