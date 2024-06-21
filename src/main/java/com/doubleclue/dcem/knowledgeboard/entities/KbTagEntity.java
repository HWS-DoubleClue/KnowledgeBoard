package com.doubleclue.dcem.knowledgeboard.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.knowledgeboard.entities.enums.KbTagStatus;

@NamedQueries({
		@NamedQuery(name = KbTagEntity.FIND_TAGS_BY_CATEGORY_ID, query = "SELECT tag FROM KbTagEntity tag WHERE tag.category.id = ?1 ORDER BY tag.name"),
		@NamedQuery(name = KbTagEntity.FIND_TAG_BY_NAME_AND_CATEGORY_ID, query = "SELECT tag FROM KbTagEntity tag WHERE tag.name = ?1 AND tag.category.id = ?2"),
		// TODO refactor name (autocomplete)
		@NamedQuery(name = KbTagEntity.FIND_TAGS_BY_NAME_AND_CATEGORY_ID, query = "SELECT tag FROM KbTagEntity tag WHERE LOWER(tag.name) LIKE ?1 AND tag.category.id = ?2"),
		@NamedQuery(name = KbTagEntity.FIND_TAGS_BY_QUESTION, query = "SELECT tag FROM KbQuestionEntity question JOIN question.tags tag WHERE question.id = ?1"),
		@NamedQuery(name = KbTagEntity.FIND_TAGS_FROM_CATEGORY_NOT_CONTAINED_IN_QUESTION_AND_AUTOCOMPLETE_NAME, query = "SELECT tag FROM KbTagEntity tag WHERE tag.category = ?1"
				+ "AND tag NOT IN (SELECT tag FROM KbQuestionEntity question JOIN question.tags tag WHERE question = ?2)"
				+ "AND LOWER(tag.name) LIKE ?3 ORDER BY tag.name ASC"), })

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "kb_tags", uniqueConstraints = @UniqueConstraint(name = "UK_KB_TAGS", columnNames = { "dc_name", "category_id" }))
public class KbTagEntity extends EntityInterface {

	public static final String FIND_TAGS_BY_CATEGORY_ID = "knowledgeboard.tag.findTagsByCategoryId";
	public static final String FIND_TAG_BY_NAME_AND_CATEGORY_ID = "knowledgeboard.tag.findTagByNameAndCategoryId";
	public static final String FIND_TAGS_BY_NAME_AND_CATEGORY_ID = "knowledgeboard.tag.findTagsByNameAndCategoryId";
	public static final String FIND_TAGS_BY_QUESTION = "knowledgeboard.tag.findTagsByQuestion";
	public static final String FIND_TAGS_FROM_CATEGORY_NOT_CONTAINED_IN_QUESTION_AND_AUTOCOMPLETE_NAME = "knowledgeboard.tag.findTagsFromCategoryNotContainedInQuestionAndAutoCompleteName";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Size(min = 1, max = 63)
	@Column(name = "dc_name", nullable = false, length = 63)
	private String name;

	@DcemGui(subClass = "name")
	@ManyToOne
	@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "FK_KB_TAGS_CATEGORY"), nullable = false, updatable = false)
	private KbCategoryEntity category;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	private KbTagStatus status = KbTagStatus.Approved;

	@DcemGui
	@Size(max = 111)
	@Column(name = "description", length = 111)
	private String description;

	@Version
	private int jpaVersion;

	public KbTagEntity() {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public KbCategoryEntity getCategory() {
		return category;
	}

	public void setCategory(KbCategoryEntity category) {
		this.category = category;
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
		KbTagEntity other = (KbTagEntity) obj;
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
		return name;
	}

	public KbTagStatus getStatus() {
		return status;
	}

	public void setStatus(KbTagStatus status) {
		this.status = status;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}
}
