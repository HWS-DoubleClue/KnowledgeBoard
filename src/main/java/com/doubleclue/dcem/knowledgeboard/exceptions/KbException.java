package com.doubleclue.dcem.knowledgeboard.exceptions;

import java.util.ResourceBundle;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

public class KbException extends DcemException {

	private static final long serialVersionUID = 1L;

	private KbErrorCodes kbErrorCode;

	public String getLocalizedMessageWithMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(KbModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getMessageFromBundle(bundle, KbErrorCodes.class.getSimpleName() + "." + kbErrorCode.name(), super.getMessage());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public String getLocalizedMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(KbModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getStringSafely(bundle, KbErrorCodes.class.getSimpleName() + "." + kbErrorCode.name());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public KbException(KbErrorCodes skillsErrorCode, String message, Throwable cause) {
		super(null, message, cause);
		this.kbErrorCode = skillsErrorCode;
	}

	public KbException(KbErrorCodes skillsErrorCode, String message) {
		super(null, message, null);
		this.kbErrorCode = skillsErrorCode;
	}

	public KbErrorCodes getSkillsErrorCode() {
		return kbErrorCode;
	}

	public void setSkillsErrorCode(KbErrorCodes skillsErrorCode) {
		this.kbErrorCode = skillsErrorCode;
	}

	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(kbErrorCode.name() + " - ");
		if (getMessage() != null) {
			sb.append(getMessage());
		}
		if (getCause() != null) {
			sb.append(" - ");
			sb.append(getCause().toString());
		}
		return sb.toString();
	}

}