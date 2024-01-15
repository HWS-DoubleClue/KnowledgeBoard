
create table kb_categories (
dc_id integer not null auto_increment,
description varchar(111),
jpaVersion integer not null,
dc_name varchar(63) not null,
dc_public bit not null,
primary key (dc_id)
) engine=InnoDB;

create table kb_questions (
dc_id integer not null auto_increment,
creation_date datetime not null,
jpaVersion integer not null,
last_modified_date datetime not null,
number_of_replies integer not null,
question_preview varchar(255) not null,
dc_status integer not null,
dc_title varchar(111) not null,
author_id integer,
category_id integer not null,
last_modified_by_id integer,
questioncontent_id integer not null,
primary key (dc_id)
) engine=InnoDB;

create table kb_questions_tags (
question_id integer not null,
tag_id integer not null
) engine=InnoDB;

create table kb_replies (
dc_id integer not null auto_increment,
creation_date datetime not null,
highlighted bit not null,
jpaVersion integer not null,
last_modified_date datetime not null,
author_id integer,
last_modified_by_id integer,
question_id integer not null,
replycontent_id integer not null,
primary key (dc_id)
) engine=InnoDB;

create table kb_tags (
dc_id integer not null auto_increment,
description varchar(111),
jpaVersion integer not null,
dc_name varchar(63) not null,
dc_status integer not null,
category_id integer not null,
primary key (dc_id)
) engine=InnoDB;

create table kb_text_content (
dc_id integer not null auto_increment,
content longtext not null,
jpaVersion integer not null,
primary key (dc_id)
) engine=InnoDB;

create table kb_usercategory (
category_id integer not null,
user_id integer not null,
admin_in_category bit not null,
user_disabled_in_category bit not null,
following_all_tags bit not null,
hidden_in_dashboard bit not null,
jpaVersion integer not null,
primary key (category_id, user_id)
) engine=InnoDB;

create table kb_usercategory_question (
category_id integer not null,
user_id integer not null,
question_id integer not null
) engine=InnoDB;

create table kb_usercategory_tag (
category_id integer not null,
user_id integer not null,
tag_id integer not null
) engine=InnoDB;

create table kb_users (
dc_id integer not null,
user_disabled bit not null,
jpaVersion integer not null,
notification_disabled bit not null,
primary key (dc_id)
) engine=InnoDB;

alter table kb_categories
add constraint UK_KB_CATEGORIES unique (dc_name);

create index idx_kb_question_date on kb_questions (creation_date);

alter table kb_tags
add constraint UK_KB_TAGS unique (dc_name, category_id);

alter table kb_questions
add constraint FK_KB_QUESTION_AUTHOR
foreign key (author_id)
references core_user (dc_id);

alter table kb_questions
add constraint FK_KB_QUESTION_CATEGORY
foreign key (category_id)
references kb_categories (dc_id);

alter table kb_questions
add constraint FK_KB_QUESTION_CREATOR
foreign key (last_modified_by_id)
references core_user (dc_id);

alter table kb_questions
add constraint FK_KB_QUESTION_TEXTCONTENT
foreign key (questioncontent_id)
references kb_text_content (dc_id);

alter table kb_questions_tags
add constraint FK_KB_TAGS_QUESTIONS
foreign key (tag_id)
references kb_tags (dc_id);

alter table kb_questions_tags
add constraint FK_KB_QUESTIONS_TAGS
foreign key (question_id)
references kb_questions (dc_id);

alter table kb_replies
add constraint FK_KB_REPLIES_AUTHOR
foreign key (author_id)
references core_user (dc_id);

alter table kb_replies
add constraint FK_KB_REPLIES_MODIFIEDBY
foreign key (last_modified_by_id)
references core_user (dc_id);

alter table kb_replies
add constraint FK_KB_REPLIES_QUESTION
foreign key (question_id)
references kb_questions (dc_id);

alter table kb_replies
add constraint FK_KB_REPLY_TEXTCONTENT
foreign key (replycontent_id)
references kb_text_content (dc_id);

alter table kb_tags
add constraint FK_KB_TAGS_CATEGORY
foreign key (category_id)
references kb_categories (dc_id);

alter table kb_usercategory
add constraint FK7sgpt7u6jpcl99kbj0k09atdi
foreign key (category_id)
references kb_categories (dc_id);

alter table kb_usercategory
add constraint FKiucq04gy2dc3dffftr11c6ue2
foreign key (user_id)
references kb_users (dc_id);

alter table kb_usercategory_question
add constraint FK_KB_QUESTION_USERCATEGORY
foreign key (question_id)
references kb_questions (dc_id);

alter table kb_usercategory_question
add constraint FK_KB_USERCATEGORY_QUESTION
foreign key (category_id, user_id)
references kb_usercategory (category_id, user_id);

alter table kb_usercategory_tag
add constraint FK_KB_TAG_USERCATEGORY
foreign key (tag_id)
references kb_tags (dc_id);

alter table kb_usercategory_tag
add constraint FK_KB_USERCATEGORY_TAG
foreign key (category_id, user_id)
references kb_usercategory (category_id, user_id);

alter table kb_users
add constraint FK_KB_USER
foreign key (dc_id)
references core_user (dc_id);
