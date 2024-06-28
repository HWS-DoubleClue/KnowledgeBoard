package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
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
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbTagLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbQuestionTagDialog")
@SessionScoped
public class KbQuestionTagDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbQuestionTagDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbQuestionView kbQuestionView;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	KbTagLogic kbTagLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbQuestionEntity kbQuestionEntity;
	private String tagName;
	private List<KbTagEntity> selectedTags;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionAddTag() {
		try {
			KbTagEntity kbTagEntity = kbTagLogic.getTagByNameAndCategoryId(tagName, kbQuestionEntity.getCategory().getId());
			if (kbTagEntity == null) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.tagDialog.invalid.tagName"), "questionTagForm:addTagDialogMsg");
				return;
			}
			if (kbQuestionEntity.getTags().contains(kbTagEntity)) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.tagDialog.invalid.duplicateTag"), "questionTagForm:addTagDialogMsg");
				return;
			}			
			kbQuestionEntity = kbQuestionLogic.getQuestionWithLazyAttribute(kbQuestionEntity.getId(),KbQuestionEntity.GRAPH_QUESTION_TAGS);
			kbQuestionEntity.getTags().add(kbTagEntity);
			kbQuestionLogic.detachEntity(kbQuestionEntity);
			kbQuestionLogic.addOrUpdateQuestion(kbQuestionEntity, this.getAutoViewAction().getDcemAction());
			JsfUtils.addInfoMessageToComponentId(String.format(JsfUtils.getStringSafely(resourceBundle, "question.tagDialog.success.addTag"), tagName),
					"questionTagForm:addTagDialogMsg");
		} catch (Exception e) {
			logger.error("Tag: " + tagName + " could not be added to question: " + kbQuestionEntity.getTitle(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "questionTagForm:addTagDialogMsg");
		} finally {
			tagName = null;
		}
	}

	public void actionRemoveTag() {
		if (selectedTags == null || selectedTags.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "question.tagDialog.invalid.emptySelection");
			return;
		}
		try {
			kbQuestionEntity = kbQuestionLogic.getQuestionWithLazyAttribute(kbQuestionEntity.getId(),KbQuestionEntity.GRAPH_QUESTION_TAGS);
			kbQuestionEntity.getTags().removeAll(selectedTags);
			kbQuestionLogic.detachEntity(kbQuestionEntity);
			kbQuestionLogic.addOrUpdateQuestion(kbQuestionEntity, this.getAutoViewAction().getDcemAction());
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "question.tagDialog.success.removeTag");
		} catch (Exception e) {
			logger.error("Could not remove selected tags from question: " + kbQuestionEntity.getTitle(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbQuestionEntity = (KbQuestionEntity) this.getActionObject();
		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(), kbQuestionEntity.getCategory().getId());
		if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
					"Operating user does not have management rights for tags of category: " + kbQuestionEntity.getCategory().getName());
		}
		kbQuestionEntity = kbQuestionLogic.getQuestionWithLazyAttribute(kbQuestionEntity.getId(),KbQuestionEntity.GRAPH_QUESTION_TAGS);
	}

	public List<String> actionCompleteTag(String searchTagName) {
		try {
			List<KbTagEntity> filteredTags = kbTagLogic.getTagsNotInQuestionFilterByName(searchTagName, kbQuestionEntity, 30);
			List<String> filteredTagsAsString = new ArrayList<String>(filteredTags.size());
			for (KbTagEntity categoryTag : filteredTags) {
				filteredTagsAsString.add(categoryTag.getName());
			}
			return filteredTagsAsString;
		} catch (Exception e) {
			logger.error("Could not fetch tags of category: " + kbQuestionEntity.getCategory().getName() + " and filter String: " + searchTagName, e);
			
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.tagDialog.error.categoryTagComplete"),
					"questionTagForm:addTagDialogMsg");
			return new ArrayList<String>();
		}
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "40em";
	}

	public void leavingDialog() {
		kbQuestionEntity = null;
		tagName = null;
		selectedTags = null;
	}

	public KbQuestionEntity getKbQuestionEntity() {
		return kbQuestionEntity;
	}

	public void setKbQuestionEntity(KbQuestionEntity kbQuestionEntity) {
		this.kbQuestionEntity = kbQuestionEntity;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<KbTagEntity> getSelectedTags() {
		return selectedTags;
	}

	public void setSelectedTags(List<KbTagEntity> selectedTags) {
		this.selectedTags = selectedTags;
	}

}