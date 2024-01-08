package com.doubleclue.dcem.knowledgeboard.model;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.gui.KbDashboardView;
import com.doubleclue.dcem.knowledgeboard.gui.KbQuestionDialog;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;

@SuppressWarnings("serial")
@Named("kbLazyQuestionModel")
@SessionScoped
public class LazyQuestionModel extends LazyDataModel<KbQuestionEntity> {

	private Logger logger = LogManager.getLogger(KbQuestionDialog.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbDashboardView kbDashboardView;

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		try {
			return kbQuestionLogic.getQuestionCountContaining(kbDashboardView.getFilterText(), operatorSessionBean.getDcemUser().getId());
		} catch (Exception e) {
			logger.error("Could not get count of filtered questions. Searchstring was: " + kbDashboardView.getFilterText(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			return -1;
		}
	}

	@Override
	public List<KbQuestionEntity> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		try {
			return kbQuestionLogic.getQuestionsPaginatedAndFiltered(first, pageSize, kbDashboardView.getFilterText(),
					operatorSessionBean.getDcemUser().getId());
		} catch (Exception e) {
			logger.error("Could not filter questions. Searchfilter was: " + kbDashboardView.getFilterText(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			return null;
		}
	}
}
