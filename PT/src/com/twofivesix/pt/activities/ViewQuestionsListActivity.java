package com.twofivesix.pt.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;
import com.twofivesix.pt.interfaces.SyncCaller;
import com.twofivesix.pt.listAdapters.QuestionListAdapter;
import com.twofivesix.pt.tasks.QuestionSyncTask;
import com.twofivesix.pt.tasks.SyncTask;
import com.twofixesix.pt.helpers.DatabaseHelper;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class ViewQuestionsListActivity extends Activity implements SyncCaller {
	
		protected SQLiteDatabase db;
		protected Cursor cursor;
		protected EditText addQuestionText;
		protected ListAdapter adapter;
		protected ListView questionList;

		private static final int CONTMENU_DELETE = 1234;
		private static final int CONTMENU_EDIT = 1235;
		protected static final int ADD_QUESTION_REQUEST_CODE = 3016;
		private static final int EDIT_REQUEST_CODE = 3017;
		
		private ArrayList<Question> questionArrayList = new ArrayList<Question>();
		private SyncTask questionsSyncTask;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_questions_activity);
        
        db = (new DatabaseHelper(this)).getWritableDatabase();
        questionList = (ListView) findViewById(R.id.currentQuestionsList);
        updateQuestionList();
        registerForContextMenu(questionList);
        questionList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(R.string.question_list_context_menu_title);
				menu.add(0, CONTMENU_EDIT, 0, R.string.edit_question_cont);
				menu.add(0, CONTMENU_DELETE, 0, R.string.delete_question_cont);
			}
		});
        
        Button addQuestionBtn = (Button) findViewById(R.id.addNewQuestionBtn);
        addQuestionBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startActivityForResult(
						new Intent(ViewQuestionsListActivity.this, AddEditQuestionActivity.class), 
						ADD_QUESTION_REQUEST_CODE);
			}
		});
    }
    
    protected void updateQuestionList() {
    	questionArrayList = DatabaseHelper.buildQuestionsList(db);
        questionList.setAdapter(new QuestionListAdapter(this, questionArrayList));
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode == RESULT_OK)
	    {
	    	Question question = (Question) data.getSerializableExtra("question");
	    	String oldQuestion = data.getStringExtra("oldQuestion");
	    	Toast toast = null;
	    	if(requestCode == ADD_QUESTION_REQUEST_CODE)
	    	{
	    		if(DatabaseHelper.addQuestion(question, db) && 
	    				!(NetworkConnectivityHelper.isConnected(this) && 
	    						!Question.submitAddQuestion(this, question, db)
	    				)
	    			)
	    			toast = Toast.makeText(this, R.string.success_add_question, 5000);
	    		else
	    			toast = Toast.makeText(this, R.string.error_add_update_question, 5000);	    					    				
	    	}
	    	else if(requestCode == EDIT_REQUEST_CODE)
	    	{
	    		if(DatabaseHelper.updateQuestion(question, oldQuestion, db) && 
	    				!(NetworkConnectivityHelper.isConnected(this) && 
	    						!Question.submitUpdateQuestion(this, question, db)
	    				)
	    			)
	    			toast = Toast.makeText(this, R.string.success_edit_question, 5000);
	    		else
	    			toast = Toast.makeText(this, R.string.error_add_update_question, 5000);	    
	    	}
	    	if(toast != null)
	    	{
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				updateQuestionList();
	    	}
	    }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.questions_options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Question questionContexted = (menuInfo != null) 
    			?(Question) questionList.getAdapter().getItem(menuInfo.position)
    			:null;
    	
    	switch (item.getItemId()) {
		case R.id.question_sync_option_item:
			syncQuestions();
			return true;
		case CONTMENU_EDIT:
            editQuestion(questionContexted);
            return true;
        case CONTMENU_DELETE:
            confirmDeleteQuestion(questionContexted);
            return true;
		default:
			return false;
		}
    }

	private void syncQuestions() {
		if(NetworkConnectivityHelper.isConnected(ViewQuestionsListActivity.this))
		{
			//Partner.syncPartners(settings.getUserID(), db);
			ProgressDialog progressDialog = new ProgressDialog(ViewQuestionsListActivity.this);
			progressDialog.setMessage(getString(R.string.syncing_questions));
			progressDialog.setCancelable(false);
			
			questionsSyncTask = new QuestionSyncTask(ViewQuestionsListActivity.this, progressDialog, db);
			Integer userID = (new SharedPreferencesHelper(this)).getUserID();
			questionsSyncTask.execute(userID);
		}
	}

	// =======================================
    // General Functions
    // =======================================
    protected void editQuestion(Question question) {
    	Intent i = new Intent(this, AddEditQuestionActivity.class);
    	i.putExtra("question", question);
		startActivityForResult(i, EDIT_REQUEST_CODE);
    	/*Toast toast = Toast.makeText(this, "Edit question: " + question.getQuestion(), 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();*/
	}
    
    private void confirmDeleteQuestion(final Question questionContexted) {
    	
    	if(NetworkConnectivityHelper.isConnected(this))
    	{
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	        switch (which){
        	        case DialogInterface.BUTTON_POSITIVE:
        	            deleteQuestion(questionContexted);
        	            break;

        	        case DialogInterface.BUTTON_NEGATIVE:
        	            break;
        	        }
        	    }
        	};
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(R.string.delete_question);
        	builder.setMessage(R.string.are_you_sure);
        	builder.setPositiveButton(R.string.yes, dialogClickListener);
        	builder.setNegativeButton(R.string.no, dialogClickListener);
        	builder.show();
    	}
    	else
    	{
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int which) {
        	    	dialog.cancel();
        	    }
        	};
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(R.string.error_no_network_connection);
        	builder.setPositiveButton(R.string.okay, dialogClickListener);
        	builder.show();
    	}
    }

    protected void deleteQuestion(Question question) {
    	Toast toast;
    	int userID = new SharedPreferencesHelper(this).getUserID();
    	//Log.d("SPENCER", "deleteQuestion()");
		if(!(Question.submitDetelteQuestion( userID, question) &&
				db.delete(DatabaseHelper.getQuestionTable(), "question = ?", new String[] {question.getQuestion()}) == 1))
		{
			syncQuestions();
			toast = Toast.makeText(this, R.string.error_delete_question, 5000);
		}
		else
			toast = Toast.makeText(this, R.string.deleted_question_success, 5000);
			
		updateQuestionList();
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
    }

	public void syncResults(Boolean result) {
		if(result)
		{
			Toast.makeText(ViewQuestionsListActivity.this, "" + getText(R.string.success_questions_sync), Toast.LENGTH_LONG).show();
			updateQuestionList();
		}
		else
			displaySyncError();
	}

	private void displaySyncError() 
	{
		if(questionsSyncTask != null)
			questionsSyncTask.showSyncError(this);
	}
}