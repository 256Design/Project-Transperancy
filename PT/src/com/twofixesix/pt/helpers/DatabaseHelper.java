package com.twofixesix.pt.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.twofivesix.pt.data.Partner;
import com.twofivesix.pt.data.Question;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "project_transparency";
    private static final int DATABASE_VERSION = 7;
	private static final String QUESTION_TABLE = "tb_questions";
	private static final String PARTNER_TABLE = "tb_partners";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		 String sql;
		 sql = "CREATE TABLE IF NOT EXISTS tb_questions (" +
                 "_id INTEGER DEFAULT -1, " + 
                 "question TEXT PRIMARY KEY, " +
                 "type TEXT default 'YES_NO', " +
                 "positive TEXT, " +
                 "date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                 "out_of_sync INTEGER DEFAULT 0)";
		db.execSQL(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS " + getPartnerTable() + "(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"email TEXT," +
				"state TEXT," +
				"date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		db.execSQL(sql);
		
		/*ContentValues values = new ContentValues();
		
		values.put("question", "Have you done x since last reporting?");
		values.put("positive", "YES");
		db.insert(getQuestionTable(), null, values);
		
		values.put("question", "Have you been free since last reporting?");
		values.put("positive", "YES");
		db.insert(getQuestionTable(), null, values);
		
		values.put("question", "Have you pooped since last reporting?");
		values.put("positive", "NO");
		db.insert(getQuestionTable(), null, values);
		
		values = new ContentValues();
		values.put("email", "sober320@uwsp.edu");
		values.put("state", "confirm");
		db.insert(getPartnerTable(), null, values);*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + getQuestionTable());
		db.execSQL("DROP TABLE IF EXISTS " + getPartnerTable());
        onCreate(db);
	}
	
	public static boolean addUpdateQuestion(Question question, String oldQuestion, SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT count(*) from "+ getQuestionTable() + " WHERE _id = " + question.getId(), 
				null);
		c.moveToFirst();
		Log.d("Spencer", c.getInt(0) + "");
		Boolean inTable = (c.getInt(0) == 1);
		c.close();
		if(!inTable)
			return addQuestion(question, db);
		else
			return updateQuestion(question, oldQuestion, db);
	}
	
	public static boolean updateQuestion(Question question, String oldQuestion, SQLiteDatabase db) {
		if(oldQuestion == null || oldQuestion.equals(""))
			oldQuestion = question.getQuestion();
		try
		{
			db.execSQL("UPDATE " + QUESTION_TABLE + " " +
					"SET _id=" + question.getId() + ", " +
					"question = '" + question.getQuestion() + "', " +
					"type = '"+question.getType()+"', " +
					"positive = '"+question.getPositive()+"', " +
					"out_of_sync = 1 " +
					"WHERE question = '" + oldQuestion + "'");
/*			db.execSQL("UPDATE " + QUESTION_TABLE + " " +
					"SET _id= '?', question = '?', " +
					"type = '?', " +
					"positive = '?', " +
					"out_of_sync = 1 " +
					"WHERE question = '?'",
					new String[]{(question.getId()+""), question.getQuestion(),
						question.getType(),
						question.getPositive(),
						oldQuestion});*/
			return true;
		}
		catch (SQLException e) {
			Log.e("SPENCER", e.getMessage());
			return false;
		}
		/*
		ContentValues cv = new ContentValues();
    	cv.put("_id", question.getId());
    	cv.put("type", question.getType());
    	cv.put("positive", question.getPositive());
    	cv.put("out_of_sync", 1);
		return db.update(getQuestionTable(), cv, "question = '?'", 
				new String[] {(oldQuestion != null && !oldQuestion.equals(""))?oldQuestion :question.getQuestion()}) == 1;
		*/
	}
	
	public static boolean updateQuestionId(Question question, SQLiteDatabase db)
	{
		db.execSQL("UPDATE " + QUESTION_TABLE + " SET _id=" + question.getId() + ", out_of_sync = 0 WHERE question = '" + question.getQuestion() + "'");
		return true;
		/*
		ContentValues cv = new ContentValues();
    	cv.put("_id", question.getId());
    	return db.update(getQuestionTable(), cv, "question = '?'", 
				new String[] {question.getQuestion()}) == 1;
				*/
	}
	
	public static boolean addQuestion(Question question, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
    	cv.put("question", question.getQuestion());
    	cv.put("type", question.getType());
    	cv.put("positive", question.getPositive());
    	long result = db.insert(getQuestionTable(), null, cv);
    	return (result != -1);
    	/*if(result != -1)
    	{
    		question.setId((int) result);
    		return 1;
    	}
		return -1;*/
	}
	
	public static boolean setInSync(Question question, SQLiteDatabase db)
	{
		db.execSQL("UPDATE " + QUESTION_TABLE + " SET _id=" + question.getId() + ", out_of_sync = 0 WHERE question = '" + question.getQuestion() + "'");
		return true;
		/*ContentValues cv = new ContentValues();
    	cv.put("_id", question.getId());
    	cv.put("out_of_sync", 0);
		return db.update(getQuestionTable(), cv, "question = '?'", new String[] {question.getQuestion()}) == 1;*/
	}
	
	public static ArrayList<Question> buildQuestionsList(SQLiteDatabase db)
	{
		Cursor cursor = db.rawQuery("SELECT _id, question, type, positive, date_added from " + DatabaseHelper.getQuestionTable(), null);
        
        ArrayList<Question> questionArrayList = new ArrayList<Question>();
        
		int idRow = cursor.getColumnIndex("_id");
        int questionRow = cursor.getColumnIndex("question");
        int typeRow = cursor.getColumnIndex("type");
        int positiveRow = cursor.getColumnIndex("positive");
        int dateAddedRow = cursor.getColumnIndex("date_added");
        
        SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateAdded;
        while(cursor.moveToNext())
        {
        	//Log.d("SPENCER", cursor.getString(dateAddedRow));
        	try {
				dateAdded = parserSDF.parse(cursor.getString(dateAddedRow));
			} catch (ParseException e) {
				dateAdded = new Date();
			}
        	Question q = new Question(cursor.getInt(idRow), cursor.getString(questionRow), cursor.getString(typeRow), 
        			cursor.getString(positiveRow), dateAdded);
        	questionArrayList.add(q);
        }
		cursor.close();
        
        return questionArrayList;
	}
	
	public static ArrayList<Question> buildUpdatedQuestionsList(
			SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT _id, question, type, positive, date_added, out_of_sync from " + DatabaseHelper.getQuestionTable(), null);
        
        ArrayList<Question> questionArrayList = new ArrayList<Question>();
        
		int idRow = cursor.getColumnIndex("_id");
        int questionRow = cursor.getColumnIndex("question");
        int typeRow = cursor.getColumnIndex("type");
        int positiveRow = cursor.getColumnIndex("positive");
        int dateAddedRow = cursor.getColumnIndex("date_added");
        int outOfSyncRow = cursor.getColumnIndex("out_of_sync");
        
        SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateAdded;
        while(cursor.moveToNext())
        {
        	//Log.d("SPENCER", cursor.getString(dateAddedRow));
        	try {
				dateAdded = parserSDF.parse(cursor.getString(dateAddedRow));
			} catch (ParseException e) {
				dateAdded = new Date();
			}
        	Question q = new Question(cursor.getInt(idRow), cursor.getString(questionRow), cursor.getString(typeRow), 
        			cursor.getString(positiveRow), dateAdded);
        	//d(cursor.getInt(outOfSyncRow) + " " + q.getId() + " " + questionArrayList.size());
        	if(q.getId() == -1 || cursor.getInt(outOfSyncRow) == 1)
        		questionArrayList.add(q);
        }
		cursor.close();
        
        return questionArrayList;
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

	public static long addUpdatePartner(Partner partner, SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT count(*) from "+ getQuestionTable() + " WHERE _id = " + partner.getId(), 
				null);
		c.moveToFirst();
		Log.d("Spencer", c.getInt(0) + "");
		Boolean inTable = (c.getInt(0) == 1);
		c.close();
		if(!inTable)
			return addPartner(partner, db);
		else
			return updatePartner(partner, db);
	}
	
	public static int updatePartner(Partner partner, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put("email", partner.getEmail());
		cv.put("state", partner.getState());
		return db.update(getPartnerTable(), cv, "_id = ?", new String[] {Integer.toString(partner.getId())});
	}
	
	public static int updatePartnerState(String email, String state, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put("state", state);
		return db.update(getPartnerTable(), cv, "email = ?", new String[] {email});
	}
	
	public static long addPartner(Partner partner, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put("email", partner.getEmail());
		cv.put("state", partner.getState());
		long result = db.insert(getPartnerTable(), null, cv);
		if(result != -1)
		{
			partner.setId((int) result);
			return 1;
		}
		return -1;
	}

	/** Checks for the existence of a partner.
	 * 
	 * @param newPartner
	 * @param db
	 * @return true if there is a partner of the
	 * same email in the database.
	 */
	public static boolean checkExistingPartner(Partner newPartner,
			SQLiteDatabase db) {
		String[] params = {newPartner.getEmail()};
		
		Cursor c = db.rawQuery("SELECT count(*) from "+ getPartnerTable() + " WHERE email = ?" , 
				params);
		c.moveToFirst();
		Boolean inTable = (c.getInt(0) == 1);
		c.close();
		return inTable;
	}

	public static ArrayList<String> getPartnerEmails(SQLiteDatabase db) {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		Cursor c = db.rawQuery("SELECT email from "+getPartnerTable(), null);
		while(c.moveToNext())
		{
			toReturn.add(c.getString(0));
		}
		c.close();
		
		return toReturn;
	}

	public static void rewriteQuestionsList(
			ArrayList<Question> newQuestionList, SQLiteDatabase db) 
	{
		d("rewriteQuestionsList");
		db.delete(QUESTION_TABLE, null, null);
		if(newQuestionList != null)
		{
			for (Question question : newQuestionList) {
				ContentValues values = new ContentValues();
				values.put("_id", question.getId());
				values.put("question", question.getQuestion());
				values.put("type", question.getType());
				values.put("positive", question.getPositive());
				values.put("date_added", question.getDateAdded().getTime());
				db.insert(QUESTION_TABLE, null, values );
			}
		}
	}
	
	private static void d(String string) {
		Log.d("SPENCER", string);
	}
	
}
