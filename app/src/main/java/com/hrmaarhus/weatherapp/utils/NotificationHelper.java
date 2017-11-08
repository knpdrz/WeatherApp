package com.hrmaarhus.weatherapp.utils;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import com.hrmaarhus.weatherapp.R;

import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_ID;
import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_NAME;
import static com.hrmaarhus.weatherapp.utils.Globals.UNIQUE_NOTIFICATION_ID;

/**
 * Created by rjkey on 02-11-2017.
 */

//Code is heavily inspired by this youtube video https://www.youtube.com/watch?v=CVI4CfdtbkA
public class NotificationHelper extends ContextWrapper {

    private NotificationManager _manager;

    public NotificationHelper(Context base) {
        super(base);
    }

    public void CreateNotification(String title, String content){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            CreateChannel();

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentText(content)
                    .setContentTitle(title)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher);

            getManager().notify(UNIQUE_NOTIFICATION_ID, notification.build());
        }
        else{
            NotificationCompat.Builder _notificationBuilder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content);

            Intent intent = new Intent(this, NotificationHelper.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            _notificationBuilder.setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(UNIQUE_NOTIFICATION_ID,_notificationBuilder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CreateChannel(){
        NotificationChannel channel  = null;

        channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager(){
        if (_manager == null){
            _manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return _manager;
    }
}
