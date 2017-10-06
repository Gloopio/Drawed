package io.gloop.drawed.model;

import java.util.HashMap;
import java.util.Map;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 06.10.17.
 */

public class BoardInfo extends GloopObject {

    private String boardId;
    private boolean privateBoard = false;
    private boolean freezeBoard = false;
    private String name;
    private int color;
    private int size = 0;
    private Map<String, String> members = new HashMap<>();

    public BoardInfo() {}

    public BoardInfo(String boardId) {
        this.boardId = boardId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
