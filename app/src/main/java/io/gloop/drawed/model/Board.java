package io.gloop.drawed.model;

import java.util.ArrayList;
import java.util.List;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 16.02.17.
 */
public class Board extends GloopObject {

    private boolean privateBoard;
    private boolean freezeBoard;
    private String name;
    private int color;
    private List<Line> lines = new ArrayList<>();

    public Board() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addLine(Line line) {
        this.lines.add(line);
    }

    public List<Line> getLines() {
        return lines;
    }

    @Override
    public String toString() {
        return "Board{" +
                "name='" + name + '\'' +
                ", lines=" + lines +
                '}';
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void clear() {
        this.lines = new ArrayList<>();
        this.save();
    }
}
