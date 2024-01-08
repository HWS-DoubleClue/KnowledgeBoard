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
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@Named("kbQuestionFollowerDialog")
@SessionScoped
public class KbQuestionFollowerDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbQuestionFollowerDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbQuestionView kbQuestionView;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private ResourceBundle resourceBundle;
	private KbQuestionEntity kbQuestionEntity;
	private String userLoginId;
	private List<KbUserCategoryEntity> selectedFollower;
	private List<KbUserCategoryEntity> followers;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionAddFollower() {
		try {
			KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategoryByLoginIdAndCategoryId(userLoginId,
					kbQuestionEntity.getCategory().getId());
			if (kbUserCategoryEntity == null) {
				
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.followerDialog.invalid.userName"),
						"questionFollowerForm:addFollowerDialogMsg");
				return;
			}
			if (followers.contains(kbUserCategoryEntity)) {
				
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.followerDialog.invalid.duplicateFollower"),
						"questionFollowerForm:addFollowerDialogMsg");
				return;
			}
			kbUserCategoryEntity.getFollowedQuestions().add(kbQuestionEntity);
			// kbUserCategoryEntity = kbUserLogic.updateUserCategory(kbUserCategoryEntity);
			followers.add(kbUserCategoryEntity);
			JsfUtils.addInfoMessageToComponentId(
					String.format(JsfUtils.getStringSafely(resourceBundle, "question.followerDialog.success.addMember"), userLoginId),
					"questionFollowerForm:addFollowerDialogMsg");
		} catch (Exception e) {
			logger.error("UserLoginId: " + userLoginId + " could not be added as follower to question: " + kbQuestionEntity.getTitle(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "questionFollowerForm:addFollowerDialogMsg");
		} finally {
			userLoginId = null;
		}
	}

	public void actionRemoveFollower() {
		if (selectedFollower == null || selectedFollower.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "question.followerDialog.invalid.emptySelection");
			return;
		}
		try {
			for (int i = 0; i < selectedFollower.size(); i++) {
				KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategory(selectedFollower.get(i).getKbUser().getId(),
						selectedFollower.get(i).getCategory().getId());
				kbUserCategoryEntity.getFollowedQuestions().remove(kbQuestionEntity);
				selectedFollower.set(i, kbUserCategoryEntity);
			}
			kbUserLogic.updateUserCategories(selectedFollower, getAutoViewAction().getDcemAction());
			followers.removeAll(selectedFollower);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "question.followerDialog.success.removeMember");
		} catch (Exception e) {
			logger.error("Could not remove selected followers from question: " + kbQuestionEntity.getTitle(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbQuestionEntity = (KbQuestionEntity) this.getActionObject();
		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
				kbQuestionEntity.getCategory().getId());
		if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, autoViewAction) == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS,
					"Operating user does not have management rights for tags of category: " + kbQuestionEntity.getCategory().getName());
		}
		followers = kbUserLogic.getFollowerOfQuestion(kbQuestionEntity);
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

	public List<String> actionCompleteMember(String searchLoginId) {
		try {
			List<KbUserCategoryEntity> filteredMembers = kbUserLogic.getCategoryMembersNotFollowingQuestionFilterByLoginId(searchLoginId, kbQuestionEntity, 30);
			List<String> filteredMembersAsString = new ArrayList<String>(filteredMembers.size());
			for (KbUserCategoryEntity categoryMember : filteredMembers) {
				filteredMembersAsString.add(categoryMember.getKbUser().getDcemUser().getLoginId());
			}
			return filteredMembersAsString;
		} catch (Exception e) {
			logger.error("Could not fetch members of category: " + kbQuestionEntity.getCategory().getName() + " and filter String: " + searchLoginId, e);
			
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "question.followerDialog.error.categoryMemberComplete"),
					"questionFollowerForm:addFollowerDialogMsg");
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
		kbQuestionEntity = null;
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

	public KbQuestionEntity getKbQuestionEntity() {
		return kbQuestionEntity;
	}

	public void setKbQuestionEntity(KbQuestionEntity kbQuestionEntity) {
		this.kbQuestionEntity = kbQuestionEntity;
	}

}