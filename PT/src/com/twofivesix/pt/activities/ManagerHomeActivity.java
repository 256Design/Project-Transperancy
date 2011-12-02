package com.twofivesix.pt.activities;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Partner;
import com.twofivesix.pt.data.validator.RegExpressionValidator;
import com.twofivesix.pt.listAdapters.PartnerListAdapter;
import com.twofixesix.pt.helpers.DatabaseHelper;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class ManagerHomeActivity extends Activity {
	protected SQLiteDatabase db;
	protected Cursor cursor;
	
	protected TextView reportingTitle;
	protected TextView lastReportingInfo;
	protected Button reportNowButton;
	protected TextView questionsInfo;
	protected TextView partnersInfo;
	protected Button questionsButton;
	protected ListView partnersListView;
	protected EditText newPartnerEmailET;
	protected Button saveNewPartnerButton;

	private static final int CONTMENU_DELETE = 1234;
	private static final int CONTMENU_EDIT = 1235;
	
	protected static final int QUESTIONS_LIST_REQUEST_CODE = 1; 
	protected static final int PARTNERS_LIST_REQUEST_CODE = 2; 
	
	private ArrayList<Partner> partnerArrayList = new ArrayList<Partner>();
	private SharedPreferencesHelper settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manager_home_activity);
		
		settings = new SharedPreferencesHelper(this);
		db = (new DatabaseHelper(this)).getWritableDatabase();
		
		reportingTitle = (TextView) findViewById(R.id.manager_reporting_title);
		lastReportingInfo = (TextView) findViewById(R.id.manager_last_reporting_textview);
		reportNowButton = (Button) findViewById(R.id.manager_reporting_button);
		questionsInfo = (TextView) findViewById(R.id.manager_questions_details_textview);
		partnersInfo = (TextView) findViewById(R.id.manager_partners_details);
		questionsButton = (Button) findViewById(R.id.manager_questions);
		partnersListView = (ListView) findViewById(R.id.manager_current_partners_list);
		newPartnerEmailET = (EditText) findViewById(R.id.manager_new_partner_email);
		saveNewPartnerButton = (Button) findViewById(R.id.manager_save_new_partner);
		
		reportNowButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(
						new Intent(ManagerHomeActivity.this, ReportingActivity.class), 0);
			}
		});
		
		questionsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(
						new Intent(ManagerHomeActivity.this, ViewQuestionsListActivity.class), ManagerHomeActivity.QUESTIONS_LIST_REQUEST_CODE);
			}
		});
		
		registerForContextMenu(partnersListView);
        partnersListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info =
			            (AdapterView.AdapterContextMenuInfo) menuInfo;
				if(partnersListView.getAdapter().getItem(info.position) != null)
				{
					menu.setHeaderTitle(R.string.partner_list_context_menu_title);
					//menu.add(0, CONTMENU_EDIT, 0, R.string.edit_partner_cont);
					menu.add(0, CONTMENU_DELETE, 0, R.string.delete_partner_cont);
				}
			}
		});
        
        saveNewPartnerButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Partner newPartner = new Partner(newPartnerEmailET.getText().toString());
				if(checkNotUser(newPartner.getEmail()))
				{
					if(validPartnerEmail(newPartner.getEmail()))
					{
						if(!DatabaseHelper.checkExistingPartner(newPartner, db))
							confirmAddParner(newPartner);
						else
							showPartnerAddError(getString(R.string.error_existing_partner));
					}					
					else
						showPartnerAddError(getString(R.string.error_invalid_email));
				}
				else
					showPartnerAddError(getString(R.string.error_cant_add_self));
			}
		});
        
        syncPartnersStatus();
		
		updateQuestionsInfo();
		updatePartnerList();
	}
	
	@Override
	protected void onResume() 
	{
		CharSequence info;
		if(settings.getLastReportDateMillis() != 0)
		{
			int count;
			String interval;
			long difference = System.currentTimeMillis() - settings.getLastReportDateMillis();
			count = (int)(difference/(1000*60*60*24));
			if(count > 0)
			{
				if(count == 1)
					interval = getString(R.string.day);
				else
					interval = getString(R.string.days);
			}
			else
			{
				count = (int)(difference/(1000*60*60));
				if(count > 0)
				{
					if(count == 1)
						interval = getString(R.string.hour);
					else
						interval = getString(R.string.hours);
				}
				else 
				{
					count = (int)(difference/(1000*60));
					if(count > 0)
					{
						if(count == 1)
							interval = getString(R.string.minute);
						else
							interval = getString(R.string.minute);
					}
					else
					{
						count = 0;
						interval = getString(R.string.moments);
					}	
				}
				
			}
			if(count == 0)
				info = getText(R.string.manager_it_has_been).toString().replace("$", interval);
			else
				info = getText(R.string.manager_it_has_been).toString().replace("$", count + " " + interval);
				
			lastReportingInfo.setText(info);
		}
		else
		{
			lastReportingInfo.setText(R.string.manager_never_reported);
		}
		super.onResume();
	}
	
	protected boolean checkNotUser(String email)
	{
		return !(settings.getUserEmail().equals(email));
	}

	protected boolean validPartnerEmail(String email) {
		Pattern partner = Pattern.compile(RegExpressionValidator.EMAIL_REGEX);
		return partner.matcher(email).find();
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
    
    protected void updatePartnerList() {
    	cursor = db.rawQuery("SELECT _id, email, state, date_added from " + DatabaseHelper.getPartnerTable(), null);
        
        partnerArrayList = new ArrayList<Partner>();
        int idRow = cursor.getColumnIndex("_id");
        int emailRow = cursor.getColumnIndex("email");
        int stateRow = cursor.getColumnIndex("state");
        int dateAddedRow = cursor.getColumnIndex("date_added");
        while(cursor.moveToNext())
        {
        	Partner p = new Partner(cursor.getInt(idRow), cursor.getString(emailRow), cursor.getString(stateRow), new java.sql.Date(cursor.getLong(dateAddedRow)));
        	partnerArrayList.add(p);
        }
        //partnerArrayList.add(null);
        //Log.d("SPENCER", (partnerList == null) + " items");
        PartnerListAdapter adapter = new PartnerListAdapter(this, partnerArrayList);
        partnersListView.setAdapter(adapter);
	}
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	//Log.d("SPENCER", "onContextItemSelected()");
    	
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Partner partnerContexted = (Partner) partnersListView.getAdapter()
                .getItem(menuInfo.position);
    	 
        /* Switch on the ID of the item, to get what the user selected. */
        switch (item.getItemId()) {
                case CONTMENU_DELETE:
                    confirmDeletePartner(partnerContexted);
                    return true;
                default:
                    return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_options_menu, menu);
        
        Intent settingsIntent = new Intent(ManagerHomeActivity.this, SettingsActivity.class);
        MenuItem settingsItem = menu.findItem(R.id.settings_option_item);
        settingsItem.setIntent(settingsIntent);
        
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Partner partnerContexted = (menuInfo != null) 
    			?(Partner) partnersListView.getAdapter().getItem(menuInfo.position)
    			:null;
    	
    	switch (item.getItemId()) {
		case R.id.settings_option_item:
			ManagerHomeActivity.this.startActivity(item.getIntent());
			return true;
		case R.id.partner_sync_option_item:
			if(syncPartnersStatus())
				updatePartnerList();
			return true;
		case R.id.logout_option_item:
			setResult(LoginActivity.LOGOUT_RESULT_CODE);
			finish();
			return true;
         case CONTMENU_DELETE:
             confirmDeletePartner(partnerContexted);
             return true;
		default:
			return false;
		}
    }

	private boolean syncPartnersStatus() {
		if(NetworkConnectivityHelper.isConnected(ManagerHomeActivity.this))
			try {
				return Partner.syncPartners(settings.getUserID(), db);
			} catch (Exception e) {
				return false;
			}
		else
			return false;
	}

	// =======================================
    // General Functions
    // =======================================
    
    private void confirmDeletePartner(final Partner partnerContexted) {
    	
    	//Log.d("SPENCER", "confirmDeletePartner()");
    	
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	            deletePartner(partnerContexted);
    	            break;

    	        case DialogInterface.BUTTON_NEGATIVE:
    	            break;
    	        }
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.delete_partner);
    	builder.setMessage(R.string.are_you_sure);
    	builder.setPositiveButton(R.string.yes, dialogClickListener);
    	builder.setNegativeButton(R.string.no, dialogClickListener);
    	builder.show();
    }

    protected void deletePartner(Partner partnerContexted) {
    	if(Partner.submitDeteltePartner(settings.getUserID(),partnerContexted, this) && 
    			db.delete(DatabaseHelper.getPartnerTable(), "email = ?", new String[] {partnerContexted.getEmail()}) == 1)
    	{
	    	updatePartnerList();
	    	Toast toast = Toast.makeText(this, "Deleted parnter: " + partnerContexted.getEmail(), 5000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
    	}
    	else
    	{
    		Toast toast = Toast.makeText(this, "Error deleting parnter: " + partnerContexted.getEmail(), 5000);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();    		
    	}
    }
    
    protected void confirmAddParner(final Partner newPartner) {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	if(which == DialogInterface.BUTTON_POSITIVE)
    	    		addPartner(newPartner);
    	    }
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getText(R.string.confirm_add_partner).toString().replace("$", newPartner.getEmail()));
    	builder.setTitle(R.string.are_you_sure);
    	builder.setPositiveButton(R.string.yes, dialogClickListener);
    	builder.setNegativeButton(R.string.no, dialogClickListener);
    	builder.show();
    }

	protected void addPartner(Partner newPartner) {
		if(Partner.submitAddPartner(this, newPartner) && DatabaseHelper.addUpdatePartner(newPartner, db) == 1)
		{
			newPartnerEmailET.setText("");
			InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(newPartnerEmailET.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(saveNewPartnerButton.getWindowToken(), 0);
			updatePartnerList();
		}
		else
			showPartnerAddError();
	}

	private void showPartnerAddError() {
		Toast toast = Toast.makeText(this, R.string.error_add_partner, 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	private void showPartnerAddError(String string) {
		Toast toast = Toast.makeText(this, string, 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
