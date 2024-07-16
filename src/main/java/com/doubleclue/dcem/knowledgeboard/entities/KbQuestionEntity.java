package com.doubleclue.dcem.knowledgeboard.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbQuestionStatus;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@NamedEntityGraphs({
		@NamedEntityGraph(name = KbQuestionEntity.GRAPH_QUESTION_TAGS_AND_CONTENT, attributeNodes = { @NamedAttributeNode(value = "questionContent"),
				@NamedAttributeNode(value = "category"), @NamedAttributeNode(value = "tags"),
				@NamedAttributeNode(value = "author", subgraph = "subgraph.author"), }, subgraphs = {
						@NamedSubgraph(name = "subgraph.author", attributeNodes = { @NamedAttributeNode(value = "dcemUserExt"), }) }),
		@NamedEntityGraph(name = KbQuestionEntity.GRAPH_QUESTION_TAGS, attributeNodes = { @NamedAttributeNode(value = "tags"),
				@NamedAttributeNode(value = "category"), }),
		@NamedEntityGraph(name = KbQuestionEntity.GRAPH_QUESTION_REPLIES, attributeNodes = { @NamedAttributeNode(value = "replies"),
				@NamedAttributeNode(value = "category"), }), })

@NamedQueries({
		@NamedQuery(name = KbQuestionEntity.FIND_QUESTIONS_BY_IDS, query = "SELECT DISTINCT question FROM KbQuestionEntity question WHERE question.id in ?1 ORDER BY question.creationDate DESC"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_QUESTIONS_CONTAINING_TAGS, query = "SELECT DISTINCT question FROM KbQuestionEntity question JOIN question.tags tag WHERE tag in ?1 ORDER BY question.creationDate DESC"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_QUESTIONS_NOT_CONTAINING_TAG_AND_AUTOCOMPLETE_TITLE, query = "SELECT question FROM KbQuestionEntity question WHERE question.category = ?1 AND ?2 NOT MEMBER OF question.tags AND LOWER(question.title) LIKE ?3 ORDER BY question.title ASC"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_QUESTIONS_CREATED_BY, query = "SELECT question FROM KbQuestionEntity question WHERE question.author.id = ?1 ORDER BY question.creationDate DESC"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_DASHBOARD_QUESTIONS, query = "SELECT question.id FROM KbQuestionEntity question "
				+ "LEFT JOIN KbUserCategoryEntity userCategory ON userCategory.category = question.category AND userCategory.kbUser.id = ?1 "
				+ "WHERE (question.category.publicCategory = True AND (userCategory IS NULL OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False))) "
				+ "OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False) " + "ORDER BY question.creationDate DESC"),
		@NamedQuery(name = KbQuestionEntity.FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS, query = "SELECT COUNT(question) FROM KbQuestionEntity question "
				+ "LEFT JOIN KbUserCategoryEntity userCategory ON userCategory.category = question.category AND userCategory.kbUser.id = ?1 "
				+ "WHERE (question.category.publicCategory = True AND (userCategory IS NULL OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False))) "
				+ "OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False)"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_DASHBOARD_QUESTIONS_FILTERED, query = "SELECT question.id FROM KbQuestionEntity question "
				+ "LEFT JOIN KbUserCategoryEntity userCategory ON userCategory.category = question.category AND userCategory.kbUser.id = ?1 "
				+ "LEFT JOIN question.tags tag "
				+ "WHERE ((question.category.publicCategory = True AND (userCategory IS NULL OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False))) "
				+ "OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False)) "
				+ "AND (LOWER(question.title) LIKE(?2) OR LOWER(question.questionContent.content) LIKE LOWER(?2) OR LOWER(tag.name) LIKE LOWER(?2)) "
				+ "ORDER BY question.creationDate DESC"),
		@NamedQuery(name = KbQuestionEntity.FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS_FILTERED, query = "SELECT COUNT(question) FROM KbQuestionEntity question "
				+ "LEFT JOIN KbUserCategoryEntity userCategory ON userCategory.category = question.category AND userCategory.kbUser.id = ?1 "
				+ "LEFT JOIN question.tags tag "
				+ "WHERE ((question.category.publicCategory = True AND (userCategory IS NULL OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False))) "
				+ "OR (userCategory.disabled = False AND userCategory.hiddenInDashboard = False)) "
				+ "AND (LOWER(question.title) LIKE(?2) OR LOWER(question.questionContent.content) LIKE LOWER(?2) OR LOWER(tag.name) LIKE LOWER(?2))"),
		@NamedQuery(name = KbQuestionEntity.FIND_ALL_QUESTIONS_CONTAINING_DCEMUSER, query = "SELECT question FROM KbQuestionEntity question WHERE question.author = ?1 OR question.lastModifiedBy = ?1"), })

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "kb_questions", indexes = { @Index(name = "idx_kb_question_date", columnList = "creation_date") })
public class KbQuestionEntity extends EntityInterface {

