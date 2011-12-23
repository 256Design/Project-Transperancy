package com.twofivesix.pt.alarms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DailyQuestionPrompt extends Service {
	
	@Override
    public void onCreate() {
		Log.d("SPENCER", "Daily reminder started");
        ReportPromtAlarmHelper.startRepeatingReminder(this);
        stopSelf();
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
