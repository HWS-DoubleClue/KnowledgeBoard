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
		} else {
			em.merge(replyEntity);
		}
	}

	public List<KbReplyEntity> getRepliesByQuestion(KbQuestionEntity kbQuestionEntity) {
		TypedQuery<KbReplyEntity> query = em.createNamedQuery(KbReplyEntity.FIND_ALL_REPLIES_FROM_QUESTION, KbReplyEntity.class);
		query.setParameter(1, kbQuestionEntity);
		query.setHint("javax.persistence.fetchgraph", em.getEntityGraph(KbReplyEntity.GRAPH_REPLIES_WITH_AUTHOR_AND_CONTENT));
		return query.getResultList();
	}

	@DcemTransactional
	public void removeReply(KbReplyEntity kbReplyEntity, DcemAction dcemAction) throws Exception {
		kbReplyEntity = em.merge(kbReplyEntity);
		KbQuestionEntity kbQuestionEntity = kbQuestionLogic.getQuestionWithOptionalAttribute(kbReplyEntity.getQuestion().getId(),
				KbQuestionEntity.GRAPH_QUESTION_REPLIES);
		kbQuestionEntity.removeReply(kbReplyEntity);
		em.merge(kbQuestionEntity);
		em.remove(kbReplyEntity);
		auditingLogic.addAudit(dcemAction, kbReplyEntity.toString());
	}

	@DcemTransactional
	public void removeUserFromReplies(DcemUser dcemUser) {
		em.createQuery("UPDATE KbReplyEntity reply SET reply.author = null WHERE reply.author = ?1").setParameter(1, dcemUser).executeUpdate();
		em.createQuery("UPDATE KbReplyEntity reply SET reply.lastModifiedBy = null WHERE reply.lastModifiedBy = ?1").setParameter(1, dcemUser).executeUpdate();
	}
}
