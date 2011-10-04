package com.twofivesix.pt.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;

public class LoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;
	
	SharedPreferencesHelper settings;
	private EditText etEmailAddress;
	private EditText etPassword;
	private Button bLogin;
	private Button bRecover;
	private Button bRegister;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		settings = new SharedPreferencesHelper(this);
		if(settings.getLoggedIn())
		{
			// TODO REMOVE THIS!
			//settings.setLoggedIn(false);
			// user is logged in, bypass activity
			//startActivityForResult(new Intent(LoginActivity.this, ViewQuestionsListActivity.class), LOGIN_REQUEST_CODE);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		
		bLogin = (Button) findViewById(R.id.login_button);
		bRecover = (Button) findViewById(R.id.login_recover_button);
		bRegister = (Button) findViewById(R.id.login_register_button);
		etEmailAddress = (EditText) findViewById(R.id.login_usernameTV); 
		etPassword = (EditText) findViewById(R.id.login_passowrdTV);
		

		etEmailAddress.setText(settings.getUserEmail());
		etPassword.setText(settings.getUserEmail());
		
		bLogin.setOnClickListener(loginOnClickListener);
		bRecover.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//startActivityForResult(new Intent(LoginActivity.this, RecoverActivity.class), RECOVER_REQUEST_CODE);
			}
		});
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
			Log.d("SPENCER", "RESULT_CANCELED");
			finish();
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
	
	protected OnClickListener loginOnClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			if(validate())
			{
				ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
				progressDialog.setMessage("Logging in...");
				progressDialog.setCancelable(false);
				progressDialog.show();
				
				String result = "";
				
				try 
				{
					HttpClient client = new DefaultHttpClient();
				    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/login.php");
				    
				    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			        nameValuePairs.add(new BasicNameValuePair("emailAddress", etEmailAddress.getText().toString()));
			        nameValuePairs.add(new BasicNameValuePair("password", etPassword.getText().toString()));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			        
					Log.d("SPENCER", "send login request");
			        // Execute HTTP Post Request
			        HttpResponse response = client.execute(httppost);
			        //System.out.println("response");
					BufferedReader rd = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent()));
					
					String line;
					while ((line = rd.readLine()) != null)
					{
						result = line;
						Log.d("SPENCER", "Log on result: |" + result + "|");
					}
				}
				catch (Exception e) {
					result = "Error";
					e.printStackTrace();
				}
				finally
				{
					progressDialog.cancel();
				}
				
				if(result.equals("Success"))
				{
					settings.setUserEmail(etEmailAddress.getText().toString());
					settings.setUserPassword(etPassword.getText().toString());
					settings.setLoggedIn(true);
					startActivityForResult(new Intent(LoginActivity.this, ViewQuestionsListActivity.class), LOGIN_REQUEST_CODE);
				}
				else 
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
			}
		}
	};


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
