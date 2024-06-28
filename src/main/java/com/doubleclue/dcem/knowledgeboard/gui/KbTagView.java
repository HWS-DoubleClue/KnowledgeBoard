package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity_;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity_;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;
import com.doubleclue.dcem.knowledgeboard.subjects.KbTagSubject;

@SuppressWarnings("serial")
@Named("kbTagView")
@SessionScoped
public class KbTagView extends DcemView {

	// private Logger logger = LogManager.getLogger(KbTagView.class);

	@Inject
	KbModule kbModule;

	@Inject
	private KbTagSubject kbTagSubject;

	@Inject
	private KbTagDialog kbTagDialog;

	@Inject
	private KbTagFollowerDialog kbTagFollowerDialog;

	@Inject
	private KbTagQuestionDialog kbTagQuestionDialog;

	@Inject
	private KbTagMergeDialog kbTagMergeDialog;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		subject = kbTagSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, kbTagDialog, KbConstants.KB_TAG_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, kbTagDialog, KbConstants.KB_TAG_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, kbTagDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(KbConstants.KB_SHOW_FOLLOWER, resourceBundle, kbTagFollowerDialog, KbConstants.KB_TAG_FOLLOWER_DIALOG);
		addAutoViewAction(KbConstants.KB_SHOW_QUESTIONS, resourceBundle, kbTagQuestionDialog, KbConstants.KB_TAG_QUESTION_DIALOG);
		addAutoViewAction(KbConstants.KB_MERGE_TAGS, resourceBundle, kbTagMergeDialog, KbConstants.KB_TAGS_MERGE_DIALOG);

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
		Root<KbTagEntity> tagRoot = (Root<KbTagEntity>) root;
		Join<KbTagEntity, KbCategoryEntity> categoryJoin = tagRoot.join(KbTagEntity_.category);

		// Create query predicate to check if a public category is accessible (not disabled)
		Subquery<Integer> disabledPublicCategoryIds = KbUtils.getDisabledPublicUserCategoryIds(criteriaBuilder, operatorSessionBean.getDcemUser());
		Predicate publicCategoryPredicate = criteriaBuilder.equal(categoryJoin.get(KbCategoryEntity_.publicCategory), true);
		Predicate publicAccessibleCategories = criteriaBuilder.and(publicCategoryPredicate,
				criteriaBuilder.not(criteriaBuilder.in(categoryJoin.get(KbCategoryEntity_.id)).value(disabledPublicCategoryIds)));

		// Create query predicate to check if a private category is accessible (member + not disabled)
		Subquery<Integer> privateCategoryIds = KbUtils.getPrivateAccessibleCategoryIds(criteriaBuilder, operatorSessionBean.getDcemUser());
		Predicate privateAccessibleCategories = criteriaBuilder.in(categoryJoin.get(KbCategoryEntity_.id)).value(privateCategoryIds);

		predicates.add(criteriaBuilder.or(publicAccessibleCategories, privateAccessibleCategories));
		return predicates;
	}

	public boolean isViewManager() {
		return viewManager;
	}
}
