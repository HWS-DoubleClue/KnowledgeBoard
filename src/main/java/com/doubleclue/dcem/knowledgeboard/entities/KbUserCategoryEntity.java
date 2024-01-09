package com.doubleclue.dcem.knowledgeboard.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedEntityGraphs({
	@NamedEntityGraph(name = KbUserCategoryEntity.GRAPH_USER_FOLLOWED_QUESTIONS, attributeNodes = {
		@NamedAttributeNode(value = "followedQuestions", subgraph = "subgraph.questionAuthor"),},
			subgraphs = {
					@NamedSubgraph(name = "subgraph.questionAuthor", attributeNodes = { 
						@NamedAttributeNode(value = "author") }),
		}),
	@NamedEntityGraph(name = KbUserCategoryEntity.GRAPH_USER_FOLLOWED_TAGS, attributeNodes = {
		@NamedAttributeNode(value = "followedTags")}),
})

@NamedQueries({
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_MEMBERS_OF_CATEGORY, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.category = ?1 ORDER BY userCategory.kbUser.dcemUser.loginId ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_CATEGORIES_OF_MEMBER, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.kbUser.id = ?1 ORDER BY userCategory.category.name ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_MEMBER_OF_CATEGORY_BY_LOGINID_AND_CATEGORY_ID, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.kbUser.dcemUser.loginId = ?1 AND userCategory.category.id = ?2"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_FILTERED_CATEGORYMEMBER_LIST_BY_TAG, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.category = ?1 AND ?2 NOT MEMBER OF userCategory.followedTags AND LOWER(userCategory.kbUser.dcemUser.loginId) LIKE ?3 ORDER BY userCategory.kbUser.dcemUser.loginId ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_FILTERED_CATEGORYMEMBER_LIST_BY_QUESTION, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.category = ?1 AND ?2 NOT MEMBER OF userCategory.followedQuestions AND LOWER(userCategory.kbUser.dcemUser.loginId) LIKE ?3 ORDER BY userCategory.kbUser.dcemUser.loginId ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_QUESTIONS, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "JOIN FETCH userCategory.kbUser kbUser JOIN FETCH kbUser.dcemUser dcemUser Join userCategory.followedQuestions question WHERE question IN ?1 ORDER BY dcemUser.loginId ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_TAGS, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "JOIN FETCH userCategory.kbUser kbUser JOIN FETCH kbUser.dcemUser dcemUser JOIN userCategory.followedTags tag WHERE tag IN ?1 ORDER BY dcemUser.loginId ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_USERCATEGORIES_WHERE_USER_IS_NOT_DISABLED_ADMIN, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.kbUser.dcemUser.id = ?1 AND userCategory.admin = True AND userCategory.disabled = False"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_USERCATEGORIES_BY_KBUSERS, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "WHERE userCategory.kbUser IN ?1 ORDER BY userCategory.category.name ASC"),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_QUESTION_FOR_EMAILTASK, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "JOIN userCategory.kbUser kbUser "
				+ "WHERE ?1 MEMBER OF userCategory.followedQuestions AND kbUser.notificationDisabled = False AND userCategory.disabled = False "),
		@NamedQuery(name = KbUserCategoryEntity.FIND_ALL_FOLLOWER_OF_TAGS_FOR_EMAILTASK, query = "SELECT userCategory FROM KbUserCategoryEntity userCategory "
				+ "JOIN userCategory.kbUser kbUser JOIN userCategory.category category LEFT JOIN userCategory.followedTags tag "
				+ "WHERE category = ?1 AND (userCategory.followingAllTags = True OR tag IN ?2) AND kbUser.notificationDisabled = False AND userCategory.disabled = False "),
//		@NamedQuery(name = KbUserCategoryEntity.FIND_DCEMUSER_NOT_IN_CATEGORY_BY_GROUPNAME, query = "SELECT dcemUser FROM DcemUser dcemUser LEFT JOIN KbUserCategoryEntity userCategory ON dcemUser.id = userCategory.kbUser.id AND userCategory.category = ?1 " +
//                "WHERE userCategory.category.id IS NULL AND dcemUser IN ?2"),

})