	public static final String GRAPH_QUESTION_TAGS_AND_CONTENT = "knowledgeboard.question.withLazyTagsAndContent";
	public static final String GRAPH_QUESTION_TAGS = "knowledgeboard.question.withLazyTagsAndCategories";
	public static final String GRAPH_QUESTION_REPLIES = "knowledgeboard.question.withLazyReplies";

	public static final String FIND_QUESTIONS_BY_IDS = "knowledgeboard.question.findQuestionsByIds";
	public static final String FIND_ALL_QUESTIONS_CONTAINING_TAGS = "knowledgeboard.question.findAllQuestionsContainingTags";
	public static final String FIND_ALL_QUESTIONS_NOT_CONTAINING_TAG_AND_AUTOCOMPLETE_TITLE = "knowledgeboard.question.findAllQuestionsNotContainingTagAndAutocompleteTitle";
	public static final String FIND_ALL_QUESTIONS_CREATED_BY = "knowledgeboard.question.findAllQuestionsCreatedBy";
	public static final String FIND_ALL_DASHBOARD_QUESTIONS = "knowledgeboard.question.findAllDashboardQuestions";
	public static final String FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS = "knowledgeboard.question.findCountOfAllDashboardQuestions";
	public static final String FIND_ALL_DASHBOARD_QUESTIONS_FILTERED = "knowledgeboard.question.findAllDashboardQuestionsFiltered";
	public static final String FIND_COUNT_OF_ALL_DASHBOARD_QUESTIONS_FILTERED = "knowledgeboard.question.findCountOfAllDashboardQuestionsFiltered";
	public static final String FIND_ALL_QUESTIONS_CONTAINING_DCEMUSER = "knowledgeboard.question.findAllQuestionsContainingDcemuser";

	@Id
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Size(max = 128)
	@Column(name = "dc_title", length = 128, nullable = false)
	private String title;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	private KbQuestionStatus status;

