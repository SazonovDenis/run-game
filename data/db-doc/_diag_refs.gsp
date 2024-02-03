<%@ page import="jandcode.core.dbm.doc.*; jandcode.core.dbm.domain.*; jandcode.core.dbm.dbstruct.*; jandcode.core.dbm.*; jandcode.jc.*" %>
<%
    GspScript th = this

    /**
     * Генерация диаграм ссылок
     */
    th.vars.gen_diag_refs = {
        List<DomainRefs> domainRefs = th.args.domainRefs

        for (dr in domainRefs) {
            th.pushFile("images/${dr.domain.dbTableName}__refs.puml")
%>
@startuml
hide circle
hide empty members

skinparam roundcorner 5
skinparam shadowing false

skinparam class {
    BackgroundColor white
    ArrowColor #2688d4
    BorderColor #2688d4
}

class ${dr.domain.dbTableName} [[../index.html#${dr.domain.dbTableName}]] #back:lightblue {
}

<% for (d in dr.domains) { %>
class ${d.dbTableName} [[../index.html#${d.dbTableName}]] {
}
<% } %>
<%
        // ссылки
        for (r in dr.refs) {
            out("""${r.from.dbTableName} --> ${r.to.dbTableName} : ${r.field.name}""")
            out("\n")
        }
%>
@enduml
<%
            th.popFile()
        }
    }
%>
