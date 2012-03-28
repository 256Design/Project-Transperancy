package com.twofivesix.pt.data.validator;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.twofivesix.pt.R;

/**
 * Generic abstract validation class for the 
 * validation of Android input fields
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public abstract class AbstractValidator implements Validator {
		
	private boolean _required = false;
	private boolean _enabled = true;
	
	protected String _faultMessage = "Validation failure";
	protected String _requiredMessage = "The field is required";
	
	/** CONSTRUCTORS */
	
	public AbstractValidator() {}
	
	public AbstractValidator(boolean required) {
		_required = required;
	}

	public AbstractValidator(boolean required, boolean enabled) {
		_required = required;
		_enabled = enabled;
	}

	public ValidationResult validate() {
		ValidationResult _vr = null;
		if (_enabled) {
			if (_required && getSource() == null) {
				_vr = new ValidationResult(false, _requiredMessage);
			}
		} else {
			_vr = new ValidationResult(true, "");
		}
		return _vr;
	}
	
	/**
	 * THis is a convenience method that enables you
	 * to quickly validate all validators.
	 * 
	 * @param validators an array of validators
	 * @return an array of validation failure results.
	 */
	public static List<ValidationResult> validateAll(List<Validator> validators) {
		List<ValidationResult> _result = new ArrayList<ValidationResult>();
		ValidationResult _vr = null;
		for (Validator v : validators) {
			_vr = v.validate();
			if (!_vr.isValid()) {
				_result.add(_vr);
			}
		}
		return _result;
	}
	
	public abstract Object getSource();
		
	public void setFaultMessage(String message) {
		_faultMessage = message;
	}
	
	public void setRequiredMessage(String message) {
		_requiredMessage = message;
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	public void setRequired(boolean required) {
		_required = required;
	}
	
	public boolean isEnabled() {
		return _enabled;
	}

	public boolean isRequired() {
		return _required;
	}
	
	public static boolean validateList(List<Validator> validators, 
			Context context)
	{
		List<ValidationResult> _validationResults = 
				AbstractValidator.validateAll(validators);
		if (_validationResults.size()==0) {
        	return true;
        }
		ValidationResult result = _validationResults.get(0);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(result.getMessage());
		builder.setPositiveButton(
			R.string.okay, 
			new DialogInterface.OnClickListener()
				{
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
		if(result.getSource() != null)
			result.getSource().requestFocus();
		return false;
	}
}
