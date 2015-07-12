package me.williamhester.models;

/**
 * Created by william on 7/11/15.
 */
public class RedditAboutMe {
    private boolean has_mail;
    private boolean hide_from_robots;
    private boolean has_mod_mail;
    private boolean over_18;
    private boolean is_gold;
    private boolean is_mod;
    private boolean has_verified_email;
    private double created;
    private double created_utc;
    private int gold_creddits;
    private int link_karma;
    private int comment_karma;
    private int inbox_count;
    private String name;
    private Object gold_expiration;
    private String id;

    public String getUsername() {
        return name;
    }
}
