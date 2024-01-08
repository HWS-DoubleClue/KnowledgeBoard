
create table kb_categories (
dc_id int identity not null,
description varchar(111),
jpaVersion int not null,
dc_name varchar(63) not null,
dc_public bit,
primary key (dc_id)
);

create table kb_questions (
dc_id int identity not null,
creation_date datetime2 not null,
jpaVersion int not null,
last_modified_date datetime2 not null,
number_of_replies int,
question_preview varchar(255),
dc_status int not null,
dc_title varchar(111),
author_id int,
category_id int not null,
last_modified_by_id int,
questioncontent_id int not null,
primary key (dc_id)
);

create table kb_questions_tags (
question_id int not null,
tag_id int not null
);

create table kb_replies (
dc_id int identity not null,
creation_date datetime2 not null,
highlighted bit,
jpaVersion int not null,
last_modified_date datetime2 not null,
author_id int,
last_modified_by_id int,
question_id int not null,
replycontent_id int not null,
primary key (dc_id)
);

create table kb_tags (
dc_id int identity not null,
description varchar(111),
jpaVersion int not null,
dc_name varchar(63) not null,
dc_status int not null,
category_id int not null,
primary key (dc_id)
);

create table kb_text_content (
dc_id int identity not null,
content varchar(MAX) not null,
jpaVersion int not null,
primary key (dc_id)
);

create table kb_usercategory (
category_id int not null,
user_id int not null,
admin_in_category bit,
user_disabled_in_category bit,
following_all_tags bit,
hidden_in_dashboard bit,
jpaVersion int not null,
primary key (category_id, user_id)
);

create table kb_usercategory_question (
category_id int not null,
user_id int not null,
question_id int not null
);

create table kb_usercategory_tag (
category_id int not null,
user_id int not null,
tag_id int not null
);

create table kb_users (
dc_id int not null,
user_disabled bit,
jpaVersion int not null,
notification_disabled bit,
primary key (dc_id)
);

alter table kb_categories
add constraint UK_KB_CATEGORIES unique (dc_name);

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
