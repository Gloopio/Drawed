package io.gloop.drawed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.gloop.Gloop;
import io.gloop.drawed.model.BoardAccessRequest;
import io.gloop.permissions.GloopGroup;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String YES_ACTION = "YES_ACTION";
    public static final String NO_ACTION = "NO_ACTION";
    public static final String ACCESS_REQUEST = "ACCESS_REQUEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        BoardAccessRequest request = (BoardAccessRequest) intent.getSerializableExtra(ACCESS_REQUEST);

        if (YES_ACTION.equals(action)) {
            Toast.makeText(context, "YES CALLED", Toast.LENGTH_SHORT).show();

            GloopGroup group = Gloop
                    .all(GloopGroup.class)
                    .where()
                    .equalsTo("objectId", request.getBoardGroupId())
                    .first();
            group.addMember(request.getUserId());
            group.save();

            request.delete();

        } else if (NO_ACTION.equals(action)) {
            Toast.makeText(context, "STOP CALLED", Toast.LENGTH_SHORT).show();

            request.delete();
        }
    }
}