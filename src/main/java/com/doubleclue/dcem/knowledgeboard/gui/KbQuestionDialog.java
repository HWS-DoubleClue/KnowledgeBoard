package com.doubleclue.dcem.knowledgeboard.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.doubleclue.dcem.knowledgeboard.exceptions.KbErrorCodes;
import com.doubleclue.dcem.knowledgeboard.exceptions.KbException;
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
	KbTagView kbTagView;

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
	private List<KbCategoryEntity> adminCategories;
	private List<KbTagEntity> toBeAddedTags;
	private KbTagEntity toBeAddedTag;
	private boolean categoryAdmin;

	private boolean editMode;
	private boolean notifyAllMembers;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		questionEntity.setTitle(questionEntity.getTitle().trim());
		List<String> duplicatedTagNames = findDuplicatedTags();
		if (duplicatedTagNames.isEmpty() == false) {
			JsfUtils.addErrorMessage(
					JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "question.dialog.invalid.duplicatedTags") + " " + String.join(", ", duplicatedTagNames));
			return false;
		}
		List<KbTagEntity> newTags = categoryAdmin ? toBeAddedTags : new ArrayList<KbTagEntity>();

		questionEntity.setQuestionContent(questionBody);
		questionEntity.setCategory(kbCategoryLogic.getCategoryById(categoryId));
		questionEntity.setStatus(questionStatus);
		questionEntity.setQuestionContent(questionBody);
		LocalDateTime currentTime = LocalDateTime.now();
		questionEntity.setLastModifiedOn(currentTime);
		questionEntity.setLastModifiedBy(operatorSessionBean.getDcemUser());
		List<KbTagEntity> selectedTags = new ArrayList<KbTagEntity>();
		for (String tagName : tagDualList.getTarget()) {
			KbTagEntity selectedTag = tagMapping.get(tagName);
			if (selectedTag != null) {
				selectedTags.add(tagMapping.get(tagName));
			}
		}
		questionEntity.setTags(selectedTags);

		if (getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)
				|| getAutoViewAction().getDcemAction().getAction().equals(KbConstants.KB_NEW_POST)) {
			questionEntity.setCreationDate(currentTime);
			questionEntity.setAuthor(operatorSessionBean.getDcemUser());

			kbQuestionLogic.addOrUpdateQuestionWithNewTags(questionEntity, newTags, getAutoViewAction().getDcemAction());

			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getOrCreateKbUserCategory(operatorSessionBean.getDcemUser(), questionEntity.getCategory());
			operatorUserCategory.getFollowedQuestions().add(questionEntity);
			kbUserLogic.updateUserCategory(operatorUserCategory, this.getAutoViewAction().getDcemAction(), false);

			if (viewNavigator.getActiveView().equals(dashboardView)) {
				dashboardView.getCategoryMap().put(operatorUserCategory.getCategory(), operatorUserCategory);
			}
			kbEmailLogic.notifyNewPost(questionEntity, notifyAllMembers && categoryAdmin);
		} else {
			kbQuestionLogic.addOrUpdateQuestionWithNewTags(questionEntity, newTags, getAutoViewAction().getDcemAction());
		}
		return true;
	}

	private List<String> findDuplicatedTags() throws Exception {
		List<String> duplicatedTagNames = new ArrayList<String>();
		for (KbTagEntity tag : toBeAddedTags) {
			if (tagMapping.containsKey(tag.getName())) {
				duplicatedTagNames.add(tag.getName());
			}
		}
		return duplicatedTagNames;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		notifyAllMembers = false;
		questionEntity = (KbQuestionEntity) dcemView.getActionObject();
		categoriesSelectOne = new ArrayList<SelectItem>();
		toBeAddedTags = new ArrayList<KbTagEntity>();
		toBeAddedTag = new KbTagEntity();

		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)
				|| autoViewAction.getDcemAction().getAction().equals(KbConstants.KB_NEW_POST)) {
			categoryId = 0;
			editMode = false;
			categoryAdmin = false;
			List<KbCategoryEntity> accessibleCategories;
			if (viewNavigator.getActiveView().equals(kbQuestionView) && kbQuestionView.isViewManager()) {
				accessibleCategories = kbCategoryLogic.getAllCategoriesWithLazyAttribute(KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
			} else {
				accessibleCategories = kbCategoryLogic.getAccessibleCategoriesWithLazyAttribute(operatorSessionBean.getDcemUser().getId(),
						KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
				if (accessibleCategories.isEmpty()) {
					throw new KbException(KbErrorCodes.NO_ACCESS_TO_CATEGORY, "Operating user does not have management rights for any category.");
				}
			}
			loadAdminCategories();
			questionBody = new KbTextContentEntity();
			questionStatus = KbQuestionStatus.Open;
			categoriesSelectOne.add(new SelectItem(null, JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "question.dialog.selectCategory")));
			for (KbCategoryEntity category : accessibleCategories) {
				categoriesSelectOne.add(new SelectItem(category.getId(), category.getName()));
			}
			initDualList(questionEntity.getTags());
			return;
		}
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					questionEntity.getCategory().getId());
			if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
				if ((questionEntity.getAuthor() != null && questionEntity.getAuthor().equals(operatorSessionBean.getDcemUser())) == false) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
							"Operating user does not have management rights for tags of category: " + questionEntity.getCategory().getName());
				}
			}
			loadAdminCategories();
			editMode = true;
			categoryAdmin = adminCategories.contains(questionEntity.getCategory());
			categoryId = questionEntity.getCategory().getId();
			questionEntity = kbQuestionLogic.getQuestionWithLazyAttribute(questionEntity.getId(), KbQuestionEntity.GRAPH_QUESTION_TAGS_AND_CONTENT);
			questionBody = questionEntity.getQuestionContent();
			questionStatus = questionEntity.getStatus();
			categoriesSelectOne.add(new SelectItem(questionEntity.getCategory().getId(), questionEntity.getCategory().getName()));
			initDualList(questionEntity.getTags());
			return;
		}
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_DELETE)) {
			List<Object> selectedQuestions = autoViewBean.getSelectedItems();
			Set<KbCategoryEntity> categories = selectedQuestions.stream().map(question -> ((KbQuestionEntity) question).getCategory())
					.collect(Collectors.toSet());
			for (KbCategoryEntity category : categories) {
				KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(), category.getId());
				if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
							"Operating user does not have management rights for tags of category: " + category.getName());
				}
			}
			return;
		}

	}

	private void loadAdminCategories() {
		try {
			if (kbTagView.isViewManager()) {
				adminCategories = kbCategoryLogic.getAllCategoriesWithLazyAttribute(null);
			} else {
				adminCategories = kbCategoryLogic.getAdminCategoriesWithLazyAttribute(operatorSessionBean.getDcemUser().getId(), null);
			}
		} catch (Exception e) {
			logger.error("Could not admin Categories for user : " + operatorSessionBean.getDcemUser().getDisplayName(), e);
			JsfUtils.addWarnMessage(JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "error.global"));
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

	public void actionUpdateCategory() {
		try {
			List<KbTagEntity> tagList = kbTagLogic.getTagsByCategoryId(categoryId);
			tagMapping = convertTagsToMap(tagList);
			tagDualList = new DualListModel<String>(convertTagsToStrings(tagList), new ArrayList<String>());
			categoryAdmin = adminCategories.stream().anyMatch(category -> category.getId().equals(categoryId));
			notifyAllMembers = notifyAllMembers && categoryAdmin;
		} catch (Exception e) {
			logger.error("Could not get Tags from Category: " + categoryId, e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	public void actionNewTag() {
		toBeAddedTag.setName(toBeAddedTag.getName().trim());
		if (toBeAddedTag.getName().isEmpty() && toBeAddedTag.getName().length() < 2) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "tag.dialog.invalid.name");
			return;
		}
		if (KbUtils.isValidName(toBeAddedTag.getName()) == false) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "kb.invalidCharacters") + ": " + KbConstants.SPECIAL_CHARACTERS);
			return;
		}
		if (newTagAlreadyExists(toBeAddedTags, toBeAddedTag) == false) {
			toBeAddedTags.add(toBeAddedTag);
			toBeAddedTag = new KbTagEntity();
		} else {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "question.dialog.invalid.duplicatedNewTags");
		}
	}

	private boolean newTagAlreadyExists(List<KbTagEntity> tagList, KbTagEntity tag) {
		for (KbTagEntity newTag : tagList) {
			if (newTag.getName().toLowerCase().equals(tag.getName().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void removeToBeAddedTag(KbTagEntity removeTag) {
		toBeAddedTags.removeIf(tag -> tag.getName().equals(removeTag.getName()));
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
		return "85vw";
	}

	public String getHeight() {
		return "90vh";
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
		toBeAddedTags = null;
		toBeAddedTag = null;
		adminCategories = null;
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

	public List<KbTagEntity> getToBeAddedTags() {
		return toBeAddedTags;
	}

	public void setToBeAddedTags(List<KbTagEntity> toBeAddedTags) {
		this.toBeAddedTags = toBeAddedTags;
	}

	public KbTagEntity getToBeAddedTag() {
		return toBeAddedTag;
	}

	public void setToBeAddedTag(KbTagEntity toBeAddedTag) {
		this.toBeAddedTag = toBeAddedTag;
	}

	public boolean isCategoryAdmin() {
		return categoryAdmin;
	}

	public void setCategoryAdmin(boolean categoryAdmin) {
		this.categoryAdmin = categoryAdmin;
	}

	public boolean isNotifyAllMembers() {
		return notifyAllMembers;
	}

	public void setNotifyAllMembers(boolean notifyAllMembers) {
		this.notifyAllMembers = notifyAllMembers;
	}
}
