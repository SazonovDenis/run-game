<%@ page import="jandcode.commons.*; jandcode.core.dbm.domain.*; jandcode.core.dbm.dbstruct.*; jandcode.core.dbm.*; jandcode.jc.*" %>
<%
    GspScript th = this

    DomainDbUtils dbUtils = this.args.dbUtils

    th.classpath("jandcode-mdoc")
    def mdEngine = UtClass.createInst("jandcode.mdoc.flexmark.FlexmarkEngine")

    /**
     * Вывод title, если он есть. Иначе выводится имя с указанием, что title не указан
     */
    th.vars.out_title = { a ->
        if (a.title) {
            if (a.class.toString().contains(".DomainImpl")) {
                out("""<span class="title-table">${a.title}</span>""")
            } else {
                out("""<span class="title">${a.title}</span>""")
            }
        } else {
            out("<span>${a.name} (заголовок не указан)</span>")
        }
    }

    /**
     * Вывод коментария, если он есть.
     * Коментарий берется из conf.
     */
    th.vars.out_comment = { a, title = null ->
        String rem = a.conf['comment']
        if (!rem) {
            return
        }
        if (title) {
            out(title)
        }
        def html = th.vars.md_to_html(rem)
        out("""<div class="comment">${html}</div>""")
    }

    /**
     * Возвращает map с информацией о типе поля
     */
    th.vars.get_field_type = { Field f, DomainGroup domainGroup = null ->
        def res = [
                text   : "?",       // текст типа
                refInfo: null       // информация о ссылке, если не null
        ]
        // это ссылка?
        if (domainGroup == null) {
            domainGroup = dbUtils.domainGroup
        }
        def refInfo = domainGroup.getRefInfo(f, true)
        if (refInfo != null) {
            def s = refInfo.ref
            if (refInfo.refDomain) {
                s = refInfo.refDomain.dbTableName
            }
            res['text'] = "ref[" + s + "]"
            res['refInfo'] = refInfo
        } else {
            res['text'] = f.dbDataType.name

            if (f.size > 0) {
                res['text'] = res['text'] + "[" + f.size + "]"
            }
        }
        return res
    }

    /**
     * Вывод типа поля. Если это ссылка на домен, выводится ссылка на таблицу
     */
    th.vars.out_field_type = { Field f ->
        def ft = th.vars.get_field_type(f)
        def s = ft.text
        if (ft.refInfo) {
            // это ссылка
            if (ft.refInfo.refDomain != null) {
                s = """<a href="#${ft.refInfo.refDomain.dbTableName}">${s}</a>"""
            } else {
                s = """<span class="bad-link">${s}</span>"""
            }
        }
        out(s)
    }


    /**
     * Сгенерировать все svg для диаграм
     */
    th.vars.gen_diags_svg = {
        def lib = th.ctx.getLib("plantuml")
        ut.runcmd(cmd: ['java', '-jar', lib.jar, '-tsvg', '.'], dir: "${th.outDir}/images")
    }

    /**
     * Конвертация md -> html
     */
    th.vars.md_to_html = { s ->
        def doc = mdEngine.parser.parse(s)
        return mdEngine.renderer.render(doc)
    }
%>