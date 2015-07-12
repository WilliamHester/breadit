package me.williamhester.models;

/**
 * Created by william on 7/11/15.
 */
public class OauthLogin {

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String scope;
    private long expires_in;

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

}
