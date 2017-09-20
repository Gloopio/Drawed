package io.gloop.drawed.model;

import android.net.Uri;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 20.09.17.
 */

public class UserInfo extends GloopObject {

    private String email;
    private String imageURL;
    private String userName;


    public UserInfo() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getImageURL() {
        return Uri.parse(imageURL);
    }

    public void setImageURL(Uri imageURL) {
        this.imageURL = imageURL.toString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
