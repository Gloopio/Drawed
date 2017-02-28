package io.gloop.drawed;

import java.util.Queue;

import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;

public class SaveInBackgroundWorker extends Thread {
    private static int instance = 0;
    private final Queue<Line> queue;
    private Board board;

    public SaveInBackgroundWorker(Queue queue, Board board) {
        this.queue = queue;
        this.board = board;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Line newLine;

                synchronized (queue) {
                    while (queue.isEmpty())
                        queue.wait();

                    // Get the next work item off of the queue
                    newLine = queue.poll();
                }

                // Process the work item
                board.addLine(newLine);
                board.save();
//                newLine.save();   // TODO save in bakground. can be done outside the thread by calling saveInBackground
            } catch (InterruptedException ie) {
                break;  // Terminate
            }
        }
    }
}