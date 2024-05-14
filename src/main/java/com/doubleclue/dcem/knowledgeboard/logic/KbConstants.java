package com.doubleclue.dcem.knowledgeboard.logic;

public class KbConstants {

	// View & Dialog links
	public static final String KB_CATEGORY_DIALOG = "/modules/knowledgeboard/kbCategoryDialog.xhtml";
	public static final String KB_CATEGORY_MEMBER_DIALOG = "/modules/knowledgeboard/kbCategoryUserDialog.xhtml";
	public static final String KB_TAG_DIALOG = "/modules/knowledgeboard/kbTagDialog.xhtml";
	public static final String KB_TAG_FOLLOWER_DIALOG = "/modules/knowledgeboard/kbTagFollowerDialog.xhtml";
	public static final String KB_TAG_QUESTION_DIALOG = "/modules/knowledgeboard/kbTagQuestionDialog.xhtml";
	public static final String KB_QUESTION_DIALOG = "/modules/knowledgeboard/kbQuestionDialog.xhtml";
	public static final String KB_QUESTION_FOLLOWER_DIALOG = "/modules/knowledgeboard/kbQuestionFollowerDialog.xhtml";
	public static final String KB_QUESTION_TAG_DIALOG = "/modules/knowledgeboard/kbQuestionTagDialog.xhtml";
	public static final String KB_USER_DIALOG = "/modules/knowledgeboard/kbUserDialog.xhtml";
	public static final String KB_USER_CREATED_QUESTIONS_DIALOG = "/modules/knowledgeboard/kbUserCreatedQuestionsDialog.xhtml";
	public static final String KB_USER_FOLLOWED_QUESTIONS_DIALOG = "/modules/knowledgeboard/kbUserFollowedQuestionsDialog.xhtml";
	public static final String KB_USER_FOLLOWED_TAGS_DIALOG = "/modules/knowledgeboard/kbUserFollowedTagsDialog.xhtml";
	public static final String KB_NOTIFICATION_DIALOG = "/modules/knowledgeboard/kbNotificationDialog.xhtml";
	public static final String KB_DASHBOARD_VIEW = "/modules/knowledgeboard/kbDashboard.xhtml";
	public static final String KB_REPLY_DIALOG = "/modules/knowledgeboard/kbReplyDialog.xhtml";

	// Template and Text Resource Names
	public static final String KB_EMAIL_REPLY_TEMPLATE = "kb.newReply.Notification";
	public static final String KB_EMAIL_REPLY_SUBJECT = "kb.newReply.EmailSubject";
	public static final String KB_EMAIL_QUESTION_TEMPLATE = "kb.newQuestion.Notification";
	public static final String KB_EMAIL_QUESTION_SUBJECT = "kb.newQuestion.EmailSubject";

	// Action Names
	public static final String KB_SHOW_FOLLOWER = "showFollower";
	public static final String KB_SHOW_QUESTIONS = "showQuestions";
	public static final String KB_SHOW_TAGS = "showTags";
	public static final String KB_SHOW_QUESTION_FOLLOWER = "showQuestionFollower";
	public static final String KB_SHOW_REPLYVIEW = "showReplyView";
	public static final String KB_MANAGE_CATEGORY_MEMBER = "manageCategoryMember";
	public static final String KB_MANAGE_NOTIFICATIONS = "manageNotifications";
	public static final String KB_SHOW_TAG_FOLLOWER = "showTagFollower";
	public static final String KB_NEW_POST = "newPost";
	public static final String KB_SHOW_CREATED_QUESTIONS = "showCreatedQuestions";
	public static final String KB_SHOW_FOLLOWED_QUESTIONS = "showFollowedQuestions";
	public static final String KB_SHOW_FOLLOWED_TAGS = "showFollowedTags";
	public static final String KB_ADD_REPLY = "addReply";
	public static final String KB_EDIT_REPLY = "editReply";
	public static final String KB_EDIT_QUESTION = "editQuestion";

	// EL-Method Names
	public static final String KB_EL_METHOD_QUESTIONVIEW_GOTO_REPLYVIEW = "#{kbQuestionView.openQuestionDetails()}";

}
