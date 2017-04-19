package io.gloop.drawed.model;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 14.04.17.
 */

public class PrivateBoardRequest extends GloopObject {

    private String boardName;
    private String boardCreator;
    private String groupId;

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getBoardCreator() {
        return boardCreator;
    }

    public void setBoardCreator(String boardCreator) {
        this.boardCreator = boardCreator;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}
