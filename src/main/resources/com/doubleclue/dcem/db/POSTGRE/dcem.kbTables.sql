
create table kb_categories (
dc_id  serial not null,
description varchar(111),
jpaVersion int4 not null,
dc_name varchar(63) not null,
dc_public boolean,
primary key (dc_id)
);

create table kb_questions (
dc_id  serial not null,
creation_date timestamp not null,
jpaVersion int4 not null,
last_modified_date timestamp not null,
number_of_replies int4,
question_preview varchar(255),
dc_status int4 not null,
dc_title varchar(111),
author_id int4,
category_id int4 not null,
last_modified_by_id int4,
questioncontent_id int4 not null,
primary key (dc_id)
);

create table kb_questions_tags (
question_id int4 not null,
tag_id int4 not null
);

create table kb_replies (
dc_id  serial not null,
creation_date timestamp not null,
highlighted boolean,
jpaVersion int4 not null,
last_modified_date timestamp not null,
author_id int4,
last_modified_by_id int4,
question_id int4 not null,
replycontent_id int4 not null,
primary key (dc_id)
);

create table kb_tags (
dc_id  serial not null,
description varchar(111),
jpaVersion int4 not null,
dc_name varchar(63) not null,
dc_status int4 not null,
category_id int4 not null,
primary key (dc_id)
);

create table kb_text_content (
dc_id  serial not null,
content varchar(5242880) not null,
jpaVersion int4 not null,
primary key (dc_id)
);

create table kb_usercategory (
category_id int4 not null,
user_id int4 not null,
admin_in_category boolean,
user_disabled_in_category boolean,
following_all_tags boolean,
hidden_in_dashboard boolean,
jpaVersion int4 not null,
primary key (category_id, user_id)
);

create table kb_usercategory_question (
category_id int4 not null,
user_id int4 not null,
question_id int4 not null
);

create table kb_usercategory_tag (
category_id int4 not null,
user_id int4 not null,
tag_id int4 not null
);

create table kb_users (
dc_id int4 not null,
user_disabled boolean,
jpaVersion int4 not null,
notification_disabled boolean,
primary key (dc_id)
);

alter table if exists kb_categories
add constraint UK_KB_CATEGORIES unique (dc_name);

alter table if exists kb_tags
add constraint UK_KB_TAGS unique (dc_name, category_id);

alter table if exists kb_questions
add constraint FK_KB_QUESTION_AUTHOR
foreign key (author_id)
references core_user;

alter table if exists kb_questions
add constraint FK_KB_QUESTION_CATEGORY
foreign key (category_id)
references kb_categories;

alter table if exists kb_questions
add constraint FK_KB_QUESTION_CREATOR
foreign key (last_modified_by_id)
references core_user;

alter table if exists kb_questions
add constraint FK_KB_QUESTION_TEXTCONTENT
foreign key (questioncontent_id)
references kb_text_content;

alter table if exists kb_questions_tags
add constraint FK_KB_TAGS_QUESTIONS
foreign key (tag_id)
references kb_tags;

alter table if exists kb_questions_tags
add constraint FK_KB_QUESTIONS_TAGS
foreign key (question_id)
references kb_questions;

alter table if exists kb_replies
add constraint FK_KB_REPLIES_AUTHOR
foreign key (author_id)
references core_user;

alter table if exists kb_replies
add constraint FK_KB_REPLIES_MODIFIEDBY
foreign key (last_modified_by_id)
references core_user;

alter table if exists kb_replies
add constraint FK_KB_REPLIES_QUESTION
foreign key (question_id)
references kb_questions;

alter table if exists kb_replies
add constraint FK_KB_REPLY_TEXTCONTENT
foreign key (replycontent_id)
references kb_text_content;

alter table if exists kb_tags
add constraint FK_KB_TAGS_CATEGORY
foreign key (category_id)
references kb_categories;

alter table if exists kb_usercategory
add constraint FK7sgpt7u6jpcl99kbj0k09atdi
foreign key (category_id)
references kb_categories;

alter table if exists kb_usercategory
add constraint FKiucq04gy2dc3dffftr11c6ue2
foreign key (user_id)
references kb_users;

alter table if exists kb_usercategory_question
add constraint FK_KB_QUESTION_USERCATEGORY
foreign key (question_id)
references kb_questions;

alter table if exists kb_usercategory_question
add constraint FK_KB_USERCATEGORY_QUESTION
foreign key (category_id, user_id)
references kb_usercategory;

alter table if exists kb_usercategory_tag
add constraint FK_KB_TAG_USERCATEGORY
foreign key (tag_id)
references kb_tags;

alter table if exists kb_usercategory_tag
add constraint FK_KB_USERCATEGORY_TAG
foreign key (category_id, user_id)
references kb_usercategory;

alter table if exists kb_users
add constraint FK_KB_USER
foreign key (dc_id)
references core_user;
