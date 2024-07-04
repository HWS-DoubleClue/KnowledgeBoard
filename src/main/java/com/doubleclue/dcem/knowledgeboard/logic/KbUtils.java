package com.doubleclue.dcem.knowledgeboard.logic;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jsoup.Jsoup;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.knowledgeboard.entities.KbCategoryEntity_;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserCategoryEntity_;
import com.doubleclue.dcem.knowledgeboard.entities.KbUserEntity_;

public class KbUtils {

	public static String parseHtmlToString(String html) {
		return Jsoup.parse(html).text();
	}
	
	public static boolean invalidTextContent(String text) throws Exception {
		if (text == null) {
			return true;
		}
		String zeroWidthNoBreakSpace = new String("\ufeff".getBytes("UTF-16"), "UTF-16"); // Primefaces gives a weird empty string...
		String htmlReducedText = parseHtmlToString(text).trim();
		htmlReducedText = htmlReducedText.replace(zeroWidthNoBreakSpace, "");
		return htmlReducedText.isBlank();
	}

	public static Subquery<Integer> getUserAdminCategoryIds(CriteriaBuilder criteriaBuilder, DcemUser dcemUser) {
		// Create subquery to retrieve all category IDs where the User is Member of
		// Join table would result in duplicate entries
		CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
		Subquery<Integer> categoryIdSubquery = criteriaQuery.subquery(Integer.class);
		Root<KbUserCategoryEntity> userCategoryRoot = categoryIdSubquery.from(KbUserCategoryEntity.class);
		categoryIdSubquery.select(userCategoryRoot.get(KbUserCategoryEntity_.category).get(KbCategoryEntity_.id));

		Predicate userCategoryIsDisabled = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.disabled), false);
		Predicate userCategoryIsAdmin = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.admin), true);
		Predicate userCategoryId = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.kbUser).get(KbUserEntity_.id), dcemUser.getId());

		return categoryIdSubquery.where(criteriaBuilder.and(userCategoryIsAdmin, userCategoryIsDisabled, userCategoryId));
	}

	public static Subquery<Integer> getDisabledPublicUserCategoryIds(CriteriaBuilder criteriaBuilder, DcemUser dcemUser) {
		CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
		Subquery<Integer> categoryIdSubquery = criteriaQuery.subquery(Integer.class);
		Root<KbUserCategoryEntity> userCategoryRoot = categoryIdSubquery.from(KbUserCategoryEntity.class);
		categoryIdSubquery.select(userCategoryRoot.get(KbUserCategoryEntity_.category).get(KbCategoryEntity_.id));

		Predicate openCategory = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.category).get(KbCategoryEntity_.publicCategory), true);
		Predicate userCategoryIsDisabled = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.disabled), true);
		Predicate userCategoryId = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.kbUser).get(KbUserEntity_.id), dcemUser.getId());

		return categoryIdSubquery.where(criteriaBuilder.and(openCategory, userCategoryIsDisabled, userCategoryId));
	}

	public static Subquery<Integer> getPrivateAccessibleCategoryIds(CriteriaBuilder criteriaBuilder, DcemUser dcemUser) {
		CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
		Subquery<Integer> categoryIdSubquery = criteriaQuery.subquery(Integer.class);
		Root<KbUserCategoryEntity> userCategoryRoot = categoryIdSubquery.from(KbUserCategoryEntity.class);
		categoryIdSubquery.select(userCategoryRoot.get(KbUserCategoryEntity_.category).get(KbCategoryEntity_.id));

		Predicate privateCategory = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.category).get(KbCategoryEntity_.publicCategory), false);
		Predicate userCategoryIsDisabled = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.disabled), false);
		Predicate userCategoryId = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.kbUser).get(KbUserEntity_.id), dcemUser.getId());

		return categoryIdSubquery.where(criteriaBuilder.and(privateCategory, userCategoryIsDisabled, userCategoryId));
	}

	public static boolean hasActionRights(OperatorSessionBean operatorSessionBean, KbUserCategoryEntity kbUserCategoryEntity, AutoViewAction autoViewAction) {
		if (autoViewAction != null && operatorSessionBean.isPermission(autoViewAction.getDcemAction())) {
			return true;
		}
		if (kbUserCategoryEntity != null && kbUserCategoryEntity.isDisabled() == false && kbUserCategoryEntity.isAdmin() == true) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidName(String name) {
		if (name == null) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (KbConstants.SPECIAL_CHARACTERS.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}
}
