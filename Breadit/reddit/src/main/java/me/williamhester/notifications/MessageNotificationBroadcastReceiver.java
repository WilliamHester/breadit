package me.williamhester.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.models.GenericListing;
import me.williamhester.models.GenericResponseRedditWrapper;
import me.williamhester.models.Message;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.MessageActivity;

/**
 * Created by william on 12/22/14.
 */
public class MessageNotificationBroadcastReceiver extends BroadcastReceiver {

    public static final int ID = 0;

    @Override
    public void onReceive(final Context context, Intent intent) {
        RedditApi.getMessages(context, Message.UNREAD, null, new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e != null) {
                    // do something?
                    return;
                }
                Gson gson = new Gson();

                // Generics are just beautiful.
                TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>> token =
                        new TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>>() {};

                GenericResponseRedditWrapper<GenericListing<Message>> wrapper =
                        gson.fromJson(result, token.getType());
                GenericListing<Message> listing = wrapper.getData();
                ArrayList<Message> messages = new ArrayList<>();

                for (GenericResponseRedditWrapper<Message> message : listing.getChildren()) {
                    messages.add(message.getData());
                }

                if (messages.size() > 0) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_breadit)
                            .setContentTitle(messages.size() + " " + context.getResources()
                                    .getQuantityString(R.plurals.new_messages, messages.size()))
                            .setContentText(messages.size() + " " + context.getString(R.string.unread_messages))
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setVibrate(new long[] {0, 300, 200, 300})
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setColor(context.getResources().getColor(R.color.auburn_orange))
                            .setLights(0xffffff, 1000, 5000)
                            .setAutoCancel(true)
                            .setPublicVersion(new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_breadit)
                                    .setContentTitle(messages.size() + " " + context.getResources()
                                            .getQuantityString(R.plurals.new_messages, messages.size()))
                                    .setContentText(messages.size() + " " + context.getString(R.string.unread_messages))
                                    .setColor(context.getResources().getColor(R.color.auburn_orange))
                                    .build());

                    NotificationCompat.InboxStyle inboxStyle =
                            new NotificationCompat.InboxStyle();
                    String[] messageTexts = new String[Math.min(6, messages.size())];
                    for (int i = 0; i < Math.min(6, messages.size()); i++) {
                        Message m = messages.get(i);
                        messageTexts[i] = m.getAuthor() + ": "
                                + Html.fromHtml(Html.fromHtml(m.getBodyHtml()).toString()).toString();
                    }
                    // Sets a title for the Inbox in expanded layout
                    inboxStyle.setBigContentTitle(messages.size() + " "
                            + context.getResources()
                                    .getQuantityString(R.plurals.new_messages, messages.size()));
                    // Moves messageTexts into the expanded layout
                    for (String message : messageTexts) {
                        inboxStyle.addLine(message);
                    }
                    // Moves the expanded layout object into the notification object.
                    builder.setStyle(inboxStyle);
                    // Issue the notification here.

                    Intent messageIntent = new Intent(context, MessageActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(MessageActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(messageIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(ID, builder.build());
                }
            }
        });
    }

}
