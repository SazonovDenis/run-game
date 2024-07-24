update TagType set text='Звук для слова' where id = 1008
;

insert into TagType (id,text,code,deleted)
values (1011,'Тип вопросов в плане','plan-question-datatype',null)
;
insert into TagType (id,text,code,deleted)
values (1012,'Тип ответов в плане','plan-answer-datatype',null)
;

insert into Tag (id,tagType,value)
values (500,1011,'word-spelling')
;
insert into Tag (id,tagType,value)
values (501,1011,'word-sound')
;
insert into Tag (id,tagType,value)
values (510,1012,'word-spelling')
;
insert into Tag (id,tagType,value)
values (511,1012,'word-sound')
;
