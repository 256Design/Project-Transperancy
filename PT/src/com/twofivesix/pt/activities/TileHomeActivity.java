package com.twofivesix.pt.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.CustomTitleBar;
import com.twofivesix.pt.data.TileItem;
import com.twofivesix.pt.listAdapters.TileWithNameAdapter;

public class TileHomeActivity extends Activity {
	
	private static final int REPORTING_REQUEST = 1;
	private static final int QUESTIONS_REQUEST = 2;
	private static final int PARNTERS_REQUEST = 3;
	private ArrayList<TileItem> tileList = new ArrayList<TileItem>();
	private boolean customTitleSupported;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//check if custom title is supported BEFORE setting the content view!
		customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.tile_home_layout);
		
		if(customTitleSupported)
			CustomTitleBar.customTitleBar(this, getText(R.string.settings_sync_title).toString());
		
	    tileList.add(new TileItem(R.drawable.reporting, R.string.reporting, ReportingActivity.class, REPORTING_REQUEST));
	    tileList.add(new TileItem(R.drawable.question, R.string.questions, ViewQuestionsListActivity.class, QUESTIONS_REQUEST));
	    tileList.add(new TileItem(R.drawable.links, R.string.partners, ViewPartnersListActivity.class, PARNTERS_REQUEST));
		
		final GridView gridview = (GridView) findViewById(R.id.gridview);
		final TileWithNameAdapter tileAdapter = new TileWithNameAdapter(this, tileList);
        gridview.setAdapter(tileAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(TileHomeActivity.this, "" + getText(tileList.get(position).getLabelRef()), Toast.LENGTH_SHORT).show();
        		startActivityForResult(
        				new Intent(
        						TileHomeActivity.this, 
        						tileList.get(position).getActivityDestination()
        				), 
        				tileList.get(position).getRequestCode()
        		);
            }
        });
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_options_menu, menu);
        
        Intent settingsIntent = new Intent(TileHomeActivity.this, SettingsActivity.class);
        MenuItem settingsItem = menu.findItem(R.id.settings_option_item);
        settingsItem.setIntent(settingsIntent);
        
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	//AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	
    	switch (item.getItemId()) {
		case R.id.settings_option_item:
			startActivity(item.getIntent());
			return true;
		case R.id.logout_option_item:
			setResult(LoginActivity.LOGOUT_RESULT_CODE);
			finish();
			return true;
		default:
			return false;
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshNotificationsCounts();
	}

	private void refreshNotificationsCounts() {
		// TODO Implment in 0.2.1
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == REPORTING_REQUEST && resultCode == RESULT_OK)
            Toast.makeText(TileHomeActivity.this, "" + getText(R.string.success_report), Toast.LENGTH_LONG).show();
	}
}
