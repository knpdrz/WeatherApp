package com.hrmaarhus.weatherapp.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.hrmaarhus.weatherapp.R;
import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_ID;
import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_NAME;

/**
 * Created by rjkey on 02-11-2017.
 */

//Code is heavly inspired by this youtube video https://www.youtube.com/watch?v=CVI4CfdtbkA
public class NotificationHelper extends ContextWrapper {

    private NotificationManager _manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base){
        super(base);
        CreateChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CreateChannel(){
        NotificationChannel channel  = new NotificationChannel(NOTIFICATION_CHANNEL_ID,NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager(){
        if (_manager == null){
            _manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return _manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getChannelNotification(String title, String body){
        return new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setContentText(body)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round);
    }
}
