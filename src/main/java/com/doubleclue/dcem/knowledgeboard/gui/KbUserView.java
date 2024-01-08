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

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.subjects.KbUserSubject;

@SuppressWarnings("serial")
@Named("kbUserView")
@SessionScoped
public class KbUserView extends DcemView {

	// private Logger logger = LogManager.getLogger(KbTagView.class);

	@Inject
	KbModule kbModule;

	@Inject
	private KbUserSubject kbUserSubject;

	@Inject
	private KbUserDialog kbUserDialog;

	@Inject
	private KbUserCreatedQuestionsDialog kbUserCreatedQuestionsDialog;

	@Inject
	private KbUserFollowedQuestionsDialog kbUserFollowedQuestionsDialog;

	@Inject
	private KbUserFollowedTagsDialog kbUserFollowedTagsDialog;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		subject = kbUserSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, kbUserDialog, KbConstants.KB_USER_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, kbUserDialog, KbConstants.KB_USER_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, kbUserDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(KbConstants.KB_SHOW_CREATED_QUESTIONS, resourceBundle, kbUserCreatedQuestionsDialog, KbConstants.KB_USER_CREATED_QUESTIONS_DIALOG);
		addAutoViewAction(KbConstants.KB_SHOW_FOLLOWED_QUESTIONS, resourceBundle, kbUserFollowedQuestionsDialog, KbConstants.KB_USER_FOLLOWED_QUESTIONS_DIALOG);
		addAutoViewAction(KbConstants.KB_SHOW_FOLLOWED_TAGS, resourceBundle, kbUserFollowedTagsDialog, KbConstants.KB_USER_FOLLOWED_TAGS_DIALOG);

		viewManager = operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_MANAGE));
	}

	@Override
	public void reload() {
	}

	@Override
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root) {
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		return predicates;
	}

	public boolean isViewManager() {
		return viewManager;
	}

}
