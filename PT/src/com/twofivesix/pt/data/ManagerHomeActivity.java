package com.twofivesix.pt.data;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ManagerHomeActivity extends Activity {
	protected SQLiteDatabase db;
	protected Cursor cursor;
	
	protected TextView questionsInfo;
	protected TextView partnersInfo;
	protected Button questionsButton;
	protected Button partnersButton;
	
	protected static final int QUESTIONS_LIST_REQUEST_CODE = 1; 
	protected static final int PARTNERS_LIST_REQUEST_CODE = 2; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manager_home_activity);
		
		db = (new DatabaseHelper(this)).getWritableDatabase();
		
		questionsInfo = (TextView) findViewById(R.id.manager_questions_details);
		partnersInfo = (TextView) findViewById(R.id.manager_partners_details);
		questionsButton = (Button) findViewById(R.id.manager_questions);
		partnersButton = (Button) findViewById(R.id.manager_partners);
		
		questionsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(
						new Intent(ManagerHomeActivity.this, ViewQuestionsListActivity.class), ManagerHomeActivity.QUESTIONS_LIST_REQUEST_CODE);
			}
		});
		
		partnersButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(
						new Intent(ManagerHomeActivity.this, ViewPartnersListActivity.class), ManagerHomeActivity.PARTNERS_LIST_REQUEST_CODE);
			}
		});
		
		updateQuestionsInfo();
		updatePartnersInfo();
	}
	
	protected void updateQuestionsInfo()
	{
		cursor = db.rawQuery("SELECT count(*) from " + DatabaseHelper.getQuestionTable(), null);
		long count = 0;
		if(cursor.moveToFirst())
			count = cursor.getLong(0);
		questionsInfo.setText(R.string.manager_question_listed);
		questionsInfo.append("" + count);
	}
	
	protected void updatePartnersInfo()
	{
		cursor = db.rawQuery("SELECT count(*) from " + DatabaseHelper.getPartnerTable(), null);
		long count = 0;
		if(cursor.moveToFirst())
			count = cursor.getLong(0);
		partnersInfo.setText(R.string.manager_partners_listed);
		partnersInfo.append("" + count);
	}
}
