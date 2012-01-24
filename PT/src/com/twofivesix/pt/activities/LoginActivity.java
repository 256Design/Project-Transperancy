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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;
import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.helpers.NetworkConnectivityHelper;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;
import com.twofivesix.pt.helpers.VersionAlertHelper;
import com.twofivesix.pt.tasks.GeneralHttpTask;
import com.twofivesix.pt.tasks.GeneralHttpTask.OnResponseListener;
import com.twofivesix.pt.tasks.LoginTask;

public class LoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;
	public static final int LOGOUT_RESULT_CODE = 2;
	
	SharedPreferencesHelper settings;
	SharedPreferences sharedPreferences;
	private RelativeLayout loginParentLayout;
	private LinearLayout loginFormLayout;
	private EditText etEmailAddress;
	private EditText etPassword;
	private Button bLogin;
	private CheckBox cbAutoLogin;
	private CheckBox cbRememberMe;
	private Button bRecover;
	private Button bRegister;
	
	private final Class<?> LOGIN_DESTINATION = TileHomeActivity.class;

	private LinearLayout recoverFormLayout;
	private TextView recoverTitle;
	private EditText recoverEmail; 
	private Button recoverSubmit;
	
	private final int recoverFormID = 12344;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		settings = new SharedPreferencesHelper(this);
		
		Boolean loggedIn = settings.getLoggedIn();
		Log.d("SPENCER", "default shared prefs isLoggedIn: "+ loggedIn);
		
		if(settings.getAutoLogin() && settings.getLoggedIn())
		{
			// user is logged in, bypass activity
			startActivityForResult(new Intent(LoginActivity.this, LOGIN_DESTINATION), LOGIN_REQUEST_CODE);
		}
		else
		{
			// runs a check for first version run then show change log if something 
			// is different 
			new VersionAlertHelper(this, settings);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		bLogin = (Button) findViewById(R.id.login_button);
		loginFormLayout = (LinearLayout) findViewById(R.id.linearLayout1);
		loginParentLayout = (RelativeLayout) findViewById(R.id.login_rel_layout);
		bRecover = (Button) findViewById(R.id.login_recover_button);
		bRegister = (Button) findViewById(R.id.login_register_button);
		etEmailAddress = (EditText) findViewById(R.id.login_usernameTV1); 
		etPassword = (EditText) findViewById(R.id.login_passowrdTV);
		cbAutoLogin = (CheckBox) findViewById(R.id.login_auto_login);
		cbRememberMe = (CheckBox) findViewById(R.id.login_remember_me);
		
		if(settings.getRememberMe())
		{
			etEmailAddress.setText(settings.getUserEmail());
			cbRememberMe.setChecked(true);
		}
		
		bLogin.setOnClickListener(loginOnClickListener);
		
		cbAutoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbRememberMe.setEnabled(!isChecked);
			}
		});
		
		bRecover.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				showRecoverLoginForm();
			}
		});
		
		if(bRegister != null)
		bRegister.setOnClickListener(new OnClickListener() 
		{
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
	
	public void onBackPressed() {
		if (loginParentLayout.findViewById(recoverFormID) != null) {
			removeRecoverLoginForm();
		}
		else
			finish();
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
	
	protected OnClickListener recoverOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if(recoverEmail.length()>6)
			{
				ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setMessage(getString(R.string.sending_recovery_request));
				progressDialog.setCancelable(false);
				
				GeneralHttpTask recoveryTask = new GeneralHttpTask(
						progressDialog, 
						recoveryRequestResponseListener);
				String url = "http://www.256design.com/projectTransparency/project/" +
						"recoverRequest.php?e=" + recoverEmail.getText().toString();
				recoveryTask.execute(url, "202");
			}
		}
	};
	
	protected OnResponseListener recoveryRequestResponseListener = new OnResponseListener() {
		
		public void onSuccess() {
			removeRecoverLoginForm();
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
			builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.success_sending_recover);
			builder.show();
		}
		
		public void onFailure(String message) {
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
			builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.failure_sending_recover);
			builder.show();
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
			}
		});
		builder.setMessage(R.string.login_invalid_error);
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		alert.show();
	}
	
	protected void showRecoverLoginForm() 
	{
		if(recoverFormLayout == null)
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					loginFormLayout.getWidth(), loginFormLayout.getHeight());
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			recoverFormLayout = new LinearLayout(LoginActivity.this);
			recoverFormLayout.setId(recoverFormID );
			recoverFormLayout.setLayoutParams(params);
			recoverFormLayout.setOrientation(LinearLayout.VERTICAL);
			recoverFormLayout.setPadding(
					loginFormLayout.getPaddingLeft(), 
					0, 
					loginFormLayout.getPaddingRight(), 
					loginFormLayout.getPaddingBottom());
			recoverFormLayout.setBackgroundResource(R.drawable.recover_back);
			
			recoverTitle = new TextView(LoginActivity.this);
			recoverTitle.setPadding(0, -15, 0, 0);
			recoverTitle.setGravity(Gravity.RIGHT);
			recoverTitle.setText(R.string.recover_login);
			recoverTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
			recoverTitle.setTypeface(null, Typeface.BOLD);
			recoverTitle.setTextColor(R.color.black);
			recoverFormLayout.addView(recoverTitle);
			
			recoverEmail = new EditText(LoginActivity.this);
			recoverEmail.setHint(R.string.email);
			recoverEmail.setLayoutParams(
					new LayoutParams(LayoutParams.FILL_PARENT, 
							LayoutParams.WRAP_CONTENT)
					);
			recoverFormLayout.addView(recoverEmail);
			
			recoverSubmit = new Button(LoginActivity.this);
			recoverSubmit.setText(R.string.submit);
			recoverSubmit.setLayoutParams(
					new LayoutParams(LayoutParams.WRAP_CONTENT, 
							LayoutParams.WRAP_CONTENT)
					);
			recoverSubmit.setOnClickListener(recoverOnClickListener);
			recoverFormLayout.addView(recoverSubmit);
		}
		
		loginParentLayout.addView(recoverFormLayout);
		
		bRecover.setEnabled(false);
	}
	
	private void removeRecoverLoginForm() {
		bRecover.setEnabled(true);
		loginParentLayout.removeView(recoverFormLayout);
	}
	
	public void login(int id)
	{
		int existingID = settings.getUserID();
		if(id != existingID)
		{
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			dbHelper.clearData(db);
			settings.clearData();
		}
		settings.setLoggedIn(true);
		settings.setUserID(id);
		settings.setUserEmail(etEmailAddress.getText().toString());
		settings.setAutoLogin(cbAutoLogin.isChecked());
		settings.setRememberMe(cbRememberMe.isChecked());
		Intent intent = new Intent(LoginActivity.this, LOGIN_DESTINATION);
//		bundle.putBoolean("sync", id != existingID);
		// just set the "sync" extra to true so that it always sync's on login
		intent.putExtra("sync", true);
		startActivityForResult(intent, LOGIN_REQUEST_CODE);
	}

	protected boolean validate() {
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(new RegExpressionValidator(
				etEmailAddress, 
				RegExpressionValidator.EMAIL_REGEX, "Invalid Email", "Please Enter A Valid Email"));
		validators.add(new RegExpressionValidator(
				etPassword, 
				RegExpressionValidator.PASSWORD_REGEX, "Invalid Password", "Please Enter A Valid Password"));
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