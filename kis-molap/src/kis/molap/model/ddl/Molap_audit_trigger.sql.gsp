<%@ page import="jandcode.commons.variant.VariantDataType; jandcode.core.store.StoreField; jandcode.core.dbm.domain.*; jandcode.core.dbm.dbstruct.*; " %>

<%

  // Генерация триггеров на аудит изменений
  def ut = new DomainDbUtils(this.model)

  for (Domain d : ut.domains) {
    def tableName = d.getName()

    if (tableName.toUpperCase().startsWith('CUBE_') || tableName.toUpperCase().startsWith('MOLAP_')) {
      continue
    }

    // Соберем не BLOB поля
    List fields = []
    for (Field f : d.getFields()) {
      if (f.dbDataType.getSqlType(f.size) != 'text') {
        fields.add(f.name)
      }
    }
%>


/* ================================ */
/* ${tableName}                              */
/* ================================ */

/*
  Реакция на добавление записи в ${tableName}
*/
CREATE OR REPLACE FUNCTION aud_${tableName}_ins() RETURNS TRIGGER AS '
DECLARE
  dt timestamp;
  dat json;
BEGIN
  select now() into dt;

  dat = json_build_object(
<%
    int n = 0
    for (String fieldName : fields) {
%>
    ''${fieldName}'', NEW.${fieldName}${n == fields.size()-1 ? "" : ","}
<%
      n = n + 1
    }
%>
  );

  --
  insert into Molap_audit (id, dt, opr, tableName, tableId, dat)
    values (nextval(''g_Molap_audit''), NOW()::timestamp, 1, ''${tableName}'', NEW.id, dat);

  --
  RETURN NULL;
END;

' LANGUAGE plpgsql;
~~

/*
  Реакция на изменение записи в ${tableName}
*/
CREATE OR REPLACE FUNCTION aud_${tableName}_upd() RETURNS TRIGGER AS '
DECLARE
  dt timestamp;
  dat json;
BEGIN
  select now() into dt;

  dat = json_build_object(
<%
    n = 0
    for (String fieldName : fields) {
%>
    ''${fieldName}'', NEW.${fieldName}${n == fields.size()-1 ? "" : ","}
<%
      n = n + 1
    }
%>
  );

  --
  insert into Molap_audit (id, dt, opr, tableName, tableId, dat)
    values (nextval(''g_Molap_audit''), NOW()::timestamp, 2, ''${tableName}'', NEW.id, dat);

  --
  RETURN NULL;
END;

' LANGUAGE plpgsql;
~~

/*
  Реакция на удаление записи в ${tableName}
*/
CREATE OR REPLACE FUNCTION aud_${tableName}_del() RETURNS TRIGGER AS '
DECLARE
  dt timestamp;
  dat json;
BEGIN
  select now() into dt;

  dat = json_build_object(
<%
    n = 0
    for (String fieldName : fields) {
%>
   ''${fieldName}'', OLD.${fieldName}${n == fields.size()-1 ? "" : ","}
<%
      n = n + 1
    }
%>
  );

  --
  insert into Molap_audit (id, dt, opr, tableName, tableId, dat)
    values (nextval(''g_Molap_audit''), NOW()::timestamp, 3, ''${tableName}'', OLD.id, dat);

  --
  RETURN NULL;
END;

' LANGUAGE plpgsql;
~~

/*
  Триггеры для ${tableName}
*/

CREATE OR REPLACE TRIGGER aud_${tableName}_ins AFTER INSERT ON ${tableName}
FOR EACH ROW EXECUTE PROCEDURE aud_${tableName}_ins();
~~

CREATE OR REPLACE TRIGGER aud_${tableName}_upd AFTER UPDATE ON ${tableName}
FOR EACH ROW EXECUTE PROCEDURE aud_${tableName}_upd();
~~

CREATE OR REPLACE TRIGGER aud_${tableName}_del AFTER DELETE ON ${tableName}
FOR EACH ROW EXECUTE PROCEDURE aud_${tableName}_del();
~~

<%
  }
%>