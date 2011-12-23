package com.twofivesix.pt.activities;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.ListView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.alarms.ReportPromtAlarmHelper;
import com.twofivesix.pt.data.TimePreference;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class SettingsActivity extends PreferenceActivity {
	
	private CheckBoxPreference remindersEnabledCB;
	protected TimePreference reminderTimePreference;
	protected ListPreference reminderIntervalPreference;
	protected PreferenceScreen loginReferenceScreen;
	
	protected PreferenceManager prefMgr;
	protected String reminderTime;
	protected int reminderInterval;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prefMgr = getPreferenceManager();
		
		// Get current reminder settings and save them for later comparison
		reminderTime = prefMgr.getSharedPreferences().getString(SharedPreferencesHelper.REMINDER_TIME_KEY, "");
		reminderInterval = Integer.parseInt(prefMgr.getSharedPreferences().getString(SharedPreferencesHelper.REMINDER_INTERVAL_KEY, "0"));

		addPreferencesFromResource(R.xml.settings);
		
		remindersEnabledCB = (CheckBoxPreference) getPreferenceScreen().findPreference("reminders_on");
		reminderTimePreference = (TimePreference) getPreferenceScreen().findPreference("reminder_time");
		reminderIntervalPreference = (ListPreference) getPreferenceScreen().findPreference("reminder_interval");
		loginReferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("login_screen");
		
		//Log.d("SPENER", "Set PreferenceChangeListener");
		//Log.d("SPENCER", "" + (remindersEnabledCB == null));
		remindersEnabledCB.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//Log.d("SPENCER", "Reminders Enabled Pref Changed to " + newValue.toString());
				boolean enabled = (Boolean) newValue;
				reminderTimePreference.setEnabled(enabled);
				reminderIntervalPreference.setEnabled(enabled);
				if(!enabled)
				{
					ReportPromtAlarmHelper.stopPendingIntent(SettingsActivity.this);
					if(ReportPromtAlarmHelper.repeaterIsRunning(SettingsActivity.this))
						ReportPromtAlarmHelper.stopRepeatingReminder(SettingsActivity.this);
				}
				else
					ReportPromtAlarmHelper.resetPendingIntent(SettingsActivity.this, reminderTime);
				return true;
			}
		});
		
		reminderTimePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//Log.d("SPENCER", "Reminder Time Pref Changed to " + newValue.toString());
				reminderTime = (String) newValue;
				prefMgr.getSharedPreferences().edit().putString(SharedPreferencesHelper.REMINDER_TIME_KEY, reminderTime).commit();
				ReportPromtAlarmHelper.resetPendingIntent(SettingsActivity.this, reminderTime);
				return true;
			}
		});
		
		reminderIntervalPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//Log.d("SPENCER", "Reminder interval pref changed to "+ newValue.toString());
				reminderInterval = Integer.parseInt((String) newValue);
				prefMgr.getSharedPreferences().edit().putString(SharedPreferencesHelper.REMINDER_INTERVAL_KEY, reminderInterval + "").commit();
				if(ReportPromtAlarmHelper.repeaterIsRunning(SettingsActivity.this))
					ReportPromtAlarmHelper.startRepeatingReminder(SettingsActivity.this);
//					ReportPromtAlarmHelper.resetReminderIntent(SettingsActivity.this, reminderTime, reminderInterval);
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		boolean enabled = prefMgr.getSharedPreferences().getBoolean(SharedPreferencesHelper.REMINDERS_ON_KEY, false);
		reminderTimePreference.setEnabled(enabled);
		reminderIntervalPreference.setEnabled(enabled);
		super.onResume();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		//Log.d("SPENCER", l.getItemIdAtPosition(position) + " clicked");
	}
}
