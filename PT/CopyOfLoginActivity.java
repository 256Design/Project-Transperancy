package com.twofivesix.pt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CopyOfLoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;
	public static final int LOGOUT_RESULT_CODE = 2;
	
	SharedPreferences sharedPreferences;
	private EditText etEmailAddress;
	private EditText etPassword;
	private Button bLogin;
	
	private final Class<?> LOGIN_DESTINATION = DestionActivity.class; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
		sharedPreferences = getPreferences(MODE_PRIVATE);
		
		super.onCreate(savedInstanceState);

		if( sharedPreferences.getBoolean("user_logged_in", false))
		{
			// user is logged in, bypass activity
			startActivityForResult(new Intent(CopyOfLoginActivity.this, LOGIN_DESTINATION), LOGIN_REQUEST_CODE);
		}
		
		setContentView(R.layout.login_activity);
		bLogin = (Button) findViewById(R.id.login_button);
		etEmailAddress = (EditText) findViewById(R.id.login_usernameTV1); 
		etPassword = (EditText) findViewById(R.id.login_passowrdTV);
		
		bLogin.setOnClickListener(loginOnClickListener);
	}
	
	protected OnClickListener loginOnClickListener = new OnClickListener()
	{
		public void onClick(View v) 
		{
			ProgressDialog progressDialog = new ProgressDialog(CopyOfLoginActivity.this);
			progressDialog.setMessage("Logging in...");
			progressDialog.setCancelable(false);
			
			LoginTask loginTask = new LoginTask(CopyOfLoginActivity.this, progressDialog);
			loginTask.execute(etEmailAddress.getText().toString(), etPassword.getText().toString());
		}
	};
	
	public void showLoginError(String result)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(CopyOfLoginActivity.this);
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setMessage(R.string.login_invalid_error);
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		alert.show();
	}
	
	public void login(int id)
	{
		sharedPreferences.edit().putBoolean("user_logged_in", true).commit();
		startActivityForResult(new Intent(CopyOfLoginActivity.this, LOGIN_DESTINATION), LOGIN_REQUEST_CODE);
	}
}