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

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.tasks.EmailTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.knowledgeboard.entities.KbQuestionEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbReplyEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.gui.KbReplyQuestionView;
import com.doubleclue.utils.KaraUtils;

@ApplicationScoped
@Named("kbEmailLogic")
public class KbEmailLogic {

	@Inject
	EntityManager em;

	@Inject
	private KbModule kbModule;

	@Inject
	private DcemApplicationBean dcemApplicationBean;

	@Inject
	private KbUserLogic kbUserLogic;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private KbReplyQuestionView kbReplyQuestionView;

	public void notifyNewPost(KbQuestionEntity kbQuestionEntity) throws Exception {
		notifyNewPost(kbQuestionEntity, false);
	}

	public void notifyNewPost(KbQuestionEntity kbQuestionEntity, boolean toAllMembers) throws Exception {
		if (kbModule.getModulePreferences().isTurnOffEmailNotification() == false) {
			Map<String, Object> data = new HashMap<>();
			List<DcemUser> dcemUser = new ArrayList<DcemUser>();
			data.put("title", kbQuestionEntity.getTitle());
			data.put("Author", kbQuestionEntity.getAuthor().getDisplayNameOrLoginId());
			data.put("Category", kbQuestionEntity.getCategory().getName());
			data.put("TenantManagementUrl", getUrlLink(kbQuestionEntity));
			if (toAllMembers) {
				List<KbUserCategoryEntity> categoryMembers = kbUserLogic.getUserCategoriesByCategory(kbQuestionEntity.getCategory());
				dcemUser = retrieveDcemUser(categoryMembers);
			} else {
				dcemUser = retrieveDcemUser(getTagFollower(kbQuestionEntity));
			}
			dcemUser.remove(kbQuestionEntity.getAuthor());
			taskExecutor.execute(new EmailTask(dcemUser, data, KbConstants.KB_EMAIL_QUESTION_TEMPLATE, KbConstants.KB_EMAIL_QUESTION_SUBJECT, null));
		}
	}

	public void notifyNewReply(KbQuestionEntity kbQuestionEntity, KbReplyEntity kbReplyEntity) throws Exception {
		if (kbModule.getModulePreferences().isTurnOffEmailNotification() == false) {
			Map<String, Object> data = new HashMap<>();
			data.put("title", kbQuestionEntity.getTitle());
			data.put("Author", kbReplyEntity.getAuthor().getDisplayNameOrLoginId());
			data.put("TenantManagementUrl", getUrlLink(kbQuestionEntity));
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

	public String getUrlLink(KbQuestionEntity kbQuestionEntity) throws Exception {
		String url = dcemApplicationBean.getDcemManagementUrl(null) + "/" + DcemConstants.PRE_LOGIN_PAGE + DcemConstants.URL_VIEW + kbModule.getId()
				+ DcemConstants.MODULE_VIEW_SPLITTER + kbReplyQuestionView.getSubject().getViewName();
		Map<String, String> map = new HashMap<>();
		map.put(KbConstants.QUESTION_ID, kbQuestionEntity.getId().toString());
		url = url + DcemConstants.URL_PARAMS + KaraUtils.mapToUrlParamString(map);
		return url;
	}
}
