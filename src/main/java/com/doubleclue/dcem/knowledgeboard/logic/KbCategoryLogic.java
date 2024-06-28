package com.doubleclue.dcem.knowledgeboard.logic;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity;

@ApplicationScoped
@Named("kbCategoryLogic")
public class KbCategoryLogic implements Serializable {

	// private static final Logger logger = LogManager.getLogger(KbCategoryLogic.class);

	private static final long serialVersionUID = 1L;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	public KbCategoryEntity getCategoryById(int categoryId) throws Exception {
		return em.find(KbCategoryEntity.class, categoryId);
	}

	@DcemTransactional
	public void addOrUpdateCategory(KbCategoryEntity categoryEntity, DcemAction dcemAction) {
		auditingLogic.addAudit(dcemAction, categoryEntity);
		if (DcemConstants.ACTION_ADD.equals(dcemAction.getAction())) {
			em.persist(categoryEntity);
		} else if (DcemConstants.ACTION_EDIT.equals(dcemAction.getAction())) {
			em.merge(categoryEntity);
		}
	}

	@DcemTransactional
	public void removeCategory(List<KbCategoryEntity> categories, DcemAction dcemAction) {
		StringBuffer auditInformation = new StringBuffer();
		for (KbCategoryEntity category : categories) {
			category = em.merge(category);
			em.remove(category);
			auditInformation.append(category.toString());
		}
		auditingLogic.addAudit(dcemAction, auditInformation.toString());
	}

	public List<KbCategoryEntity> getAllCategoriesWithLazyAttribute(String graphName) throws Exception {
		TypedQuery<KbCategoryEntity> query = em.createNamedQuery(KbCategoryEntity.FIND_ALL_CATEGORIES, KbCategoryEntity.class);
		if (graphName != null) {
			query.setHint("javax.persistence.fetchgraph", em.getEntityGraph(graphName));
		}
		return query.getResultList();
	}

	public List<KbCategoryEntity> getAdminCategoriesWithLazyAttribute(Integer userId, String graphName) throws Exception {
		TypedQuery<KbCategoryEntity> query = em.createNamedQuery(KbCategoryEntity.FIND_ADMIN_CATEGORIES_OF_USER, KbCategoryEntity.class);
		if (graphName != null) {
			query.setHint("javax.persistence.fetchgraph", em.getEntityGraph(graphName));
		}
		return query.setParameter(1, userId).getResultList();
	}

	public List<KbCategoryEntity> getAccessibleCategoriesWithLazyAttribute(Integer userId, String graphName) throws Exception {
		TypedQuery<KbCategoryEntity> query = em.createNamedQuery(KbCategoryEntity.FIND_ACCESSIBLE_CATEGORIES_OF_USER, KbCategoryEntity.class);
		if (graphName != null) {
			query.setHint("javax.persistence.fetchgraph", em.getEntityGraph(graphName));
		}
		return query.setParameter(1, userId).getResultList();
	}
}
