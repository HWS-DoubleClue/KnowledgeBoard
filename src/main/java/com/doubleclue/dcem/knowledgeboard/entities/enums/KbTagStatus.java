package com.doubleclue.dcem.knowledgeboard.entities.enums;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

public enum KbTagStatus { // NOT USED YET
	Approved, Pending;

	public String getLocaleText() {
		return JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "tagStatus." + this.name());
	}
}
