package com.example.woundmontoringapplicationv1;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 *
 */
public class App extends ContextWrapper {

    public static final String CHANNEL_ID = "reminder1";
    public static final String CHANNEL_NAME = "Wound Monitoring Time!";

    private NotificationManager mManager;

    /**
     *
     * @param base
     */
    public App(Context base){
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    /**
     *
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel(){
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    /**
     *
     * @return
     */
    public NotificationManager getManager(){
        if(mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification(){
        return new NotificationCompat.Builder(getApplicationContext(),
                CHANNEL_ID)
                .setContentTitle("Reminder!")
                .setContentText("It's time to start monitoring your wound.")
                .setSmallIcon(R.drawable.cast_ic_notification_on);
    }
}