@Entity
@IdClass(KbUserCategoryKey.class)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "kb_usercategory")
public class KbUserCategoryEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public final static String GRAPH_USER_FOLLOWED_QUESTIONS = "knowledgeboard.usercategory.withLazyFollowedQuestions";
	public final static String GRAPH_USER_FOLLOWED_TAGS = "knowledgeboard.usercategory.withLazyFollowedTags";

	public final static String FIND_ALL_MEMBERS_OF_CATEGORY = "knowledgeboard.usercategory.findAllMembersOfCategory";
	public final static String FIND_ALL_CATEGORIES_OF_MEMBER = "knowledgeboard.usercategory.findAllCategoriesOfMember";
	public final static String FIND_MEMBER_OF_CATEGORY_BY_LOGINID_AND_CATEGORY_ID = "knowledgeboard.usercategory.findMemberOfCategoryByLoginIdAndCategoryId";
	public final static String FIND_FILTERED_CATEGORYMEMBER_LIST_BY_TAG = "knowledgeboard.usercategory.findMemberOfCategoryNotFollowingTagByLoginIdAndCategoryId";
	public final static String FIND_FILTERED_CATEGORYMEMBER_LIST_BY_QUESTION = "knowledgeboard.usercategory.findMemberOfCategoryNotFollowingQuestionByLoginIdAndCategoryId";
	public final static String FIND_ALL_FOLLOWER_OF_QUESTIONS = "knowledgeboard.usercategory.findAllUsersFollowingQuestions";
	public final static String FIND_ALL_FOLLOWER_OF_TAGS = "knowledgeboard.usercategory.findAllUsersFollowingTags";
	public final static String FIND_ALL_USERCATEGORIES_WHERE_USER_IS_NOT_DISABLED_ADMIN = "knowledgeboard.usercategory.findAllUsercategoriesWhereUserIsNotDisabledAdmin";
	public final static String FIND_ALL_USERCATEGORIES_BY_KBUSERS = "knowledgeboard.usercategory.findAllUsercategoriesByKbUsers";
	public final static String FIND_ALL_FOLLOWER_OF_QUESTION_FOR_EMAILTASK = "knowledgeboard.usercategory.findAllUsersFollowingQuestionForEmailtask";
	public final static String FIND_ALL_FOLLOWER_OF_TAGS_FOR_EMAILTASK = "knowledgeboard.usercategory.findAllUsersFollowingTagsForEmailtask";
//	public final static String FIND_DCEMUSER_NOT_IN_CATEGORY_BY_GROUPNAME = "knowledgeboard.usercategory.findDcemUserNotInCategoryByGroupname";

	@Id
	@ManyToOne
	@JoinColumn(name = "user_id", updatable = false, nullable = false)
	private KbUserEntity kbUser;

	@Id
	@ManyToOne
	@JoinColumn(name = "category_id", updatable = false, nullable = false)
	private KbCategoryEntity category;

	@Column(name = "admin_in_category", nullable = false)
	private boolean admin = false;

	@Column(name = "following_all_tags", nullable = false)
	private boolean followingAllTags = false;

	@Column(name = "hidden_in_dashboard", nullable = false)
	private boolean hiddenInDashboard = false;

	@Column(name = "user_disabled_in_category", nullable = false)
	private boolean disabled = false;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "kb_usercategory_tag", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
			@JoinColumn(name = "category_id", referencedColumnName = "category_id") }, inverseJoinColumns = @JoinColumn(name = "tag_id"), foreignKey = @ForeignKey(name = "FK_KB_USERCATEGORY_TAG"), inverseForeignKey = @ForeignKey(name = "FK_KB_TAG_USERCATEGORY"))
	private List<KbTagEntity> followedTags;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "kb_usercategory_question", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
			@JoinColumn(name = "category_id", referencedColumnName = "category_id") }, inverseJoinColumns = @JoinColumn(name = "question_id"), foreignKey = @ForeignKey(name = "FK_KB_USERCATEGORY_QUESTION"), inverseForeignKey = @ForeignKey(name = "FK_KB_QUESTION_USERCATEGORY"))
	private List<KbQuestionEntity> followedQuestions;

	@Version
	private int jpaVersion;

	public KbUserCategoryEntity() {
	}

	public KbUserCategoryEntity(KbUserEntity kbUser, KbCategoryEntity category) {
		this.kbUser = kbUser;
		this.category = category;
	}

	public KbUserEntity getKbUser() {
		return kbUser;
	}

	public void setKbUser(KbUserEntity kbUser) {
		this.kbUser = kbUser;
	}

	public KbCategoryEntity getCategory() {
		return category;
	}

	public void setCategory(KbCategoryEntity category) {
		this.category = category;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, kbUser);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KbUserCategoryEntity other = (KbUserCategoryEntity) obj;
		if (kbUser != null) {
			if (kbUser.equals(other.getKbUser()) == false) {
				return false;
			}
		} else if (other.getKbUser() != null) {
			return false;
		}
		if (category != null) {
			if (category.equals(other.getCategory()) == false) {
				return false;
			}
		} else if (other.getCategory() != null) {
			return false;
		}
		return true;
	}

	public boolean isFollowingAllTags() {
		return followingAllTags;
	}

	public void setFollowingAllTags(boolean followingAllTags) {
		this.followingAllTags = followingAllTags;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public List<KbTagEntity> getFollowedTags() {
		if (followedTags == null) {
			followedTags = new ArrayList<KbTagEntity>();
		}
		return followedTags;
	}

	public void setFollowedTags(List<KbTagEntity> followedTags) {
		this.followedTags = followedTags;
	}

	public List<KbQuestionEntity> getFollowedQuestions() {
		if (followedQuestions == null) {
			followedQuestions = new ArrayList<KbQuestionEntity>();
		}
		return followedQuestions;
	}

	public void setFollowedQuestions(List<KbQuestionEntity> followedQuestions) {
		this.followedQuestions = followedQuestions;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public boolean isHiddenInDashboard() {
		return hiddenInDashboard;
	}

	public void setHiddenInDashboard(boolean hiddenInDashboard) {
		this.hiddenInDashboard = hiddenInDashboard;
	}
}
