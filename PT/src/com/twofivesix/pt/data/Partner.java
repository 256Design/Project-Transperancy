package com.twofivesix.pt.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;

public class Partner implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRMED = "confirm";
	public static final String UNCONFIRMED = "unconfirmed";
	public static final String DENIED = "denied";
	
	private int id = -1;
	private String email;
	private String state;
	private Date dateAdded;
	
	public Partner(String email, String state, Date dateAdded)
	{
		this.email = email;
		this.state = state;
		this.dateAdded = dateAdded;
	}
	
	public Partner(int id, String email, String state, Date dateAdded)
	{
		this.id = id;
		this.email = email;
		this.state = state;
		this.dateAdded = dateAdded;
	}

	public Partner(String newPartnerEmailET) {
		this.email = newPartnerEmailET;
		this.state = UNCONFIRMED;
		this.dateAdded = new Date(System.currentTimeMillis());
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}

	public Date getDateAdded() {
		return dateAdded;
	}
	
	public static java.sql.Date getNow()
	{
		return new java.sql.Date(Calendar.getInstance().getTime().getTime());
	}
	
	public static boolean submitAddPartner(Context context, Partner partner)
	{
		String result;
		
		// Register User Here
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/regesterPartner.php");
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("emailAddress", partner.getEmail()));
	        nameValuePairs.add(new BasicNameValuePair("userID", "" + new SharedPreferencesHelper(context).getUserID()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
			do
			{
				// Execute HTTP Post Request
				Log.d("SPENCER", "send add partner request");
				response = client.execute(httppost);
				//Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");						
			} while (response.getStatusLine().getStatusCode() == 408);
	        
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
		if(result.equals("Success"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean syncPartners(int userID, SQLiteDatabase db) throws Exception
	{
		try
		{
			URL url = new URL("http://www.256design.com/projectTransparency/project/syncPartners.php?id="+userID);
			URLConnection conn = url.openConnection();
			BufferedReader rd = new BufferedReader(new 
			InputStreamReader(conn.getInputStream()));
            String line = "";
            ArrayList<Partner> newPartnersList = new ArrayList<Partner>();
            while ((line = rd.readLine()) != null) {
        		//Log.d("SPENCER", line);
        		if(line.equals("None"))
        		{
        			break;
        		}
        		String[] splitLine = line.split(":");
        		Partner partnerToAdd = new Partner(splitLine[1]);
        		if(splitLine[0].equals("Conf"))
        		{
        			partnerToAdd.setState(CONFIRMED);
        			newPartnersList.add(partnerToAdd);
        		}
        		else if(splitLine[0].equals("Unconf"))
        		{
        			partnerToAdd.setState(UNCONFIRMED);
        			newPartnersList.add(partnerToAdd);
        		}
        		else
        		{
        			partnerToAdd.setState(DENIED);
        			newPartnersList.add(partnerToAdd);
        		}
        		//Log.d("SPENCER", "added " + splitLine[1] + " to " + splitLine[0]);
            }
            
            ArrayList<String> currentPartners = DatabaseHelper.getPartnerEmails(db);
            topLevel:
            for (String currentPartnerEmail : currentPartners) {
            	//Log.d("SPENCER", "checking " + currentPartnerEmail);
            	
				for (Partner partner : newPartnersList) {
					if(currentPartnerEmail.equals(partner.getEmail()))
					{
						DatabaseHelper.updatePartnerState(partner.getEmail(), partner.getState(), db);
						newPartnersList.remove(partner);
						continue topLevel;
					}
				}
				DatabaseHelper.updatePartnerState(currentPartnerEmail, Partner.DENIED, db);
			}
            
            for (Partner partner : newPartnersList)
            {
            	DatabaseHelper.addPartner(partner, db);
            }
            
            return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static boolean submitDeteltePartner(int userID, Partner partnerContexted,
			Activity managerHomeActivity) {
		try {
			URL url = new URL("http://www.256design.com/projectTransparency/project/deletePartnerHeader.php?id="+userID+"&email="+partnerContexted.getEmail());
			HttpClient client = new DefaultHttpClient();
		    HttpGet httppost = new HttpGet(url.toURI());
	       
			int executeCount = 0;
			HttpResponse response;
			int responseCode;
			do
			{
				//publishProgress(executeCount+1,5);
				// Execute HTTP Post Request
				executeCount++;
				response = client.execute(httppost);
				responseCode  = response.getStatusLine().getStatusCode();						
			} while (executeCount < 5 && responseCode == 408);
			
			if(responseCode == 202)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;			
		}
	}

	public static boolean validPartnerEmail(String email) {
		Pattern partner = Pattern.compile(RegExpressionValidator.EMAIL_REGEX);
		return partner.matcher(email).find();
	}
}
