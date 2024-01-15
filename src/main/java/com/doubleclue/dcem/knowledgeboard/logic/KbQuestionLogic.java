package com.doubleclue.dcem.knowledgeboard.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;

@ApplicationScoped
@Named("kbQuestionLogic")
public class KbQuestionLogic {

	// private static final Logger logger = LogManager.getLogger(KbQuestionLogic.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	KbUserLogic kbUserLogic;

	public KbQuestionEntity getQuestionById(int id) throws Exception {
		return em.find(KbQuestionEntity.class, id);
	}

	@DcemTransactional
	public KbQuestionEntity updateQuestion(KbQuestionEntity questionEntity) {
		return em.merge(questionEntity);
	}

	@DcemTransactional
	public void updateQuestions(List<KbQuestionEntity> selectedQuestions) {
		for (KbQuestionEntity question : selectedQuestions) {
			em.merge(question);
		}
	}

	@DcemTransactional
	public void addOrUpdateQuestion(KbQuestionEntity questionEntity, DcemAction dcemAction) {
		// auditingLogic.addAudit(dcemAction, questionEntity)
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(KbConstants.KB_NEW_POST)) {
			em.persist(questionEntity);
		} else if (dcemAction.getAction().equals(DcemConstants.ACTION_EDIT)) {
			em.merge(questionEntity);
		}
	}

	public List<KbQuestionEntity> getAllQuestionsContainingOneOfTags(List<KbTagEntity> tags) throws Exception {
		TypedQuery<KbQuestionEntity> query = em.createNamedQuery(KbQuestionEntity.FIND_ALL_QUESTIONS_CONTAINING_TAGS, KbQuestionEntity.class);
		query.setParameter(1, tags);
		return query.getResultList();
	}

	public List<KbQuestionEntity> getQuestionsNotContainingTagAndFilterTitleBy(String search, KbTagEntity kbTagEntity, int max) throws Exception {
		TypedQuery<KbQuestionEntity> query = em.createNamedQuery(KbQuestionEntity.FIND_ALL_QUESTIONS_NOT_CONTAINING_TAG_AND_AUTOCOMPLETE_TITLE,
				KbQuestionEntity.class);
		query.setParameter(1, kbTagEntity.getCategory());
		query.setParameter(2, kbTagEntity);
		query.setParameter(3, "%" + search.toLowerCase().trim() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	@DcemTransactional
	public void deleteQuestions(List<KbQuestionEntity> questions, DcemAction dcemAction) throws Exception {
		List<KbUserCategoryEntity> followers = kbUserLogic.getFollowerOfQuestions(questions);
		for (KbUserCategoryEntity follower : followers) {
			follower.getFollowedQuestions().removeAll(questions);
		}

		StringBuffer auditInformation = new StringBuffer();
		for (KbQuestionEntity question : questions) {
			question = em.merge(question);
			em.remove(question);
			auditInformation.append(question.toString());
		}
		auditingLogic.addAudit(dcemAction, auditInformation.toString());
	}

	public KbQuestionEntity getQuestionWithOptionalAttribute(Integer questionId, String graphName) throws Exception {
		Map<String, Object> properties = new HashMap<>();
		if (graphName != null) {
			EntityGraph<?> entityGraph = em.getEntityGraph(graphName);
			properties.put("javax.persistence.fetchgraph", entityGraph);
			properties.put("org.hibernate.cacheable", false);
			properties.put("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		}
		return em.find(KbQuestionEntity.class, questionId, properties);
	}

	public List<KbQuestionEntity> getQuestionsCreatedBy(Integer authorId) throws Exception {
		TypedQuery<KbQuestionEntity> query = em.createNamedQuery(KbQuestionEntity.FIND_ALL_QUESTIONS_CREATED_BY, KbQuestionEntity.class);
		query.setParameter(1, authorId);
		return query.getResultList();
	}

	public List<KbQuestionEntity> getQuestionsPaginatedAndFiltered(int first, int pageSize, String searchString, int userId) throws Exception {
		// NOTE: If not done in two separate queries, Hibernate will fetch ALL questions
		// and will paginate in memory!
		TypedQuery<Integer> queryQuestionIds;
		if (searchString == null || searchString.isEmpty()) {
			queryQuestionIds = em.createNamedQuery(KbQuestionEntity.FIND_ALL_DASHBOARD_QUESTIONS, Integer.class);
		} else {
			queryQuestionIds = em.createNamedQuery(KbQuestionEntity.FIND_ALL_DASHBOARD_QUESTIONS_FILTERED, Integer.class);
			queryQuestionIds.setParameter(2, "%" + searchString + "%");
		}
		queryQuestionIds.setParameter(1, userId);
		queryQuestionIds.setFirstResult(first);
		queryQuestionIds.setMaxResults(pageSize);
		List<Integer> questionIds = queryQuestionIds.getResultList();

		EntityGraph<?> entityGraph = em.getEntityGraph(KbQuestionEntity.GRAPH_QUESTION_TAGS);
		TypedQuery<KbQuestionEntity> queryQuestionEntities = em.createNamedQuery(KbQuestionEntity.FIND_QUESTIONS_BY_IDS, KbQuestionEntity.class);
		queryQuestionEntities.setParameter(1, questionIds);
		queryQuestionEntities.setHint("javax.persistence.fetchgraph", entityGraph);
		queryQuestionEntities.setHint("org.hibernate.cacheable", false);
		queryQuestionEntities.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		return queryQuestionEntities.getResultList();
	}

	public int getQuestionCountContaining(String searchString, int userId) throws Exception {
		TypedQuery<Long> query;
		if (searchString == null || searchString.isEmpty()) {
			query = em.createNamedQuery(KbQuestionEntity.FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS, Long.class);
		} else {
			query = em.createNamedQuery(KbQuestionEntity.FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS_FILTERED, Long.class);
			query.setParameter(2, "%" + searchString + "%");
		}
		query.setParameter(1, userId);
		return query.getSingleResult().intValue();
	}

	@DcemTransactional
	public void removeUserFromQuestions(DcemUser dcemUser) {
		TypedQuery<KbQuestionEntity> query = em.createNamedQuery(KbQuestionEntity.FIND_ALL_QUESTIONS_CONTAINING_DCEMUSER, KbQuestionEntity.class);
		query.setParameter(1, dcemUser);
		List<KbQuestionEntity> questions = query.getResultList();
		for (KbQuestionEntity question : questions) {
			if (dcemUser.equals(question.getAuthor())) {
				question.setAuthor(null);
			}
			if (dcemUser.equals(question.getLastModifiedBy())) {
				question.setLastModifiedBy(null);
			}
		}
	}
}
