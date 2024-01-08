package com.doubleclue.dcem.knowledgeboard.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.tasks.EmailTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbReplyEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;

@ApplicationScoped
@Named("kbEmailLogic")
public class KbEmailLogic {

	@Inject
	EntityManager em;

	@Inject
	KbModule kbModule;
	
	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	TaskExecutor taskExecutor;

	public void notifyNewPost(KbQuestionEntity kbQuestionEntity) throws Exception {
		if (kbModule.getModulePreferences().isTurnOffEmailNotification() == false) {
			Map<String, Object> data = new HashMap<>();
			data.put("title", kbQuestionEntity.getTitle());
			data.put("Author", kbQuestionEntity.getAuthor().getDisplayNameOrLoginId());
			data.put("Category", kbQuestionEntity.getCategory().getName());
			data.put("TenantManagementUrl", dcemApplicationBean.getDcemManagementUrl(null));
			List<DcemUser> dcemUser = retrieveDcemUser(getTagFollower(kbQuestionEntity));
			dcemUser.remove(kbQuestionEntity.getAuthor());
			taskExecutor.execute(new EmailTask(dcemUser, data, KbConstants.KB_EMAIL_QUESTION_TEMPLATE, KbConstants.KB_EMAIL_QUESTION_SUBJECT, null));
		}
	}

	public void notifyNewReply(KbQuestionEntity kbQuestionEntity, KbReplyEntity kbReplyEntity) throws Exception {
		if (kbModule.getModulePreferences().isTurnOffEmailNotification() == false) {
			Map<String, Object> data = new HashMap<>();
			data.put("title", kbQuestionEntity.getTitle());
			data.put("Author", kbReplyEntity.getAuthor().getDisplayNameOrLoginId());
			data.put("TenantManagementUrl", dcemApplicationBean.getDcemManagementUrl(null));
			List<DcemUser> dcemUser = retrieveDcemUser(getQuestionFollower(kbQuestionEntity));
			dcemUser.remove(kbReplyEntity.getAuthor());
			taskExecutor.execute(new EmailTask(dcemUser, data, KbConstants.KB_EMAIL_REPLY_TEMPLATE, KbConstants.KB_EMAIL_REPLY_SUBJECT, null));
		}
	}

	private List<DcemUser> retrieveDcemUser(List<KbUserCategoryEntity> userCategories) {
		List<DcemUser> dcemUser = new ArrayList<DcemUser>(userCategories.size());
		for (KbUserCategoryEntity userCategory : userCategories) {
			dcemUser.add(userCategory.getKbUser().getDcemUser());
		}
		return dcemUser;
	}

	private List<KbUserCategoryEntity> getQuestionFollower(KbQuestionEntity kbQuestionEntity) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_QUESTION_FOR_EMAILTASK,
				KbUserCategoryEntity.class);
		query.setParameter(1, kbQuestionEntity);
		return query.getResultList();
	}

	private List<KbUserCategoryEntity> getTagFollower(KbQuestionEntity kbQuestionEntity) throws Exception {
		TypedQuery<KbUserCategoryEntity> query = em.createNamedQuery(KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_TAGS_FOR_EMAILTASK, KbUserCategoryEntity.class);
		query.setParameter(1, kbQuestionEntity.getCategory());
		query.setParameter(2, kbQuestionEntity.getTags());
		return query.getResultList();
	}

}
