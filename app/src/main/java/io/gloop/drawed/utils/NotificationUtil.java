package io.gloop.drawed.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import io.gloop.GloopLogger;
import io.gloop.drawed.R;
import io.gloop.drawed.SplashActivity;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.drawed.receivers.NotificationReceiver;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class NotificationUtil {

    public static void show(Context ctx, BoardAccessRequest accessRequest) {
        GloopLogger.i("Grant access to user via notification");

        Intent intent = new Intent(ctx, SplashActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Drawed")
                .setContentTitle("Grant user access to private board")
                .setContentText("Give user: " + accessRequest.getUserId() + " access to board: " + accessRequest.getBoardName())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");

        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction(NotificationReceiver.YES_ACTION);
        yesReceive.putExtra(NotificationReceiver.ACCESS_REQUEST, accessRequest);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(ctx, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        b.addAction(R.drawable.ic_done_black_24dp, "Yes", pendingIntentYes);

        //No intent
        Intent noReceive = new Intent();
        noReceive.setAction(NotificationReceiver.NO_ACTION);
        yesReceive.putExtra(NotificationReceiver.ACCESS_REQUEST, accessRequest);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(ctx, 12345, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        b.addAction(R.drawable.ic_clear_black_24dp, "No", pendingIntentNo);


        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }
}
