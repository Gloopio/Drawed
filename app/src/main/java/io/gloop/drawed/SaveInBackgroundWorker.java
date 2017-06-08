package io.gloop.drawed;

import android.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;
import io.gloop.drawed.utils.ScreenUtil;

public class SaveInBackgroundWorker extends Thread {
    private final Queue<Pair<Board, Line>> queue;

    private boolean run = true;

    private static volatile SaveInBackgroundWorker instance = null;

    public static SaveInBackgroundWorker getInstance() {
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

    void stopWorker() {
        synchronized (queue) {
            run = false;
            queue.notifyAll();
        }
        instance = null;
    }

    @Override
    public void run() {
        while (run) {
            try {
                Pair<Board, Line> pair;

                synchronized (queue) {
                    while (queue.isEmpty() && run) {
                        queue.wait();
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
                    newLine.setUser(board.getOwner(), board.getPermission());  // TODO find a way to do this in the sdk. (All objects inside another object need to have the same owner.)
                    newLine.setBrushSize((int) ScreenUtil.normalize(newLine.getBrushSize()));
                    newLine = ScreenUtil.normalize(newLine);

                    synchronized (board) {
                        board.addLine(newLine);
                        board.save();
                    }
                }

            } catch (InterruptedException ie) {
                break;  // Terminate
            }
        }
    }

    void addItem(Board board, Line newLine) {
        synchronized (queue) {
            queue.add(new Pair<>(board, newLine));
            queue.notifyAll();
        }


    }

    void addItem(Board board, List<Point> points, int paintColor, float brushSize) {
        Line line = new Line(points, paintColor, (int) brushSize);
        synchronized (queue) {
            queue.add(new Pair<>(board, line));
            queue.notifyAll();
        }
    }
}