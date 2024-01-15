
create table kb_categories (
dc_id number(10,0) generated as identity,
description varchar2(111 char),
jpaVersion number(10,0) not null,
dc_name varchar2(63 char) not null,
dc_public number(1,0),
primary key (dc_id)
);

create table kb_questions (
dc_id number(10,0) generated as identity,
creation_date timestamp not null,
jpaVersion number(10,0) not null,
last_modified_date timestamp not null,
number_of_replies number(10,0) not null,
question_preview varchar2(255 char),
dc_status number(10,0) not null,
dc_title varchar2(111 char),
author_id number(10,0),
category_id number(10,0) not null,
last_modified_by_id number(10,0),
questioncontent_id number(10,0) not null,
primary key (dc_id)
);

create table kb_questions_tags (
question_id number(10,0) not null,
tag_id number(10,0) not null
);

create table kb_replies (
dc_id number(10,0) generated as identity,
creation_date timestamp not null,
highlighted number(1,0) not null,
jpaVersion number(10,0) not null,
last_modified_date timestamp not null,
author_id number(10,0),
last_modified_by_id number(10,0),
question_id number(10,0) not null,
replycontent_id number(10,0) not null,
primary key (dc_id)
);

create table kb_tags (
dc_id number(10,0) generated as identity,
description varchar2(111 char),
jpaVersion number(10,0) not null,
dc_name varchar2(63 char) not null,
dc_status number(10,0) not null,
category_id number(10,0) not null,
primary key (dc_id)
);

create table kb_text_content (
dc_id number(10,0) generated as identity,
content long not null,
jpaVersion number(10,0) not null,
primary key (dc_id)
);

create table kb_usercategory (
category_id number(10,0) not null,
user_id number(10,0) not null,
admin_in_category number(1,0) not null,
user_disabled_in_category number(1,0) not null,
following_all_tags number(1,0) not null,
hidden_in_dashboard number(1,0) not null,
jpaVersion number(10,0) not null,
primary key (category_id, user_id)
);

create table kb_usercategory_question (
category_id number(10,0) not null,
user_id number(10,0) not null,
question_id number(10,0) not null
);

create table kb_usercategory_tag (
category_id number(10,0) not null,
user_id number(10,0) not null,
tag_id number(10,0) not null
);

create table kb_users (
dc_id number(10,0) not null,
user_disabled number(1,0) not null,
jpaVersion number(10,0) not null,
notification_disabled number(1,0) not null,
primary key (dc_id)
);

alter table kb_categories
add constraint UK_KB_CATEGORIES unique (dc_name);

create index idx_kb_question_date on kb_questions (creation_date);

alter table kb_tags
add constraint UK_KB_TAGS unique (dc_name, category_id);

alter table kb_questions
add constraint FK_KB_QUESTION_AUTHOR
foreign key (author_id)
references core_user;

alter table kb_questions
add constraint FK_KB_QUESTION_CATEGORY
foreign key (category_id)
references kb_categories;

alter table kb_questions
add constraint FK_KB_QUESTION_CREATOR
foreign key (last_modified_by_id)
references core_user;

alter table kb_questions
add constraint FK_KB_QUESTION_TEXTCONTENT
foreign key (questioncontent_id)
references kb_text_content;

alter table kb_questions_tags
add constraint FK_KB_TAGS_QUESTIONS
foreign key (tag_id)
references kb_tags;

alter table kb_questions_tags
add constraint FK_KB_QUESTIONS_TAGS
foreign key (question_id)
references kb_questions;

alter table kb_replies
add constraint FK_KB_REPLIES_AUTHOR
foreign key (author_id)
references core_user;

alter table kb_replies
add constraint FK_KB_REPLIES_MODIFIEDBY
foreign key (last_modified_by_id)
references core_user;

alter table kb_replies
add constraint FK_KB_REPLIES_QUESTION
foreign key (question_id)
references kb_questions;

alter table kb_replies
add constraint FK_KB_REPLY_TEXTCONTENT
foreign key (replycontent_id)
references kb_text_content;

alter table kb_tags
add constraint FK_KB_TAGS_CATEGORY
foreign key (category_id)
references kb_categories;

alter table kb_usercategory
add constraint FK7sgpt7u6jpcl99kbj0k09atdi
foreign key (category_id)
references kb_categories;

alter table kb_usercategory
add constraint FKiucq04gy2dc3dffftr11c6ue2
foreign key (user_id)
references kb_users;

alter table kb_usercategory_question
add constraint FK_KB_QUESTION_USERCATEGORY
foreign key (question_id)
references kb_questions;

alter table kb_usercategory_question
add constraint FK_KB_USERCATEGORY_QUESTION
foreign key (category_id, user_id)
references kb_usercategory;

alter table kb_usercategory_tag
add constraint FK_KB_TAG_USERCATEGORY
foreign key (tag_id)
references kb_tags;

alter table kb_usercategory_tag
add constraint FK_KB_USERCATEGORY_TAG
foreign key (category_id, user_id)
references kb_usercategory;

alter table kb_users
add constraint FK_KB_USER
foreign key (dc_id)
references core_user;
