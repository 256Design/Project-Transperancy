package com.twofivesix.pt.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;

public class AddEditQuestionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_edit_question_layout);
		
		final EditText questionText = (EditText) findViewById(R.id.addQuestionText);
		final Spinner questionTypeSpinner = (Spinner) findViewById(R.id.question_type_spinner);
		final Spinner posResponseSpinner = (Spinner) findViewById(R.id.pos_response_spinner);
		Button b = (Button) findViewById(R.id.addQuestionBtn);
		
		ArrayAdapter<CharSequence> qTypeAdapter = ArrayAdapter.createFromResource(
				this, R.array.question_type_array, android.R.layout.simple_spinner_item);
		qTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		questionTypeSpinner.setAdapter(qTypeAdapter);
		questionTypeSpinner.setSelection(0, false);
		ArrayAdapter<CharSequence> posResponseAdapter = ArrayAdapter.createFromResource(this, 
				R.array.pos_response_array, 
				android.R.layout.simple_spinner_item);
		posResponseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		posResponseSpinner.setAdapter(posResponseAdapter);
		
		// Check for pushed question for editing
		final Question pushedQuestion = (Question) getIntent().getSerializableExtra("question");
		if(pushedQuestion != null)
		{
			questionText.setText(pushedQuestion.getQuestion());
			questionTypeSpinner.setSelection(qTypeAdapter.getPosition(pushedQuestion.getType()));
			posResponseSpinner.setSelection(posResponseAdapter.getPosition(pushedQuestion.getPositive()));
			b.setText(R.string.save_question_btn_text);
			((TextView) findViewById(R.id.addEditQuestionHeader)).setText(R.string.update_question_label);
		}
		
		b.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String oldQuestion = "";
				Question returnQuestion;
				String question = questionText.getText().toString();
				String type = questionTypeSpinner.getSelectedItem().toString();
				String posResponse = posResponseSpinner.getSelectedItem().toString();
				if(pushedQuestion != null)
				{
					oldQuestion = pushedQuestion.getQuestion();
					pushedQuestion.setQuestion(question);
					pushedQuestion.setType(type);
					pushedQuestion.setPositive(posResponse);
					returnQuestion = pushedQuestion;
				}
				else
				{
					returnQuestion = new Question(question, type, posResponse, Question.getNow());
				}
				
				Intent intent = new Intent();
				intent.putExtra("question", returnQuestion);
				intent.putExtra("oldQuestion", oldQuestion);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
