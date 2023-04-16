<%@ page import="jandcode.core.dbm.doc.*; jandcode.core.dbm.domain.*; jandcode.core.dbm.dbstruct.*; jandcode.core.dbm.*; jandcode.jc.*" %>
<%
  GspScript th = this

  GspScript utils = th.create("${th.scriptDir}/_utils.gsp")

  /**
   * Генерация всех диаграм
   */
  th.vars.gen_diag_domains = {
    List<Diagram> diags = th.args.diags

    for (diag in diags) {
      th.pushFile("images/${diag.name}__diagram.puml")
      def links = []
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

<%
    for (di in diag.domainInfos) {
      Domain d = di.domain
      def colors = ""
      if (!di.showfields) {
        colors = '#line:silver'
      }
%>
class ${d.dbTableName} [[../index.html#${d.dbTableName}]] ${colors} {
<%
    for (f in d.fields) {
      def ft = utils.vars.get_field_type(f, diag.domainGroup)
      def ft_text = ft.text
      if (ft.refInfo && di.showrefs) {
        // это ссылка
        if (ft.refInfo.refDomain != null) {
          ft_text = """<color:green>${ft_text}</color>"""
          links.add("""${d.dbTableName} --> ${ft.refInfo.refDomain.dbTableName} : ${f.name}""")
        } else {
          ft_text = """<color:silver>${ft_text}</color>"""
        }
      } else {
        ft_text = """<color:gray>${ft_text}</color>"""
      }
      if (di.showfields) {
%>
${f.name}: ${ft_text}
<%
      }
    }
%>
}

<% } %>
<%

    // ссылки
    for (link in links) {
      out(link)
      out("\n")
    }
%>
@enduml
<%
      th.popFile()
    }
  }
%>