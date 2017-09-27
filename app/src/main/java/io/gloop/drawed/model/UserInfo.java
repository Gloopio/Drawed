package io.gloop.drawed.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 20.09.17.
 */

public class UserInfo extends GloopObject {

    private String email;
    private String imageURL;
    private String userName;
    private List<String> favoriesBoardId = new ArrayList<>();

    public UserInfo() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getImageURL() {
        if (imageURL != null)
            return Uri.parse(imageURL);
        else
            return null;
    }

    public void setImageURL(Uri imageURL) {
        if (imageURL != null)
            this.imageURL = imageURL.toString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addFavoriteBoardId(String boardId) {
        if (this.favoriesBoardId == null)
            this.favoriesBoardId = new ArrayList<>();
        this.favoriesBoardId.add(boardId);
    }

    public void removeFavoriteBoardId(String boardId) {
        if (this.favoriesBoardId != null)
            this.favoriesBoardId.remove(boardId);
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getFavoritesBoardId() {
        if (favoriesBoardId == null)
            return new ArrayList<>();
        else
            return favoriesBoardId;
    }

    public void setFavoritesBoardId(List<String> favoriesBoardId) {
        this.favoriesBoardId = favoriesBoardId;
    }
}
