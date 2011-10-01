package com.twofivesix.pt.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity {
	protected static final int LOGIN_REQUEST_CODE = 0;
	protected static final int RECOVER_REQUEST_CODE = 1;
	protected static final int REGISTER_REQUEST_CODE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		Button bLogin = (Button) findViewById(R.id.login_button);
//		Button bRecover = (Button) findViewById(R.id.login_recover_button);
		Button bRegister = (Button) findViewById(R.id.login_register_button);
		
		bLogin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(new Intent(LoginActivity.this, ViewQuestionsListActivity.class), LOGIN_REQUEST_CODE);
			}
		});
		/*bRecover.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(new Intent(LoginActivity.this, RecoverActivity.class), RECOVER_REQUEST_CODE);
			}
		});*/
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
			
		}
	}
}
