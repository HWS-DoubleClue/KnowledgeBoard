package com.doubleclue.dcem.knowledgeboard.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.compare.CompareException;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbTagEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryKey;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity;

@ApplicationScoped
@Named("kbUserLogic")
public class KbUserLogic {

	private static final Logger logger = LogManager.getLogger(KbQuestionLogic.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	KbQuestionLogic kbQuestionLogic;

	@Inject
	KbReplyLogic kbReplyLogic;
	
	@Inject
	GroupLogic groupLogic;

	// ### KbUser

	public KbUserEntity getKbUser(Integer userId) throws Exception {
		return em.find(KbUserEntity.class, userId);
	}

	@DcemTransactional
	public void addOrUpdateKbUser(KbUserEntity kbUserEntity, DcemAction dcemAction) {
		kbUserEntity.setId(kbUserEntity.getDcemUser().getId());
		auditingLogic.addAudit(dcemAction, kbUserEntity);
		if (DcemConstants.ACTION_ADD.equals(dcemAction.getAction())) {
			em.persist(kbUserEntity);
		} else {
			em.merge(kbUserEntity);
		}
	}

	@DcemTransactional
	public KbUserEntity getOrCreateKbUser(DcemUser dcemUser) {
		KbUserEntity kbUser = em.find(KbUserEntity.class, dcemUser.getId());
		if (kbUser == null) {
			kbUser = new KbUserEntity(dcemUser);
			kbUser.setId(dcemUser.getId());
			em.persist(kbUser);
		}
		return kbUser;
	}

	@DcemTransactional
	public void removeKbUsers(List<KbUserEntity> kbUsers, DcemAction dcemAction) throws Exception {
		List<KbUserCategoryEntity> userCategories = getUserCategoriesByKbUsers(kbUsers);
		for (KbUserCategoryEntity userCategory : userCategories) {
			em.remove(userCategory);
		}

		StringBuffer auditInformation = new StringBuffer();
		for (KbUserEntity kbUser : kbUsers) {
			kbUser = em.merge(kbUser);
			em.remove(kbUser);
			auditInformation.append(kbUser.getDcemUser().getLoginId() + "; ");
		}
		auditingLogic.addAudit(dcemAction, auditInformation.toString());
	}

	// ### KbUserCategory

	public KbUserCategoryEntity getKbUserCategory(int userId, int categoryId) {
		return em.find(KbUserCategoryEntity.class, new KbUserCategoryKey(userId, categoryId));
	}
	
	public void detachUserCategory(KbUserCategoryEntity userCategory) {
		em.detach(userCategory);
	}

	public KbUserCategoryEntity getKbUserCategoryByLoginIdAndCategoryId(String dcemLoginId, int categoryId) throws Exception {
		KbUserCategoryEntity result;
		try {
			TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_MEMBER_OF_CATEGORY_BY_LOGINID_AND_CATEGORY_ID,
					KbUserCategoryEntity.class);
			query.setParameter(1, dcemLoginId);
			query.setParameter(2, categoryId);
			result = query.getSingleResult();
		} catch (NoResultException e) {
			result = null;
		}
		return result;
	}

	@DcemTransactional
	public KbUserCategoryEntity getOrCreateKbUserCategory(DcemUser dcemUser, KbCategoryEntity category) throws Exception {
		KbUserCategoryEntity result;
		result = getKbUserCategory(dcemUser.getId(), category.getId());
		if (result == null) {
			KbUserEntity kbUser = getOrCreateKbUser(dcemUser);
			category = em.find(KbCategoryEntity.class, category.getId());
			result = new KbUserCategoryEntity(kbUser, category);
			em.persist(result);
		}
		return result;
	}

	@DcemTransactional
	public void addUserCategory(KbUserCategoryEntity kbUserCategoryEntity) {
		em.persist(kbUserCategoryEntity);
	}

	@DcemTransactional
	public KbUserCategoryEntity updateUserCategory(KbUserCategoryEntity kbUserCategoryEntity, DcemAction dcemAction) {
		KbUserCategoryEntity oldEntity = getKbUserCategory(kbUserCategoryEntity.getKbUser().getId(), kbUserCategoryEntity.getCategory().getId());
		try {
			String changes = CompareUtils.compareObjects(oldEntity, kbUserCategoryEntity);
			auditingLogic.addAudit(dcemAction, changes);
		} catch (CompareException e) {
			logger.warn("Could not compare UserCategory", e);
		}
		return em.merge(kbUserCategoryEntity);
	}
	
	@DcemTransactional
	public KbUserCategoryEntity updateUserCategory(KbUserCategoryEntity kbUserCategoryEntity) {
		return em.merge(kbUserCategoryEntity);
	}

	@DcemTransactional
	public void updateUserCategories(List<KbUserCategoryEntity> selectedFollower, DcemAction dcemAction) {
		for (KbUserCategoryEntity follower : selectedFollower) {
			em.merge(follower);
		}
	}

