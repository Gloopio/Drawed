package io.gloop.drawed.model;


import java.util.ArrayList;
import java.util.List;

import io.gloop.GloopObject;
import io.gloop.annotations.Serializer;
import io.gloop.drawed.serializers.PointsSerializer;

/**
 * Created by Alex Untertrifaller on 16.02.17.
 */

public class Line extends GloopObject {

    private int color;
    private int brushSize;

    @Serializer(PointsSerializer.class)
    private List<Point> points = new ArrayList<>();

    public Line() {
    }

    public Line(List<Point> line, int color, int brushSize) {
        this.points = line;
        this.color = color;
        this.brushSize = brushSize;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "Line{" +
                "color=" + color +
                ", brushSize=" + brushSize +
                ", points=" + points +
                '}';
    }

    public int getColor() {
        return color;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
