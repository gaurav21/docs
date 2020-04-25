alter table T_USER add column USE_INBOXENABLED_B bit default 0;
alter table T_USER add column USE_INBOXUSERNAME_C varchar(50);
alter table T_USER add column USE_INBOXPASSWORD_C varchar(60);
alter table T_USER add column USE_INBOXPORT_C varchar(4);
alter table T_USER add column USE_INBOXHOSTNAME_C varchar(50);
alter table T_USER add column USE_TAGS_C varchar(1000);
alter table T_USER add column USE_COMPANY_C int;