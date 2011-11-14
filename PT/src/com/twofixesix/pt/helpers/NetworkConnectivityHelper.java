package com.twofixesix.pt.helpers;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkConnectivityHelper {
	public static boolean isConnected(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
	    return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting() || 
	         cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
	}
}
