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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.EditTextMatchRequiredValidator;
import com.twofivesix.pt.data.validator.RadioButtonRequiredValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;

public class RegisterActivity extends Activity {
	protected EditText etEmailAddress;
	protected EditText etPassword;
	protected EditText etPasswordConf;
	protected EditText etFullName;
	protected EditText etBirthYear;
	protected RadioButton radioMale;
	protected RadioButton radioFemale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		Button bSubmit = (Button) findViewById(R.id.register_submit_btn);
		Button bCancel = (Button) findViewById(R.id.register_cancel_btn);
		etEmailAddress = (EditText) findViewById(R.id.register_email_et);
		etPassword = (EditText) findViewById(R.id.register_password_et);
		etPasswordConf = (EditText) findViewById(R.id.register_conf_password_et);
		etFullName = (EditText) findViewById(R.id.register_full_name_et);
		etBirthYear = (EditText) findViewById(R.id.register_birth_year_et);
		radioMale = (RadioButton) findViewById(R.id.register_male_radio);
		radioFemale = (RadioButton) findViewById(R.id.register_female_radio);
		
		bSubmit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(validate())
				{
					ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
					progressDialog.setMessage("Sending Registration...");
					progressDialog.setCancelable(false);
					progressDialog.show();
					
					String result;
					
					// Register User Here
					try
					{
						HttpClient client = new DefaultHttpClient();
					    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/regester.php");
					    
					    
						/*HttpGet request = new HttpGet("http://www.256design.com/projectTransparency/project/regester.php?" +
								"emailAddress=soberstadt@gmail.com" +
								"&password=google" +
								"&firstName=Spencer" +
								"&lastName=O" +
								"&gender=M" +
								"&birthYear=1991");*/
					    Log.d("SPENCER", (etFullName.getText().toString()==null)?"null":etFullName.getText().toString());
					    int i = etFullName.getText().toString().lastIndexOf(" ");
						String fName = etFullName.getText().toString().substring(0, i);
						String lName = etFullName.getText().toString().substring(i+1);
						Log.d("SPENCER", "set vals. Email: " + etEmailAddress.getText().toString());
						String gender = (radioMale.isChecked()) ? "M" : "F";
						
						
						
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				        nameValuePairs.add(new BasicNameValuePair("emailAddress", etEmailAddress.getText().toString()));
				        nameValuePairs.add(new BasicNameValuePair("password", etPassword.getText().toString()));
				        nameValuePairs.add(new BasicNameValuePair("firstName", fName));
				        nameValuePairs.add(new BasicNameValuePair("lastName", lName));
				        nameValuePairs.add(new BasicNameValuePair("gender", gender));
				        nameValuePairs.add(new BasicNameValuePair("birthYear", etBirthYear.getText().toString()));
				        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				        
				        
				        
				        // Execute HTTP Post Request
				        HttpResponse response = client.execute(httppost);
				        //System.out.println("response");
						BufferedReader rd = new BufferedReader(new InputStreamReader(
								response.getEntity().getContent()));
						
						Log.d("SPENCER", "send register request");
						
						String line = "";
						result = line;
						while ((line = rd.readLine()) != null) {
							Log.d("SPENCER", "|" + line + "|");
							result = line;
						}
					}
					catch(Exception e)
					{
						result = "Error";
						Log.e("SPENCER", "|" + e.getMessage() + "|");
					}
					finally
					{
						progressDialog.dismiss();
					}
					
					Log.d("SPENCER", "|" + result + "|");
					AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
					if(result.equals("Success"))
					{
						/*Toast toast;
						toast = Toast.makeText(RegisterActivity.this, R.string.register_success, 5000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();*/
						
						builder.setMessage(R.string.register_success);
						builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
								dialog.cancel();
							}
						});
						
						
						Intent i = new Intent();
						i.putExtra("emailAddress", etEmailAddress.getText().toString());
						i.putExtra("password", etPassword.getText().toString());
						setResult(RESULT_OK, i);
					}
					else
					{
						builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});

						if(result.equals("Email address already in use."))
							builder.setMessage(R.string.register_existing_email);
						else
							builder.setMessage(R.string.register_error);
						/*Toast toast;
				    	if(result.equals("Email address already in use."))
				    		toast = Toast.makeText(RegisterActivity.this, R.string.register_existing_email, 5000);
				    	else
				    		toast = Toast.makeText(RegisterActivity.this, R.string.register_error, 5000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();*/
					}
					AlertDialog alert = builder.create();
					alert.setCancelable(false);
					alert.show();
				}
			}
		});
		bCancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent().putExtra("emailAddress", etEmailAddress.getText().toString());
				setResult(RESULT_CANCELED, i);
				finish();
			}
		});
	}
	
	protected boolean validate() {
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(new RegExpressionValidator(etEmailAddress, RegExpressionValidator.EMAIL_REGEX, "Invalid Email", "Please Enter A Valid Email"));
		validators.add(new RegExpressionValidator(etPassword, RegExpressionValidator.PASSWORD_REGEX, "Invalid Password", "Please Enter A Valid Password"));
		validators.add(new EditTextMatchRequiredValidator(etPassword, etPasswordConf, "Your Passwords Don't Match"));
		validators.add(new RegExpressionValidator(etFullName, RegExpressionValidator.FULL_NAME_REGEX, "Invalid Full Name", "Please Enter A Valid Full Name"));
		validators.add(new RegExpressionValidator(etBirthYear, RegExpressionValidator.BIRTH_YEAR_REGEX, "Invalid Birth Year", "Please Enter A Valid Birth Year"));
		validators.add(new RadioButtonRequiredValidator((RadioGroup) findViewById(R.id.register_gender_radios), "Please Select Your Gender"));
		List<ValidationResult> _validationResults = AbstractValidator.validateAll(validators);
		if (_validationResults.size()==0) {
        	return true;
        }
		else
		{
			ValidationResult result = _validationResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
