package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbTagLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbTagDialog")
@SessionScoped
public class KbTagDialog extends DcemDialog {

	// private Logger logger = LogManager.getLogger(KbTagDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbModule kbModule;

	@Inject
	KbTagView kbTagView;

	@Inject
	KbTagLogic kbTagLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private int categoryId;
	private KbTagEntity tagEntity;
	private List<KbCategoryEntity> editableCategories;
	private List<SelectItem> categoriesSelectOne;
	private boolean editMode;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		tagEntity.setName(tagEntity.getName().trim());        
		if (tagEntity.getName().isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "tag.dialog.invalid.name");
			return false;
		}
		if (KbUtils.isValidName(tagEntity.getName()) == false) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "kb.invalidCharacters") + ": " + KbConstants.SPECIAL_CHARACTERS);
			return false;
		}
		if (editMode == false) {
			tagEntity.setCategory(retrieveCategoryById(categoryId));
		}
		kbTagLogic.addOrUpdateTag(tagEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		tagEntity = (KbTagEntity) this.getActionObject();
		categoriesSelectOne = new ArrayList<SelectItem>();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
			editMode = false;
			if (kbTagView.isViewManager()) {
				editableCategories = kbCategoryLogic.getAllCategoriesWithOptionalAttribute(null);
			} else {
				editableCategories = kbCategoryLogic.getAdminCategoriesWithOptionalAttribute(operatorSessionBean.getDcemUser().getId(), null);
				if (editableCategories.size() == 0) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, "Operating user does not have management rights for any category.");
				}
			}
			categoriesSelectOne.add(new SelectItem("", JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "tag.dialog.selectCategory")));
			for (KbCategoryEntity category : editableCategories) {
				categoriesSelectOne.add(new SelectItem(category.getId(), category.getName()));
			}
			return;
		}
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					tagEntity.getCategory().getId());
			if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
				throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
						"Operating user does not have management rights for tags of category: " + tagEntity.getCategory().getName());
			}
			editMode = true;
			categoriesSelectOne.add(new SelectItem(tagEntity.getCategory().getId(), tagEntity.getCategory().getName()));
			return;
		}
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_DELETE)) {
			List<Object> selectedTags = autoViewBean.getSelectedItems();
			Set<KbCategoryEntity> categories = selectedTags.stream().map(tag -> ((KbTagEntity) tag).getCategory()).collect(Collectors.toSet());
			for (KbCategoryEntity category : categories) {
				KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
						category.getId());
				if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
							"Operating user does not have management rights for tags of category: " + category.getName());
				}
			}
			return;
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> tagsObj = autoViewBean.getSelectedItems();
		List<KbTagEntity> tags = new ArrayList<KbTagEntity>();
		for (Object tagObj : tagsObj) {
			tags.add((KbTagEntity) tagObj);
		}
		kbTagLogic.removeTags(tags, getAutoViewAction().getDcemAction());
	}

	public void leavingDialog() {
		categoryId = 0;
		tagEntity = null;
		editableCategories = null;
		categoriesSelectOne = null;
		editMode = false;
	}

	public String getWidth() {
		return "32em";
	}

	public String getHeight() {
		return "25em";
	}

	private KbCategoryEntity retrieveCategoryById(int id) {
		if (editableCategories == null) {
			return null;
		}
		for (KbCategoryEntity category : editableCategories) {
			if (category.getId() == id) {
				return category;
			}
		}
		return null;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public KbTagEntity getTagEntity() {
		return tagEntity;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public List<SelectItem> getCategoriesSelectOne() {
		return categoriesSelectOne;
	}
}
