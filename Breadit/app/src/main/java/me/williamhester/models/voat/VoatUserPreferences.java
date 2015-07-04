package me.williamhester.models.voat;

/**
 * Created by william on 7/4/15.
 */
public class VoatUserPreferences {
    private boolean disableCustomCSS;
    private boolean enableNightMode;
    private boolean openLinksNewWindow;
    private boolean enableAdultContent;
    private boolean publiclyDisplayVotes;
    private boolean publiclyDisplaySubscriptions;
    private String language;

    public boolean isNsfwEnabled() {
        return enableAdultContent;
    }
}
