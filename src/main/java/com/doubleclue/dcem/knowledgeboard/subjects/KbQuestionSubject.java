package com.doubleclue.dcem.knowledgeboard.subjects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.gui.KbDashboardView;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class KbQuestionSubject extends SubjectAbs {


	@Inject
	KbDashboardView kbDashboardView;

	public KbQuestionSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		RawAction rawActionFollower = new RawAction(KbConstants.KB_SHOW_FOLLOWER,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActionFollower.setActionType(ActionType.DIALOG);
		rawActionFollower.setIcon("fa fa-users");
		rawActions.add(rawActionFollower);

		RawAction rawActionQuestion = new RawAction(KbConstants.KB_SHOW_TAGS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActionQuestion.setActionType(ActionType.DIALOG);
		rawActionQuestion.setIcon("fas fa-tags");
		rawActions.add(rawActionQuestion);

		RawAction rawActionQuestionDetails = new RawAction(KbConstants.KB_SHOW_REPLYVIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY);
		rawActionQuestionDetails.setActionType(ActionType.EL_METHOD);
		rawActionQuestionDetails.setElMethodExpression(KbConstants.KB_EL_METHOD_QUESTIONVIEW_GOTO_REPLYVIEW);
		rawActionQuestionDetails.setIcon("fas fa-reply");
		rawActions.add(rawActionQuestionDetails);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_HELPDESK, DcemConstants.SYSTEM_ROLE_VIEWER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	};

	@Override
	public String getModuleId() {
		return KbModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 200;
	}

	@Override
	public String getIconName() {
		return "far fa-question-circle";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return KbQuestionEntity.class;
	}

	public boolean forceView(DcemUser user) {
		return kbDashboardView.isAdminOfAnyCategory();
	}

	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		if (KbConstants.KB_SHOW_REPLYVIEW.equals(dcemAction.getAction())){
			return true;
		}
		return forceView(dcemUser);
	}
}
