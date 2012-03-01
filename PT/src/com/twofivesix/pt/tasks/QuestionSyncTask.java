package com.twofivesix.pt.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;
import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.interfaces.SyncCaller;

public class QuestionSyncTask extends SyncTask {

	public QuestionSyncTask(SyncCaller activity, ProgressDialog progressDialog,
			SQLiteDatabase db) {
		super(activity, progressDialog, db);
	}
	
	public QuestionSyncTask(SyncCaller activity, Context context)
	{
		super(
			activity, 
			progressDialog(context), 
			(new DatabaseHelper(context)).getWritableDatabase());
	}

	@Override
	protected Boolean doInBackground(Integer... params) 
	{
		try
		{
			String url = "http://www.256design.com/projectTransparency/project/syncQuestions.php?id="+params[0];
			
			
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(url);
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    
		    String newUpdateQuestionString = "";
		    ArrayList<Question> questions = DatabaseHelper.buildUpdatedQuestionsList(db);
		    //d(questions.size() + " to sync");
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
			
			Log.d("SPENCER", "question sync response: " + response.getStatusLine().getStatusCode());
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
	        			//d("line: " + splitLine[4]);
	        			SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        			Date dateAdded = new Date();
	        			try
	        			{
	        				//d(splitLine[4]);
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
				/*
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					d(line);
				}
				*/
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static ProgressDialog progressDialog(Context context) 
	{
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getString(R.string.syncing_questions));
		progressDialog.setCancelable(false);
		
		return progressDialog;
	}

}