	public List<KbUserCategoryEntity> getUserCategoriesByCategory(KbCategoryEntity kbCategoryEntity) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_MEMBERS_OF_CATEGORY, KbUserCategoryEntity.class);
		query.setParameter(1, kbCategoryEntity);
		return query.getResultList();
	}

	@DcemTransactional
	public void removeUserCategories(List<KbUserCategoryEntity> kbUserCategories, DcemAction dcemAction) {
		StringBuffer auditInformation = new StringBuffer();
		for (KbUserCategoryEntity kbUserCategory : kbUserCategories) {
			kbUserCategory = em.merge(kbUserCategory);
			em.remove(kbUserCategory);
			auditInformation.append(kbUserCategory.getKbUser().getDcemUser().getLoginId() + " (" + kbUserCategory.getCategory().getName() + "); ");
		}
		if (dcemAction != null) {
			auditingLogic.addAudit(dcemAction, auditInformation.toString());
		}
	}

	public List<KbUserCategoryEntity> getCategoryMembersNotFollowingTagFilterByLoginId(String loginId, KbTagEntity kbTagEntity, int max) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_FILTERED_CATEGORYMEMBER_LIST_BY_TAG, KbUserCategoryEntity.class);
		query.setParameter(1, kbTagEntity.getCategory());
		query.setParameter(2, kbTagEntity);
		query.setParameter(3, "%" + loginId.toLowerCase().trim() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public List<KbUserCategoryEntity> getFollowerOfQuestion(KbQuestionEntity kbQuestionEntity) throws Exception {
		return getFollowerOfQuestions(Arrays.asList(kbQuestionEntity));
	}

	public List<KbUserCategoryEntity> getFollowerOfQuestions(List<KbQuestionEntity> questions) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_QUESTIONS, KbUserCategoryEntity.class);
		query.setParameter(1, questions);
		return query.getResultList();
	}

	public List<KbUserCategoryEntity> getFollowerOfTag(KbTagEntity kbTagEntity) throws Exception {
		return getFollowerOfTags(Arrays.asList(kbTagEntity));
	}

	public List<KbUserCategoryEntity> getFollowerOfTags(List<KbTagEntity> tags) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_TAGS, KbUserCategoryEntity.class);
		query.setParameter(1, tags);
		return query.getResultList();
	}

	public List<KbUserCategoryEntity> getCategoryMembersNotFollowingQuestionFilterByLoginId(String loginId, KbQuestionEntity kbQuestionEntity, int max)
			throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_FILTERED_CATEGORYMEMBER_LIST_BY_QUESTION,
				KbUserCategoryEntity.class);
		query.setParameter(1, kbQuestionEntity.getCategory());
		query.setParameter(2, kbQuestionEntity);
		query.setParameter(3, "%" + loginId.toLowerCase().trim() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}
	
	public List<KbUserCategoryEntity> getUserCategoriesByUserIdWithOptionalAttribute(Integer userId, String graphName) {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_CATEGORIES_OF_MEMBER, KbUserCategoryEntity.class);
		if (graphName != null) {
			query.setHint("javax.persistence.fetchgraph", em.getEntityGraph(graphName));
		}
		return query.setParameter(1, userId).getResultList();
	}

	public boolean isUserAdminAndNotDisabled(Integer userId) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_USERCATEGORIES_WHERE_USER_IS_NOT_DISABLED_ADMIN,
				KbUserCategoryEntity.class);
		query.setParameter(1, userId);
		query.setMaxResults(1);
		return query.getResultList().isEmpty() == false;
	}

	private List<KbUserCategoryEntity> getUserCategoriesByKbUsers(List<KbUserEntity> kbUsers) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_USERCATEGORIES_BY_KBUSERS, KbUserCategoryEntity.class);
		query.setParameter(1, kbUsers);
		return query.getResultList();
	}

	@DcemTransactional
	public void createOrUpdateUserCategories(Collection<KbUserCategoryEntity> userCategories, KbUserEntity kbUserEntity) {
		kbUserEntity = em.merge(kbUserEntity);
		for (KbUserCategoryEntity userCategory : userCategories) {
			if (userCategory.getKbUser() == null) {
				userCategory.setKbUser(kbUserEntity);
				userCategory.setCategory(em.find(KbCategoryEntity.class, userCategory.getCategory().getId()));
				em.persist(userCategory);
			} else {
				em.merge(userCategory);
			}
		}
	}

	@DcemTransactional
	public void deleteUserFromKb(DcemUser dcemUser) {
		KbUserEntity kbUser = em.find(KbUserEntity.class, dcemUser.getId());
		if (kbUser != null) {
			removeUserCategories(getUserCategoriesByUserIdWithOptionalAttribute(dcemUser.getId(), null), null);
			em.remove(kbUser);
		}
		kbReplyLogic.removeUserFromReplies(dcemUser);
		kbQuestionLogic.removeUserFromQuestions(dcemUser);
	}
	
	@DcemTransactional
	public List<KbUserCategoryEntity> addGroupToCategory(List<DcemUser> newDcemUserMembers, KbCategoryEntity kbCategoryEntity) throws Exception {
		List<KbUserCategoryEntity> newCategoryMembers = new ArrayList<KbUserCategoryEntity>();
		for (DcemUser newMember : newDcemUserMembers) {
			KbUserCategoryEntity newCategoryMember = getOrCreateKbUserCategory(newMember, kbCategoryEntity);
			newCategoryMembers.add(newCategoryMember);
		}
		return newCategoryMembers;
	}

	//#### gets dcemUser
	
//	public List<DcemUser> getDcemUserOfGroupNotInCategory(KbCategoryEntity kbCategoryEntity, List<DcemUser> groupMembers) {
//		TypedQuery<DcemUser> query = em.createNamedQuery(KbUserCategoryEntity.FIND_DCEMUSER_NOT_IN_CATEGORY_BY_GROUPNAME,
//				DcemUser.class);
//		query.setParameter(1, kbCategoryEntity);
//		query.setParameter(2, groupMembers);
//		return query.getResultList();
//	}



}
