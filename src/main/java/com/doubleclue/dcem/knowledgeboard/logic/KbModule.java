package com.doubleclue.dcem.knowledgeboard.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@ApplicationScoped
@Named("knowledgeBoardModule")
public class KbModule extends DcemModule {

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private static final long serialVersionUID = 1L;
	public final static String MODULE_ID = "kb";
	public final static String MODULE_NAME = "Knowledge Board";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.knowledgeboard.resources.Messages";
	public final static int MODULE_RANK = 10000;
	
	@Override
	public void init() throws DcemException {
	}

	public void start() throws DcemException {
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return MODULE_RANK;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new KbPreferences();
	}

	@Override
	public KbPreferences getModulePreferences() {
		return ((KbPreferences) super.getModulePreferences());
	}

	@Override
	@DcemTransactional
	public void deleteUserFromDb(DcemUser dcemUser) {
			kbUserLogic.deleteUserFromKb(dcemUser);
	}
}