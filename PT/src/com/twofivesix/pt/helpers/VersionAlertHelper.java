package com.twofivesix.pt.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.TextView;

import com.twofivesix.pt.R;

public class VersionAlertHelper {

	protected Context context;
	private int versionCode;
	protected SharedPreferencesHelper preferencesHelper;

	public VersionAlertHelper(Context context, SharedPreferencesHelper prefsHelper)
	{
		this.context = context;
		this.preferencesHelper = prefsHelper;
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
											context.getPackageName(), 0);
			versionCode = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int lastReadVersion = preferencesHelper.getLastVersionInfoRead();
		if(versionCode != 0 && versionCode > lastReadVersion)
		{
			showUpdate(context).setOnDismissListener(new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					preferencesHelper.setLastVersionInfoRead(versionCode);
				}
			});
		}
	}
	
	public static AlertDialog showUpdate(Context context)
	{
		AlertDialog newUpdateAlert = new AlertDialog.Builder(context).create();
		newUpdateAlert.setTitle("New Version");
		newUpdateAlert.setMessage("Here are some of the things that changed:" +
				"\n\n- Added: request a password reset." +
				"\n\n- Fixed: Major bug that caused items on the reporting " +
				"screen to be out of order or repeated.");
		newUpdateAlert.setButton(context.getString(R.string.okay), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});
		
		newUpdateAlert.show();
		((TextView)newUpdateAlert.findViewById(android.R.id.message)).setTextSize(14);
		return newUpdateAlert;
	}
}
