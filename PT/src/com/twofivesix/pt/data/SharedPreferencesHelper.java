package com.twofivesix.pt.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
	private static final String SharedPrefsName = "PROJECTTRANSPARENCY";
	
	private SharedPreferences sharedPreferences;
	
	public SharedPreferencesHelper(Context context)
	{
		sharedPreferences = context.getSharedPreferences(SharedPreferencesHelper.SharedPrefsName, 0);
	}
	
	public String getUserEmail()
	{
		return sharedPreferences.getString("user_email", "");
	}
	
	public void setUserEmail(String email)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString("user_email", email);
    	editor.commit();
	}
	
	public boolean getLoggedIn()
	{
		return sharedPreferences.getBoolean("user_logged_in", false);
	}
	
	public void setLoggedIn(boolean loggedIn) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putBoolean("user_logged_in", loggedIn);
    	editor.commit();
	}
	
	public String getUserPassword()
	{
		return sharedPreferences.getString("user_password", "");
	}

	public void setUserPassword(String password) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString("user_password", password);
    	editor.commit();
	}
}
