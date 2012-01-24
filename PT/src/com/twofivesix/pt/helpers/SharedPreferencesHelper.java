package com.twofivesix.pt.helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twofivesix.pt.R;

public class SharedPreferencesHelper {
	
	//public static final String SharedPrefsName = "PROJECTTRANSPARENCY";

	public static final String USER_EMAIL = "user_email";
	public static final String USER_PASSWORD_KEY = "user_password";
	public static final String USER_LOGGED_IN = "user_logged_in";
	public static final String REMINDER_INTERVAL_KEY = "reminder_interval";
	public static final String REMINDER_TIME_KEY = "reminder_time";
	public static final String REMINDERS_ON_KEY = "reminders_on";
	public static final String USER_ID_KEY = "user_id";
	public static final String AUTO_LOGIN_KEY = "auto_login";
	public static final String LAST_REPORT_KEY = "last_report_time";
	public static final String REMEMBER_ME_KEY = "remember_me";
	public static final String SEND_TO_SELF_KEY = "copy_of_report_to_self";
	public static final String ADD_FOLLOW_UP_KEY = "add_follow_up";
	public static final String LAST_VERSION_INFO_READ_KEY = "last_version_info_read";
	public static final String SYNC_COUNT_KEY = "sync_count_key";

	
	private SharedPreferences sharedPreferences;
	private Context context;
	
	
	public SharedPreferencesHelper(Context context)
	{
		this.context = context;
		
		//sharedPreferences = context.getSharedPreferences(SharedPreferencesHelper.SharedPrefsName, Context.MODE_WORLD_READABLE);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		//prefs = context.getPreferences(Activity.MODE_PRIVATE);
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
		Log.d("SPENCER", "sp.getLoggedIn() = "+loggedIn);
		//Log.d("SPENCER", "p.getLoggedIn() = "+prefs.getBoolean(USER_LOGGED_IN, false));
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		Log.d("SPENCER", "setLoggedIn("+loggedIn+")");
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putBoolean(USER_LOGGED_IN, loggedIn);
    	editor.commit();
    	//prefs.edit().putBoolean(USER_LOGGED_IN, loggedIn).commit();
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
		return sharedPreferences.getBoolean(
				context.getString(R.string.reminders_on_key), false
				);
	}

	public boolean getAutoLogin() {
		return sharedPreferences.getBoolean(AUTO_LOGIN_KEY, false);
	}

	public void setAutoLogin(boolean value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(AUTO_LOGIN_KEY, value);
		editor.commit();
	}
	
	public boolean getRememberMe() {
		return sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
	}
	
	public void setRememberMe(boolean value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(REMEMBER_ME_KEY, value);
		editor.commit();
	}

	public boolean getSendToSelf() {
		return sharedPreferences.getBoolean(SEND_TO_SELF_KEY, false);
	}

	public boolean getAddFollowUp() {
		return sharedPreferences.getBoolean(ADD_FOLLOW_UP_KEY, true);
	}
	
	public int getLastVersionInfoRead()
	{
		return sharedPreferences.getInt(LAST_VERSION_INFO_READ_KEY, 0);
	}
	
	public void setLastVersionInfoRead(int versionCode)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(LAST_VERSION_INFO_READ_KEY, versionCode);
		editor.commit();
	}
	
	public int getSyncCount()
	{
		return sharedPreferences.getInt(SYNC_COUNT_KEY, 0);
	}
	
	public void setSyncCount(int count)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(SYNC_COUNT_KEY, count);
		editor.commit();
	}
	
	public void resetSyncCount()
	{
		setSyncCount(10);
	}

	public void clearData() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}
}
