package com.example.woundmontoringapplicationv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

/**
 * The AlertReceiver class defines the tailored notifications that
 * are to be sent to the user when it is time for them to start monitoring their wound.
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

       String contentDescription = "It's time to start monitoring Dressing " + intent.getStringExtra("QRID") + ", which is located on your " + intent.getStringExtra("Location");

       NotificationCompat.Builder builder = notificationHelper.getChannelNotification(contentDescription);
       builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentDescription));
       notificationHelper.getManager().notify(1, builder.build());
    }
}
