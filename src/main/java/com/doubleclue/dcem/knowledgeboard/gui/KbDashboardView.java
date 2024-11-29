package com.doubleclue.dcem.knowledgeboard.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbReplyLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.model.LazyQuestionModel;
import com.doubleclue.dcem.knowledgeboard.subjects.KbDashboardSubject;

@SuppressWarnings("serial")
@Named("kbDashboardView")
@SessionScoped
public class KbDashboardView extends DcemView {

	@Inject
	KbModule kbModule;

	@Inject
	KbDashboardSubject kbDashboardSubject;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	KbQuestionDialog kbQuestionDialog;

	@Inject
	KbReplyQuestionView kbReplyQuestionView;

	@Inject
	KbNotificationDialog kbNotificationDialog;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	private LazyQuestionModel lazyQuestions;

	@Inject
	private KbReplyLogic kbReplyLogic;

	private String filterText;
	private KbUserEntity kbUserEntity;
	private HashMap<KbCategoryEntity, KbUserCategoryEntity> categoryMap;
	private List<KbCategoryEntity> accessibleCategories;
	private Set<KbCategoryEntity> selectedCategories;

	private AutoViewAction newPostAction;
	private AutoViewAction notificationAction;
	private Boolean adminOfAnyCategory;

	@PostConstruct
	public void init() {
		subject = kbDashboardSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		newPostAction = createAutoViewAction(KbConstants.KB_NEW_POST, resourceBundle, kbQuestionDialog, KbConstants.KB_QUESTION_DIALOG, null);
		notificationAction = createAutoViewAction(KbConstants.KB_MANAGE_NOTIFICATIONS, resourceBundle, kbNotificationDialog, KbConstants.KB_NOTIFICATION_DIALOG,
				null);
	}

	@Override
	public void reload() {
		try {
			lazyQuestions.setSearchTerm(null);
			categoryMap = new HashMap<KbCategoryEntity, KbUserCategoryEntity>();
			kbUserEntity = kbUserLogic.getKbUser(operatorSessionBean.getDcemUser().getId());
			accessibleCategories = kbCategoryLogic.getAccessibleCategoriesWithLazyAttribute(operatorSessionBean.getDcemUser().getId(), null);
			selectedCategories = new HashSet<KbCategoryEntity>(accessibleCategories);
			for (KbCategoryEntity category : accessibleCategories) {
				categoryMap.put(category, new KbUserCategoryEntity(null, category));
				// Null is used as an identifier if the entity has to be persisted or merged!
			}
			if (kbUserEntity != null) {
				List<KbUserCategoryEntity> kbUserCategories = kbUserLogic.getUserCategoriesByUserIdWithLazyAttribute(operatorSessionBean.getDcemUser().getId(),
						KbUserCategoryEntity.GRAPH_USER_FOLLOWED_QUESTIONS);
				for (KbUserCategoryEntity kbUserCategory : kbUserCategories) {
					categoryMap.put(kbUserCategory.getCategory(), kbUserCategory);
					if (kbUserCategory.isHiddenInDashboard() == true) {
						selectedCategories.remove(kbUserCategory.getCategory());
					}
				}
			}
		} catch (Exception e) {
			kbUserEntity = null;
			logger.error("Could not get userCategory from user: " + operatorSessionBean.getDcemUser().getLoginId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "dashboard.error.user");
		}
	}

	public void actionSaveCategorySettings() {
		try {
			kbUserEntity = kbUserLogic.getOrCreateKbUser(operatorSessionBean.getDcemUser());
			for (KbCategoryEntity category : accessibleCategories) {
				categoryMap.get(category).setHiddenInDashboard(selectedCategories.contains(category) == false);
			}
			kbUserLogic.createOrUpdateUserCategories(categoryMap.values(), kbUserEntity);
			reload();
			lazyQuestions.setSearchTerm(filterText);
		} catch (Exception e) {
			kbUserEntity = null;
			logger.error("Could not update userCategory from user: " + operatorSessionBean.getDcemUser().getLoginId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "dashboard.error.user");
		}
	}

	@Override
	public void leavingView() {
		filterText = null;
		kbUserEntity = null;
		categoryMap = null;
		selectedCategories = null;
	}

