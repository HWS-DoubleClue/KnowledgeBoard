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
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity_;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;
import com.doubleclue.dcem.knowledgeboard.subjects.KbQuestionSubject;

@SuppressWarnings("serial")
@Named("kbQuestionView")
@SessionScoped
public class KbQuestionView extends DcemView {

	// private Logger logger = LogManager.getLogger(KbQuestionView.class);

	@Inject
	KbModule kbModule;

	@Inject
	private KbQuestionSubject kbQuestionSubject;

	@Inject
	private KbReplyQuestionView kbReplyQuestionView;

	@Inject
	private KbQuestionDialog kbQuestionDialog;

	@Inject
	private KbQuestionFollowerDialog kbQuestionFollowerDialog;

	@Inject
	private KbQuestionTagDialog kbQuestionTagDialog;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		subject = kbQuestionSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, kbQuestionDialog, KbConstants.KB_QUESTION_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, kbQuestionDialog, KbConstants.KB_QUESTION_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, kbQuestionDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(KbConstants.KB_SHOW_FOLLOWER, resourceBundle, kbQuestionFollowerDialog, KbConstants.KB_QUESTION_FOLLOWER_DIALOG);
		addAutoViewAction(KbConstants.KB_SHOW_TAGS, resourceBundle, kbQuestionTagDialog, KbConstants.KB_QUESTION_TAG_DIALOG);

		addAutoViewAction(KbConstants.KB_SHOW_REPLYVIEW, resourceBundle, null, null);

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
		Root<KbQuestionEntity> questionRoot = (Root<KbQuestionEntity>) root;
		Join<KbQuestionEntity, KbCategoryEntity> categoryJoin = questionRoot.join(KbQuestionEntity_.category);

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

	public void openQuestionDetails() {
		KbQuestionEntity kbQuestionEntity = (KbQuestionEntity) getActionObject();
		kbReplyQuestionView.setKbQuestionEntity(kbQuestionEntity);
		viewNavigator.setActiveView(KbModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + kbReplyQuestionView.getSubject().getViewName());
	}

}
