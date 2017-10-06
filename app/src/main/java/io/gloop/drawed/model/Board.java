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

    private BoardInfo boardInfo = new BoardInfo(getObjectId());
    private List<Line> lines = new ArrayList<>();

    public Board() {
        super();
    }

    public String getName() {
        return this.boardInfo.getName();
    }

    public void setName(String name) {
        this.boardInfo.setName(name);
    }

    @Override
    public void setUser(String userId, int permission) {
        super.setUser(userId, permission);
        boardInfo.setUser(userId,permission);
    }

    @Override
    public void save() {
        boardInfo.save();
        super.save();
    }

    public void addLine(Line line) {
        this.lines.add(line);
        this.boardInfo.setSize(lines.size());
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
        this.boardInfo.setSize(lines.size());
    }

    public int getColor() {
        return this.boardInfo.getColor();
    }

    public void setColor(int color) {
        this.boardInfo.setColor(color);
    }

    public void clear() {
        this.lines = new ArrayList<>();
        this.save();
    }

    public boolean isPrivateBoard() {
        return this.boardInfo.isPrivateBoard();
    }

    public void setPrivateBoard(boolean privateBoard) {
        this.boardInfo.setPrivateBoard(privateBoard);
    }

    public boolean isFreezeBoard() {
        return this.boardInfo.isFreezeBoard();
    }

    public void setFreezeBoard(boolean freezeBoard) {
        this.boardInfo.setFreezeBoard(freezeBoard);
    }

    public Map<String, String> getMembers() {
        return this.boardInfo.getMembers();
    }

    public void setMembers(Map<String, String> members) {
        this.boardInfo.setMembers(members);
    }

    public void addMember(String email, String imageUri) {
        if (this.boardInfo.getMembers() == null) {
            this.boardInfo.setMembers(new HashMap<String, String>());
        }
        this.boardInfo.addMember(email, imageUri);
    }

    public void removeMemeber(String email) {
        if (this.boardInfo.getMembers() != null) {
            this.boardInfo.getMembers().remove(email);
        }
    }
}
