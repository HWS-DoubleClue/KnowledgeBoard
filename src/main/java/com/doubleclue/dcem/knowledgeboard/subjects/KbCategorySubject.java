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
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.gui.KbDashboardView;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;


@SuppressWarnings("serial")
@ApplicationScoped
public class KbCategorySubject extends SubjectAbs {

//	private Logger logger = LogManager.getLogger(KbCategorySubject.class);
	
	@Inject 
	private KbDashboardView kbDashboardView;
	
	public KbCategorySubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));
		
		RawAction rawActionManageCategoryMember = new RawAction(KbConstants.KB_MANAGE_CATEGORY_MEMBER,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		rawActionManageCategoryMember.setActionType(ActionType.DIALOG);
		rawActionManageCategoryMember.setIcon("fa fa-users");
		rawActions.add(rawActionManageCategoryMember);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	};

	@Override
	public String getModuleId() {
		return KbModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 800;
	}

	@Override
	public String getIconName() {
		return "far fa-folder-open";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return KbCategoryEntity.class;
	}

	@Override
	public boolean forceView(DcemUser user) {
		return kbDashboardView.isAdminOfAnyCategory();
	}
	
	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		if (KbConstants.KB_MANAGE_CATEGORY_MEMBER.equals(dcemAction.getAction())){
			return true;
		}
		return false;
	}
}
