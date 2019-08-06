package ru.bpcbt.utils;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwingWorkerExecutor {
    private static final int MAX_WORKER_THREAD = 9;
    private static final SwingWorkerExecutor executor = new SwingWorkerExecutor();

    private ExecutorService workerThreadPool = Executors.newFixedThreadPool(MAX_WORKER_THREAD);

    private SwingWorkerExecutor() {//Utils class
    }

    public static SwingWorkerExecutor getExecutor() {
        return executor;
    }

    public void execute(SwingWorker worker) {
        workerThreadPool.submit(worker);
    }
}