package com.example.woundmontoringapplicationv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

/**
 *
 */
public class AlertReceiver extends BroadcastReceiver {

    /**
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
       App notificationHelper = new App(context);

       String contentDescription = "It's time to start monitoring Dressing " + intent.getStringExtra("QRID") + ", which is located on your " + intent.getStringExtra("location");

       NotificationCompat.Builder builder = notificationHelper.getChannelNotification(contentDescription);
       notificationHelper.getManager().notify(1, builder.build());
    }
}
