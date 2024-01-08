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

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbTagFollowerDialog")
@SessionScoped
public class KbTagFollowerDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbTagFollowerDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbTagView kbTagView;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbTagEntity kbTagEntity;
	private String userLoginId;
	private List<KbUserCategoryEntity> selectedFollower;
	private List<KbUserCategoryEntity> followers;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionAddFollower() {
		try {
			KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategoryByLoginIdAndCategoryId(userLoginId, kbTagEntity.getCategory().getId());
			if (kbUserCategoryEntity == null) {
				
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.followerDialog.invalid.userName"), "tagFollowerForm:addFollowerDialogMsg");
				return;
			}
			if (followers.contains(kbUserCategoryEntity)) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.followerDialog.invalid.duplicateFollower"),
						"tagFollowerForm:addFollowerDialogMsg");
				return;
			}
			kbUserCategoryEntity.getFollowedTags().add(kbTagEntity);
			kbUserLogic.updateUserCategory(kbUserCategoryEntity);
			followers.add(kbUserCategoryEntity);
			JsfUtils.addInfoMessageToComponentId(String.format(JsfUtils.getStringSafely(resourceBundle, "tag.followerDialog.success.addMember"), userLoginId),
					"tagFollowerForm:addFollowerDialogMsg");
		} catch (Exception e) {
			logger.error("UserLoginId: " + userLoginId + " could not be added as follower to tag: " + kbTagEntity.getName(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "tagFollowerForm:addFollowerDialogMsg");
		} finally {
			userLoginId = null;
		}
	}

	public void actionRemoveFollower() {
		if (selectedFollower == null || selectedFollower.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "tag.followerDialog.invalid.emptySelection");
			return;
		}
		try {
			for (int i = 0; i < selectedFollower.size(); i++) {
				KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategory(selectedFollower.get(i).getKbUser().getId(),
						selectedFollower.get(i).getCategory().getId());
				kbUserCategoryEntity.getFollowedTags().remove(kbTagEntity);
				selectedFollower.set(i, kbUserCategoryEntity);
			}
			kbUserLogic.updateUserCategories(selectedFollower, getAutoViewAction().getDcemAction());
			followers.removeAll(selectedFollower);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "tag.followerDialog.success.removeMember");
		} catch (Exception e) {
			logger.error("Could not remove selected followers from tag: " + kbTagEntity.getName(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbTagEntity = (KbTagEntity) this.getActionObject();
		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(), kbTagEntity.getCategory().getId());
		if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
					"Operating user does not have management rights for tags of category: " + kbTagEntity.getCategory().getName());
		}
		followers = kbUserLogic.getFollowerOfTag(kbTagEntity);
	}

	public List<KbUserCategoryEntity> getFollowers() {
		if (followers == null) {
			return new ArrayList<KbUserCategoryEntity>();
		}
		return followers;
	}

	public void setFollowers(List<KbUserCategoryEntity> followers) {
		this.followers = followers;
	}

	public List<String> actionCompleteMember(String loginId) {
		try {
			List<KbUserCategoryEntity> filteredMembers = kbUserLogic.getCategoryMembersNotFollowingTagFilterByLoginId(loginId, kbTagEntity, 30);
			List<String> filteredMembersAsString = new ArrayList<String>(filteredMembers.size());
			for (KbUserCategoryEntity categoryMember : filteredMembers) {
				filteredMembersAsString.add(categoryMember.getKbUser().getDcemUser().getLoginId());
			}
			return filteredMembersAsString;
		} catch (Exception e) {
			logger.error("Could not fetch members of category: " + kbTagEntity.getCategory().getName() + " and filter String: " + loginId, e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "tag.followerDialog.error.categoryMemberComplete"),
					"tagFollowerForm:addFollowerDialogMsg");
			return new ArrayList<String>();
		}
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "30em";
	}

	public void leavingDialog() {
		kbTagEntity = null;
		userLoginId = null;
		selectedFollower = null;
		followers = null;
	}

	public List<KbUserCategoryEntity> getSelectedFollower() {
		return selectedFollower;
	}

	public void setSelectedFollower(List<KbUserCategoryEntity> selectedFollower) {
		this.selectedFollower = selectedFollower;
	}

	public KbTagEntity getKbTagEntity() {
		return kbTagEntity;
	}

	public void setKbTagEntity(KbTagEntity kbTagEntity) {
		this.kbTagEntity = kbTagEntity;
	}

}