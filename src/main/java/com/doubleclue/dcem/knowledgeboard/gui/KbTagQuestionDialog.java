package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbTagQuestionDialog")
@SessionScoped
public class KbTagQuestionDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbTagQuestionDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbTagView kbTagView;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbTagEntity kbTagEntity;
	private String questionInfo;
	private List<KbQuestionEntity> selectedQuestions;
	private List<KbQuestionEntity> questionsContainingTag;
	private SelectItem[] questionStatusSelection;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionAddTag() {
		try {
			// Autocomplete only accepts a List of Strings (without a converter)
			// Workaround: Recover the Id of the String (ID: xxx)
			String[] parts = questionInfo.split("[ )]");
			int questionId = Integer.parseInt(parts[1]);
			KbQuestionEntity kbQuestionEntity = kbQuestionLogic.getQuestionById(questionId);
			if (kbQuestionEntity == null) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.questionDialog.invalid.questionName"), "tagQuestionDialog:addTagDialogMsg");
				return;
			}
			if (kbQuestionEntity.getTags().contains(kbTagEntity)) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.questionDialog.invalid.duplicateTag"), "tagQuestionDialog:addTagDialogMsg");
				return;
			}
			kbQuestionEntity.getTags().add(kbTagEntity);
			kbQuestionEntity = kbQuestionLogic.updateQuestion(kbQuestionEntity);
			questionsContainingTag.add(kbQuestionEntity);
			JsfUtils.addInfoMessageToComponentId(String.format(JsfUtils.getStringSafely(resourceBundle, "tag.questionDialog.success.addTag"), questionInfo),
					"tagQuestionDialog:addTagDialogMsg");
		} catch (Exception e) {
			logger.error("Tag: " + kbTagEntity.getName() + "in category: " + kbTagEntity.getCategory().getName() + " could not be added to question: "
					+ questionInfo, e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "tagFollowerForm:addFollowerDialogMsg");
		} finally {
			questionInfo = null;
		}
	}

	public void actionRemoveTag() {
		if (selectedQuestions == null || selectedQuestions.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "tag.questionDialog.invalid.emptySelection");
			return;
		}
		try {
			for (int i = 0; i < selectedQuestions.size(); i++) {
				KbQuestionEntity kbQuestionEntity = kbQuestionLogic.getQuestionById(selectedQuestions.get(i).getId());
				kbQuestionEntity.getTags().remove(kbTagEntity);
				selectedQuestions.set(i, kbQuestionEntity);
			}
			kbQuestionLogic.updateQuestions(selectedQuestions);
			questionsContainingTag.removeAll(selectedQuestions);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "tag.questionDialog.success.removeTag");
		} catch (Exception e) {
			logger.error("Could not remove tag: " + kbTagEntity.getName() + " from selected questions.", e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbTagEntity = (KbTagEntity) this.getActionObject();
		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(), kbTagEntity.getCategory().getId());
		if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
					"Operating user does not have management rights for tags of category: " + kbTagEntity.getCategory().getName());
		}
		questionsContainingTag = kbQuestionLogic.getAllQuestionsContainingOneOfTags(Arrays.asList(kbTagEntity));
	}

	public List<String> actionCompleteQuestion(String title) {
		List<String> filteredQuestionsAsString = new ArrayList<String>();
		try {
			List<KbQuestionEntity> filteredQuestions = kbQuestionLogic.getQuestionsNotContainingTagAndFilterTitleBy(title, kbTagEntity, 30);
			for (KbQuestionEntity question : filteredQuestions) {
				filteredQuestionsAsString.add("(ID: " + question.getId() + ") " + question.getTitle());
			}
		} catch (Exception e) {
			logger.error("Could not create filtered question list by search string: " + title + " of category: " + kbTagEntity.getCategory().getName(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.questionDialog.error.questionComplete"), "tagQuestionDialog:addTagDialogMsg");
		}
		return filteredQuestionsAsString;
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "70em";
	}

	public void leavingDialog() {
		kbTagEntity = null;
		questionInfo = null;
		selectedQuestions = null;
		questionsContainingTag = null;
		questionStatusSelection = null;
	}

	public KbTagEntity getKbTagEntity() {
		return kbTagEntity;
	}

	public void setKbTagEntity(KbTagEntity kbTagEntity) {
		this.kbTagEntity = kbTagEntity;
	}

	public List<KbQuestionEntity> getSelectedQuestions() {
		return selectedQuestions;
	}

	public List<KbQuestionEntity> getQuestionsContainingTag() {
		return questionsContainingTag;
	}

	public void setSelectedQuestions(List<KbQuestionEntity> selectedQuestions) {
		this.selectedQuestions = selectedQuestions;
	}

	public void setQuestionsContainingTag(List<KbQuestionEntity> questionsContainingTag) {
		this.questionsContainingTag = questionsContainingTag;
	}

	public String getQuestionInfo() {
		return questionInfo;
	}

	public void setQuestionInfo(String questionInfo) {
		this.questionInfo = questionInfo;
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
}