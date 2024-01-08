package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;

@Named("kbNotificationDialog")
@SessionScoped
public class KbNotificationDialog extends DcemDialog {

	// private Logger logger = LogManager.getLogger(KbNotificationDialog.class);

	private static final long serialVersionUID = 1L;

	@Inject
	private KbUserLogic kbUserLogic;

	@Inject
	private KbCategoryLogic kbCategoryLogic;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	private String searchString;
	private KbUserEntity kbUserEntity;
	private List<KbCategoryEntity> accessibleCategories;
	private HashMap<KbCategoryEntity, KbUserCategoryEntity> categoryMemberMap;

	public KbNotificationDialog() {

	}

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() {
		kbUserLogic.createOrUpdateUserCategories(categoryMemberMap.values(), kbUserEntity);
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		categoryMemberMap = new HashMap<KbCategoryEntity, KbUserCategoryEntity>();
		kbUserEntity = kbUserLogic.getOrCreateKbUser(operatorSessionBean.getDcemUser());
		accessibleCategories = kbCategoryLogic.getAccessibleCategoriesWithOptionalAttribute(operatorSessionBean.getDcemUser().getId(),
				KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
		for (KbCategoryEntity category : accessibleCategories) {
			categoryMemberMap.put(category, new KbUserCategoryEntity(null, category));
			// Null is used as an identifier if the entity has to be persisted or merged!
		}
		List<KbUserCategoryEntity> kbUserCategories = kbUserLogic.getUserCategoriesByUserIdWithOptionalAttribute(operatorSessionBean.getDcemUser().getId(),
				KbUserCategoryEntity.GRAPH_USER_FOLLOWED_TAGS);
		for (KbUserCategoryEntity kbUserCategory : kbUserCategories) {
			categoryMemberMap.put(kbUserCategory.getCategory(), kbUserCategory);
		}
	}

	@Override
	public void leavingDialog() {
		searchString = null;
		kbUserEntity = null;
		accessibleCategories = null;
		categoryMemberMap = null;
	}

	public List<KbTagEntity> getFilteredTags(KbCategoryEntity kbCategoryEntity) {
		if (searchString == null) {
			searchString = "";
		}
		return kbCategoryEntity.getTags().stream().filter(tag -> tag.getName().toLowerCase().contains(searchString.toLowerCase()))
				.collect(Collectors.toList());
	}

	public String getFollowIcon(KbCategoryEntity kbCategoryEntity) {
		if (categoryMemberMap.get(kbCategoryEntity).isFollowingAllTags() == true) {
			return "fa fa-solid fa-bell";
		} else {
			return "fa fa-regular fa-bell-slash";
		}
	}

	public void actionToggleFollowCategory(KbCategoryEntity kbCategoryEntity) {
		KbUserCategoryEntity userCategory = categoryMemberMap.get(kbCategoryEntity);
		if (userCategory.isFollowingAllTags() == true) {
			userCategory.setFollowingAllTags(false);
		} else {
			userCategory.setFollowingAllTags(true);
		}
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	@Override
	public String getHeight() {
		return "45em";
	}

	@Override
	public String getWidth() {
		return "70em";
	}

	public List<KbCategoryEntity> getAccessibleCategories() {
		return accessibleCategories;
	}

	public KbUserEntity getKbUserEntity() {
		return kbUserEntity;
	}

	public void setKbUserEntity(KbUserEntity kbUserEntity) {
		this.kbUserEntity = kbUserEntity;
	}

	public HashMap<KbCategoryEntity, KbUserCategoryEntity> getCategoryMemberMap() {
		return categoryMemberMap;
	}

	public void setCategoryMemberMap(HashMap<KbCategoryEntity, KbUserCategoryEntity> categoryMemberMap) {
		this.categoryMemberMap = categoryMemberMap;
	}
}
