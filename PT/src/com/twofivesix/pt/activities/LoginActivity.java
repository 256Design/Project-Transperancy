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
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.EditTextMatchRequiredValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;
import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.helpers.NetworkConnectivityHelper;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;
import com.twofivesix.pt.helpers.VersionAlertHelper;
import com.twofivesix.pt.tasks.GeneralHttpTask;
import com.twofivesix.pt.tasks.RegisterTask;
import com.twofivesix.pt.tasks.GeneralHttpTask.OnResponseListener;
import com.twofivesix.pt.tasks.LoginTask;

public class LoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;
	public static final int LOGOUT_RESULT_CODE = 2;
	
	public static final String RECOVERY_STATE = "recovery"; 
	public static final String REGISTER_STATE = "register"; 
	
	SharedPreferencesHelper settings;
	SharedPreferences sharedPreferences;
	private RelativeLayout loginParentLayout;
//	private LinearLayout loginFormLayout;
	private EditText etEmailAddress;
	private EditText etPassword;
	private Button bLogin;
	private CheckBox cbAutoLogin;
	private CheckBox cbRememberMe;
	private Button bRecover;
	private Button bRegister;
	
	private final Class<?> LOGIN_DESTINATION = TileHomeActivity.class;

	private ViewGroup recoverFormLayout;
	private ViewGroup registerFormView;
	private TextView recoverTitle;
	private EditText recoverEmail; 
	private Button recoverSubmit;
	
	private final int secondaryFormID = 12344;
	private String showing;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
//		Log.d("SPENCER", "onCreate()");
		
		settings = new SharedPreferencesHelper(this);
		
		// check if user is logged in
		//Boolean loggedIn = settings.getLoggedIn();
		//Log.d("SPENCER", "default shared prefs isLoggedIn: "+ loggedIn);
		
		if(settings.getAutoLogin() && settings.getLoggedIn())
		{
			// user is logged in, bypass activity
			startActivityForResult(
					new Intent(LoginActivity.this, LOGIN_DESTINATION), 
					LOGIN_REQUEST_CODE
			);
		}
		else
		{
			// runs a check for first version run then show change log if something 
			// is different 
			new VersionAlertHelper(this, settings);
		}
		
		//  get component references
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_activity);
		loginParentLayout = (RelativeLayout) findViewById(R.id.login_rel_layout);
		bLogin = (Button) loginParentLayout.findViewById(R.id.login_button);
