package com.twofivesix.pt.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Question implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TYPE_SHORT_ANSWER = "SHORT_ANSWER";
	public static final String TYPE_YES_NO = "YES_NO";
	public static final String RESP_YES = "respYes";
	public static final String RESP_NO = "respNo";
	
	private int id = -1;
	private String question;
	private String type;
	private String positive;
	private Date dateAdded;
	private String response;
	
	public Question(String question, String type, String positive, Date dateAdded)
	{
		this.question = question;
		this.type = type;
		this.positive = positive;
		this.dateAdded = dateAdded;
	}
	
	public Question(int id, String question, String type, String positive, Date dateAdded)
	{
		this.id = id;
		this.question = question;
		this.type = type;
		this.positive = positive;
		this.dateAdded = dateAdded;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if(this.id == -1)
			this.id = id;
		else
			throw new RuntimeException("Can not override id: " + this.id + ".");
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPositive() {
		return positive;
	}

	public void setPositive(String positive) {
		this.positive = positive;
	}

	public Date getDateAdded() {
		return dateAdded;
	}
	
	public static Date getNow()
	{
		return new java.sql.Date(Calendar.getInstance().getTime().getTime());
	}
	
	public String getResponse()
	{
		return response;
	}
	
	public void setResponse(String response) 
	{
		this.response = response;
	}
	
	public static boolean submitAddQuestion(Context context, Question question, SQLiteDatabase db)
	{
		String result;
		int responseCode = 0;
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(
		    		"http://www.256design.com/projectTransparency/project/modQuestion.php?add&userID="
		    				+new SharedPreferencesHelper(context).getUserID());
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("question", question.getQuestion()));
	        nameValuePairs.add(new BasicNameValuePair("type", question.getType()));
	        nameValuePairs.add(new BasicNameValuePair("positive", question.getPositive()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
	        int tries = 5;
			do
			{
				// Execute HTTP Post Request
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();
				Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");						
			} while (--tries > 0 && responseCode == 408);
	        
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			//Log.d("SPENCER", "send register request");
			
			String line = "";
			result = line;
			while ((line = rd.readLine()) != null) {
				//Log.d("SPENCER", "|" + line + "|");
				result = line;
			}
		}
		catch(Exception e)
		{
			result = "Error";
			Log.e("SPENCER", "|" + e.getMessage() + "|");
		}
		
		Log.d("SPENCER", "|" + result + "|");
		if(responseCode == 201)
		{
			try
			{
				question.setId(Integer.parseInt(result));
			}
			catch (NumberFormatException e) {
				return false;
			}
			d("set question id to " + question.getId());
			return DatabaseHelper.updateQuestionId(question, db);
		}
		else
		{
			return false;
		}
	}
	
	public static boolean submitUpdateQuestion(
			Context context,
			Question question, SQLiteDatabase db)
	{
		if(question.getId() == -1)
			return submitAddQuestion(context, question, db);
		String result;
		int responseCode = 0;
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/modQuestion.php?update&userID="+new SharedPreferencesHelper(context).getUserID());
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("questionID", question.getId()+""));
	        nameValuePairs.add(new BasicNameValuePair("question", question.getQuestion()));
	        nameValuePairs.add(new BasicNameValuePair("type", question.getType()));
	        nameValuePairs.add(new BasicNameValuePair("positive", question.getPositive()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
	        int tries = 5;
			do
			{
				response = client.execute(httppost);
			} while (--tries > 0 && responseCode == 408);

			responseCode = response.getStatusLine().getStatusCode();
			Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");					
	        
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String line = "";
			result = line;
			while ((line = rd.readLine()) != null) {
				result = line;
			}
		}
		catch(Exception e)
		{
			result = "Error";
			Log.e("SPENCER", "|" + e.getMessage() + "|");
		}
		
		Log.d("SPENCER", "|" + result + "|");
		if(responseCode == 202)
		{
			return DatabaseHelper.setInSync(question, db); 
		}
		else
		{
			return false;
		}
	}

	public static boolean submitDetelteQuestion(int userID, Question questionContexted) {
		d("submitDetelteQuestion");
		String result;
		int responseCode = 0;
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/modQuestion.php?delete&userID="+userID);
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("questionID", questionContexted.getId()+""));
	        nameValuePairs.add(new BasicNameValuePair("question", questionContexted.getQuestion()));
	        nameValuePairs.add(new BasicNameValuePair("type", questionContexted.getType()));
	        nameValuePairs.add(new BasicNameValuePair("positive", questionContexted.getPositive()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
	        int tries = 5;
			do
			{
				response = client.execute(httppost);
			} while (--tries > 0 && responseCode == 408);

			responseCode = response.getStatusLine().getStatusCode();
			Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");					
	        
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String line = "";
			result = line;
			while ((line = rd.readLine()) != null) {
				result = line;
			}
			Log.d("SPENCER", "|" + result + "|");
		}
		catch(Exception e)
		{
			result = "Error";
			Log.e("SPENCER", "|" + e.getMessage() + "|");
		}
		
		return responseCode == 202;
	}
	
	public static boolean syncQuestions(int userID, SQLiteDatabase db) throws Exception
	{
		try
		{
			String url = "http://www.256design.com/projectTransparency/project/syncQuestions.php?id="+userID;
			
			
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(url);
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    
		    String newUpdateQuestionString = "";
		    ArrayList<Question> questions = DatabaseHelper.buildUpdatedQuestionsList(db);
		    d(questions.size() + " to sync");
		    if(questions.size() != 0)
		    {
			    for (Question question : questions) {
					newUpdateQuestionString += question.getId() + "|" + question.getQuestion().replace("|", "") + "|" + question.getType() + "|" + question.getPositive() + "\n";
				}
			    nameValuePairs.add(new BasicNameValuePair("questions", newUpdateQuestionString));
		    }
		    
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	       
			HttpResponse response;
			int tries = 5;
			do
			{
				// Execute HTTP Post Request
				response = client.execute(httppost);
				Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");						
			} while (--tries > 0 && response.getStatusLine().getStatusCode() == 408);
			
			if(response.getStatusLine().getStatusCode() == 202)
			{
				BufferedReader rd = new BufferedReader(new InputStreamReader(
							response.getEntity().getContent()));
	            String line = "";
	            ArrayList<Question> newQuestionList = new ArrayList<Question>();
	            while ((line = rd.readLine()) != null) {
	        		if(!line.equals("None"))
	        		{
	        			String[] splitLine = line.split("\\|");
	        			d("line: " + splitLine[4]);
	        			SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        			Date dateAdded = new Date();
	        			try
	        			{
	        				d(splitLine[4]);
	        				dateAdded = parserSDF.parse(splitLine[4]);
	        				Question questionToAdd = new Question(Integer.parseInt(splitLine[0]), splitLine[1], splitLine[2], splitLine[3], dateAdded);
	        				newQuestionList.add(questionToAdd);
	        			}
	        			catch (Exception e) {
	        				Log.e("SPENCER", e.getMessage());
						}
	        		}
	            }
	            DatabaseHelper.rewriteQuestionsList(newQuestionList, db);
	            return true;
			}
			else if (response.getStatusLine().getStatusCode() == 204)
			{
				DatabaseHelper.rewriteQuestionsList(null, db);
				return true;
			}
			else
			{
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					d(line);
				}
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	private static void d(String string) {
		Log.d("SPENCER", string);
	}
}
