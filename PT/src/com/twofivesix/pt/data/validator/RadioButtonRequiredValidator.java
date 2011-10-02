package com.twofivesix.pt.data.validator;

import android.widget.RadioGroup;

/**
 * A Checkbox required field
 * implementation of the validator.
 * With this validator a checkbox can be tested
 * for the required option.
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public class RadioButtonRequiredValidator extends AbstractValidator {
	
	private RadioGroup _source;
	
	/** CONSTRUCTORS */
	
	public RadioButtonRequiredValidator(RadioGroup source) {
		super(true);
		_source = source;
	}

	public RadioButtonRequiredValidator(RadioGroup source, String requiredMessage) {
		super(true);
		_source = source;
		_requiredMessage = requiredMessage;
	}
	
	@Override
	public ValidationResult validate() {
		ValidationResult _v = super.validate();
		if (_v==null) {
			if (_source.getCheckedRadioButtonId() != -1) {
				_v = new ValidationResult(true, "",_source);
			} else {
				_v = new ValidationResult(false, _requiredMessage,_source);
			}
		}
		return _v;
	}

	@Override
	public Object getSource() {
		return _source;
	}

}