//		loginFormLayout = (LinearLayout) loginParentLayout.findViewById(R.id.linearLayout1);
		bRecover = (Button) loginParentLayout.findViewById(R.id.login_recover_button);
		bRegister = (Button) loginParentLayout.findViewById(R.id.login_register_button);
		etEmailAddress = (EditText) loginParentLayout.findViewById(R.id.login_usernameTV1); 
		etPassword = (EditText) loginParentLayout.findViewById(R.id.login_passowrdTV);
		cbAutoLogin = (CheckBox) loginParentLayout.findViewById(R.id.login_auto_login);
		cbRememberMe = (CheckBox) loginParentLayout.findViewById(R.id.login_remember_me);
		
		// set up remember me and auto login check boxes 
		if(settings.getRememberMe())
		{
			etEmailAddress.setText(settings.getUserEmail());
			cbRememberMe.setChecked(true);
		}
		cbAutoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbRememberMe.setEnabled(!isChecked);
				if(isChecked)
					cbRememberMe.setChecked(true);
			}
		});

		// add on click listeners
		bLogin.setOnClickListener(loginOnClickListener);
		bRecover.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				showRecoverLoginForm();
			}
		});
		bRegister.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				showRegisterForm();
			}
		});
		showing = (String) getLastNonConfigurationInstance();
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
		if (loginParentLayout.findViewById(secondaryFormID) != null) {
			removeRecoverLoginForm();
			removeRegisterLoginForm();
		}
		else
			finish();
	};
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		for (int i = 0; i < loginParentLayout.getChildCount(); i++)
		{
			if(loginParentLayout.getChildAt(i) == recoverFormLayout)
			{
				return LoginActivity.RECOVERY_STATE + ";"+
						rSemis(recoverEmail.getText().toString());
			}
			if(loginParentLayout.getChildAt(i) == registerFormView)
			{
				return LoginActivity.REGISTER_STATE + ";"+
						rSemis(registerEmail.getText().toString()) + ";"+
						rSemis(registerPassword.getText().toString()) + ";"+
						rSemis(registerPasswordConf.getText().toString()) + ";"+
						rSemis(registerFullName.getText().toString());
			}
		}
		return rSemis(etEmailAddress.getText().toString()) + ";" +
				rSemis(etPassword.getText().toString());
		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// recover recovered state
		if(showing != null)
		{
			String[] lastState = showing.split(";");
			if(lastState[0].equals(LoginActivity.RECOVERY_STATE))
			{
				showRecoverLoginForm();
				recoverEmail.setText(lastState[1]);
			}
			else if(lastState[0].equals(LoginActivity.REGISTER_STATE))
			{
				showRegisterForm();
				registerEmail.setText(lastState[1]);
				registerPassword.setText(lastState[2]);
				registerPasswordConf.setText(lastState[3]);
				registerFullName.setText(lastState[4]);
			}
			else
			{
				etEmailAddress.setText(lastState[0]);
				etPassword.setText(lastState[1]);
			}
		}
	}
	
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
				GeneralHttpTask recoveryTask = new GeneralHttpTask(
						LoginActivity.this, 
						getString(R.string.sending_recovery_request),
						recoveryRequestResponseListener);
				String url = "http://www.256design.com/projectTransparency/project/" +
						"recoverRequest.php?e=" + recoverEmail.getText().toString();
				recoveryTask.execute(url, "202");
			}
		}
	};
	
	protected OnClickListener registerOnClickListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(validateRegister())
			{
				OnResponseListener responder = new OnResponseListener() {
					
					public void onSuccess() 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder .setMessage(R.string.register_success);
						builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						
						etEmailAddress.setText(registerEmail.getText());
						removeRegisterLoginForm();
						
						AlertDialog alert = builder.create();
						alert.setCancelable(false);
						alert.show();
					}
					
					public void onFailure(String message) 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(
								LoginActivity.this);
						builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});

						if(message.equals("409"))
							builder.setMessage(R.string.register_existing_email);
						else
							builder.setMessage(R.string.register_error);
						
						AlertDialog alert = builder.create();
						alert.setCancelable(false);
						alert.show();
					}
				};
				RegisterTask regTask = new RegisterTask(
						LoginActivity.this, 
						getString(R.string.sending_registration),
						responder);
				regTask.execute(registerEmail.getText().toString(),
						registerPassword.getText().toString(), 
						registerFullName.getText().toString());
			}
			else
			{
				
			}
		}
	};
	
	protected boolean validateRegister()
	{
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(
				new RegExpressionValidator(
						registerEmail, 
						RegExpressionValidator.EMAIL_REGEX, 
						getString(R.string.invalid_email), 
						"Please Enter A Valid Email"));
		validators.add(
				new RegExpressionValidator(registerPassword, 
						RegExpressionValidator.PASSWORD_REGEX, 
						getString(R.string.invalid_password), 
						"Please Enter A Valid Password"));
		validators.add(
				new EditTextMatchRequiredValidator(registerPassword, 
						registerPasswordConf, 
						"Your Passwords Don't Match"));
		validators.add(
				new RegExpressionValidator(registerFullName, 
						RegExpressionValidator.FULL_NAME_REGEX, 
						"Invalid Full Name", 
						"Please Enter A Valid Full Name"));
		List<ValidationResult> _validationResults = AbstractValidator.validateAll(validators);
		if (_validationResults.size()==0) {
        	return true;
        }
		else
		{
			ValidationResult result = _validationResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
	
	protected OnResponseListener recoveryRequestResponseListener = new OnResponseListener() 
	{
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
	
	// register control views
	private EditText registerEmail;
	private EditText registerPassword;
	private Button registerSubmit;
	private EditText registerPasswordConf;
	private EditText registerFullName;
	// end register control views
	
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
	/**
	 * @return a <code>ScrollView</code> with a <code>LinearLayout</code>
	 * as its only child 
	 */
	protected ScrollView buildSecondaryLayout()
	{
		ScrollView secondaryScrollView = new ScrollView(LoginActivity.this);
		float density = getResources().getDisplayMetrics().density;
		int orientation = getResources().getConfiguration().orientation;
		RelativeLayout.LayoutParams params;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			params = new RelativeLayout.LayoutParams(
					(int)(330 * density), (int)(240 * density));
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//			secondaryScrollView.setGravity
		}
		else
		{
			params = new RelativeLayout.LayoutParams(
					(int)(240 * density), (int)(330 * density));
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		secondaryScrollView.setId(secondaryFormID );
		secondaryScrollView.setLayoutParams(params);
		secondaryScrollView.setBackgroundColor(Color.RED);
		secondaryScrollView.setBackgroundResource(R.drawable.recover_back);
		
		LinearLayout layout = new LinearLayout(LoginActivity.this);
		params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		layout.setGravity(Gravity.TOP);
		layout.setOrientation(LinearLayout.VERTICAL);
		secondaryScrollView.addView(layout);
		
		return secondaryScrollView;
	}
	
	// TODO copy contents of login email over
	protected void showRegisterForm()
	{
		Log.d("SPENCER", "showRegisterForm()");
		ScrollView scrollView = buildSecondaryLayout();
		LinearLayout secondaryFormLayout = (LinearLayout) scrollView.getChildAt(0);
		
		recoverTitle = new TextView(LoginActivity.this);
		recoverTitle.setPadding(0, -15, 0, 0);
		recoverTitle.setGravity(Gravity.RIGHT);
		recoverTitle.setText(R.string.register);
		recoverTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
		recoverTitle.setTypeface(null, Typeface.BOLD);
		recoverTitle.setTextColor(R.color.black);
		secondaryFormLayout.addView(recoverTitle);
		
		LayoutParams editTextParams = new LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		
		TextView emailLabel = new TextView(LoginActivity.this);
		emailLabel.setText(R.string.email);
		emailLabel.setTextColor(Color.DKGRAY);
		secondaryFormLayout.addView(emailLabel);
		
		registerEmail = new EditText(LoginActivity.this);
		registerEmail.setLayoutParams(editTextParams);
		registerEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		secondaryFormLayout.addView(registerEmail);
		
		TextView passwordLabel = new TextView(LoginActivity.this);
		passwordLabel.setTextColor(Color.DKGRAY);
		passwordLabel.setText(R.string.password);
		secondaryFormLayout.addView(passwordLabel);
		
		registerPassword = new EditText(LoginActivity.this);
		registerPassword.setLayoutParams(editTextParams);
		registerPassword.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_VARIATION_PASSWORD);
		secondaryFormLayout.addView(registerPassword);
		
		TextView passwordConfLabel = new TextView(LoginActivity.this);
		passwordConfLabel.setTextColor(Color.DKGRAY);
		passwordConfLabel.setText(R.string.confirm_password);
		secondaryFormLayout.addView(passwordConfLabel);
		
		registerPasswordConf = new EditText(LoginActivity.this);
		registerPasswordConf.setLayoutParams(editTextParams);
		registerPasswordConf.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_VARIATION_PASSWORD);
		secondaryFormLayout.addView(registerPasswordConf);
		
		TextView fullNameLabel = new TextView(LoginActivity.this);
		fullNameLabel.setTextColor(Color.DKGRAY);
		fullNameLabel.setText(R.string.full_name);
		secondaryFormLayout.addView(fullNameLabel);
		
		registerFullName = new EditText(LoginActivity.this);
		registerFullName.setLayoutParams(editTextParams);
		registerFullName.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		secondaryFormLayout.addView(registerFullName);
		
		registerSubmit = new Button(LoginActivity.this);
		registerSubmit.setText(R.string.submit);
		registerSubmit.setLayoutParams(
				new LayoutParams(LayoutParams.WRAP_CONTENT, 
						LayoutParams.WRAP_CONTENT)
				);
		registerSubmit.setOnClickListener(registerOnClickListener);
		secondaryFormLayout.addView(registerSubmit);
		
		registerFormView = scrollView;
		
		removeRecoverLoginForm();
		removeRegisterLoginForm();
		
		loginParentLayout.addView(registerFormView);
		
		bRegister.setEnabled(false);
	}

	protected void showRecoverLoginForm() 
	{
		Log.d("SPENCER", "showRecoverLoginForm()");
		ScrollView scrollView = buildSecondaryLayout();
		LinearLayout secondaryFormLayout = (LinearLayout) scrollView.getChildAt(0);
		
		recoverTitle = new TextView(LoginActivity.this);
		recoverTitle.setPadding(0, -15, 0, 0);
		recoverTitle.setGravity(Gravity.RIGHT);
		recoverTitle.setText(R.string.recover_login);
		recoverTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
		recoverTitle.setTypeface(null, Typeface.BOLD);
		recoverTitle.setTextColor(R.color.black);
		secondaryFormLayout.addView(recoverTitle);
		
		recoverEmail = new EditText(LoginActivity.this);
		recoverEmail.setHint(R.string.email);
		recoverEmail.setLayoutParams(
				new LayoutParams(LayoutParams.FILL_PARENT, 
						LayoutParams.WRAP_CONTENT)
				);
		secondaryFormLayout.addView(recoverEmail);
		
		recoverSubmit = new Button(LoginActivity.this);
		recoverSubmit.setText(R.string.submit);
		recoverSubmit.setLayoutParams(
				new LayoutParams(LayoutParams.WRAP_CONTENT, 
						LayoutParams.WRAP_CONTENT)
				);
		recoverSubmit.setOnClickListener(recoverOnClickListener);
		secondaryFormLayout.addView(recoverSubmit);
		
		recoverFormLayout = scrollView;
		
		removeRegisterLoginForm();
		removeRecoverLoginForm();
		
		if(loginParentLayout.getDrawableState().length == 1)
		{
			Log.w("SPENCER", "ASDFASDF");
			loginParentLayout.addView(recoverFormLayout);
			loginParentLayout.childDrawableStateChanged(recoverFormLayout);
			loginParentLayout.invalidate();	
		}
		else
			loginParentLayout.addView(recoverFormLayout);
		
		bRecover.setEnabled(false);
	}
	
	private void removeRegisterLoginForm() {
		bRegister.setEnabled(true);
		loginParentLayout.removeView(registerFormView);
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
				RegExpressionValidator.EMAIL_REGEX, getString(R.string.invalid_email), "Please Enter A Valid Email"));
		validators.add(new RegExpressionValidator(
				etPassword, 
				RegExpressionValidator.PASSWORD_REGEX, getString(R.string.invalid_password), "Please Enter A Valid Password"));
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

	private String rSemis(String in)
	{
		return in.replace(";", "");
	}
}