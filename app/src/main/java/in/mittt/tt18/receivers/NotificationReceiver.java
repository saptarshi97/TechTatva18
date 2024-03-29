package in.mittt.tt18.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import in.mittt.tt18.R;
import in.mittt.tt18.activities.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {
    private final String NOTIFICATION_TITLE="Upcoming Event";
    private String notificationText="";
    private final String LAUNCH_APPLICATION="Launch TechTatva'18";
    private final String NOTIFICATION_CHANNEL = "tech_tatva_2018";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent!=null){
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent appIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
            String eventName = intent.getStringExtra("eventName");
            notificationText = eventName+" at "+intent.getStringExtra("startTime")+", "+intent.getStringExtra("eventVenue");
            String catName = intent.getStringExtra("catName");
            //IconCollection i = new IconCollection();
            //int catIcon  = i.getIconResource(context, catName);
            Notification notify = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(notificationText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.white))
                    .addAction(new android.support.v4.app.NotificationCompat.Action(0, LAUNCH_APPLICATION, pendingIntent))
                    .build();

            if (notificationManager != null) {
                notificationManager.notify(Integer.parseInt(intent.getStringExtra("eventID")), notify);
            }
        }

    }
}

