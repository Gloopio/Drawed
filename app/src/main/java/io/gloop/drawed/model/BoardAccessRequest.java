package io.gloop.drawed.model;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 04.04.17.
 */

public class BoardAccessRequest extends GloopObject {

    private String boardName;
    private String boardOwner;
    private String userId;

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    @Override
    public String getOwner() {
        return boardOwner;
    }

    public void setBoardOwner(String boardOwner) {
        this.boardOwner = boardOwner;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
