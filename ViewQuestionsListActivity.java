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
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ViewQuestionsListActivity extends Activity {
	
		protected SQLiteDatabase db;
		protected Cursor cursor;
		protected EditText addQuestionText;
		protected ListAdapter adapter;
		protected ListView questionList;

		private static final int CONTMENU_DELETE = 1234;
		private static final int CONTMENU_EDIT = 1235;
		private static final String QUESTION_TABLE = "tb_questions";
		
		private ArrayList<Question> questionArrayList = new ArrayList<Question>();
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        db = (new DatabaseHelper(this)).getWritableDatabase();
        //Log.d("SPENCER", findViewById(R.id.currentQuestionsList).getClass().toString());
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
						0);
			}
		});
    }
    
    protected void updateQuestionList() {
    	cursor = db.rawQuery("SELECT _id, question, type, positive, date_added from " + QUESTION_TABLE, null);
        adapter = new SimpleCursorAdapter(
        		this, 
        		R.layout.edit_question_list_item, cursor, 
        		new String[] {"question", "date_added"}, 
        		new int[] {R.id.question, R.id.added});
        
        questionArrayList = new ArrayList<Question>();
        int idRow = cursor.getColumnIndex("_id");
        int questionRow = cursor.getColumnIndex("question");
        int typeRow = cursor.getColumnIndex("type");
        int positiveRow = cursor.getColumnIndex("positive");
        int dateAddedRow = cursor.getColumnIndex("date_added");
        while(cursor.moveToNext())
        {
        	Question q = new Question(cursor.getInt(idRow), cursor.getString(questionRow), cursor.getString(typeRow), 
        			cursor.getString(positiveRow), new java.sql.Date(cursor.getLong(dateAddedRow)));
        	questionArrayList.add(q);
        }
        questionList.setAdapter(new QuestionListAdapter(this, questionArrayList));
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode == RESULT_OK)
	    {
	    	//String newQuestionText = (String) data.getCharSequenceExtra("new_question_text");
//	    	String newQuestionType = (String) data.getCharSequenceExtra("new_question_type");
//	    	String newQuestionPos = (String) data.getCharSequenceExtra("new_question_pos");
	    	//Question question = new Question(newQuestionText, newQuestionType, newQuestionPos, Question.getNow());
	    	Question question = (Question) data.getSerializableExtra("question");
	    	/*ContentValues cv = new ContentValues();
	    	cv.put("question", newQuestionText);
	    	cv.put("type", newQuestionType);
	    	cv.put("positive", newQuestionPos);
	    	db.insert(QUESTION_TABLE, null, cv);*/
	    	Toast toast;
	    	if(DatabaseHelper.addUpdateQuestion(question, db) == 1)
	    		toast = Toast.makeText(this, R.string.success_add_question, 5000);
	    	else
	    		toast = Toast.makeText(this, R.string.error_add_update_question, 5000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			updateQuestionList();
	    }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	Question questionContexted = (Question) questionList.getAdapter()
                .getItem(menuInfo.position);
    	 
        /* Switch on the ID of the item, to get what the user selected. */
        switch (item.getItemId()) {
                case CONTMENU_EDIT:
                    editQuestion(questionContexted);
                    return true;
                case CONTMENU_DELETE:
                    confirmDeleteQuestion(questionContexted);
                    return true;
                default:
                    return super.onContextItemSelected(item);
        }
    }

	// =======================================
    // General Functions
    // =======================================
    protected void editQuestion(Question question) {
    	Intent i = new Intent(this, AddEditQuestionActivity.class);
    	i.putExtra("question", question);
		startActivityForResult(i, 0);
    	/*Toast toast = Toast.makeText(this, "Edit question: " + question.getQuestion(), 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();*/
	}
    
    private void confirmDeleteQuestion(final Question questionContexted) {
    	
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

    protected void deleteQuestion(Question question) {
    	db.delete(QUESTION_TABLE, "question = ?", new String[] {question.getQuestion()});
    	updateQuestionList();
    	Toast toast = Toast.makeText(this, "Delete question: " + question.getQuestion(), 5000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
    }
}