	public void actionSearch() {
		if (filterText != null) {
			filterText = filterText.trim();
		}
		lazyQuestions.setSearchTerm(filterText);
	}

	public void openQuestion(KbQuestionEntity kbQuestionEntity) {
		kbReplyQuestionView.setKbQuestionEntity(kbQuestionEntity);
		viewNavigator.setActiveView(KbModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + kbReplyQuestionView.getSubject().getViewName());
	}

	public void actionToggleFollowQuestion(KbQuestionEntity kbQuestionEntity) {
		try {
			kbUserEntity = kbUserLogic.getOrCreateKbUser(operatorSessionBean.getDcemUser());
			KbUserCategoryEntity userCategory = categoryMap.get(kbQuestionEntity.getCategory());
			if (userCategory == null) {
				userCategory = kbUserLogic.getOrCreateKbUserCategory(operatorSessionBean.getDcemUser(), kbQuestionEntity.getCategory());
			}
			if (userCategory.getFollowedQuestions().contains(kbQuestionEntity)) {
				userCategory.getFollowedQuestions().remove(kbQuestionEntity);
			} else {
				userCategory.getFollowedQuestions().add(kbQuestionEntity);
			}
			KbCategoryEntity category = kbCategoryLogic.getCategoryById(userCategory.getCategory().getId());
			userCategory.setCategory(category); // needs an attached category for persist
			if (userCategory.getKbUser() == null) {
				userCategory.setKbUser(kbUserEntity);
				kbUserLogic.addUserCategory(userCategory, notificationAction.getDcemAction());
			} else {
				userCategory = kbUserLogic.updateUserCategory(userCategory, notificationAction.getDcemAction());
			}
			categoryMap.put(category, userCategory);
		} catch (Exception e) {
			logger.error("User: " + operatorSessionBean.getDcemUser().getLoginId() + " could not toggle follow question: " + kbQuestionEntity.getId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	public String getFollowIcon(KbQuestionEntity kbQuestionEntity) {
		try {
			KbCategoryEntity category = kbQuestionEntity.getCategory();
			if (categoryMap.get(category) != null && categoryMap.get(category).getFollowedQuestions().contains(kbQuestionEntity)) {
				return "fa fa-solid fa-bell";
			} else {
				return "fa fa-regular fa-bell-slash";
			}
		} catch (Exception e) {
			logger.error("Error while checking if user: " + kbUserEntity.getDcemUser().getLoginId() + " follows question: " + kbQuestionEntity.getId(), e);
			return "";
		}
	}

	public AutoViewAction getNewPostAction() {
		return newPostAction;
	}

	public AutoViewAction getNotificationAction() {
		return notificationAction;
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		if (dcemUser == null) {
			return JsfUtils.getDefaultUserImage();
		}
		byte[] image = dcemUser.getPhoto();
		if (image != null) {
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	@Override
	public LazyQuestionModel getLazyModel() {
		return lazyQuestions;
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	public HashMap<KbCategoryEntity, KbUserCategoryEntity> getCategoryMap() {
		return categoryMap;
	}

	public Set<KbCategoryEntity> getSelectedCategories() {
		return selectedCategories;
	}

	public void setSelectedCategories(Set<KbCategoryEntity> selectedCategories) {
		this.selectedCategories = selectedCategories;
	}

	public List<KbCategoryEntity> getAccessibleCategories() {
		if (accessibleCategories == null) {
			accessibleCategories = new ArrayList<KbCategoryEntity>();
		}
		return accessibleCategories;
	}

	public Boolean isAdminOfAnyCategory() {
		if (adminOfAnyCategory == null) {
			try {
				adminOfAnyCategory = kbUserLogic.isUserAdminAndNotDisabled(operatorSessionBean.getDcemUser().getId());
			} catch (Exception e) {
				logger.error("Could not check if user (" + operatorSessionBean.getDcemUser().getLoginId()
						+ ") is admin and not disabled for at least one knowledgeboard category", e);
				adminOfAnyCategory = false;
			}
		}
		return adminOfAnyCategory;
	}

	public LocalDateTime getYoungestReplyDate(KbQuestionEntity question) {
		try {
			LocalDateTime replyDate = kbReplyLogic.getYoungestReplyCreationDate(question);
			return replyDate;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
}
