package com.twofivesix.pt.data.validator;

import android.widget.CheckBox;

/**
 * A Checkbox required field
 * implementation of the validator.
 * With this validator a checkbox can be tested
 * for the required option.
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public class CheckBoxRequiredValidator extends AbstractValidator {
	
	private CheckBox _source;
	
	/** CONSTRUCTORS */
	
	public CheckBoxRequiredValidator(CheckBox source) {
		super(true);
		_source = source;
	}

	public CheckBoxRequiredValidator(CheckBox source, String requiredMessage) {
		super(true);
		_source = source;
		_requiredMessage = requiredMessage;
	}
	
	@Override
	public ValidationResult validate() {
		ValidationResult _v = super.validate();
		if (_v==null) {
			if (_source.isChecked()) {
				_v = new ValidationResult(true, "");
			} else {
				_v = new ValidationResult(false, _requiredMessage);
			}
		}
		return _v;
	}

	@Override
	public Object getSource() {
		return _source;
	}

}
