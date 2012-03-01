package com.twofivesix.pt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;

public class AddEditQuestionActivity extends Activity {
	
	protected EditText customPromptText;
	protected TextView questionsOptionLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_edit_question_layout);
		
		final LinearLayout layout = (LinearLayout) findViewById(R.id.addEditQuestionLayout);
		final EditText questionText = (EditText) findViewById(R.id.addQuestionText);
		final Spinner questionTypeSpinner = (Spinner) findViewById(R.id.question_type_spinner);
		
		questionsOptionLabel = (TextView) findViewById(R.id.questionOptionLabel);
		questionsOptionLabel.setText(R.string.positive_response);
		final Spinner posResponseSpinner = (Spinner) findViewById(R.id.pos_response_spinner);
		customPromptText = (EditText) findViewById(R.id.questionOptionText);
		layout.removeView(customPromptText);
		
		Button addQuestionButton = (Button) findViewById(R.id.addQuestionBtn);
		
		ArrayAdapter<CharSequence> qTypeAdapter = ArrayAdapter.createFromResource(
				this, R.array.question_type_entry_array, android.R.layout.simple_spinner_item);
		qTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		questionTypeSpinner.setAdapter(qTypeAdapter);
		questionTypeSpinner.setSelection(0, false);
		ArrayAdapter<CharSequence> posResponseAdapter = ArrayAdapter.createFromResource(this, 
				R.array.pos_response_array, 
				android.R.layout.simple_spinner_item);
		questionTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String newValue = getResources().getStringArray(
							R.array.question_type_value_array
						)[position];
				Log.d("SPENCER", newValue);
				if(newValue.equals("YES_NO"))
				{
					if(layout.indexOfChild(posResponseSpinner) == -1)
					{
						int viewPosition = layout.indexOfChild(questionsOptionLabel)+1;
						if(layout.indexOfChild(customPromptText) != -1)
							layout.removeView(customPromptText);
						layout.addView(posResponseSpinner, viewPosition);
					}
					questionsOptionLabel.setText(R.string.positive_response);
				}
				else if(newValue.equals("SHORT_ANSWER"))
				{
					if(layout.indexOfChild(customPromptText) == -1)
					{
//						int viewPosition = layout.indexOfChild(posResponseSpinner);
						if(layout.indexOfChild(posResponseSpinner) != -1)
							layout.removeView(posResponseSpinner);
						//layout.addView(customPromptText, viewPosition);
					}
					questionsOptionLabel.setText("");
				}
			}

			public void onNothingSelected(AdapterView<?> parent) 
			{
				if(layout.indexOfChild(posResponseSpinner) != -1)
					layout.removeView(posResponseSpinner);
				questionsOptionLabel.setText("");
			}
		});
		
		posResponseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		posResponseSpinner.setAdapter(posResponseAdapter);
		
		// Check for pushed question for editing
		final Question pushedQuestion = (Question) getIntent().getSerializableExtra("question");
		if(pushedQuestion != null)
		{
			questionText.setText(pushedQuestion.getQuestion());
			
			String[] questionType = getResources().getStringArray(
					R.array.question_type_value_array
				);
			int i = 0;
			for(i = 0; i < questionType.length; i++)
			{
				if(questionType[i].equals(pushedQuestion.getType()))
					break;
			}
			questionTypeSpinner.setSelection(i);
			posResponseSpinner.setSelection(posResponseAdapter.getPosition(pushedQuestion.getPositive()));
			addQuestionButton.setText(R.string.save_question_btn_text);
			((TextView) findViewById(R.id.addEditQuestionHeader)).setText(R.string.update_question_label);

			if(!pushedQuestion.getType().equals(Question.TYPE_YES_NO))
			{
				layout.removeView(posResponseSpinner);
			}
		}
		
		addQuestionButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String oldQuestionString = "";
				Question returnQuestion;
				String question = questionText.getText().toString();
				int typePos = questionTypeSpinner.getSelectedItemPosition();
				String typeValue = getResources().getStringArray(
						R.array.question_type_value_array
					)[typePos];
				
				// Complete 1-1-12 - FIX ME deal with the pos repsonse on short answer q's
				String posResponse = "";
				if(typeValue.equals(Question.TYPE_YES_NO))
				{
					posResponse = posResponseSpinner.getSelectedItem().toString();
				}
				else if(typeValue.equals(Question.TYPE_SHORT_ANSWER))
				{
					
				}
				else
				{
					Log.e("SPENCER", "Unidentified question type: " + typeValue);
					posResponse = "ERROR";
				}
				
				if(pushedQuestion != null)
				{
					oldQuestionString = pushedQuestion.getQuestion();
					pushedQuestion.setQuestion(question);
					pushedQuestion.setType(typeValue);
					pushedQuestion.setPositive(posResponse);
					returnQuestion = pushedQuestion;
				}
				else
				{
					returnQuestion = new Question(question, typeValue, posResponse, Question.getNow());
				}
				
				Intent intent = new Intent();
				intent.putExtra("question", returnQuestion);
				intent.putExtra("oldQuestion", oldQuestionString);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		// Add Listener to layout to hide soft keyboard
		layout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(questionText.getWindowToken(), 0);
			}
		});
	}
}