	@DcemCompare(ignore = true)
	@OrderBy("creationDate ASC")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "question", cascade = CascadeType.ALL)
	private List<KbReplyEntity> replies;

	@DcemGui
	@Column(name = "number_of_replies", nullable = false)
	private int numberOfReplies = 0;

	@DcemCompare(ignore = true)
	@DcemGui(subClass = "displayName")
	@ManyToOne
	@JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "FK_KB_QUESTION_AUTHOR"))
	private DcemUser author;

	@DcemCompare(ignore = true)
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "questioncontent_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_KB_QUESTION_TEXTCONTENT"), nullable = false)
	private KbTextContentEntity questionContent;

	@Column(name = "question_preview", length = 255, nullable = false)
	private String questionPreview;

	@DcemCompare(ignore = true)
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	@Column(name = "creation_date", updatable = false, nullable = false)
	private LocalDateTime creationDate;

	@DcemGui
	@ManyToOne
	@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "FK_KB_QUESTION_CATEGORY"), nullable = false)
	private KbCategoryEntity category;

	@DcemGui(visible = false, variableType = VariableType.LIST)
	@ManyToMany(fetch = FetchType.LAZY)
	@OrderBy("name ASC")
	@JoinTable(name = "kb_questions_tags", joinColumns = @JoinColumn(name = "question_id"), foreignKey = @ForeignKey(name = "FK_KB_QUESTIONS_TAGS"), inverseJoinColumns = @JoinColumn(name = "tag_id"), inverseForeignKey = @ForeignKey(name = "FK_KB_TAGS_QUESTIONS"))
	private List<KbTagEntity> tags;

	@DcemCompare(ignore = true)
	@DcemGui(subClass = "displayName", visible = false)
	@ManyToOne
	@JoinColumn(name = "last_modified_by_id", foreignKey = @ForeignKey(name = "FK_KB_QUESTION_CREATOR"), referencedColumnName = "dc_id")
	private DcemUser lastModifiedBy;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false)
	@Column(name = "last_modified_date", nullable = false)
	private LocalDateTime lastModifiedOn;

	@Version
	private int jpaVersion;

	public KbQuestionEntity() {
	}

	public void addTag(KbTagEntity tag) {
		if (tags == null) {
			tags = new ArrayList<>();
		}
		tags.add(tag);

	}

	public void removeTag(KbTagEntity tag) {
		if (tag == null || tags == null) {
			return;
		}
		tags.remove(tag);
	}

	public void addReply(KbReplyEntity reply) {
		if (reply == null) {
			return;
		}
		if (replies == null) {
			replies = new ArrayList<>();
		}
		replies.add(reply);
		numberOfReplies = replies.size();
	}

	public void removeReply(KbReplyEntity reply) {
		if (replies == null || reply == null) {
			return;
		}
		replies.remove(reply);
		numberOfReplies = replies.size();
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			id = null;
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public KbQuestionStatus getStatus() {
		return status;
	}

	public void setStatus(KbQuestionStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public DcemUser getAuthor() {
		return author;
	}

	public void setAuthor(DcemUser author) {
		this.author = author;
	}

	public KbCategoryEntity getCategory() {
		return category;
	}

	public void setCategory(KbCategoryEntity category) {
		this.category = category;
	}

	public List<KbTagEntity> getTags() { 
		if (tags == null) {
			tags = new ArrayList<KbTagEntity>();
		}
		return tags;
	}

	public void setTags(List<KbTagEntity> tags) {
		this.tags = tags;
	}

	public List<KbReplyEntity> getReplies() {
		if (replies == null) {
			replies = new ArrayList<KbReplyEntity>();
		}
		return replies;
	}

	public void setReplies(List<KbReplyEntity> replies) {
		if (replies != null) {
			numberOfReplies = replies.size();
		}
		this.replies = replies;
	}

	public int getNumberOfReplies() {
		return numberOfReplies;
	}

	public void setNumberOfReplies(int numberOfReplies) {
		this.numberOfReplies = numberOfReplies;
	}

	public LocalDateTime getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(LocalDateTime lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public DcemUser getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(DcemUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public KbTextContentEntity getQuestionContent() {
		return questionContent;
	}

	public void setQuestionContent(KbTextContentEntity questionContent) {
		this.questionContent = questionContent;
		String preview = KbUtils.parseHtmlToString(questionContent.getContent()).trim();
		this.questionPreview = preview.substring(0, Math.min(255, preview.length()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return prime * result + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KbQuestionEntity other = (KbQuestionEntity) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (id.equals(other.getId()) == false) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("Questiontitle: \'%s\' in Category: \'%s\'", title, category);
	}

	public String getQuestionPreview() {
		return questionPreview;
	}

	public void setQuestionPreview(String questionPreview) {
		this.questionPreview = questionPreview;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}
}
