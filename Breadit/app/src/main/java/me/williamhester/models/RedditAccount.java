package me.williamhester.models;

import io.realm.RealmObject;

/**
 * This is the database object for the Realm database that holds the accounts.
 */
public class RedditAccount extends RealmObject {

    private String accessToken;
    private String refreshToken;
    private String username;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
