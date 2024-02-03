<%@ page import="jandcode.commons.named.*; jandcode.core.dbm.doc.*; jandcode.core.dbm.*; jandcode.core.dbm.dbstruct.*; jandcode.jc.*;jandcode.commons.*;jandcode.core.dbm.domain.*" %>
<%
    GspScript th = this

    DomainDbUtils dbUtils = this.args.dbUtils

    def diagUtils = new DiagramUtils(dbUtils.domainGroup)
    this.args.diagUtils = diagUtils

    def diags = diagUtils.loadDiagrams()
    this.args.diags = diags

    def domainRefs = dbUtils.domainGroup.getDomainRefs()
    this.args.domainRefs = domainRefs

    Set<Domain> domainsInDiag = new HashSet()
    for (d in diags) {
        domainsInDiag.addAll(d.domains)
    }

    GspScript utils = th.create("${th.scriptDir}/_utils.gsp")
    GspScript diag = th.create("${th.scriptDir}/_diag.gsp")
    GspScript diag_refs = th.create("${th.scriptDir}/_diag_refs.gsp")

    // копируем все ресурсы
    th.ant.copy(todir: th.outDir) {
        fileset(dir: th.scriptDir) {
            include(name: '**/*.js')
            include(name: '**/*.css')
        }
    }
    th.ant.mkdir(dir: "${th.outDir}/images")

    th.changeFile("index.html")
%>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Документация по баз данных</title>
    <link rel="stylesheet" href="style.css"/>
</head>

<body>

<h1><a id="_top"></a>Документация по базе данных</h1>

<div>Модель: <b>${dbUtils.model.modelDef.instanceOf.name}</b></div>

<h2>Содержание</h2>
<ul>
    <li><a href="#__toc_diagram">Диаграммы</a></li>
    <li><a href="#__toc_tab">Таблицы</a></li>
    <%
        if (dbUtils.domains.size() - domainsInDiag.size() == 0) {
    %>
    Нет таблиц, не попавших на диаграммы
    <%
        } else {
    %>
    <li><a href="#__toc_tab_not_in_diagram">Таблицы, не попавшие на диаграммы (${
            dbUtils.domains.size() - domainsInDiag.size()})</a></li>
    <%
        }
    %>
</ul>

%{--
==================================================================================
 список диаграмм
==================================================================================
--}%
<h2><a id="__toc_diagram"></a>Диаграммы</h2>
<ul>
    <% for (d in diags) { %>
    <li><a href="#${d.name}__diagram">${d.title}</a></li>
    <% } %>
</ul>

%{--
==================================================================================
 список таблиц
==================================================================================
--}%
<h2><a id="__toc_tab"></a>Таблицы</h2>

<div>Всего таблиц: <b>${dbUtils.domains.size()}</b></div>
<ul>
    <%
        for (d in dbUtils.domains) {
    %>
    <li>
        <a href="#${d.dbTableName}">${d.dbTableName}</a>
        <span class="small-title">${d.title}</span>
    </li>
    <%
        }
    %>
</ul>

%{--
==================================================================================
 диаграммы
==================================================================================
--}%
<% for (d in diags) { %>
<h2 class="table_desc"><a id="${d.name}__diagram"></a>${d.title}</h2>

<div class="diagram-wrapper">
    <object data="images/${d.name}__diagram.svg"></object>
</div>

<% utils.vars.out_comment(d, "<h3>Описание</h3>") %>

<% } %>

%{--
==================================================================================
 описание конкретных таблиц
==================================================================================
--}%
<%
    for (d in dbUtils.domains) {
%>
<h2 class="table_desc"><a id="${d.dbTableName}"></a>${d.dbTableName}</h2>
<% utils.vars.out_title(d) %>
<% utils.vars.out_comment(d) %>

<%
        def usedInDiags = diagUtils.usedInDiagrams(diags, d)
        if (usedInDiags.size() > 0) {
%>
<h3>Диаграммы</h3>
<ul>
    <% for (dg in usedInDiags) { %>
    <li><a href="#${dg.name}__diagram">${dg.title}</a></li>
    <% } %>
</ul>
<%
        }
%>
<h3>Поля</h3>
<table class="table-data">
    <tr>
        <th>Поле</th>
        <th>Тип</th>
        <th>Описание</th>
    </tr>
    <% for (f in d.fields) { %>
    <tr>
        <td>
            ${f.name}
        </td>
        <td>
            <% utils.vars.out_field_type(f) %>
        </td>
        <td>
            <% utils.vars.out_title(f) %>
            <% utils.vars.out_comment(f) %>
        </td>
    </tr>
    <% } %>
</table>

<h3>Диаграмма ссылок</h3>

<div class="diagram-wrapper">
    <object data="images/${d.name}__refs.svg"></object>
</div>

<%
    }
%>

%{--
==================================================================================
 таблицы без диаграм
==================================================================================
--}%
<h2><a id="__toc_tab_not_in_diagram"></a>Таблицы, не попавшие на диаграммы</h2>

<div>Всего таблиц: <b>${dbUtils.domains.size() - domainsInDiag.size()}</b></div>
<ul>
    <%
        for (d in dbUtils.domains) {
            if (domainsInDiag.contains(d)) {
                continue
            }
    %>
    <li>
        <a href="#${d.dbTableName}">${d.dbTableName}</a>
        <span class="small-title">${d.title}</span>
    </li>
    <%
        }
    %>
</ul>

</body>
</html>
%{--
==================================================================================
 генерация файлов диаграм
==================================================================================
--}%
<%
    diag.vars.gen_diag_domains()
    diag_refs.vars.gen_diag_refs()
    utils.vars.gen_diags_svg()
%>