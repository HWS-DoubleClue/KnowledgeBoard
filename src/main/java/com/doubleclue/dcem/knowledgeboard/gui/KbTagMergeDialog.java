package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;

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
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.exceptions.KbErrorCodes;
import com.doubleclue.dcem.knowledgeboard.exceptions.KbException;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbTagLogic;

@SessionScoped
@Named("tagMergeDialog")
public class KbTagMergeDialog extends DcemDialog {

	private static final long serialVersionUID = 1L;
	// private ResourceBundle resourceBundle;

	private Logger Logger = LogManager.getLogger(KbTagMergeDialog.class);

	@Inject
	KbModule kbModule;

	@Inject
	KbTagView kbTagView;

	@Inject
	KbTagLogic kbTagLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private int categoryId;
	private List<SelectItem> categoriesSelectOne;
	private String nameOfMainTag;
	private String nameOfMergingTag;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		KbTagEntity mainTag = kbTagLogic.getTagByNameAndCategoryId(nameOfMainTag, categoryId);
		KbTagEntity mergingTag = kbTagLogic.getTagByNameAndCategoryId(nameOfMergingTag, categoryId);

		if (mainTag == null || mergingTag == null) {
			JsfUtils.addErrorMessage("nothing selected");
			return false;
		}
		if (mergingTag.equals(mainTag)) {
			JsfUtils.addErrorMessage("same tag");
			return false;
		}
		kbTagLogic.mergeTags(getAutoViewAction().getDcemAction(), mainTag, mergingTag);
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		categoryId = 0;
		categoriesSelectOne = new ArrayList<SelectItem>();
		nameOfMainTag = "";
		nameOfMergingTag = "";
		List<KbCategoryEntity> accessibleCategories;
		if (viewNavigator.getActiveView().equals(kbTagView) && kbTagView.isViewManager()) {
			accessibleCategories = kbCategoryLogic.getAllCategoriesWithOptionalAttribute(KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
		} else {
			accessibleCategories = kbCategoryLogic.getAccessibleCategoriesWithOptionalAttribute(operatorSessionBean.getDcemUser().getId(),
					KbCategoryEntity.GRAPH_CATEGORIES_TAGS);
			if (accessibleCategories.isEmpty()) {
				throw new KbException(KbErrorCodes.NO_ACCESS_TO_CATEGORY, "Operating user does not have management rights for any category.");
			}
		}
		categoriesSelectOne.add(new SelectItem(null, JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "question.dialog.selectCategory")));
		for (KbCategoryEntity category : accessibleCategories) {
			categoriesSelectOne.add(new SelectItem(category.getId(), category.getName()));
		}
	}

	// @Override
	// public void actionConfirm() throws Exception {
	// }

	// public void actionUpdateTags() {
	// try {
	// kbTagLogic.getTagsByCategoryId(categoryId);
	// } catch (Exception e) {
	// logger.error("Could not get Tags from Category: " + categoryId, e);
	// JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
	// }
	// }

	public List<String> actionCompleteTag(String name) {
		List<String> filteredTagsAsString = new ArrayList<String>();
		try {
			List<KbTagEntity> filteredTags = kbTagLogic.getTagsByNameAndCategoryId(name, categoryId, 10);
			for (KbTagEntity tag : filteredTags) {
				filteredTagsAsString.add(tag.getName());
			}
		} catch (Exception e) {
			Logger.error("Could not create filtered tag list by search string: " + name, e);
			// JsfUtils.addErrorMessage(JsfUtils.getStringSafely(resourceBundle, "error.global"));
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
		return filteredTagsAsString;
	}

	public void leavingDialog() {
	}

	public String getWidth() {
		return "65em";
	}

	public String getHeight() {
		return "25em";
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public List<SelectItem> getCategoriesSelectOne() {
		return categoriesSelectOne;
	}

	public void setCategoriesSelectOne(List<SelectItem> categoriesSelectOne) {
		this.categoriesSelectOne = categoriesSelectOne;
	}

	public String getNameOfMainTag() {
		return nameOfMainTag;
	}

	public void setNameOfMainTag(String nameOfMainTag) {
		this.nameOfMainTag = nameOfMainTag;
	}

	public String getNameOfMergingTag() {
		return nameOfMergingTag;
	}

	public void setNameOfMergingTag(String nameOfMergingTag) {
		this.nameOfMergingTag = nameOfMergingTag;
	}
}