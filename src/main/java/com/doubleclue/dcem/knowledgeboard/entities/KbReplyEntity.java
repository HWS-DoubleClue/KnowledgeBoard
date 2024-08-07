package com.doubleclue.dcem.knowledgeboard.entities;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.knowledgeboard.logic.KbUtils;

@NamedEntityGraphs({ @NamedEntityGraph(name = KbReplyEntity.GRAPH_REPLIES_WITH_AUTHOR_AND_CONTENT, attributeNodes = {
		@NamedAttributeNode(value = "replyContent"), @NamedAttributeNode(value = "author", subgraph = "subgraph.author"), }, subgraphs = {
				@NamedSubgraph(name = "subgraph.author", attributeNodes = { @NamedAttributeNode(value = "dcemUserExt"), }) }), })

@NamedQueries({
		@NamedQuery(name = KbReplyEntity.FIND_ALL_REPLIES_CONTAINING_DCEMUSER, query = "SELECT reply FROM KbReplyEntity reply WHERE reply.author = ?1 OR reply.lastModifiedBy = ?1"),
		@NamedQuery(name = KbReplyEntity.FIND_ALL_REPLIES_FROM_QUESTION, query = "SELECT reply FROM KbReplyEntity reply WHERE reply.question = ?1 ORDER BY reply.creationDate ASC"),
		@NamedQuery(name = KbReplyEntity.REMOVE_USER_FROM_REPLY_AUTHOR, query = "UPDATE KbReplyEntity reply SET reply.author = null WHERE reply.author = ?1"),
		@NamedQuery(name = KbReplyEntity.REMOVE_USER_FROM_REPLY_LASTMODIFIED, query = "UPDATE KbReplyEntity reply SET reply.lastModifiedBy = null WHERE reply.lastModifiedBy = ?1"),
		@NamedQuery(name = KbReplyEntity.GET_YOUNGEST_REPLY_OF_QUESTION, query = "SELECT MAX(reply.creationDate) FROM KbReplyEntity reply WHERE question = ?1"),
		})

@Entity
@Table(name = "kb_replies")
public class KbReplyEntity extends EntityInterface {

	public static final String GRAPH_REPLIES_WITH_AUTHOR_AND_CONTENT = "knowledgeboard.reply.withLazyAuthorAndContent";
	public static final String FIND_ALL_REPLIES_CONTAINING_DCEMUSER = "knowledgeboard.reply.findAllRepliesContainingDcemuser";
	public static final String FIND_ALL_REPLIES_FROM_QUESTION = "knowledgeboard.reply.findAllRepliesFromQuestion";
	public static final String REMOVE_USER_FROM_REPLY_AUTHOR = "knowledgeboard.reply.removeUserFromReplyAuthor";
	public static final String REMOVE_USER_FROM_REPLY_LASTMODIFIED = "knowledgeboard.reply.removeUserFromReplyLastmodified";
	public static final String GET_YOUNGEST_REPLY_OF_QUESTION = "knowledgeboard.reply.getYoungestReplyOfQuestion";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemCompare(withoutResult = true)
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "replycontent_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_KB_REPLY_TEXTCONTENT"), nullable = false)
	private KbTextContentEntity replyContent;

	@Column(name = "highlighted", nullable = false)
	private boolean highlighted;

	@ManyToOne
	@JoinColumn(name = "question_id", foreignKey = @ForeignKey(name = "FK_KB_REPLIES_QUESTION"), nullable = false)
	private KbQuestionEntity question;

	@DcemCompare(ignore = true)
	@ManyToOne
	@DcemGui(subClass = "displayName")
	@JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "FK_KB_REPLIES_AUTHOR"))
	private DcemUser author;

	@DcemCompare(ignore = true)
	@Column(name = "creation_date", nullable = false, updatable = false)
	private LocalDateTime creationDate;

	@DcemCompare(ignore = true)
	@ManyToOne
	@DcemGui(subClass = "displayName", visible = false)
	@JoinColumn(name = "last_modified_by_id", foreignKey = @ForeignKey(name = "FK_KB_REPLIES_MODIFIEDBY"), referencedColumnName = "dc_id")
	private DcemUser lastModifiedBy;

	@DcemCompare(ignore = true)
	@Column(name = "last_modified_date", nullable = false)
	private LocalDateTime lastModifiedOn;

	@Version
	private int jpaVersion;

	public KbReplyEntity() {
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

	public KbQuestionEntity getQuestion() {
		return question;
	}

	public void setQuestion(KbQuestionEntity question) {
		this.question = question;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(LocalDateTime lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public DcemUser getAuthor() {
		return author;
	}

	public void setAuthor(DcemUser author) {
		this.author = author;
	}

	public void setId(Integer id) {
		this.id = id;
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
		KbReplyEntity other = (KbReplyEntity) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (id.equals(other.getId()) == false) {
			return false;
		}
		return true;
	}

	public KbTextContentEntity getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(KbTextContentEntity replyContent) {
		this.replyContent = replyContent;
	}

	public DcemUser getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(DcemUser lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public String getAuditInfo() {
		if (replyContent == null) {
			return "Reply entity without content";
		}
		String preview = KbUtils.parseHtmlToString(replyContent.getContent()).trim();
		preview = preview.substring(0, Math.min(255, preview.length()));
		if (question == null) {
			return "Reply with content: " + preview;
		}
		return "Reply to \"" + question.getTitle() + "\" with content: " + preview;
	}

	@Override
	public String toString() {
		return "Reply=[Preview='" + replyContent + "', question=" + question + "]";
	}
}
