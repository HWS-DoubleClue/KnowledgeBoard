package com.doubleclue.dcem.knowledgeboard.entities;

import java.io.Serializable;

public class KbUserCategoryKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer kbUser;

	private Integer category;

	public KbUserCategoryKey() {
		super();
	}

	public KbUserCategoryKey(Integer kbUser, Integer category) {
		super();
		this.kbUser = kbUser;
		this.category = category;
	}

	public KbUserCategoryKey(Integer kbUser, KbCategoryEntity kbCategoryEntity) {
		super();
		this.kbUser = kbUser;
		this.category = 0;
		if (kbCategoryEntity != null) {
			category = kbCategoryEntity.getId();
		}
	}

	public KbUserCategoryKey(KbUserEntity kbUserEntity, Integer category) {
		super();
		this.kbUser = 0;
		this.category = category;
		if (kbUser != null) {
			kbUser = kbUserEntity.getId();
		}
	}

	public KbUserCategoryKey(KbUserEntity kbUserEntity, KbCategoryEntity kbCategoryEntity) {
		super();
		this.kbUser = 0;
		this.category = 0;
		if (kbCategoryEntity != null) {
			category = kbCategoryEntity.getId();
		}
		if (kbUserEntity != null) {
			kbUser = kbUserEntity.getId();
		}
	}

	public Integer getKbUser() {
		return kbUser;
	}

	public void setKbUser(Integer kbUser) {
		this.kbUser = kbUser;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		return prime * result + ((kbUser == null) ? 0 : kbUser.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KbUserCategoryKey other = (KbUserCategoryKey) obj;
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

}
