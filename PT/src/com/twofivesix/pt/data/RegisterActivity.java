package com.twofivesix.pt.data;

import java.util.ArrayList;
import java.util.List;

import com.twofivesix.pt.data.validator.AbstractValidator;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.data.validator.ValidationResult;
import com.twofivesix.pt.data.validator.Validator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class RegisterActivity extends Activity {
	protected EditText etEmailAddress;
	protected EditText etPassword;
	protected EditText etPasswordComf;
	protected EditText etFullName;
	protected EditText etBirthYear;
	protected RadioButton radioMale;
	protected RadioButton radioFemale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		Button bSubmit = (Button) findViewById(R.id.register_submit_btn);
		Button bCancel = (Button) findViewById(R.id.register_cancel_btn);
		etEmailAddress = (EditText) findViewById(R.id.register_email_et);
		
		bSubmit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(validate())
				{
					// Register User Here
					Intent i = new Intent();
					i.putExtra("emailAddress", etEmailAddress.getText().toString());
					setResult(RESULT_OK, i);
					finish();
				}
			}
		});
		bCancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	protected boolean validate() {
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(new RegExpressionValidator(etEmailAddress, ".+@.+\\.[a-z]+", "Invalid Email", "Please Enter A Valid Email"));
		List<ValidationResult> _validationResults = AbstractValidator.validateAll(validators);
		if (_validationResults.size()==0) {
        	return true;
        }
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
			builder.setMessage(_validationResults.get(0).getMessage());
			builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		return false;
	}
}
