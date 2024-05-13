package com.doubleclue.dcem.knowledgeboard.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.IPhoto;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "kb_users")
public class KbUserEntity extends EntityInterface implements Serializable, IPhoto {

	private static final long serialVersionUID = 1L;

	@Id
	private Integer id;

	@DcemGui(name = "photo", subClass = "photo", variableType = VariableType.IMAGE)
	@Transient
	byte[] photo;

	@DcemCompare(ignore = true)
	@MapsId
	@DcemGui(subClass = "displayName", sortOrder = SortOrder.ASCENDING)
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "dc_id", foreignKey = @ForeignKey(name = "FK_KB_USER"), nullable = false, updatable = false)
	private DcemUser dcemUser;

	@DcemGui
	@Column(name = "notification_disabled", nullable = false)
	boolean notificationDisabled = true;

	@Column(name = "user_disabled", nullable = false)
	boolean disabled = false;

	@Version
	private int jpaVersion;

	public KbUserEntity() {
		// super();
	}

	public KbUserEntity(DcemUser dcemUser) {
		setDcemUser(dcemUser);
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

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
		if (dcemUser != null) {
			this.id = dcemUser.getId();
		}
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
		KbUserEntity other = (KbUserEntity) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (id.equals(other.getId()) == false) {
			return false;
		}
		return true;
	}

	public boolean isNotificationDisabled() {
		return notificationDisabled;
	}

	public void setNotificationDisabled(boolean notificationDisabled) {
		this.notificationDisabled = notificationDisabled;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public byte[] getPhoto() {
		if (dcemUser != null) {
			return dcemUser.getPhoto();
		}
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public DcemUser getDcemUserId() {
		return dcemUser;
	}

	@Override
	public String toString() {
		if (dcemUser != null) {
			return "KB-User: " + dcemUser.getDisplayName() + " (" + dcemUser.getLoginId() + ")";
		}
		return "KbUserEntity has no dcemUser";
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
