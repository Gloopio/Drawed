package io.gloop.drawed.model;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 04.04.17.
 */

public class BoardAccessRequest extends GloopObject {

    private String boardName;
    private String boardGroupId;
    private String boardCreator;
    private String userId;

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }


    public void setBoardCreator(String boardCreator) {
        this.boardCreator = boardCreator;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBoardGroupId() {
        return boardGroupId;
    }

    public void setBoardGroupId(String boardGroupId) {
        this.boardGroupId = boardGroupId;
    }

    public String getBoardCreator() {
        return boardCreator;
    }
}
