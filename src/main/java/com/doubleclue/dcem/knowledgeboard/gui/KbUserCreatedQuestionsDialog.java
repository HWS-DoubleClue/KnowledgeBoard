package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.subjects.KbUserSubject;

@Named("kbUserCreatedQuestionsDialog")
@SessionScoped
public class KbUserCreatedQuestionsDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbUserCreatedQuestionsDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbUserSubject kbUserSubject;

	@Inject
	KbUserView kbUserView;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbUserEntity kbUserEntity;
	private List<KbQuestionEntity> createdQuestions;
	private List<KbQuestionEntity> selectedQuestions;
	private SelectItem[] questionStatusSelection;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbUserEntity = (KbUserEntity) this.getActionObject();
		createdQuestions = kbQuestionLogic.getQuestionsCreatedBy(kbUserEntity.getId());
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "60em";
	}

	public void leavingDialog() {
		createdQuestions = null;
		selectedQuestions = null;
		questionStatusSelection = null;
	}

	public List<KbQuestionEntity> getSelectedQuestions() {
		return selectedQuestions;
	}

	public void setSelectedQuestions(List<KbQuestionEntity> selectedQuestions) {
		this.selectedQuestions = selectedQuestions;
	}

	public SelectItem[] getQuestionStatusSelection() {
		if (questionStatusSelection == null) {
			KbQuestionStatus[] questionStatus = KbQuestionStatus.values();
			questionStatusSelection = new SelectItem[questionStatus.length + 1];
			questionStatusSelection[0] = new SelectItem(null, "");
			int i = 1;
			for (KbQuestionStatus status : questionStatus) {
				questionStatusSelection[i] = new SelectItem(status, status.getLocaleText());
				i++;
			}
		}
		return questionStatusSelection;
	}

	public List<KbQuestionEntity> getCreatedQuestions() {
		return createdQuestions;
	}

	public KbUserEntity getKbUserEntity() {
		return kbUserEntity;
	}
}