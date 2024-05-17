package run.game.dao.statistic

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Statistic_list extends RgmMdbUtils {

    /**
     *
     */
    @DaoMethod
    Store byPlan(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.plan")

        //
        long idUsr = getContextOrCurrentUsrId()
        mdb.loadQuery(st, sqlPlan(), [usr: idUsr, dbeg: dbeg, dend: dend])

        mdb.outTable(mdb.loadQuery(sqlPlan(), [usr: idUsr, dbeg: dbeg, dend: dend]))

        //
        return st
    }

    @DaoMethod
    Store byGame(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.game")

        //
        long idUsr = getContextOrCurrentUsrId()
        mdb.loadQuery(st, sqlGame(), [usr: idUsr, dbeg: dbeg, dend: dend])

        mdb.outTable(mdb.loadQuery(sqlGame(), [usr: idUsr, dbeg: dbeg, dend: dend]))

        //
        return st
    }

    @DaoMethod
    Store byWord(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.word")

        //
        long idUsr = getContextOrCurrentUsrId()
        mdb.loadQuery(st, sqlWord(), [usr: idUsr, dbeg: dbeg, dend: dend])

        mdb.outTable(mdb.loadQuery(sqlWord(), [usr: idUsr, dbeg: dbeg, dend: dend]))

        //
        return st
    }

    String sqlBase(){
        return """
select 
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer
    
from
    GameTask 
    join Task on (GameTask.task = Task.id)

where
    GameTask.dtTask >= :dbeg and 
    GameTask.dtTask < :dend and
    GameTask.usr = :usr 
"""
    }

    String sqlPlan(){
        return """
with lst_GameTask as (
${sqlBase()}
)


select 
    Plan.id plan,
    Plan.text as planText,

    count(*) as count,
    sum(1) as ratingTask

from
    lst_GameTask 
    join PlanFact on (
        lst_GameTask.factQuestion = PlanFact.factQuestion and
        lst_GameTask.factAnswer = PlanFact.factAnswer
    )
    join Plan on (
        PlanFact.plan = Plan.id
    )   

group by
    Plan.id,
    Plan.text
"""
    }

    String sqlGame(){
        return """
with lst_GameTask as (
${sqlBase()}
)


select
    Game.id game, 
    Game.dbeg, 
    Game.dend, 
    Game.plan,
    Plan.text as planText,
    
    count(*) as count,
    sum(1) as ratingTask

from
    lst_GameTask 
    join Game on (
        lst_GameTask.game = Game.id
    )
    join Plan on (
        Game.plan = Plan.id
    )   

group by 
    Game.id, 
    Game.dbeg, 
    Game.dend, 
    Game.plan,
    Plan.text

"""
    }

    String sqlWord(){
        return """
with lst_GameTask as (
${sqlBase()}
)


select 
    lst_GameTask.factQuestion,
    lst_GameTask.factAnswer,

    count(*) as count,
    sum(1) as ratingTask

from
    lst_GameTask 

group by 
    lst_GameTask.factQuestion,
    lst_GameTask.factAnswer

"""
    }

}
