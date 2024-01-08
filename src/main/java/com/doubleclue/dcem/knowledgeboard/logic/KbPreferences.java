package com.doubleclue.dcem.knowledgeboard.logic;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "kbPreferences")
public class KbPreferences extends ModulePreferences {

	@DcemGui(style = "width: 30em", help = "Link for manual. Link must start with 'http'. In case text ends with '_', the language suffix 'en.pdf or 'de.pdf' will be added respectivly.")
	private String manualsLink = "https://doubleclue.com/files/DC_Knowledgeboard_Manual_";

	@DcemGui(help = "Disable all E-mail notifications for the Knowledgeboard.", separator = "General")
	private boolean turnOffEmailNotification = false;

	public String getManualsLink() {
		return manualsLink;
	}

	public void setManualsLink(String manualsLink) {
		this.manualsLink = manualsLink;
	}

	public boolean isTurnOffEmailNotification() {
		return turnOffEmailNotification;
	}

	public void setTurnOffEmailNotification(boolean turnOffEmailNotification) {
		this.turnOffEmailNotification = turnOffEmailNotification;
	}

}