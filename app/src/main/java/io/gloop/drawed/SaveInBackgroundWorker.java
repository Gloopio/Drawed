package io.gloop.drawed;

import java.util.LinkedList;
import java.util.Queue;

import io.gloop.GloopLogger;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;

public class SaveInBackgroundWorker extends Thread {
    private final Queue<Line> queue;
    private Board board;

    private boolean run = true;

    public SaveInBackgroundWorker(Board board) {
//        this.queue = queue;
        this.queue = new LinkedList<>();
        this.board = board;
    }

    public void stopWorker() {
        synchronized (queue) {
            GloopLogger.i("Stop worker thread.");
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
                        GloopLogger.i("wake up");
                    }

                    if (!run && queue.isEmpty())
                        break;

                    GloopLogger.i("get new line to save");
                    // Get the next work item off of the queue
                    newLine = queue.poll();
                }

                GloopLogger.i("save line");
                // Process the work item
                newLine.setUser(board.getOwner(), board.getPermission());  // TODO find a way to do this in the sdk. (All objects inside another object need to have the same owner.)
                board.addLine(newLine);

                GloopLogger.i("board user: " + board.getOwner());

                board.save();

                GloopLogger.i("line saved");
            } catch (InterruptedException ie) {
                break;  // Terminate
            }
        }
        GloopLogger.i("Service stopped");
    }

    public void addItem(Line newLine) {
        synchronized (queue) {
            queue.add(newLine);
            queue.notifyAll();
        }
    }
}