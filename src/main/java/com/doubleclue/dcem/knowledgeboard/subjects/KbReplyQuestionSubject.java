package com.doubleclue.dcem.knowledgeboard.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class KbReplyQuestionSubject extends SubjectAbs {
	
	public KbReplyQuestionSubject() {
		setHiddenMenu(true);
		
		RawAction rawActionEditQuestion = new RawAction(DcemConstants.ACTION_EDIT,
				new String[] {DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		rawActionEditQuestion.setActionType(ActionType.DIALOG);
		rawActionEditQuestion.setIcon("fa fa-pencil");
		rawActions.add(rawActionEditQuestion);
		
		RawAction rawActionEditReply = new RawAction(KbConstants.KB_EDIT_REPLY,
				new String[] {DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		rawActionEditReply.setActionType(ActionType.DIALOG);
		rawActionEditReply.setIcon("fa fa-pencil");
		rawActions.add(rawActionEditReply);
		
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] {DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	}

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
		return "/modules/knowledgeboard/kbReplyQuestionView.xhtml";
	}
	
	@Override
	public boolean forceView(DcemUser user) {
		return true;
	}
	
	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		return true;
	}

}
