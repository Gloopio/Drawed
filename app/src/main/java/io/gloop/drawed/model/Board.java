package io.gloop.drawed.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 16.02.17.
 */
public class Board extends GloopObject {

    private boolean privateBoard = false;
    private boolean freezeBoard = false;
    private String name;
    private int color;

    private List<Line> lines = new ArrayList<>();

    private Map<String, String> members = new HashMap<>();

    public Board() {
        super();
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

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "Board{" +
                "name='" + name + '\'' +
                ", lines=" + lines +
                '}';
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void clear() {
        this.lines = new ArrayList<>();
        this.save();
    }

    public boolean isPrivateBoard() {
        return privateBoard;
    }

    public void setPrivateBoard(boolean privateBoard) {
        this.privateBoard = privateBoard;
    }

    public boolean isFreezeBoard() {
        return freezeBoard;
    }

    public void setFreezeBoard(boolean freezeBoard) {
        this.freezeBoard = freezeBoard;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    public void addMember(String email, String imageUri) {
        if (this.members == null) {
            this.members = new HashMap<>();
        }
        this.members.put(email, imageUri);
    }

    public void removeMemeber(String email) {
        if (this.members != null)
            this.members.remove(email);
    }
}
