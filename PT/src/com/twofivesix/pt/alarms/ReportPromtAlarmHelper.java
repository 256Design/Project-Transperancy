package com.twofivesix.pt.alarms;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;

public class ReportPromtAlarmHelper {
	private static final int REPEATING_SENDER_REQUEST_CODE = 333;
	private static final int DAILY_SENDER_REQUEST_CODE = 334;
	private static Toast mToast;

	public static void resetPendingIntent(Context context, String reminderTime) 
	{
//		Log.d("SPENCER", "resetPendingIntent(context, " + reminderTime+ ")");
        if(!reminderTime.equals(""))
        {
        	PendingIntent sender = PendingIntent.getService(context,
        		DAILY_SENDER_REQUEST_CODE, new Intent(context, DailyQuestionPrompt.class), 0);

	        // We want the alarm to go off 30 seconds from now.
	        Calendar calendar = Calendar.getInstance();
	        Date date = new Date();
	        date.setHours(Integer.parseInt(reminderTime.split(":")[0]));
	        date.setMinutes(Integer.parseInt(reminderTime.split(":")[1]));
	        date.setSeconds(0);
	        calendar.setTime(date);
	        if(date.compareTo(new Date()) <= 0)
	        	calendar.add(Calendar.DATE, 1);
	        //calendar.setTimeInMillis(System.currentTimeMillis());
	        //calendar.add(Calendar.SECOND, 30);
	
	        // Schedule the alarm!
	        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*60*24, sender);
	
			// Tell the user about what we did.
	        if (mToast != null) {
	            mToast.cancel();
	        }
	        mToast = Toast.makeText(context, context.getString(R.string.reminder_scheduled_for) + " " + build12HourTime(calendar),
	                Toast.LENGTH_LONG);
	        mToast.show();
        }
	}
	
	public static void stopPendingIntent(Context context) {
		PendingIntent sender = PendingIntent.getService(context,
        		DAILY_SENDER_REQUEST_CODE, new Intent(context, DailyQuestionPrompt.class), PendingIntent.FLAG_NO_CREATE);
		
		if(sender != null)
		{
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(sender);
		}
	}

	public static void resetReminderIntent(Context context, String reminderTime, int reminderInterval) {
//		Log.d("SPENCER", "resetReminderIntent(context, " + reminderTime+ ", " + reminderInterval + ")");
		Intent intent = new Intent(context, ReportPromptAlarm.class);
		PendingIntent sender = PendingIntent.getService(context,
                REPEATING_SENDER_REQUEST_CODE, intent, 0);
        // We want the alarm to go off 30 seconds from now.
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        date.setHours(Integer.parseInt(reminderTime.split(":")[0]));
        date.setMinutes(Integer.parseInt(reminderTime.split(":")[1]));
        date.setSeconds(0);
        calendar.setTime(date);
        if(date.compareTo(new Date()) <= 0)
        	calendar.add(Calendar.DATE, 1);
        calendar.add(Calendar.MINUTE, reminderInterval);
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.add(Calendar.SECOND, 30);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*reminderInterval, sender);

		// Tell the user about what we did.
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, context.getString(R.string.reminder_scheduled_for) + " " + build12HourTime(calendar),
                Toast.LENGTH_LONG);
        mToast.show();
	}

	private static String build12HourTime(Calendar date) {
		String _return;
		if(date.get(Calendar.HOUR_OF_DAY) > 12)
			_return = "" + (date.get(Calendar.HOUR_OF_DAY) - 12);
		else if(date.get(Calendar.HOUR_OF_DAY) == 0)
			_return = "12";
		else
			_return = "" + date.get(Calendar.HOUR_OF_DAY);
		_return += ":";
		if(date.get(Calendar.MINUTE) < 10)
			_return += "0" + date.get(Calendar.MINUTE);
		else
			_return += date.get(Calendar.MINUTE);
		_return += " " + ((date.get(Calendar.AM_PM)==Calendar.AM)?"am":"pm");
		return _return;
	}

	public static boolean repeaterIsRunning(Context context) {
		Intent intent = new Intent(context, ReportPromptAlarm.class);
		PendingIntent sender = PendingIntent.getService(context, REPEATING_SENDER_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE);
		/*if(sender != null)
			Log.d("SPENCER", "Sender is running");
		else
			Log.d("SPENCER", "Sender is not running");*/
		return sender != null;
	}

	public static void startRepeatingReminder(Context context) {
		SharedPreferencesHelper preferencesHelper = new SharedPreferencesHelper(context);
		long reminderInterval = preferencesHelper.getReminderInterval();
//		Log.d("SPENCER", "reteating reminder every " + reminderInterval + " minutes");
		
		Intent intent = new Intent(context, ReportPromptAlarm.class);
		PendingIntent sender = PendingIntent.getService(context,
                REPEATING_SENDER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(reminderInterval != 0)
        	am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*reminderInterval, sender);
        else
        	am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);        	
	}

	public static void stopRepeatingReminder(Context context) {
		Intent intent = new Intent(context, ReportPromptAlarm.class);
		PendingIntent sender = PendingIntent.getService(context,
                REPEATING_SENDER_REQUEST_CODE, intent, 0);
        
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
	}
}
