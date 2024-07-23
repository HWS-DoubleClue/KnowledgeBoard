package com.doubleclue.dcem.knowledgeboard.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.gui.MfaLoginView;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbReplyEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTextContentEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbCategoryLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbConstants;
import com.doubleclue.dcem.knowledgeboard.logic.KbEmailLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbReplyLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUserLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;
import com.doubleclue.dcem.knowledgeboard.subjects.KbQuestionSubject;
import com.doubleclue.dcem.knowledgeboard.subjects.KbReplyQuestionSubject;

@SuppressWarnings("serial")
@Named("kbReplyQuestionView")
@SessionScoped
public class KbReplyQuestionView extends DcemView {

	private static final Logger logger = LogManager.getLogger(KbQuestionLogic.class);

	private static final String QUESTION_ID = "questionId=";

	@Inject
	KbModule kbModule;

	@Inject
	KbReplyQuestionSubject kbReplyQuestionSubject;

	@Inject
	KbQuestionSubject kbQuestionSubject;

	@Inject
	KbDashboardView kbDashboardView;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	KbQuestionDialog kbQuestionDialog;

	@Inject
	KbReplyDialog kbReplyDialog;

	@Inject
	KbUserLogic kbUserLogic;

	@Inject
	KbReplyLogic kbReplyLogic;

	@Inject
	KbEmailLogic kbEmailLogic;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	MfaLoginView loginView;

	@Inject
	KbQuestionLogic questionLogic;

	private AutoViewAction editQuestion;
	private AutoViewAction addReply;
	private AutoViewAction editReply;
	private AutoViewAction removeReply;

	private KbQuestionEntity kbQuestionEntity;
	private String replyText;

