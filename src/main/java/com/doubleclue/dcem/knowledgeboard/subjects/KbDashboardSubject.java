package com.doubleclue.dcem.knowledgeboard.subjects;

import javax.enterprise.context.ApplicationScoped;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class KbDashboardSubject extends SubjectAbs {

	public KbDashboardSubject() {

		RawAction rawActionNewPost = new RawAction(KbConstants.KB_NEW_POST, new String[] { DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_HELPDESK,
				DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.CREATE_OBJECT);
		rawActionNewPost.setActionType(ActionType.CREATE_OBJECT);
		rawActionNewPost.setIcon("fa fa-plus");
		rawActions.add(rawActionNewPost);
		
		RawAction rawActionManageNotifications = new RawAction(KbConstants.KB_MANAGE_NOTIFICATIONS, new String[] { DcemConstants.SYSTEM_ROLE_USER,
				DcemConstants.SYSTEM_ROLE_HELPDESK, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.IGNORE);
		rawActionManageNotifications.setActionType(ActionType.DIALOG);
		rawActionManageNotifications.setIcon("fa fa-solid fa-bell");
		rawActions.add(rawActionManageNotifications);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	}

	@Override
	public String getModuleId() {
		return KbModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 100;
	}

	@Override
	public String getIconName() {
		return "fa fa-table";
	}

	@Override
	public String getPath() {
		return "/modules/knowledgeboard/kbDashboard.xhtml";
	}

	@Override
	public Class<?> getKlass() {
		return KbQuestionEntity.class;
	}
}