package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;

@Named("kbUserDialog")
@SessionScoped
public class KbUserDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(KbUserDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbModule kbModule;

	@Inject
	KbUserView kbUserView;

	@Inject
	UserLogic userLogic;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbUserEntity kbUserEntity;
	private String loginId;
	private boolean editMode;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		try {
			if (editMode == false) {
				DcemUser dcemUser = userLogic.getDistinctUser(loginId);
				if (dcemUser == null) {
					JsfUtils.addErrorMessage(resourceBundle, "user.dialog.invalid.userName");
					return false;
				}
				KbUserEntity kbUser = kbUserLogic.getKbUser(dcemUser.getId());
				if (kbUser != null) {
					JsfUtils.addErrorMessage(resourceBundle, "user.dialog.invalid.duplicateUser");
					return false;
				}
				kbUserEntity.setDcemUser(dcemUser);
				kbUserLogic.addKbUser(kbUserEntity);
			} else {
				kbUserLogic.updateKbUser(kbUserEntity);
			}
			return true;
		} catch (Exception e) {
			logger.error("User: " + loginId + " could not be added or edited as knowledgeboarduser", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return false;
		} finally {
			kbUserEntity = new KbUserEntity();
			loginId = null;
		}
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception { 
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_DELETE)) {
			return;
		}
		kbUserEntity = (KbUserEntity) this.getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			editMode = true;
			loginId = kbUserEntity.getDcemUser().getLoginId();
		} else {
			editMode = false;
			loginId = null;
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> kbUsersObj = autoViewBean.getSelectedItems();
		List<KbUserEntity> kbUsers = new ArrayList<KbUserEntity>();
		for (Object kbUser : kbUsersObj) {
			kbUsers.add((KbUserEntity) kbUser);
		}
		kbUserLogic.removeKbUsers(kbUsers, getAutoViewAction().getDcemAction());
	}

	public List<String> actionCompleteUser(String name) {
		List<String> userNames = userLogic.getCompleteUserList(name, 30);
		if (userNames == null) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "user.dialog.error.userComplete"), "userDialog:userDialogMsg");
		}
		return userNames;
	}

	public void leavingDialog() {
		kbUserEntity = null;
		loginId = null;
		editMode = false;
	}

	public String getWidth() {
		return "30em";
	}

	public String getHeight() {
		if (getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_DELETE)) {
			return "25em";
		}
		return "15em";
	}

	public KbUserEntity getKbUserEntity() {
		return kbUserEntity;
	}

	public void setKbUserEntity(KbUserEntity kbUserEntity) {
		this.kbUserEntity = kbUserEntity;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

}
