package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity_;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;
import com.doubleclue.dcem.knowledgeboard.subjects.KbCategorySubject;

@SuppressWarnings("serial")
@Named("kbCategoryView")
@SessionScoped
public class KbCategoryView extends DcemView {

	// private Logger logger = LogManager.getLogger(KbCategoryView.class);

	@Inject
	KbModule kbModule;

	@Inject
	KbCategoryMemberDialog kbCategoryMemberDialog;

	@Inject
	private KbCategorySubject categorySubject;

	@Inject
	private KbCategoryDialog kbCategoryDialog;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		subject = categorySubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, kbCategoryDialog, KbConstants.KB_CATEGORY_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, kbCategoryDialog, KbConstants.KB_CATEGORY_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, kbCategoryDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(KbConstants.KB_MANAGE_CATEGORY_MEMBER, resourceBundle, kbCategoryMemberDialog, KbConstants.KB_CATEGORY_MEMBER_DIALOG);

		viewManager = operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_MANAGE));
	}

	@Override
	public void reload() {
	}

	@Override
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root) {
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		if (viewManager) {
			return predicates;
		}
		@SuppressWarnings("unchecked")
		Root<KbCategoryEntity> categoryRoot = (Root<KbCategoryEntity>) root;

		// Create query predicate to check if a user has admin rights in category (+ not disabled)
		Subquery<Integer> categoryAdminIdsOfUser = KbUtils.getUserAdminCategoryIds(criteriaBuilder, operatorSessionBean.getDcemUser());
		Predicate categoryOfUserPredicate = criteriaBuilder.in(categoryRoot.get(KbCategoryEntity_.id)).value(categoryAdminIdsOfUser);

		predicates.add(categoryOfUserPredicate);
		return predicates;
	}

	public boolean isViewManager() {
		return viewManager;
	}
}