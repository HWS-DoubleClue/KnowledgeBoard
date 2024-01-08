package com.doubleclue.dcem.knowledgeboard.entities.enums;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

public enum KbQuestionStatus {
	Open, Information, Answered, Closed;

	public String getLocaleText() {
		return JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "questionStatus." + this.name());
	}
}
