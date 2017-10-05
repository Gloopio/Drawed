package io.gloop.drawed.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.gloop.Gloop;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.permissions.GloopGroup;

import static io.gloop.drawed.utils.NotificationUtil.NOTIFICATION_ID;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String YES_ACTION = "YES_ACTION";
    public static final String NO_ACTION = "NO_ACTION";
    public static final String ACCESS_REQUEST = "ACCESS_REQUEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        BoardAccessRequest request = (BoardAccessRequest) intent.getSerializableExtra(ACCESS_REQUEST);

        if (YES_ACTION.equals(action)) {
            GloopGroup group = Gloop
                    .all(GloopGroup.class)
                    .where()
                    .equalsTo("objectId", request.getBoardGroupId())
                    .first();
            group.addMember(request.getUserId());
            group.save();

            Gloop.all(BoardAccessRequest.class).where().equalsTo("objectId", request.getObjectId()).first().delete();
//            request.delete();

            Toast.makeText(context, "Successfully grant access to user " + request.getUserId(), Toast.LENGTH_SHORT).show();

        } else if (NO_ACTION.equals(action)) {
            Toast.makeText(context, "User " + request.getUserId() + " has no access to the board.", Toast.LENGTH_SHORT).show();

            request.delete();
        }

        closeNotification(context, intent);
    }

    private void closeNotification(Context context, Intent intent) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
}