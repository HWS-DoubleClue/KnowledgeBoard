package com.doubleclue.dcem.knowledgeboard.gui;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbReplyEntity;
import com.doubleclue.dcem.knowledgeboard.logic.KbModule;
import com.doubleclue.dcem.knowledgeboard.logic.KbQuestionLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbReplyLogic;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;
import com.doubleclue.dcem.knowledgeboard.subjects.KbReplyQuestionSubject;

@Named("kbReplyDialog")
@SessionScoped
public class KbReplyDialog extends DcemDialog {

	private static final long serialVersionUID = 1L;

	@Inject
	KbReplyQuestionSubject kbReplyQuestionSubject;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	KbReplyLogic kbReplyLogic;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbReplyQuestionView kbReplyQuestionView;

	private KbReplyEntity kbReplyEntity;

	@PostConstruct
	public void init() {
	}

	public boolean actionOk() throws Exception {
		if (kbReplyEntity.getReplyContent().getContent() == null || KbUtils.parseHtmlToString(kbReplyEntity.getReplyContent().getContent()).trim().isEmpty()) {
			JsfUtils.addErrorMessage(KbModule.RESOURCE_NAME, "reply.dialog.invalid.replyContent");
			return false;
		}
		kbReplyEntity.setLastModifiedOn(LocalDateTime.now());
		kbReplyEntity.setLastModifiedBy(operatorSessionBean.getDcemUser());
		kbReplyLogic.addOrUpdateReply(kbReplyEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		kbReplyEntity = (KbReplyEntity) dcemView.getActionObject();
	}

	@Override
	public void actionConfirm() throws Exception {
		kbReplyLogic.removeReply(kbReplyEntity, getAutoViewAction().getDcemAction());
	}

	@Override
	public void leavingDialog() {
		kbReplyEntity = null;
	}

	public KbReplyEntity getKbReplyEntity() {
		return kbReplyEntity;
	}

	public void setKbReplyEntity(KbReplyEntity kbReplyEntity) {
		this.kbReplyEntity = kbReplyEntity;
	}

	@Override
	public String getHeight() {
		return "85vh";
	}

	@Override
	public String getWidth() {
		return "85vw";
	}
}
