package com.doubleclue.dcem.knowledgeboard.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;

@ApplicationScoped
@Named("kbTagLogic")
public class KbTagLogic {

	// private static final Logger logger = LogManager.getLogger(KbTagLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbCategoryLogic kbCategoryLogic;

	@Inject
	KbUserLogic kbUserLogic;

	public KbTagEntity getTagById(Integer tagId) throws Exception {
		return em.find(KbTagEntity.class, tagId);
	}

	@DcemTransactional
	public void addOrUpdateTag(KbTagEntity tagEntity, DcemAction dcemAction) {
		auditingLogic.addAudit(dcemAction, tagEntity);
		if (tagEntity.getId() == null) {
			em.persist(tagEntity);
		} else {
			em.merge(tagEntity);
		}
	}

	@DcemTransactional
	public void removeTags(List<KbTagEntity> tags, DcemAction dcemAction) throws Exception {
		List<KbUserCategoryEntity> followers = kbUserLogic.getFollowerOfTags(tags);
		for (KbUserCategoryEntity follower : followers) {
			follower.getFollowedTags().removeAll(tags);
		}

		List<KbQuestionEntity> questions = kbQuestionLogic.getAllQuestionsContainingOneOfTags(tags);
		for (KbQuestionEntity question : questions) {
			question.getTags().removeAll(tags);
		}

		StringBuffer auditInformation = new StringBuffer();
		for (KbTagEntity tag : tags) {
			tag = em.merge(tag);
			em.remove(tag);
			auditInformation.append(tag.toString());
		}
		auditingLogic.addAudit(dcemAction, auditInformation.toString());
	}

	@DcemTransactional
	public void mergeTags(DcemAction dcemAction, KbTagEntity mainTag, KbTagEntity mergingTag) throws Exception {
		replaceTagInQuestions(mainTag, mergingTag);
		replaceFollowerOfTag(mainTag, mergingTag);
		em.remove(mergingTag);
		auditingLogic.addAudit(dcemAction, String.format("'%s' -> '%s'", mergingTag.getName() + " (" + mergingTag.getCategory().getName() + ")",
				mainTag.getName() + " (" + mainTag.getCategory().getName() + ")"));
	}

	private void replaceTagInQuestions(KbTagEntity mainTag, KbTagEntity mergingTag) throws Exception {
		List<KbQuestionEntity> questions = kbQuestionLogic.getAllQuestionsContainingTag(mergingTag);
		for (KbQuestionEntity question : questions) {
			question.getTags().remove(mergingTag);
			if (question.getTags().contains(mainTag) == false) {
				question.getTags().add(mainTag);
			}
		}
	}

	private void replaceFollowerOfTag(KbTagEntity mainTag, KbTagEntity mergingTag) throws Exception {
		List<KbUserCategoryEntity> followers = kbUserLogic.getFollowerOfTag(mergingTag);
		for (KbUserCategoryEntity follower : followers) {
			follower.getFollowedTags().remove(mergingTag);
			if (follower.getFollowedTags().contains(mainTag) == false) {
				follower.getFollowedTags().add(mainTag);
			}
		}
	}

	public List<KbTagEntity> getTagsByCategoryId(int categoryId) throws Exception {
		TypedQuery<KbTagEntity> query = em.createNamedQuery(KbTagEntity.FIND_TAGS_BY_CATEGORY_ID, KbTagEntity.class);
		query.setParameter(1, categoryId);
		return query.getResultList();
	}

	public List<KbTagEntity> getTagsByNameAndCategoryId(String tagName, Integer categoryId, int max) throws Exception {
		TypedQuery<KbTagEntity> query = em.createNamedQuery(KbTagEntity.FIND_TAGS_BY_NAME_AND_CATEGORY_ID, KbTagEntity.class);
		query.setParameter(1, "%" + tagName.toLowerCase().trim() + "%");
		query.setParameter(2, categoryId);
		query.setMaxResults(max);
		return query.getResultList();
	}

	public List<KbTagEntity> getTagsNotInQuestionFilterByName(String search, KbQuestionEntity kbQuestionEntity, int max) throws Exception {
		TypedQuery<KbTagEntity> query = em.createNamedQuery(KbTagEntity.FIND_TAGS_FROM_CATEGORY_NOT_CONTAINED_IN_QUESTION_AND_AUTOCOMPLETE_NAME,
				KbTagEntity.class);
		query.setParameter(1, kbQuestionEntity.getCategory());
		query.setParameter(2, kbQuestionEntity);
		query.setParameter(3, "%" + search.toLowerCase().trim() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public KbTagEntity getTagByNameAndCategoryId(String tagName, Integer categoryId) throws Exception {
		try {
			TypedQuery<KbTagEntity> query = em.createNamedQuery(KbTagEntity.FIND_TAG_BY_NAME_AND_CATEGORY_ID, KbTagEntity.class);
			query.setParameter(1, tagName);
			query.setParameter(2, categoryId);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
