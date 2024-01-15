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
import com.doubleclue.dcem.knowledgeboard.gui.KbQuestionDialog;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;

@Named("kbLazyQuestionModel")
@SessionScoped
public class LazyQuestionModel extends LazyDataModel<KbQuestionEntity> {

	private static final long serialVersionUID = 1L;

	private Logger logger = LogManager.getLogger(KbQuestionDialog.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	private String searchTerm;
	
	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		try {
			return kbQuestionLogic.getQuestionCountContaining(searchTerm, operatorSessionBean.getDcemUser().getId());
		} catch (Exception e) {
			logger.error("Could not get count of filtered questions. Searchstring was: " + searchTerm, e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			return -1;
		}
	}

	@Override
	public List<KbQuestionEntity> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		try {
			return kbQuestionLogic.getQuestionsPaginatedAndFiltered(first, pageSize, searchTerm, operatorSessionBean.getDcemUser().getId());
		} catch (Exception e) {
			logger.error("Could not filter questions. Searchfilter was: " + searchTerm, e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			return null;
		}
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
}
