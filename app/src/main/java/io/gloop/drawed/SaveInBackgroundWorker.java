package io.gloop.drawed;

import java.util.LinkedList;
import java.util.Queue;

import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.utils.ScreenUtil;

class SaveInBackgroundWorker extends Thread {
    private final Queue<Line> queue;
    private Board board;

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
                    newLine = ScreenUtil.normalize(newLine);

                    board.addLine(newLine);
                    board.save();
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
}