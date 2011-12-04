package com.twofivesix.pt.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;
import com.twofivesix.pt.tasks.LoginTask;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class LoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;
	public static final int LOGOUT_RESULT_CODE = 2;
	
	SharedPreferencesHelper settings;
	SharedPreferences sharedPreferences;
	private EditText etEmailAddress;
	private EditText etPassword;
	private Button bLogin;
	//private Button bRecover;
	private Button bRegister;
	
	private final Class<?> LOGIN_DESTINATION = TileHomeActivity.class; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		settings = new SharedPreferencesHelper(this);
		
		Boolean loggedIn = sharedPreferences.getBoolean(SharedPreferencesHelper.USER_LOGGED_IN, false);
		Log.d("SPENCER", "default shared prefs isLoggedIn: "+ loggedIn);
		if(settings.getLoggedIn() || loggedIn)
		{
			// user is logged in, bypass activity
			startActivityForResult(new Intent(LoginActivity.this, LOGIN_DESTINATION), LOGIN_REQUEST_CODE);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		bLogin = (Button) findViewById(R.id.login_button);
		//TODO Add functionality: Recover user info
		//bRecover = (Button) findViewById(R.id.login_recover_button);
		bRegister = (Button) findViewById(R.id.login_register_button);
		etEmailAddress = (EditText) findViewById(R.id.login_usernameTV1); 
		etPassword = (EditText) findViewById(R.id.login_passowrdTV);
		
		etEmailAddress.setText(settings.getUserEmail());
		etPassword.setText(settings.getUserPassword());
		
		bLogin.setOnClickListener(loginOnClickListener);
		/*bRecover.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//startActivityForResult(new Intent(LoginActivity.this, RecoverActivity.class), RECOVER_REQUEST_CODE);
			}
		});*/
		if(bRegister != null)
		bRegister.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), REGISTER_REQUEST_CODE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_CANCELED && requestCode == LOGIN_REQUEST_CODE)
		{
			//Log.d("SPENCER", "RESULT_CANCELED");
			finish();
		}
		else if(resultCode == LOGOUT_RESULT_CODE)
		{
			settings.setLoggedIn(false);
			//Log.d("SPENCER", "LOGOUT_RESULT_CODE");
		}
		else if(resultCode == RESULT_OK && requestCode == REGISTER_REQUEST_CODE)
		{
			// get returned email address and password
			String returnedEmail = data.getStringExtra("emailAddress");
			String returnedPassword = data.getStringExtra("password");
			
			// store them to shareprefs
			settings.setUserEmail(returnedEmail);
			settings.setUserPassword(returnedPassword);
			
			// put values in EditText's
	    	etEmailAddress.setText(returnedEmail);
	    	etPassword.setText(returnedPassword);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(NetworkConnectivityHelper.isConnected(this))
			enableLogin();
		else
			disableLogin();
		
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		registerReceiver(networkStateReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(networkStateReceiver);
	}
	
	protected BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if(NetworkConnectivityHelper.isConnected(LoginActivity.this))
				enableLogin();
			else
				disableLogin();
	    }
	};
	
	protected void disableLogin() {
		bLogin.setEnabled(false);
		bLogin.setText(R.string.no_network_connection);
		//bRecover.setEnabled(false);
		bRegister.setEnabled(false);
	}
	
	protected void enableLogin() {
		bLogin.setEnabled(true);
		bLogin.setText(R.string.login);
		//bRecover.setEnabled(true);
		if(bRegister != null)
			bRegister.setEnabled(true);		
	}
	
	protected OnClickListener loginOnClickListener = new OnClickListener() {
		public void onClick(View v) 
		{
			if(validate())
			{
				ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setMessage(getString(R.string.logging_in));
				progressDialog.setCancelable(false);
				
				LoginTask loginTask = new LoginTask(LoginActivity.this, progressDialog);
				loginTask.execute(etEmailAddress.getText().toString(), etPassword.getText().toString());
			}
		}
	};
	
	public void showLoginError(String result)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		if(result.equals("Unconfirmed"))
		{
			builder.setMessage(R.string.login_unconfirmed_error);
		}
		else
		{
			builder.setMessage(R.string.login_invalid_error);						
		}
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		alert.show();
	}
	
	public void showConnectionError(final LoginTask loginTask)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				loginTask.execute();
			}
		});
		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
		sharedPreferences.edit().putBoolean(SharedPreferencesHelper.USER_LOGGED_IN, true).commit();
		settings.setLoggedIn(true);
		settings.setUserID(id);
		settings.setUserEmail(etEmailAddress.getText().toString());
		Log.d("SPENCER", "id: " + id);
		startActivityForResult(new Intent(LoginActivity.this, LOGIN_DESTINATION), LOGIN_REQUEST_CODE);
	}

	protected boolean validate() {
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(new RegExpressionValidator(etEmailAddress, RegExpressionValidator.EMAIL_REGEX, "Invalid Email", "Please Enter A Valid Email"));
		validators.add(new RegExpressionValidator(etPassword, RegExpressionValidator.PASSWORD_REGEX, "Invalid Password", "Please Enter A Valid Password"));
		List<ValidationResult> _validationResults = AbstractValidator.validateAll(validators);
		if (_validationResults.size()==0) {
        	return true;
        }
		else
		{
			ValidationResult result = _validationResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(result.getMessage());
			builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
			if(result.getSource() != null)
				result.getSource().requestFocus();
		}
		return false;
	}
}