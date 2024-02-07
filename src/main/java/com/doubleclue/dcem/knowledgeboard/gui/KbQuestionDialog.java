package com.doubleclue.dcem.knowledgeboard.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DualListModel;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTextContentEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbEmailLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbTagLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbQuestionDialog")
@SessionScoped
public class KbQuestionDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(KbQuestionDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbModule kbModule;

	@Inject
	KbQuestionView kbQuestionView;

	@Inject
	KbDashboardView dashboardView;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbTagLogic kbTagLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	KbEmailLogic kbEmailLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private int categoryId;
	private SelectItem[] questionStatusSelection;
	private KbQuestionStatus questionStatus;
	private KbQuestionEntity questionEntity;
	private KbTextContentEntity questionBody;
	private DualListModel<String> tagDualList;
	private Map<String, KbTagEntity> tagMapping;
	private List<SelectItem> categoriesSelectOne;
	private boolean editMode;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		questionEntity.setTitle(questionEntity.getTitle().trim());
		if (questionEntity.getTitle().length() < 2) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "question.dialog.invalid.title");
			return false;
		}
		questionEntity.setQuestionContent(questionBody);
		if (questionEntity.getQuestionPreview().trim().isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "question.dialog.invalid.questionContent");
			return false;
		}
		questionEntity.setCategory(kbCategoryLogic.getCategoryById(categoryId));
		questionEntity.setStatus(questionStatus);
		questionEntity.setQuestionContent(questionBody);
		LocalDateTime currentTime = LocalDateTime.now();
		questionEntity.setLastModifiedOn(currentTime);
		questionEntity.setLastModifiedBy(operatorSessionBean.getDcemUser());

		List<KbTagEntity> selectedTags = new ArrayList<KbTagEntity>();
		for (String tagName : tagDualList.getTarget()) {
			selectedTags.add(tagMapping.get(tagName));
		}
		questionEntity.setTags(selectedTags);

		if (getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)
				|| getAutoViewAction().getDcemAction().getAction().equals(KbConstants.KB_NEW_POST)) {
			questionEntity.setCreationDate(currentTime);
			questionEntity.setAuthor(operatorSessionBean.getDcemUser());

			kbQuestionLogic.addOrUpdateQuestion(questionEntity, getAutoViewAction().getDcemAction());

			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getOrCreateKbUserCategory(operatorSessionBean.getDcemUser(), questionEntity.getCategory());
			operatorUserCategory.getFollowedQuestions().add(questionEntity);
			kbUserLogic.updateUserCategory(operatorUserCategory);

			if (viewNavigator.getActiveView().equals(dashboardView)) {
				dashboardView.getCategoryMap().put(operatorUserCategory.getCategory(), operatorUserCategory);
			}
			kbEmailLogic.notifyNewPost(questionEntity);
		} else {
			kbQuestionLogic.addOrUpdateQuestion(questionEntity, getAutoViewAction().getDcemAction());
		}
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		questionEntity = (KbQuestionEntity) dcemView.getActionObject();
		categoriesSelectOne = new ArrayList<SelectItem>();

		if (getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)
				|| getAutoViewAction().getDcemAction().getAction().equals(KbConstants.KB_NEW_POST)) {
			List<KbCategoryEntity> accessibleCategories;
			editMode = false;
			if (viewNavigator.getActiveView().equals(kbQuestionView) && kbQuestionView.isViewManager()) {
				accessibleCategories = kbCategoryLogic.getAllCategoriesWithOptionalAttribute(KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
			} else {
				accessibleCategories = kbCategoryLogic.getAccessibleCategoriesWithOptionalAttribute(operatorSessionBean.getDcemUser().getId(),
						KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
				if (accessibleCategories.size() == 0) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, "Operating user does not have management rights for any category.");
				}
			}
			questionBody = new KbTextContentEntity();
			categoriesSelectOne.add(new SelectItem(null, JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "question.dialog.selectCategory")));
			for (KbCategoryEntity category : accessibleCategories) {
				categoriesSelectOne.add(new SelectItem(category.getId(), category.getName()));
			}
			initDualList(questionEntity.getTags());
			return;
		}

		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
				questionEntity.getCategory().getId());
		if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
					"Operating user does not have management rights for tags of category: " + questionEntity.getCategory().getName());
		}

		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			editMode = true;
			categoryId = questionEntity.getCategory().getId();
			questionEntity = kbQuestionLogic.getQuestionWithOptionalAttribute(questionEntity.getId(), KbQuestionEntity.GRAPH_QUESTION_TAGS_AND_CONTENT);
			questionBody = questionEntity.getQuestionContent();
			questionStatus = questionEntity.getStatus();
			categoriesSelectOne.add(new SelectItem(questionEntity.getCategory().getId(), questionEntity.getCategory().getName()));
			initDualList(questionEntity.getTags());
			return;
		}

		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_DELETE)) {
			return;
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> questionsObj = autoViewBean.getSelectedItems();
		List<KbQuestionEntity> questions = new ArrayList<KbQuestionEntity>();
		for (Object questionObj : questionsObj) {
			questions.add((KbQuestionEntity) questionObj);
		}
		kbQuestionLogic.deleteQuestions(questions, getAutoViewAction().getDcemAction());
	}

	private void initDualList(List<KbTagEntity> currentTags) throws Exception {
		List<KbTagEntity> tagList = kbTagLogic.getTagsByCategoryId(categoryId);
		tagMapping = convertTagsToMap(tagList);
		if (currentTags == null) {
			currentTags = new ArrayList<>();
		}
		tagList.removeAll(currentTags);
		tagDualList = new DualListModel<String>(convertTagsToStrings(tagList), convertTagsToStrings(currentTags));
	}

	public void actionUpdateTags() {
		try {
			List<KbTagEntity> tagList = kbTagLogic.getTagsByCategoryId(categoryId);
			tagMapping = convertTagsToMap(tagList);
			tagDualList = new DualListModel<String>(convertTagsToStrings(tagList), new ArrayList<String>());
		} catch (Exception e) {
			logger.error("Could not get Tags from Category: " + categoryId, e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	private List<String> convertTagsToStrings(List<KbTagEntity> tagList) {
		List<String> list = new ArrayList<String>();
		for (KbTagEntity tag : tagList) {
			list.add(tag.getName());
		}
		return list;
	}

	private Map<String, KbTagEntity> convertTagsToMap(List<KbTagEntity> tagList) {
		Map<String, KbTagEntity> tagMap = new HashMap<String, KbTagEntity>();
		for (KbTagEntity tag : tagList) {
			tagMap.put(tag.getName(), tag);
		}
		return tagMap;
	}

	public String getWidth() {
		return "75em";
	}

	public String getHeight() {
		return "40em";
	}

	public void leavingDialog() {
		categoryId = 0;
		questionStatusSelection = null;
		questionStatus = null;
		questionEntity = null;
		questionBody = null;
		tagDualList = null;
		tagMapping = null;
		categoriesSelectOne = null;
		editMode = false;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public KbQuestionEntity getQuestionEntity() {
		return questionEntity;
	}

	public void setQuestionEntity(KbQuestionEntity questionEntity) {
		this.questionEntity = questionEntity;
	}

	public SelectItem[] getQuestionStatusSelection() {
		if (questionStatusSelection == null) {
			KbQuestionStatus[] questionStatus = KbQuestionStatus.values();
			questionStatusSelection = new SelectItem[questionStatus.length];
			int i = 0;
			for (KbQuestionStatus status : questionStatus) {
				questionStatusSelection[i] = new SelectItem(status.name(), JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "questionStatus." + status.name()));
				i++;
			}
		}
		return questionStatusSelection;
	}

	public DualListModel<String> getTagDualList() {
		return tagDualList;
	}

	public void setTagDualList(DualListModel<String> tagDualList) {
		this.tagDualList = tagDualList;
	}

	public List<String> getSelectedTags() {
		return tagDualList.getTarget();
	}

	public KbTextContentEntity getQuestionBody() {
		return questionBody;
	}

	public void setQuestionBody(KbTextContentEntity questionBody) {
		this.questionBody = questionBody;
	}

	public List<SelectItem> getCategoriesSelectOne() {
		return categoriesSelectOne;
	}

	public void setCategoriesSelectOne(List<SelectItem> categoriesSelectOne) {
		this.categoriesSelectOne = categoriesSelectOne;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public KbQuestionStatus getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(KbQuestionStatus questionStatus) {
		this.questionStatus = questionStatus;
	}
}
