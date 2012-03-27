package com.twofivesix.pt.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twofivesix.pt.R;
import com.twofivesix.pt.activities.LoginActivity;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;

public class OnStartupResponse extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int icon = R.drawable.not_icon;
		NotificationManager systemService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, LoginActivity.class), 0);

		try
		{
			SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
			String reminderTime = prefs.getReminderTime();
		
		
	//		Toast t = new Toast(context);
	//		t.setText("onReceive()");
	//		t.setDuration(Toast.LENGTH_LONG);
	//		t.show();
			//Log.d("SPENCER", "reminderTime: " + prefs.getReminderTime());
			//Log.d("SPENCER", "reminder time + " + reminderTime);
			//Log.d("SPENCER", "reminders on: " + prefs.getRemindersOn());
			
			
//			Notification notification2;
			if(prefs.getRemindersOn())
			{
				ReportPromtAlarmHelper.resetPendingIntent(context, reminderTime);
//				notification2 = new Notification(icon, "reminder started", System.currentTimeMillis());
//				notification2.setLatestEventInfo(context, "reminder started", "reminder started at " + reminderTime, pendingIntent);
			}
			else
			{
//				notification2 = new Notification(icon, "reminder disabled", System.currentTimeMillis());
//				notification2.setLatestEventInfo(context, "reminder disabled", "reminder disabled", pendingIntent);
			}
//			systemService.notify(1222, notification2);
		}
		catch (Exception e) {
			
			Notification notification1;
			notification1 = new Notification(icon, "Error Starting Daily Reminders", System.currentTimeMillis());
			notification1.setLatestEventInfo(context, "Error", "Error Starting Daily Reminders", pendingIntent);
			systemService.notify(1221, notification1);
		}
	}
}
