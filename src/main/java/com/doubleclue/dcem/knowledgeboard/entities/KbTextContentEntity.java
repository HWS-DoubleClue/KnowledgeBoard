package com.doubleclue.dcem.knowledgeboard.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.doubleclue.dcem.core.entities.EntityInterface;

@Entity
@Table(name = "kb_text_content")

public class KbTextContentEntity extends EntityInterface {

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "content", nullable = false, length = 1024 * 1024)
	private String content;

	@Version
	private int jpaVersion;

	public KbTextContentEntity() {
	}

	public KbTextContentEntity(String content) {
		this.content = content;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
		KbTextContentEntity other = (KbTextContentEntity) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (id.equals(other.getId()) == false) {
			return false;
		}
		return true;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}
}
