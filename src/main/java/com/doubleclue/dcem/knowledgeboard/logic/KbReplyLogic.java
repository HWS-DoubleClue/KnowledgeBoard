package com.doubleclue.dcem.knowledgeboard.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbReplyEntity;
import com.doubleclue.dcem.knowledgeboard.subjects.KbReplyQuestionSubject;

@ApplicationScoped
@Named("kbReplyLogic")
public class KbReplyLogic {

	// private static final Logger logger = LogManager.getLogger(KbReplyLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbReplyQuestionSubject kbReplyQuestionSubject;

	@DcemTransactional
	public void addOrUpdateReply(KbReplyEntity replyEntity, DcemAction dcemAction) {
		auditingLogic.addAudit(dcemAction, replyEntity);
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(replyEntity);
		} else if (dcemAction.getAction().equals(DcemConstants.ACTION_EDIT)) {
			em.merge(replyEntity);
		}
	}

	@DcemTransactional
	public void removeReply(KbReplyEntity kbReplyEntity) throws Exception {
		kbReplyEntity = em.merge(kbReplyEntity);
		KbQuestionEntity kbQuestionEntity = kbQuestionLogic.getQuestionWithOptionalAttribute(kbReplyEntity.getQuestion().getId(),
				KbQuestionEntity.GRAPH_QUESTION_REPLIES);
		kbQuestionEntity.removeReply(kbReplyEntity);
		kbQuestionLogic.updateQuestion(kbQuestionEntity);
		em.remove(kbReplyEntity);
		String replyContent = KbUtils.parseHtmlToString(kbReplyEntity.toString());
		auditingLogic.addAudit(new DcemAction(kbReplyQuestionSubject, DcemConstants.ACTION_DELETE),
				replyContent.substring(0, Math.min(255, replyContent.length())));
	}

	@DcemTransactional
	public void removeUserFromReplies(DcemUser dcemUser) {
		TypedQuery<KbReplyEntity> query = em.createNamedQuery(KbReplyEntity.FIND_ALL_REPLIES_CONTAINING_DCEMUSER, KbReplyEntity.class);
		query.setParameter(1, dcemUser);
		List<KbReplyEntity> replies = query.getResultList();
		for (KbReplyEntity reply : replies) {
			if (dcemUser.equals(reply.getAuthor())) {
				reply.setAuthor(null);
			}
			if (dcemUser.equals(reply.getLastModifiedBy())) {
				reply.setLastModifiedBy(null);
			}
		}
	}
}
