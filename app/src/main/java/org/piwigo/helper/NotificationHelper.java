package org.piwigo.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.piwigo.R;
import org.piwigo.ui.main.MainActivity;

public class NotificationHelper
{
// TODO: rework: CHANNEL_ID is defined but never used
    private final String CHANNEL_ID = "piwigo-info";
    public static NotificationHelper INSTANCE;
    private int progressStatus;

    public NotificationHelper(Context context)
    {
        INSTANCE = this;
        initChannels(context);
    }

    private String getChannelId()
    {
        return (CHANNEL_ID);
    }

    public int getProgressStatus()
    {
        return (progressStatus);
    }

    public void setProgressStatus(int status)
    {
        progressStatus = status;
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Piwigo",
                NotificationManager.IMPORTANCE_DEFAULT);
        // TODO: add a proper description and channel name from the resources
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    public void sendNotification(String title, String body, Context context)
    {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context,
                1, i,
                PendingIntent.FLAG_ONE_SHOT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pi)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }
}
