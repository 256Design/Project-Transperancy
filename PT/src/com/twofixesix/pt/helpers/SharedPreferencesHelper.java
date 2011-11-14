package com.twofixesix.pt.helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.twofivesix.pt.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesHelper {
	
	public static final String SharedPrefsName = "PROJECTTRANSPARENCY";

	public static final String USER_EMAIL = "user_email";
	public static final String USER_PASSWORD_KEY = "user_password";
	public static final String USER_LOGGED_IN = "user_logged_in";
	public static final String REMINDER_INTERVAL_KEY = "reminder_interval";
	public static final String REMINDER_TIME_KEY = "reminder_time";
	public static final String USER_ID_KEY = "user_id";

	
	private SharedPreferences sharedPreferences;
	private Context context;
	
	private String LAST_REPORT_KEY;
	
	public SharedPreferencesHelper(Context context)
	{
		this.context = context;
		sharedPreferences = context.getSharedPreferences(SharedPreferencesHelper.SharedPrefsName, 0);
	}
	
	public String getUserEmail()
	{
		/*Map<String, ?> all = sharedPreferences.getAll();
		for (String key : all.keySet()) {
			Log.d("SPENCER", "key("+key+") = " + all.get(key).toString());			
		}*/
		String email  = sharedPreferences.getString(USER_EMAIL, "");
		//Log.d("SPENCER", "new getUserEmail() == " + email);
		return email;
	}
	
	public void setUserEmail(String email)
	{
		//Log.d("SPENCER", "setUserEmail(" + email + ")");
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(USER_EMAIL, email);
    	editor.commit();
	}
	
	public boolean getLoggedIn()
	{
		boolean loggedIn = sharedPreferences.getBoolean(USER_LOGGED_IN, false);
		Log.d("SPENCER", "getLoggedIn() = "+loggedIn);
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		Log.d("SPENCER", "setLoggedIn("+loggedIn+")");
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putBoolean(USER_LOGGED_IN, loggedIn);
    	editor.commit();
	}
	
	public String getUserPassword()
	{
		return sharedPreferences.getString(USER_PASSWORD_KEY, "");
	}

	public void setUserPassword(String password) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(USER_PASSWORD_KEY, password);
    	editor.commit();
	}

	public Map<String, ?> getAll() {
		return sharedPreferences.getAll();
	}

	public String getReminderTime() {
		return sharedPreferences.getString(REMINDER_TIME_KEY, "");
	}
	
	public void setReminderTime(String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(REMINDER_TIME_KEY, value);
		editor.commit();
	}

	public long getReminderInterval() {
		return new Long(sharedPreferences.getString(REMINDER_INTERVAL_KEY, "-1"));
	}
	
	public void setReminderInterval(String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(REMINDER_INTERVAL_KEY, value);
		editor.commit();
	}

	public int getUserID() {
		return sharedPreferences.getInt(USER_ID_KEY, -1);
	}
	
	public void setUserID(int value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(USER_ID_KEY, value);
		editor.commit();
	}
	
	public Date getLastReportDate() {
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sharedPreferences.getLong(LAST_REPORT_KEY, 0));
        return calendar.getTime();
	}
	
	public long getLastReportDateMillis() {
		return sharedPreferences.getLong(LAST_REPORT_KEY, 0);
	}
	
	public void setLastReportDateToNow() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong(LAST_REPORT_KEY, System.currentTimeMillis());
		editor.commit();
	}

	public boolean getRemindersOn() {
		return sharedPreferences.getBoolean(context.getString(R.string.reminders_on), false);
	}
}