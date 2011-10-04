package com.twofivesix.pt.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "project_transparency";
    private static final int DATABASE_VERSION = 4;
	private static final String QUESTION_TABLE = "tb_questions";
	private static final String PARTNER_TABLE = "tb_partners";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		 String sql;
		 sql = "CREATE TABLE IF NOT EXISTS tb_questions (" +
                 "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                 "question TEXT, " +
                 "type TEXT default 'YES_NO', " +
                 "positive TEXT, " +
                 "date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		db.execSQL(sql);
		
		ContentValues values = new ContentValues();
		
		values.put("question", "Have you done x since last reporting?");
		values.put("positive", "YES");
		db.insert(getQuestionTable(), null, values);
		
		values.put("question", "Have you been free since last reporting?");
		values.put("positive", "YES");
		db.insert(getQuestionTable(), null, values);
		
		values.put("question", "Have you pooped since last reporting?");
		values.put("positive", "NO");
		db.insert(getQuestionTable(), null, values);
		
		sql = "CREATE TABLE IF NOT EXISTS " + getPartnerTable() + "(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"email TEXT," +
				"state TEXT," +
				"date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		db.execSQL(sql);
		
		values = new ContentValues();
		values.put("email", "sober320@uwsp.edu");
		values.put("state", "confirm");
		db.insert(getPartnerTable(), null, values);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + getQuestionTable());
		db.execSQL("DROP TABLE IF EXISTS " + getPartnerTable());
        onCreate(db);
	}
	
	public static long addUpdateQuestion(Question question, SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT count(*) from "+ getQuestionTable() + " WHERE _id = " + question.getId(), 
				null);
		c.moveToFirst();
		Log.d("Spencer", c.getInt(0) + "");
		Boolean inTable = (c.getInt(0) == 1);
		if(!inTable)
			return addQuestion(question, db);
		else
			return updateQuestion(question, db);
	}
	
	public static int updateQuestion(Question question, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
    	cv.put("question", question.getQuestion());
    	cv.put("type", question.getType());
    	cv.put("positive", question.getPositive());
		return db.update(getQuestionTable(), cv, "_id = ?", new String[] {Integer.toString(question.getId())});
	}
	
	public static long addQuestion(Question question, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
    	cv.put("question", question.getQuestion());
    	cv.put("type", question.getType());
    	cv.put("positive", question.getPositive());
    	long result = db.insert(getQuestionTable(), null, cv);
    	if(result != -1)
    	{
    		question.setId((int) result);
    		return 1;
    	}
		return -1;
	}

	/**
	 * @return the Question table name(String)
	 */
	public static String getQuestionTable() {
		return QUESTION_TABLE;
	}

	/**
	 * @return the Partners table name(String)
	 */
	public static String getPartnerTable() {
		return PARTNER_TABLE;
	}
}
