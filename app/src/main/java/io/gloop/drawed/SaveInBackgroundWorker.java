package io.gloop.drawed;

import android.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.gloop.GloopLogger;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;
import io.gloop.drawed.utils.ScreenUtil;

class SaveInBackgroundWorker extends Thread {
    private final Queue<Pair<Board, Line>> queue;

    private boolean run = true;

    private static volatile SaveInBackgroundWorker instance = null;

    static SaveInBackgroundWorker getInstance() {
        if (instance == null) {
            synchronized (SaveInBackgroundWorker.class) {
                if (instance == null)
                    instance = new SaveInBackgroundWorker();
            }
        }
        return instance;
    }

    private SaveInBackgroundWorker() {
        this.queue = new LinkedList<>();
        this.start();
    }

//    void startWorker() {
//        synchronized (queue) {
//            run = true;
//            queue.notifyAll();
//        }
//    }

    void stopWorker() {
        synchronized (queue) {
            run = false;
            queue.notifyAll();
        }
        instance = null;
        GloopLogger.i("stopWorker");
    }

    @Override
    public void run() {
        while (run) {
            try {
                Pair<Board, Line> pair;

                synchronized (queue) {
                    while (queue.isEmpty() && run) {
                        GloopLogger.i("wait");
                        queue.wait();
                        GloopLogger.i("wake up");
                    }

                    if (!run && queue.isEmpty())
                        break;

                    // Get the next work item off of the queue
                    pair = queue.poll();
                }

                Line newLine = pair.second;
                Board board = pair.first;
                // Process the work item
                if (newLine != null) {
                    newLine.setBrushSize((int) ScreenUtil.normalize(newLine.getBrushSize()));
                    newLine = ScreenUtil.normalize(newLine);

//                    synchronized (pair.first) {
                    board.addLine(newLine);
                    board.save();
                    GloopLogger.i("object saved");
//                    }
                }

            } catch (InterruptedException ie) {
                break;  // Terminate
            }
        }
        GloopLogger.i("stop");
    }


    void addItem(Board board, List<Point> points, int paintColor, float brushSize) {
//        if (!isAlive())
//            instance.start();

        run = true;
        Line line = new Line(points, paintColor, (int) brushSize);
        synchronized (queue) {
            queue.add(new Pair<>(board, line));

            if (!isAlive())
                instance = new SaveInBackgroundWorker();

            queue.notifyAll();
            GloopLogger.i("object added");
        }
    }
}