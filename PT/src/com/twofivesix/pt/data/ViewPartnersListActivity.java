package com.twofivesix.pt.data;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewPartnersListActivity extends Activity {
	
		protected SQLiteDatabase db;
		protected Cursor cursor;
		protected EditText addPartnerText;
		protected ListAdapter adapter;
		protected ListView partnerList;

		private static final int CONTMENU_DELETE = 1234;
		private static final int CONTMENU_EDIT = 1235;
		
		private ArrayList<Partner> partnerArrayList = new ArrayList<Partner>();
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_partners_activity);
        
        db = (new DatabaseHelper(this)).getWritableDatabase();
        partnerList = (ListView) findViewById(R.id.currentPartnersList);
        updatePartnerList();
        registerForContextMenu(partnerList);
        partnerList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(R.string.partner_list_context_menu_title);
				menu.add(0, CONTMENU_EDIT, 0, R.string.edit_partner_cont);
				menu.add(0, CONTMENU_DELETE, 0, R.string.delete_partner_cont);
			}
		});
    }
    
    protected void updatePartnerList() {
    	cursor = db.rawQuery("SELECT _id, email, date_added from " + DatabaseHelper.getPartnerTable(), null);
        
        partnerArrayList = new ArrayList<Partner>();
        int idRow = cursor.getColumnIndex("_id");
        int emailRow = cursor.getColumnIndex("email");
        int dateAddedRow = cursor.getColumnIndex("date_added");
        while(cursor.moveToNext())
        {
        	Partner p = new Partner(cursor.getInt(idRow), cursor.getString(emailRow), new java.sql.Date(cursor.getLong(dateAddedRow)));
        	partnerArrayList.add(p);
        }
        partnerArrayList.add(null);
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
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Partner partnerContexted = (Partner) partnerList.getAdapter()
                .getItem(menuInfo.position);
    	 
        /* Switch on the ID of the item, to get what the user selected. */
        switch (item.getItemId()) {
                case CONTMENU_EDIT:
                    editPartner(menuInfo.position);
                    return true;
                case CONTMENU_DELETE:
                    confirmDeletePartner(partnerContexted);
                    return true;
                default:
                    return super.onContextItemSelected(item);
        }
    }

	// =======================================
    // General Functions
    // =======================================
    protected void editPartner(int position) {
    	// TODO Implement
	}
    
    private void confirmDeletePartner(final Partner partnerContexted) {
    	
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
    	db.delete(DatabaseHelper.getPartnerTable(), "email = ?", new String[] {partnerContexted.getEmail()});
    	updatePartnerList();
    	Toast toast = Toast.makeText(this, "Delete question: " + partnerContexted.getEmail(), 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
    }
}