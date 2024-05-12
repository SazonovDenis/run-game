create sequence g_ConfirmState start with 1000 increment by 1
;
create sequence g_LinkType start with 1000 increment by 1
;
create sequence g_Link start with 1000 increment by 1
;


create table ConfirmState (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
;
create table LinkType (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
;
create table Link (
  id bigint not null,
  usrFrom bigint,
  usrTo bigint,
  linkType bigint,
  dbeg timestamp,
  dend timestamp,
  confirmState bigint
)
;


insert into ConfirmState (id,text,code,deleted)
values (1001,'Ожидает','waiting',null)
;
insert into ConfirmState (id,text,code,deleted)
values (1002,'Принят','accepted',null)
;
insert into ConfirmState (id,text,code,deleted)
values (1003,'Отвергнут','refused',null)
;
insert into ConfirmState (id,text,code,deleted)
values (1004,'Отменен','cancelled',null)
;

insert into LinkType (id,text,code,deleted)
values (1001,'Мой друг','friend',null)
;
insert into LinkType (id,text,code,deleted)
values (1002,'Мой родитель','parent',null)
;
insert into LinkType (id,text,code,deleted)
values (1003,'Мой ребенок','child',null)
;
insert into LinkType (id,text,code,deleted)
values (1004,'Мой учитель','teacher',null)
;
insert into LinkType (id,text,code,deleted)
values (1005,'Мой ученик','student',null)
;
insert into LinkType (id,text,code,deleted)
values (2000,'Заблокирован мною','blocked',null)
;


alter table ConfirmState add constraint pk_ConfirmState primary key (id)
;
alter table LinkType add constraint pk_LinkType primary key (id)
;
alter table Link add constraint pk_Link primary key (id)
;


create unique index i_Link_main on Link(usrFrom,usrTo,dbeg)
;


alter table Link add constraint fk_Link_usrFrom
foreign key(usrFrom) references Usr(id)
;
alter table Link add constraint fk_Link_usrTo
foreign key(usrTo) references Usr(id)
;
alter table Link add constraint fk_Link_linkType
foreign key(linkType) references LinkType(id)
;
alter table Link add constraint fk_Link_confirmState
foreign key(confirmState) references ConfirmState(id)
;


create index fki_Link_usrFrom on Link(usrFrom)
;
create index fki_Link_usrTo on Link(usrTo)
;
create index fki_Link_linkType on Link(linkType)
;
create index fki_Link_confirmState on Link(confirmState)
;
