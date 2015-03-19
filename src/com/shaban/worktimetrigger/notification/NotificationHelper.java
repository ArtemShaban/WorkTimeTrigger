package com.shaban.worktimetrigger.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.shaban.worktimetrigger.R;
import com.shaban.worktimetrigger.StartActivity;

/**
 * Created by Artem on 19.03.2015.
 */
public class NotificationHelper
{
    private Context context;

    public NotificationHelper(Context context)
    {
        this.context = context;
    }

    public void notify(String message)
    {
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setContentTitle("Work time trigger")
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher);
        Intent resultIntent = new Intent(context, StartActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


}


