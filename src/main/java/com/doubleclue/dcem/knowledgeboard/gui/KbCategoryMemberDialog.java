package com.doubleclue.dcem.knowledgeboard.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.subjects.KbCategorySubject;

@Named("kbCategoryMemberDialog")
@SessionScoped
public class KbCategoryMemberDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(KbCategoryMemberDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	KbCategorySubject kbCategorySubject;

	@Inject
	KbCategoryView kbCategoryView;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	private ResourceBundle resourceBundle;
	private KbCategoryEntity kbCategoryEntity;
	private String groupName;
	private List<KbUserCategoryEntity> selectedMember;
	private List<KbUserCategoryEntity> members;
	private KbUserCategoryEntity member;
	private boolean editMode;
	private SelectItem[] booleanOperator;
	private DcemUser dcemUser;
	private byte[] image;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionAddOrUpdateMember() {
		try {
			if (editMode == true) {
				member = kbUserLogic.updateUserCategory(member, this.getAutoViewAction().getDcemAction());
				members.set(members.indexOf(member), member);
				PrimeFaces.current().executeScript("PF('addMemberDialog').hide();");
				JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "category.memberDialog.success.editMember");
			} else if (editMode == false) {
				if (dcemUser == null) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "category.memberDialog.invalid.userName"),
							"categoryMemberDialog:memberDialogMsg");
					return;
				}
				KbUserCategoryEntity kbUserCategoryEntity = kbUserLogic.getKbUserCategory(dcemUser.getId(), kbCategoryEntity.getId());
				if (kbUserCategoryEntity != null) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "category.memberDialog.invalid.duplicateUserCategory"),
							"categoryMemberDialog:memberDialogMsg");
					return;
				}
				member.setKbUser(kbUserLogic.getOrCreateKbUser(dcemUser));
				member.setCategory(kbCategoryLogic.getCategoryById(kbCategoryEntity.getId()));
				kbUserLogic.addUserCategory(member, this.getAutoViewAction().getDcemAction());
				members.add(member);
				JsfUtils.addInfoMessageToComponentId(
						String.format(JsfUtils.getStringSafely(resourceBundle, "category.memberDialog.success.addMember"), dcemUser.getDisplayNameOrLoginId()),
						"categoryMemberDialog:memberDialogMsg");
				member = new KbUserCategoryEntity();
				dcemUser = null;
				image = null;
			}
		} catch (Exception e) {
			logger.error("User could not be edited/added to category: " + kbCategoryEntity.getName(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "categoryMemberDialog:memberDialogMsg");
		}
	}

	public void actionAddGroup() {
		try {
			List<KbUserCategoryEntity> newCategoryMembers = kbUserLogic.addGroupToCategory(groupName, kbCategoryEntity,
					this.getAutoViewAction().getDcemAction());
			members.addAll(newCategoryMembers);
			JsfUtils.addInfoMessageToComponentId(String.format(JsfUtils.getStringSafely(resourceBundle, "category.memberDialog.success.addGroup"), groupName),
					"categoryMemberDialog:memberDialogMsg");
		} catch (Exception e) {
			logger.error("Group: " + groupName + " could not be edited/added to category: " + kbCategoryEntity.getName(), e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "categoryMemberDialog:memberDialogMsg");
		} finally {
			groupName = null;
		}
	}

	public void actionAddMember() {
		dcemUser = null;
		member = new KbUserCategoryEntity();
		editMode = false;
	}

	public void actionEditMember() {
		if (selectedMember == null || selectedMember.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "category.memberDialog.invalid.emptySelection");
			return;
		}
		if (selectedMember.size() > 1) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "category.memberDialog.invalid.tooManySelection");
			return;
		}
		try {
			member = selectedMember.get(0);
			editMode = true;
			PrimeFaces.current().executeScript("PF('addMemberDialog').show();");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			logger.error("Could not edit member", e);
		}
	}

	public void actionRemoveMember() {
		if (selectedMember == null || selectedMember.isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "category.memberDialog.invalid.emptySelection");
			return;
		}
		try {
			kbUserLogic.removeUserCategories(selectedMember, getAutoViewAction().getDcemAction());
			members.removeAll(selectedMember);
			JsfUtils.addInfoMessage(KbModule.RESOURCE_NAME, "category.memberDialog.success.removeMember");
		} catch (Exception e) {
			logger.error("Could not remove users from category: " + kbCategoryEntity.getName(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	@Override
	public void actionConfirm() throws Exception {
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbCategoryEntity = (KbCategoryEntity) this.getActionObject();
		members = kbUserLogic.getUserCategoriesByCategory(kbCategoryEntity);
		dcemUser = null;
		image = null;
	}

	public List<String> actionCompleteGroup(String name) {
		List<String> groupOfDepartmentNames = groupLogic.getCompleteGroupList(name, 30);
		if (groupOfDepartmentNames == null) {

			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "category.memberDialog.error.groupComplete"),
					"categoryMemberDialog:memberDialogMsg");
		}
		return groupOfDepartmentNames;
	}

	public String getHeight() {
		return "40em";
	}

	public String getWidth() {
		return "75em";
	}

	public void leavingDialog() {
		kbCategoryEntity = null;
		selectedMember = null;
		members = null;
		member = null;
		editMode = false;
		booleanOperator = null;
		dcemUser = null;
		image = null;
	}

	public KbCategoryEntity getKbCategoryEntity() {
		return kbCategoryEntity;
	}

	public void setKbCategoryEntity(KbCategoryEntity kbCategoryEntity) {
		this.kbCategoryEntity = kbCategoryEntity;
	}

	public List<KbUserCategoryEntity> getSelectedMember() {
		return selectedMember;
	}

	public void setSelectedMember(List<KbUserCategoryEntity> selectedMember) {
		this.selectedMember = selectedMember;
	}

	public List<KbUserCategoryEntity> getMembers() {
		if (members == null) {
			return new ArrayList<KbUserCategoryEntity>();
		}
		return members;
	}

	public void setMembers(List<KbUserCategoryEntity> members) {
		this.members = members;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public KbUserCategoryEntity getMember() {
		return member;
	}

	public void setMember(KbUserCategoryEntity member) {
		this.member = member;
	}

	public SelectItem[] getBooleanOperator() {
		if (booleanOperator == null) {
			booleanOperator = new SelectItem[] { new SelectItem(null, ""),
					new SelectItem(Boolean.TRUE, JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "yes")),
					new SelectItem(Boolean.FALSE, JsfUtils.getStringSafely(KbModule.RESOURCE_NAME, "no")) };
		}
		return booleanOperator;
	}

	public void setBooleanOperator(SelectItem[] booleanOperator) {
		this.booleanOperator = booleanOperator;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void userListener() {
		if (dcemUser != null)
			this.image = dcemUser.getPhoto();
	}

	public StreamedContent getUserPhoto() {
		try {
			if (image == null) {
				return JsfUtils.getDefaultUserImage();
			} else {
				InputStream in = new ByteArrayInputStream(image);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

}