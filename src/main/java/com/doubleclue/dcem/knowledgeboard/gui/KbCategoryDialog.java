package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

@Named("kbCategoryDialog")
@SessionScoped
public class KbCategoryDialog extends DcemDialog {

	// private Logger logger = LogManager.getLogger(KbCategoryDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbModule kbModule;

	@Inject
	KbCategoryView kbCategoryView;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private KbCategoryEntity categoryEntity;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		categoryEntity.setName(categoryEntity.getName().trim());
		if (categoryEntity.getName().length() < 2) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "category.dialog.invalid.name");
			return false;
		}
		if (categoryEntity.getDescription() != null) {
			categoryEntity.setDescription(categoryEntity.getDescription().trim());
		}
		kbCategoryLogic.addOrUpdateCategory(categoryEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		categoryEntity = (KbCategoryEntity) this.getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			categoryEntity = kbCategoryLogic.getCategoryById(categoryEntity.getId());
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> categoriesObj = autoViewBean.getSelectedItems();
		List<KbCategoryEntity> categories = new ArrayList<KbCategoryEntity>();
		for (Object categoryObj : categoriesObj) {
			categories.add((KbCategoryEntity) categoryObj);
		}
		kbCategoryLogic.removeCategory(categories, getAutoViewAction().getDcemAction());
	}

	public void leavingDialog() {
		categoryEntity = null;
	}

	public String getWidth() {
		return "40em";
	}

	public String getHeight() {
		return "25em";
	}

	public KbCategoryEntity getCategoryEntity() {
		return categoryEntity;
	}

	public void setCategoryEntity(KbCategoryEntity categoryEntity) {
		this.categoryEntity = categoryEntity;
	}
}
