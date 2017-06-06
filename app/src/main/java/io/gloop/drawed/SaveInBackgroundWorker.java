package io.gloop.drawed;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;
import io.gloop.drawed.utils.ScreenUtil;

class SaveInBackgroundWorker extends Thread {
    private final Queue<Line> queue;
    private final Board board;

    private boolean run = true;

    SaveInBackgroundWorker(Board board) {
        this.queue = new LinkedList<>();
        this.board = board;
    }

    void stopWorker() {
        synchronized (queue) {
            run = false;
            queue.notifyAll();
        }
    }

    @Override
    public void run() {
        while (run) {
            try {
                Line newLine;

                synchronized (queue) {
                    while (queue.isEmpty() && run) {
                        queue.wait();
                    }

                    if (!run && queue.isEmpty())
                        break;

                    // Get the next work item off of the queue
                    newLine = queue.poll();
                }

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

    void addItem(Line newLine) {
        synchronized (queue) {
            queue.add(newLine);
            queue.notifyAll();
        }


    }

    void addItem(List<Point> points, int paintColor, float brushSize) {
        Line line = new Line(points, paintColor, (int) brushSize);
        synchronized (queue) {
            queue.add(line);
            queue.notifyAll();
        }
    }
}