package com.doubleclue.dcem.knowledgeboard.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;

@Named("kbUserFollowedTagsDialog")
@SessionScoped
public class KbUserFollowedTagsDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbUserFollowedTagsDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbUserView kbUserView;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private KbUserEntity kbUserEntity;
	private List<KbTagEntity> followedTags;
	private List<KbTagEntity> selectedTags;
	private List<KbUserCategoryEntity> userCategories;

	@PostConstruct
	private void init() {
	}

	public void actionRemoveTag() {
		if (selectedTags == null || selectedTags.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "user.followedTagsDialog.invalid.emptySelection");
			return;
		}
		try {
			List<KbUserCategoryEntity> changedUserCategories = new ArrayList<KbUserCategoryEntity>();
			for (int i = 0; i < userCategories.size(); i++) {
				if (Collections.disjoint(selectedTags, userCategories.get(i).getFollowedTags()) == false) {
					KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategory(userCategories.get(i).getKbUser().getId(),
							userCategories.get(i).getCategory().getId());
					kbUserCategoryEntity.getFollowedTags().removeAll(selectedTags);
					userCategories.set(i, kbUserCategoryEntity);
					changedUserCategories.add(kbUserCategoryEntity);
				}
			}
			kbUserLogic.updateUserCategories(changedUserCategories, getAutoViewAction().getDcemAction());
			followedTags.removeAll(selectedTags);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "user.followedTagsDialog.success.removeTag");
		} catch (Exception e) {
			logger.error("Could not remove selected tags from followed list of user: " + kbUserEntity.getDcemUser().getLoginId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbUserEntity = (KbUserEntity) this.getActionObject();
		userCategories = kbUserLogic.getUserCategoriesByUserIdWithLazyAttribute(kbUserEntity.getId(), KbUserCategoryEntity.GRAPH_USER_FOLLOWED_TAGS);
		followedTags = new ArrayList<>();
		for (KbUserCategoryEntity userCategory : userCategories) {
			followedTags.addAll(userCategory.getFollowedTags());
		}
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "60em";
	}

	public void leavingDialog() {
		kbUserEntity = null;
		followedTags = null;
		selectedTags = null;
		userCategories = null;
	}

	public List<KbTagEntity> getSelectedTags() {
		return selectedTags;
	}

	public void setSelectedTags(List<KbTagEntity> selectedTags) {
		this.selectedTags = selectedTags;
	}

	public List<KbTagEntity> getFollowedTags() {
		return followedTags;
	}

	public void setFollowedTags(List<KbTagEntity> followedTags) {
		this.followedTags = followedTags;
	}

	public KbUserEntity getKbUserEntity() {
		return kbUserEntity;
	}

	public void setKbUserEntity(KbUserEntity kbUserEntity) {
		this.kbUserEntity = kbUserEntity;
	}
}