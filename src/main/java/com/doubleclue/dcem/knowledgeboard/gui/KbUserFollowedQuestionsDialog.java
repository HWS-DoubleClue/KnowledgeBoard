package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.Collections;
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
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;

@Named("kbUserFollowedQuestionsDialog")
@SessionScoped
public class KbUserFollowedQuestionsDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbUserFollowedQuestionsDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbUserView kbUserView;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private SelectItem[] questionStatusSelection;
	private KbUserEntity kbUserEntity;
	private List<KbQuestionEntity> followedQuestions;
	private List<KbQuestionEntity> selectedQuestions;
	private List<KbUserCategoryEntity> userCategories;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionRemoveQuestion() {
		if (selectedQuestions == null || selectedQuestions.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "user.followedQuestionsDialog.invalid.emptySelection");
			return;
		}
		try {
			List<KbUserCategoryEntity> changedUserCategories = new ArrayList<KbUserCategoryEntity>();
			for (int i = 0; i < userCategories.size(); i++) {
				if (Collections.disjoint(selectedQuestions, userCategories.get(i).getFollowedQuestions()) == false) {
					KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategory(userCategories.get(i).getKbUser().getId(),
							userCategories.get(i).getCategory().getId());
					kbUserCategoryEntity.getFollowedQuestions().removeAll(selectedQuestions);
					userCategories.set(i, kbUserCategoryEntity);
					changedUserCategories.add(kbUserCategoryEntity);
				}
			}
			kbUserLogic.updateUserCategories(changedUserCategories, getAutoViewAction().getDcemAction());
			followedQuestions.removeAll(selectedQuestions);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "user.followedQuestionsDialog.success.removeQuestion");
		} catch (Exception e) {
			logger.error("Could not remove selected questions from followed list of user: " + kbUserEntity.getDcemUser().getLoginId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbUserEntity = (KbUserEntity) this.getActionObject();
		userCategories = kbUserLogic.getUserCategoriesByUserIdWithOptionalAttribute(kbUserEntity.getId(), KbUserCategoryEntity.GRAPH_USER_FOLLOWED_QUESTIONS);
		followedQuestions = new ArrayList<>();
		for (KbUserCategoryEntity userCategory : userCategories) {
			followedQuestions.addAll(userCategory.getFollowedQuestions());
		}
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "80em";
	}

	public void leavingDialog() {
		questionStatusSelection = null;
		kbUserEntity = null;
		followedQuestions = null;
		selectedQuestions = null;
		userCategories = null;
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

	public KbUserEntity getKbUserEntity() {
		return kbUserEntity;
	}

	public void setKbUserEntity(KbUserEntity kbUserEntity) {
		this.kbUserEntity = kbUserEntity;
	}

	public List<KbQuestionEntity> getFollowedQuestions() {
		return followedQuestions;
	}

	public void setFollowedQuestions(List<KbQuestionEntity> followedQuestions) {
		this.followedQuestions = followedQuestions;
	}
}