package com.twofivesix.pt.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;

import com.twofivesix.pt.data.Partner;
import com.twofivesix.pt.interfaces.SyncCaller;
import com.twofixesix.pt.helpers.DatabaseHelper;

public class PartnerSyncTask extends SyncTask {

	public PartnerSyncTask(SyncCaller activity, ProgressDialog progressDialog,
			SQLiteDatabase db) {
		super(activity, progressDialog, db);
	}

	@Override
	protected Boolean doInBackground(Integer... params) {
		try
		{
			URL url = new URL("http://www.256design.com/projectTransparency/project/syncPartners.php?id="+params[0]);
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
        			partnerToAdd.setState(Partner.CONFIRMED);
        			newPartnersList.add(partnerToAdd);
        		}
        		else if(splitLine[0].equals("Unconf"))
        		{
        			partnerToAdd.setState(Partner.UNCONFIRMED);
        			newPartnersList.add(partnerToAdd);
        		}
        		else
        		{
        			partnerToAdd.setState(Partner.DENIED);
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

}
