package io.gloop.drawed.model;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 14.04.17.
 */

public class PrivateBoardRequest extends GloopObject {

    private String boardName;
    private String boardOwner;

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getBoardOwner() {
        return boardOwner;
    }

    public void setBoardOwner(String boardOwner) {
        this.boardOwner = boardOwner;
    }
}
