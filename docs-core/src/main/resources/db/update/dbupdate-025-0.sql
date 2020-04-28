alter table T_USER add column USE_COMPANY_C varchar(36);
alter table T_USER add column USE_TELEGRAMUSERNAME_C varchar(50);
alter table T_USER add column USE_TELEGRAMCHATID_C varchar(50);
create memory table T_COMPANY ( COM_ID_C varchar(36) not null, COM_NAME_C varchar(150) not null, COM_CREATEDATE_D datetime not null, COM_DELETEDATE_D datetime, primary key (COM_ID_C));
alter table T_USER add constraint FK_USE_COMPANY_C foreign key (USE_COMPANY_C) references T_COMPANY (COM_ID_C) on delete restrict on update restrict;
insert into T_ROLE(ROL_ID_C, ROL_NAME_C, ROL_CREATEDATE_D) values('companyadmin', 'CompanyAdmin', NOW());
insert into T_COMPANY(COM_ID_C, COM_NAME_C, COM_CREATEDATE_D) values('specterai', 'Specter AI Pte Ltd', NOW());
update T_CONFIG set CFG_VALUE_C = '25' where CFG_ID_C = 'DB_VERSION';