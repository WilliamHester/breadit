package me.williamhester.ui.activities;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import me.williamhester.notifications.MessageNotificationBroadcastReceiver;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.MessagesFragment;

/**
 * Created by william on 12/20/14.
 */
public class MessageActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message);

        if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MessagesFragment())
                    .commit();
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(MessageNotificationBroadcastReceiver.ID);
    }
}
