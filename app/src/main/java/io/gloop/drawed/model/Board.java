package io.gloop.drawed.model;

import java.util.HashMap;
import java.util.Map;

import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 16.02.17.
 */
public class Board extends GloopObject {

    private boolean privateBoard = false;
    private boolean freezeBoard = false;
    private String name;
    private int color;

//    @Ignore
//    private GloopList<Line> lines;

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
        line.setBoardId(this.getObjectId());
        line.setUser(getOwner(), getPermission());
        line.save();
    }

    public GloopList<Line> getLines() {
//        if (lines == null)
//            lines = Gloop.all(Line.class).where().equalsTo("boardId", this.getObjectId()).all();
//        return lines;
        return Gloop.all(Line.class).where().equalsTo("boardId", this.getObjectId()).all();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void clear() {
//        if (lines == null)
//            lines = Gloop.all(Line.class).where().equalsTo("boardId", this.getObjectId()).all();
//        lines.clear();
        Gloop.all(Line.class).where().equalsTo("boardId", this.getObjectId()).all().clear();
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
