package com.doubleclue.dcem.knowledgeboard.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.utils.DisplayModes;

@NamedEntityGraphs({ @NamedEntityGraph(name = KbCategoryEntity.GRAPH_CATEGORIES_TAGS, attributeNodes = { @NamedAttributeNode(value = "tags") }) })

@NamedQueries({ 
	@NamedQuery(name = KbCategoryEntity.FIND_ALL_CATEGORIES, query = "SELECT category FROM KbCategoryEntity category ORDER BY category.name ASC"),
	@NamedQuery(name = KbCategoryEntity.FIND_ADMIN_CATEGORIES_OF_USER, query = "SELECT DISTINCT userCategory.category FROM KbUserCategoryEntity userCategory "
			+ "WHERE (userCategory.kbUser.id = ?1) AND (userCategory.admin = True) AND (userCategory.disabled = False) "
			+ "ORDER BY userCategory.category.name ASC"),
	@NamedQuery(name = KbCategoryEntity.FIND_ACCESSIBLE_CATEGORIES_OF_USER, query = "SELECT category FROM KbCategoryEntity category "
			+ "LEFT JOIN KbUserCategoryEntity userCategory ON userCategory.category = category AND userCategory.kbUser.id = ?1 "
			+ "WHERE (category.publicCategory = True AND (userCategory IS NULL OR userCategory.disabled = False )) OR userCategory.disabled = False "
			+ "ORDER BY category.name ASC"),
	})

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "kb_categories", uniqueConstraints = @UniqueConstraint(name = "UK_KB_CATEGORIES", columnNames = { "dc_name" }))
public class KbCategoryEntity extends EntityInterface {

	public final static String GRAPH_CATEGORIES_TAGS = "knowledgeboard.category.withLazyTags";

	public final static String FIND_ALL_CATEGORIES = "knowledgeboard.category.findAllCategories";
	public final static String FIND_ACCESSIBLE_CATEGORIES_OF_USER = "knowledgeboard.category.findAccessibleCategoriesOfUser";
	public final static String FIND_ADMIN_CATEGORIES_OF_USER = "knowledgeboard.category.findAdminCategoriesOfUser";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Size(min = 2, max = 63, message = "Name: {javax.validation.constraints.Size.message}")
	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Column(name = "dc_name", nullable = false, length = 63)
	private String name;

	@DcemGui
	@Size(max = 111, message = "Description: {javax.validation.constraints.Size.message}")
	@Column(name = "description", length = 111)
	private String description;

	@DcemGui
	@Column(name = "dc_public", nullable = false)
	private boolean publicCategory;

	@DcemGui(subClass = "name", displayMode = DisplayModes.NONE, variableType = VariableType.LIST)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "category", orphanRemoval = true)
	@OrderBy("name ASC")
	private List<KbTagEntity> tags = new ArrayList<>();

	@Version
	private int jpaVersion;

	public KbCategoryEntity() {
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

	public List<KbTagEntity> getTags() {
		return tags;
	}

	public void setTags(List<KbTagEntity> tags) {
		this.tags = tags;
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
		KbCategoryEntity other = (KbCategoryEntity) obj;
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

	public boolean isPublicCategory() {
		return publicCategory;
	}

	public void setPublicCategory(boolean publicCategory) {
		this.publicCategory = publicCategory;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}
}
