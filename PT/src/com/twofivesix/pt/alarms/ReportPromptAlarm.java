package com.twofivesix.pt.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import com.twofivesix.pt.R;
import com.twofivesix.pt.activities.NoteSelectedDialogActivity;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;

public class ReportPromptAlarm extends Service {
    
	private static final int NOTE_ICON = R.drawable.not_icon;
	NotificationManager mNM;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
//    	Log.d("SPENCER", "Reminder fired.");
        CharSequence text = getText(R.string.reminder_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = buildNotification(text, text, 
        		getText(R.string.reminder_service_label));
        // Send the notification.
        // We use a layout id because it is a unique number. We use it later to cancel.
        mNM.notify(R.string.reminder_service_started, notification);
    }
    
    private Notification buildNotification(CharSequence titckerText, 
    											CharSequence noteTitle, 
    											CharSequence text)
    {
    	Notification notification = new Notification(NOTE_ICON, 
    			titckerText, 
    			System.currentTimeMillis());
    	Intent intent = new Intent(this, NoteSelectedDialogActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
    	notification.setLatestEventInfo(this, noteTitle, text, contentIntent);
    	SharedPreferencesHelper preferencesHelper = new SharedPreferencesHelper(this);
    	Log.d("SPENCER", "reminderVibrate = " + preferencesHelper.getReminderVibrate());
    	if(preferencesHelper.getReminderVibrate())
    	{
    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    	}
    	Log.d("SPENCER", "reminderLight = " + preferencesHelper.getReminderLight());
    	if(preferencesHelper.getReminderLight())
    	{
    		notification.ledARGB = Color.WHITE;
    	    notification.ledOnMS = 300;
    	    notification.ledOffMS = 1000;
    	    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
    	}
    	return notification;
    }
    
    public static void closeNotification(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(
				NOTIFICATION_SERVICE);
		nm.cancel(R.string.reminder_service_started);
	}
}