	@PostConstruct
	public void init() {
		subject = kbReplyQuestionSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(KbModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		editQuestion = createAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, kbQuestionDialog, KbConstants.KB_QUESTION_DIALOG, null);
		addReply = createAutoViewAction(KbConstants.KB_ADD_REPLY, resourceBundle, null, null, null);
		editReply = createAutoViewAction(KbConstants.KB_EDIT_REPLY, resourceBundle, kbReplyDialog, KbConstants.KB_REPLY_DIALOG, null);
		removeReply = createAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, kbReplyDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH, null);
	}

	@Override
	public void reload() {
		Map<String, String> mapParam = loginView.getShareUrlParams();
		loginView.setMgtUrlParams(null);
		if (mapParam != null) {
			try {
				kbQuestionEntity = new KbQuestionEntity();
				kbQuestionEntity.setId(Integer.parseInt(mapParam.get(QUESTION_ID)));
			} catch (Exception e) {
				logger.debug("Could not parse link to question id", e);
				JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "dashboard.error.questionNotFound");
				closeQuestion();
				return;
			}
		}
		if (kbQuestionEntity != null) {
			try {
				kbQuestionEntity = kbQuestionLogic.getQuestionWithLazyAttribute(kbQuestionEntity.getId(), KbQuestionEntity.GRAPH_QUESTION_TAGS_AND_CONTENT);
				if (kbQuestionEntity == null) {
					JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "dashboard.error.questionNotFound");
					closeQuestion();
					return;
				}
				if (hasViewRights(kbQuestionEntity) == false) {
					closeQuestion();
					JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "dashboard.error.noAccessFound");
					return;
				}
				List<KbReplyEntity> replies = kbReplyLogic.getRepliesByQuestion(kbQuestionEntity);
				kbQuestionEntity.setReplies(replies);

				loadLazyPhoto(kbQuestionEntity);
			} catch (Exception e) {
				logger.error("Could not get question details of question: " + kbQuestionEntity.getId(), e);
				closeQuestion();
				JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
			}
		} else {
			closeQuestion();
		}
	}

	private boolean hasViewRights(KbQuestionEntity question) throws Exception {
		boolean questionViewManager = operatorSessionBean.isPermission(new DcemAction(kbQuestionSubject, DcemConstants.ACTION_MANAGE));
		if (questionViewManager == true) {
			return true;
		}
		KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(), question.getCategory().getId());
		if (operatorUserCategory != null) {
			return operatorUserCategory.isDisabled() == false;
		}
		return question.getCategory().isPublicCategory();
	}

	private void loadLazyPhoto(KbQuestionEntity kbQuestionEntity) {
		if (kbQuestionEntity.getAuthor() != null) {
			kbQuestionEntity.getAuthor().getPhoto();
		}
		for (KbReplyEntity reply : kbQuestionEntity.getReplies()) {
			if (reply.getAuthor() != null) {
				reply.getAuthor().getPhoto();
			}
		}
	}

	@Override
	public void leavingView() {
		kbQuestionEntity = null;
		replyText = null;
	}

	public void actionCreateReply() {
		try {
			LocalDateTime localDate = LocalDateTime.now();
			KbReplyEntity kbReplyEntity = new KbReplyEntity();
			kbReplyEntity.setQuestion(kbQuestionEntity);
			kbReplyEntity.setAuthor(operatorSessionBean.getDcemUser());
			kbReplyEntity.setLastModifiedBy(operatorSessionBean.getDcemUser());
			kbReplyEntity.setReplyContent(new KbTextContentEntity(replyText));
			kbReplyEntity.setCreationDate(localDate);
			kbReplyEntity.setLastModifiedOn(localDate);
			auditingLogic.addAudit(addReply.getDcemAction(), kbReplyEntity);
			kbQuestionEntity.addReply(kbReplyEntity);
			kbQuestionEntity = kbQuestionLogic.addOrUpdateQuestion(kbQuestionEntity, addReply.getDcemAction());
			kbEmailLogic.notifyNewReply(kbQuestionEntity, kbReplyEntity);
			replyText = null;
		} catch (Exception e) {
			logger.error("User: " + operatorSessionBean.getDcemUser().getLoginId() + " could not create reply for question: " + kbQuestionEntity.getId(), e);
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "error.global");
		}
	}

	public void editQuestion() {
		List<Object> question = new ArrayList<Object>();
		question.add((Object) kbQuestionEntity);
		autoViewBean.setSelectedItems((List<Object>) question);
		setActionObject(kbQuestionEntity);
		viewNavigator.setActiveDialog(editQuestion);
	}

	public void editReply(KbReplyEntity kbReplyEntity) {
		List<Object> reply = new ArrayList<Object>();
		reply.add((Object) kbReplyEntity);
		autoViewBean.setSelectedItems((List<Object>) reply);
		setActionObject(kbReplyEntity);
		viewNavigator.setActiveDialog(editReply);
	}

	public void deleteReply(KbReplyEntity kbReplyEntity) {
		List<Object> reply = new ArrayList<Object>();
		reply.add((Object) kbReplyEntity);
		autoViewBean.setSelectedItems((List<Object>) reply);
		setActionObject(kbReplyEntity);
		viewNavigator.setActiveDialog(removeReply);
	}

	public boolean hasReplyPermission() {
		if (kbQuestionEntity.getStatus() == KbQuestionStatus.Closed) {
			return false;
		}
		try {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					kbQuestionEntity.getCategory().getId());
			return KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, addReply);
		} catch (Exception e) {
			logger.error("Could not check edit permission for user: " + operatorSessionBean.getDcemUser().getLoginId() + " and question: "
					+ kbQuestionEntity.getId(), e);
			return false;
		}

	}

	public boolean hasQuestionEditPermission() {
		try {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					kbQuestionEntity.getCategory().getId());
			if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, editQuestion) == true) {
				return true;
			}
			if (kbQuestionEntity.getAuthor() != null && kbQuestionEntity.getAuthor().equals(operatorSessionBean.getDcemUser())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Could not check edit permission for user: " + operatorSessionBean.getDcemUser().getLoginId() + " and question: "
					+ kbQuestionEntity.getId(), e);
			return false;
		}
	}

	public boolean hasReplyEditPermission(KbReplyEntity kbReplyEntity) {
		try {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					kbQuestionEntity.getCategory().getId());
			if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, editReply) == true) {
				return true;
			}
			if (kbReplyEntity.getAuthor() != null && kbReplyEntity.getAuthor().equals(operatorSessionBean.getDcemUser())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Could not check edit permission for user: " + operatorSessionBean.getDcemUser().getLoginId() + " and reply: " + kbReplyEntity.getId(),
					e);
			return false;
		}
	}

	public boolean hasReplyDeletePermission(KbReplyEntity kbReplyEntity) {
		try {
			KbUserCategoryEntity operatorUserCategory = kbUserLogic.getKbUserCategory(operatorSessionBean.getDcemUser().getId(),
					kbQuestionEntity.getCategory().getId());
			if (KbUtils.hasActionRights(operatorSessionBean, operatorUserCategory, removeReply) == true) {
				return true;
			}
			if (kbReplyEntity.getAuthor() != null && kbReplyEntity.getAuthor().equals(operatorSessionBean.getDcemUser())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Could not check edit permission for user: " + operatorSessionBean.getDcemUser().getLoginId() + " and reply: " + kbReplyEntity.getId(),
					e);
			return false;
		}
	}

	public void closeQuestion() {
		leavingView();
		viewNavigator.setActiveView(KbModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + kbDashboardView.getSubject().getViewName());
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		if (dcemUser == null) {
			return JsfUtils.getDefaultUserImage();
		}
		byte[] image = dcemUser.getPhoto();
		if (image != null) {
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public KbQuestionEntity getKbQuestionEntity() {
		return kbQuestionEntity;
	}

	public void setKbQuestionEntity(KbQuestionEntity kbQuestionEntity) {
		this.kbQuestionEntity = kbQuestionEntity;
	}

	public String getReplyText() {
		return replyText;
	}

	public void setReplyText(String replyText) {
		this.replyText = replyText;
	}

	@Override
	public Map<String, String> getShareUrlParameters() {
		if (kbQuestionEntity == null || kbQuestionEntity.getId() == null) {
			return null;
		}
		Map<String, String> map = new HashMap<>();
		map.put(QUESTION_ID, kbQuestionEntity.getId().toString());
		return map;
	}
}
