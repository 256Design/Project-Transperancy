package com.twofivesix.pt.activities;

import java.util.ArrayList;

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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Partner;
import com.twofivesix.pt.data.PartnerListAdapter;
import com.twofixesix.pt.helpers.DatabaseHelper;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class ViewPartnersListActivity extends Activity {
	
		protected SQLiteDatabase db;
		protected Cursor cursor;
		protected EditText addPartnerText;
		protected ListAdapter adapter;
		protected ListView partnerList;

		private static final int CONTMENU_DELETE = 1234;
		
		private ArrayList<Partner> partnerArrayList = new ArrayList<Partner>();
		private Button saveNewPartnerButton;
		protected EditText newPartnerEmailET;
		private SharedPreferencesHelper settings;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_partners_activity);
        
        settings = new SharedPreferencesHelper(this);
        db = (new DatabaseHelper(this)).getWritableDatabase();
        partnerList = (ListView) findViewById(R.id.currentPartnersList);
		saveNewPartnerButton = (Button) findViewById(R.id.partners_save_new_partner);
		newPartnerEmailET = (EditText) findViewById(R.id.partners_new_partner_email);
        
        registerForContextMenu(partnerList);
        partnerList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(R.string.partner_list_context_menu_title);
				//menu.add(0, CONTMENU_EDIT, 0, R.string.edit_partner_cont);
				menu.add(0, CONTMENU_DELETE, 0, R.string.delete_partner_cont);
			}
		});
        
        saveNewPartnerButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Partner newPartner = new Partner(newPartnerEmailET.getText().toString());
				if(checkNotUser(newPartner.getEmail()))
				{
					if(Partner.validPartnerEmail(newPartner.getEmail()))
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
        
        updatePartnerList();
    }
    
    protected boolean checkNotUser(String email)
	{
		return !(settings.getUserEmail().equals(email));
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
        partnerList.setAdapter(adapter);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode == RESULT_OK)
	    {
	    	/*Question question = (Question) data.getSerializableExtra("question");
	    	Toast toast;
	    	if(DatabaseHelper.addUpdateQuestion(question, db) == 1)
	    		toast = Toast.makeText(this, R.string.success_add_question, 5000);
	    	else
	    		toast = Toast.makeText(this, R.string.error_add_update_question, 5000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			updatePartnerList();*/
	    }
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Partner partnerContexted = (menuInfo != null) 
    			?(Partner) partnerList.getAdapter().getItem(menuInfo.position)
    			:null;
    	
    	switch (item.getItemId()) {
		case R.id.partner_sync_option_item:
			if(syncPartnersStatus())
				updatePartnerList();
			return true;
         case CONTMENU_DELETE:
             confirmDeletePartner(partnerContexted);
             return true;
		default:
			return false;
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.partners_options_menu, menu);
        return true;
    }

	private boolean syncPartnersStatus() {
		if(NetworkConnectivityHelper.isConnected(ViewPartnersListActivity.this))
			return Partner.syncPartners(settings.getUserID(), db);
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