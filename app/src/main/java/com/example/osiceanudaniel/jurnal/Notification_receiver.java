package com.example.osiceanudaniel.jurnal;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class Notification_receiver extends BroadcastReceiver{

    public static final int PENDING_INTENT_REQUEST_CODE = 879;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, MainPageActivity.class);
        // replace the old activity if it is running in the background
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pI = PendingIntent.getActivity(context,
                PENDING_INTENT_REQUEST_CODE,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pI)
                .setSmallIcon(R.mipmap.notification_icon)
                .setContentTitle(context.getString(R.string.notificationTitle))
                .setContentText(context.getString(R.string.notificationMessage))
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://"
                        + context.getPackageName() + "/" + R.raw.plucky));

        notificationManager.notify(PENDING_INTENT_REQUEST_CODE, builder.build());
    }
}
