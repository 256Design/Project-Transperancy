package com.twofivesix.pt.listAdapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;

public class ReportingListItem extends LinearLayout {

	private Question question;
	private TextView questionTitle;
	private Spinner responseSpinner;
	private EditText responseEditText;

	public ReportingListItem(Context context, Question question) {
		super(context);
		/*LayoutInflater inflater = (LayoutInflater)context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null){   
            layout = inflater.inflate(R.layout.reqort_question_list_item, this);
            //addView(layout);
        }*/
		
		this.setOrientation(VERTICAL);
		//android.widget.AbsListView.LayoutParams
		android.widget.AbsListView.LayoutParams params = 
				new android.widget.AbsListView.LayoutParams(
						LayoutParams.FILL_PARENT, 
						LayoutParams.FILL_PARENT);
		int padding = (int) (context.getResources().getDisplayMetrics().density * 8); 
		this.setPadding(padding, padding, padding, padding);
		this.setLayoutParams(params);
		
		Log.d("SPENCER", "this.setLayoutParams()");
        
		questionTitle = new TextView(context);
		ViewGroup.LayoutParams titleParams = new ViewGroup.LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		questionTitle.setLayoutParams(titleParams);
		questionTitle.setTextAppearance(
				context, 
				android.R.attr.textAppearanceMedium);
		addView(questionTitle);
		
		setQuestion(question);
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @param question the question to set
	 */
	public void setQuestion(Question question) {
		if(this.question != question)
		{
			this.question = question;
			questionTitle.setText(question.getQuestion());
			
			if(question.getType().equals(Question.TYPE_YES_NO))
			{
				if(getChildAt(getChildCount()-1) == responseEditText)
				{
					removeView(responseEditText);
					addSpinner();
				}
				else if(getChildAt(getChildCount()-1) != responseSpinner)
					addSpinner();
				
				ArrayAdapter<CharSequence> qTypeAdapter = 
						ArrayAdapter.createFromResource(
								getContext(), 
								R.array.pos_response_array, 
								android.R.layout.simple_spinner_item);
				qTypeAdapter.setDropDownViewResource(
						android.R.layout.simple_spinner_dropdown_item);
				responseSpinner.setAdapter(qTypeAdapter);
				String selectionString = question.getResponse();
				if(selectionString == null)
					selectionString = question.getPositive();
				int selectionIndex = qTypeAdapter.getPosition(selectionString);
				responseSpinner.setSelection(selectionIndex);
				responseSpinner.setOnItemSelectedListener(
					new OnItemSelectedListener() 
					{
						public void onItemSelected(AdapterView<?> parent, View view,
								int position, long id) 
						{
							String newValue = getResources().getStringArray(
									R.array.pos_response_array
								)[position];
							Log.d("SPENCER", "Store " + newValue);
							storeResponse(newValue);
						}
	
						public void onNothingSelected(AdapterView<?> arg0) {}
					});
			}
			else if(question.getType().equals(Question.TYPE_SHORT_ANSWER))
			{
				if(getChildAt(getChildCount()-1) == responseSpinner)
				{
					removeView(responseSpinner);
					addEditText();
				}
				else if(getChildAt(getChildCount()-1) != responseEditText)
					addEditText();
				
				String responseString = question.getResponse();
				responseEditText.setText(responseString);
				responseEditText.addTextChangedListener(new TextWatcher() 
				{
					public void onTextChanged(CharSequence s, int start, 
							int before, int count) {}
					
					public void beforeTextChanged(CharSequence s, int start, 
							int count, int after) {}
					
					public void afterTextChanged(Editable s) 
					{
						storeEditTextResponse();
					}
				});
			}
		}
	}

	private void storeResponse(String newValue)
	{
		question.setResponse(newValue);
	}

	private void storeEditTextResponse() {
		storeResponse(responseEditText.getText().toString());
	}

	private void addEditText() {
		if(responseEditText == null)
		{
			responseEditText = new EditText(getContext());
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, 
					LayoutParams.WRAP_CONTENT);
			responseEditText.setLayoutParams(params);
			//textShortMessage|textAutoCorrect|textCapSentences|textMultiLine
			responseEditText.setInputType(
					InputType.TYPE_CLASS_TEXT|
					InputType.TYPE_TEXT_FLAG_AUTO_CORRECT|
					InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|
					InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		}
		addView(responseEditText);
	}

	private void addSpinner() {
		if(responseSpinner == null)
		{
			responseSpinner = new Spinner(getContext());
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LayoutParams.WRAP_CONTENT, 
					LayoutParams.WRAP_CONTENT);
			responseSpinner.setLayoutParams(params);
			responseSpinner.setPrompt(getContext().getText(R.string.pick_one));
		}
		addView(responseSpinner);
	}

	public void editTextRequestFocus() {
		if(responseEditText != null && 
				getChildAt(getChildCount()-1) == responseEditText)
			responseEditText.requestFocusFromTouch();
	}
}