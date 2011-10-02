package com.twofivesix.pt.data.validator;

import android.widget.EditText;

/**
 * A Checkbox required field
 * implementation of the validator.
 * With this validator a checkbox can be tested
 * for the required option.
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public class EditTextMatchRequiredValidator extends AbstractValidator {
	
	private EditText _source1;
	private EditText _source2;
	
	/** CONSTRUCTORS */
	
	public EditTextMatchRequiredValidator(EditText source1, EditText source2) {
		super(true);
		_source1 = source1;
		_source2 = source2;
	}

	public EditTextMatchRequiredValidator(EditText source1, EditText source2, String requiredMessage) {
		super(true);
		_source1 = source1;
		_source2 = source2;
		_requiredMessage = requiredMessage;
	}
	
	@Override
	public ValidationResult validate() {
		ValidationResult _v = super.validate();
		if (_v==null) {
			if (_source1.getText().toString().equals(_source2.getText().toString())) {
				_v = new ValidationResult(true, "",_source1);
			} else {
				_v = new ValidationResult(false, _requiredMessage,_source1);
			}
		}
		return _v;
	}

	@Override
	public Object getSource() {
		return _source1;
	}

}
