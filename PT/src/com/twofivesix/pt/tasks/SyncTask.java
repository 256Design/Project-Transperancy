package com.twofivesix.pt.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.twofivesix.pt.R;
import com.twofivesix.pt.interfaces.SyncCaller;

public abstract class SyncTask extends AsyncTask<Integer, Integer, Boolean> {

	protected SQLiteDatabase db;
	private ProgressDialog progressDialog;
	private SyncCaller activity;

	public SyncTask(SyncCaller activity, ProgressDialog progressDialog, SQLiteDatabase db)
	{
		this.activity = activity;
		this.db = db;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected final void onPostExecute(Boolean result) 
	{
		progressDialog.dismiss();
		activity.syncResults(result);
	}

	@Override
	protected void onPreExecute() 
	{
		progressDialog.show();
	}
	
	public void showSyncError(Context context, String message)
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.cancel();
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage(message);
    	builder.setPositiveButton(R.string.okay, dialogClickListener);
    	builder.show();
	}
	
	public void showSyncError(Context context)
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.cancel();
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage(R.string.sync_error);
    	builder.setPositiveButton(R.string.okay, dialogClickListener);
    	builder.show();
	}
}
