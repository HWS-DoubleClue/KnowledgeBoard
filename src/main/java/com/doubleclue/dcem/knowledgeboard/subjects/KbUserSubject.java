package com.doubleclue.dcem.knowledgeboard.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class KbUserSubject extends SubjectAbs {

	public KbUserSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN},
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN},
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN},
				ActionSelection.ONE_OR_MORE));
		
		RawAction rawActionCreatedQuestion = new RawAction(KbConstants.KB_SHOW_CREATED_QUESTIONS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActionCreatedQuestion.setActionType(ActionType.DIALOG);
		rawActionCreatedQuestion.setIcon("far fa-question-circle");
		rawActions.add(rawActionCreatedQuestion);
		
		RawAction rawActionFollowedQuestions = new RawAction(KbConstants.KB_SHOW_FOLLOWED_QUESTIONS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActionFollowedQuestions.setActionType(ActionType.DIALOG);
		rawActionFollowedQuestions.setIcon("fa fa-solid fa-bell");
		rawActions.add(rawActionFollowedQuestions);
		
		RawAction rawActionFollowedTags = new RawAction(KbConstants.KB_SHOW_FOLLOWED_TAGS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActionFollowedTags.setActionType(ActionType.DIALOG);
		rawActionFollowedTags.setIcon("fa fa-solid fa-bell");
		rawActions.add(rawActionFollowedTags);
	
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));

	};

	@Override
	public String getModuleId() {
		return KbModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 900;
	}

	@Override
	public String getIconName() {
		return "fa fa-user-pen";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return KbUserEntity.class;
	}
}
