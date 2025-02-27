create sequence g_ConfirmState start with 1000 increment by 1
~~
create sequence g_Cube_Dates start with 1000 increment by 1
~~
create sequence g_Cube_UsrFact start with 1000 increment by 1
~~
create sequence g_Cube_UsrGame start with 1000 increment by 1
~~
create sequence g_Cube_UsrPlan start with 1000 increment by 1
~~
create sequence g_Fact start with 1000 increment by 1
~~
create sequence g_FactTag start with 1000 increment by 1
~~
create sequence g_FactType start with 1000 increment by 1
~~
create sequence g_Game start with 1000 increment by 1
~~
create sequence g_GameTask start with 1000 increment by 1
~~
create sequence g_GameUsr start with 1000 increment by 1
~~
create sequence g_Item start with 1000 increment by 1
~~
create sequence g_ItemTag start with 1000 increment by 1
~~
create sequence g_Link start with 1000 increment by 1
~~
create sequence g_LinkType start with 1000 increment by 1
~~
create sequence g_Molap_audit start with 1000 increment by 1
~~
create sequence g_Molap_log start with 1000 increment by 1
~~
create sequence g_Molap_statistic start with 1000 increment by 1
~~
create sequence g_Molap_status start with 1000 increment by 1
~~
create sequence g_Plan start with 1000 increment by 1
~~
create sequence g_PlanFact start with 1000 increment by 1
~~
create sequence g_PlanTag start with 1000 increment by 1
~~
create sequence g_TagType start with 1000 increment by 1
~~
create sequence g_Task start with 1000 increment by 1
~~
create sequence g_TaskOption start with 1000 increment by 1
~~
create sequence g_TaskQuestion start with 1000 increment by 1
~~
create sequence g_Usr start with 1000 increment by 1
~~
create sequence g_UsrFact start with 1000 increment by 1
~~
create sequence g_UsrPlan start with 1000 increment by 1
~~
create sequence g_WordDistance start with 1000 increment by 1
~~
create sequence g_WordSynonym start with 1000 increment by 1
~~
create table ConfirmState (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
~~
create table Cube_Dates (
  dt date
)
~~
create table Cube_UsrFact (
  ratingTask double precision,
  ratingQuickness double precision,
  usr bigint,
  factQuestion bigint,
  factAnswer bigint
)
~~
create table Cube_UsrGame (
  cntTask bigint,
  cntAsked bigint,
  cntAnswered bigint,
  cntTrue bigint,
  cntFalse bigint,
  cntHint bigint,
  cntSkip bigint,
  taskStatistic bytea,
  usr bigint,
  game bigint
)
~~
create table Cube_UsrPlan (
  count bigint,
  countFull bigint,
  countLearned bigint,
  ratingTask double precision,
  ratingQuickness double precision,
  usr bigint,
  plan bigint
)
~~
create table Fact (
  id bigint not null,
  item bigint,
  factType bigint,
  value varchar(500)
)
~~
create table FactTag (
  id bigint not null,
  fact bigint,
  tagType bigint,
  tagValue varchar(500)
)
~~
create table FactType (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
~~
create table Game (
  id bigint not null,
  plan bigint,
  dbeg timestamp,
  dend timestamp
)
~~
create table GameTask (
  id bigint not null,
  task bigint,
  usr bigint,
  game bigint,
  dtTask timestamp,
  dtAnswer timestamp,
  wasTrue smallint,
  wasFalse smallint,
  wasHint smallint,
  wasSkip smallint
)
~~
create table GameUsr (
  id bigint not null,
  usr bigint,
  game bigint
)
~~
create table Item (
  id bigint not null,
  value varchar(200)
)
~~
create table ItemTag (
  id bigint not null,
  item bigint,
  tagType bigint,
  tagValue varchar(500)
)
~~
create table Link (
  id bigint not null,
  usrFrom bigint,
  usrTo bigint,
  linkType bigint,
  dbeg timestamp,
  dend timestamp,
  confirmState bigint
)
~~
create table LinkType (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
~~
create table Molap_audit (
  id bigint not null,
  dt timestamp,
  opr integer,
  tableName varchar(50),
  tableId bigint,
  dat varchar(2000)
)
~~
create table Molap_log (
  id bigint not null,
  cube_name varchar(100),
  calc_type integer,
  calc_start timestamp,
  calc_stop timestamp,
  calc_error text,
  interval_dbeg timestamp,
  interval_dend timestamp,
  interval_count bigint,
  interval_count_write bigint,
  interval_duration_dirty bigint,
  interval_duration_calc bigint,
  interval_duration_write bigint,
  audit_age_from bigint,
  audit_age_to bigint,
  audit_count_own bigint,
  audit_count_depends bigint,
  audit_count_write bigint,
  audit_duration_dirty bigint,
  audit_duration_dirty_depends bigint,
  audit_duration_convert bigint,
  audit_duration_calc bigint,
  audit_duration_write bigint
)
~~
create table Molap_statistic (
  id bigint not null,
  cube_name varchar(100),
  rec_rate double precision,
  one_rec_cost double precision,
  one_rec_durationDirty double precision,
  one_rec_durationCalc double precision,
  one_rec_durationWrite double precision,
  interval_rec_cost double precision
)
~~
create table Molap_status (
  id bigint not null,
  cube_name varchar(100),
  done_id bigint,
  done_dt timestamp,
  dt_min date,
  dt_max date
)
~~
create table Plan (
  id bigint not null,
  text varchar(200),
  isPublic smallint
)
~~
create table PlanFact (
  id bigint not null,
  plan bigint,
  factQuestion bigint,
  factAnswer bigint
)
~~
create table PlanTag (
  id bigint not null,
  plan bigint,
  tagType bigint,
  tagValue varchar(500)
)
~~
create table TagType (
  id bigint not null,
  text varchar(200),
  code varchar(50),
  deleted smallint
)
~~
create table Task (
  id bigint not null,
  factQuestion bigint,
  factAnswer bigint
)
~~
create table TaskOption (
  id bigint not null,
  task bigint,
  isTrue smallint,
  factType bigint,
  value varchar(500)
)
~~
create table TaskQuestion (
  id bigint not null,
  task bigint,
  factType bigint,
  value varchar(500)
)
~~
create table Usr (
  id bigint not null,
  login varchar(50),
  password varchar(50),
  text varchar(200),
  settings bytea
)
~~
create table UsrFact (
  id bigint not null,
  factQuestion bigint,
  factAnswer bigint,
  usr bigint,
  isHidden smallint,
  isKnownGood smallint,
  isKnownBad smallint
)
~~
create table UsrPlan (
  id bigint not null,
  plan bigint,
  usr bigint,
  isHidden smallint,
  isOwner smallint,
  isAllowed smallint
)
~~
create table WordDistance (
  id bigint not null,
  lang varchar(50),
  word varchar(200),
  matches bytea
)
~~
create table WordSynonym (
  id bigint not null,
  lang varchar(50),
  word varchar(200),
  synonyms bytea
)
~~
insert into ConfirmState (id,text,code,deleted)
values (1001,'Ожидает','waiting',null)
~~
insert into ConfirmState (id,text,code,deleted)
values (1002,'Принят','accepted',null)
~~
insert into ConfirmState (id,text,code,deleted)
values (1003,'Отвергнут','refused',null)
~~
insert into ConfirmState (id,text,code,deleted)
values (1004,'Отменен','cancelled',null)
~~
insert into FactType (id,text,code,deleted)
values (1001,'Написание слова','word-spelling',null)
~~
insert into FactType (id,text,code,deleted)
values (1002,'Перевод слова','word-translate',null)
~~
insert into FactType (id,text,code,deleted)
values (1003,'Звучание слова','word-sound',null)
~~
insert into FactType (id,text,code,deleted)
values (1004,'Изображение предмета','word-picture',null)
~~
insert into FactType (id,text,code,deleted)
values (1005,'Транскрибция слова','word-transcription',null)
~~
insert into FactType (id,text,code,deleted)
values (1006,'Идиоматическое выражение','word-idiom',null)
~~
insert into FactType (id,text,code,deleted)
values (1007,'Пример использования','word-example',null)
~~
insert into LinkType (id,text,code,deleted)
values (1001,'Мой друг','friend',null)
~~
insert into LinkType (id,text,code,deleted)
values (1002,'Мой родитель','parent',null)
~~
insert into LinkType (id,text,code,deleted)
values (1003,'Мой ребенок','child',null)
~~
insert into LinkType (id,text,code,deleted)
values (1004,'Мой учитель','teacher',null)
~~
insert into LinkType (id,text,code,deleted)
values (1005,'Мой ученик','student',null)
~~
insert into LinkType (id,text,code,deleted)
values (2000,'Заблокирован мною','blocked',null)
~~
insert into TagType (id,text,code,deleted)
values (1001,'Язык слова','word-lang',null)
~~
insert into TagType (id,text,code,deleted)
values (1002,'Направление перевода слова','word-translate-direction',null)
~~
insert into TagType (id,text,code,deleted)
values (1003,'Часть речи','word-part-of-speech',null)
~~
insert into TagType (id,text,code,deleted)
values (1004,'Пример использования слова','word-use-sample',null)
~~
insert into TagType (id,text,code,deleted)
values (1005,'Тема слова','word-category',null)
~~
insert into TagType (id,text,code,deleted)
values (1006,'Уровень сложности','level-grade',null)
~~
insert into TagType (id,text,code,deleted)
values (1007,'Топ ххх слов','top-list',null)
~~
insert into TagType (id,text,code,deleted)
values (1008,'Звук для слова','word-sound-info',null)
~~
insert into TagType (id,text,code,deleted)
values (1010,'Уровень доступа к плану','plan-access',null)
~~
insert into TagType (id,text,code,deleted)
values (1011,'Тип вопросов в плане','plan-question-facttype',null)
~~
insert into TagType (id,text,code,deleted)
values (1012,'Тип ответов в плане','plan-answer-facttype',null)
~~
alter table ConfirmState add constraint pk_ConfirmState primary key (id)
~~
alter table Fact add constraint pk_Fact primary key (id)
~~
alter table FactTag add constraint pk_FactTag primary key (id)
~~
alter table FactType add constraint pk_FactType primary key (id)
~~
alter table Game add constraint pk_Game primary key (id)
~~
alter table GameTask add constraint pk_GameTask primary key (id)
~~
alter table GameUsr add constraint pk_GameUsr primary key (id)
~~
alter table Item add constraint pk_Item primary key (id)
~~
alter table ItemTag add constraint pk_ItemTag primary key (id)
~~
alter table Link add constraint pk_Link primary key (id)
~~
alter table LinkType add constraint pk_LinkType primary key (id)
~~
alter table Molap_audit add constraint pk_Molap_audit primary key (id)
~~
alter table Molap_log add constraint pk_Molap_log primary key (id)
~~
alter table Molap_statistic add constraint pk_Molap_statistic primary key (id)
~~
alter table Molap_status add constraint pk_Molap_status primary key (id)
~~
alter table Plan add constraint pk_Plan primary key (id)
~~
alter table PlanFact add constraint pk_PlanFact primary key (id)
~~
alter table PlanTag add constraint pk_PlanTag primary key (id)
~~
alter table TagType add constraint pk_TagType primary key (id)
~~
alter table Task add constraint pk_Task primary key (id)
~~
alter table TaskOption add constraint pk_TaskOption primary key (id)
~~
alter table TaskQuestion add constraint pk_TaskQuestion primary key (id)
~~
alter table Usr add constraint pk_Usr primary key (id)
~~
alter table UsrFact add constraint pk_UsrFact primary key (id)
~~
alter table UsrPlan add constraint pk_UsrPlan primary key (id)
~~
alter table WordDistance add constraint pk_WordDistance primary key (id)
~~
alter table WordSynonym add constraint pk_WordSynonym primary key (id)
~~
create unique index i_Cube_Dates_main on Cube_Dates(dt)
~~
create unique index i_Cube_UsrFact_main on Cube_UsrFact(usr,factQuestion,factAnswer)
~~
create unique index i_Cube_UsrGame_main on Cube_UsrGame(usr,game)
~~
create unique index i_Cube_UsrPlan_main on Cube_UsrPlan(usr,plan)
~~
create unique index i_GameUsr_main on GameUsr(game,usr)
~~
create unique index i_Link_main on Link(usrFrom,usrTo,dbeg)
~~
create unique index i_PlanFact_main on PlanFact(plan,factQuestion,factAnswer)
~~
create unique index i_UsrFact_main on UsrFact(usr,factQuestion,factAnswer)
~~
create unique index i_UsrPlan_main on UsrPlan(usr,plan)
~~
create unique index i_WordDistance_main on WordDistance(word,lang)
~~
create unique index i_WordSynonym_main on WordSynonym(word,lang)
~~
alter table Fact add constraint fk_Fact_item
foreign key(item) references Item(id)
~~
alter table Fact add constraint fk_Fact_factType
foreign key(factType) references FactType(id)
~~
alter table FactTag add constraint fk_FactTag_fact
foreign key(fact) references Fact(id)
~~
alter table FactTag add constraint fk_FactTag_tagType
foreign key(tagType) references TagType(id)
~~
alter table Game add constraint fk_Game_plan
foreign key(plan) references Plan(id)
~~
alter table GameTask add constraint fk_GameTask_task
foreign key(task) references Task(id)
~~
alter table GameTask add constraint fk_GameTask_usr
foreign key(usr) references Usr(id)
~~
alter table GameTask add constraint fk_GameTask_game
foreign key(game) references Game(id)
~~
alter table GameUsr add constraint fk_GameUsr_usr
foreign key(usr) references Usr(id)
~~
alter table GameUsr add constraint fk_GameUsr_game
foreign key(game) references Game(id)
~~
alter table ItemTag add constraint fk_ItemTag_item
foreign key(item) references Item(id)
~~
alter table ItemTag add constraint fk_ItemTag_tagType
foreign key(tagType) references TagType(id)
~~
alter table Link add constraint fk_Link_usrFrom
foreign key(usrFrom) references Usr(id)
~~
alter table Link add constraint fk_Link_usrTo
foreign key(usrTo) references Usr(id)
~~
alter table Link add constraint fk_Link_linkType
foreign key(linkType) references LinkType(id)
~~
alter table Link add constraint fk_Link_confirmState
foreign key(confirmState) references ConfirmState(id)
~~
alter table PlanFact add constraint fk_PlanFact_plan
foreign key(plan) references Plan(id)
~~
alter table PlanFact add constraint fk_PlanFact_factQuestion
foreign key(factQuestion) references Fact(id)
~~
alter table PlanFact add constraint fk_PlanFact_factAnswer
foreign key(factAnswer) references Fact(id)
~~
alter table PlanTag add constraint fk_PlanTag_plan
foreign key(plan) references Plan(id)
~~
alter table PlanTag add constraint fk_PlanTag_tagType
foreign key(tagType) references TagType(id)
~~
alter table Task add constraint fk_Task_factQuestion
foreign key(factQuestion) references Fact(id)
~~
alter table Task add constraint fk_Task_factAnswer
foreign key(factAnswer) references Fact(id)
~~
alter table TaskOption add constraint fk_TaskOption_task
foreign key(task) references Task(id)
~~
alter table TaskOption add constraint fk_TaskOption_factType
foreign key(factType) references FactType(id)
~~
alter table TaskQuestion add constraint fk_TaskQuestion_task
foreign key(task) references Task(id)
~~
alter table TaskQuestion add constraint fk_TaskQuestion_factType
foreign key(factType) references FactType(id)
~~
alter table UsrFact add constraint fk_UsrFact_factQuestion
foreign key(factQuestion) references Fact(id)
~~
alter table UsrFact add constraint fk_UsrFact_factAnswer
foreign key(factAnswer) references Fact(id)
~~
alter table UsrFact add constraint fk_UsrFact_usr
foreign key(usr) references Usr(id)
~~
alter table UsrPlan add constraint fk_UsrPlan_plan
foreign key(plan) references Plan(id)
~~
alter table UsrPlan add constraint fk_UsrPlan_usr
foreign key(usr) references Usr(id)
~~
create index fki_Fact_item on Fact(item)
~~
create index fki_Fact_factType on Fact(factType)
~~
create index fki_FactTag_fact on FactTag(fact)
~~
create index fki_FactTag_tagType on FactTag(tagType)
~~
create index fki_Game_plan on Game(plan)
~~
create index fki_GameTask_task on GameTask(task)
~~
create index fki_GameTask_usr on GameTask(usr)
~~
create index fki_GameTask_game on GameTask(game)
~~
create index fki_GameUsr_usr on GameUsr(usr)
~~
create index fki_GameUsr_game on GameUsr(game)
~~
create index fki_ItemTag_item on ItemTag(item)
~~
create index fki_ItemTag_tagType on ItemTag(tagType)
~~
create index fki_Link_usrFrom on Link(usrFrom)
~~
create index fki_Link_usrTo on Link(usrTo)
~~
create index fki_Link_linkType on Link(linkType)
~~
create index fki_Link_confirmState on Link(confirmState)
~~
create index fki_PlanFact_plan on PlanFact(plan)
~~
create index fki_PlanFact_factQuestion on PlanFact(factQuestion)
~~
create index fki_PlanFact_factAnswer on PlanFact(factAnswer)
~~
create index fki_PlanTag_plan on PlanTag(plan)
~~
create index fki_PlanTag_tagType on PlanTag(tagType)
~~
create index fki_Task_factQuestion on Task(factQuestion)
~~
create index fki_Task_factAnswer on Task(factAnswer)
~~
create index fki_TaskOption_task on TaskOption(task)
~~
create index fki_TaskOption_factType on TaskOption(factType)
~~
create index fki_TaskQuestion_task on TaskQuestion(task)
~~
create index fki_TaskQuestion_factType on TaskQuestion(factType)
~~
create index fki_UsrFact_factQuestion on UsrFact(factQuestion)
~~
create index fki_UsrFact_factAnswer on UsrFact(factAnswer)
~~
create index fki_UsrFact_usr on UsrFact(usr)
~~
create index fki_UsrPlan_plan on UsrPlan(plan)
~~
create index fki_UsrPlan_usr on UsrPlan(usr)
~~
