package run.game.dao.game


import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

@Deprecated
class Server_Plan_Test extends RgmBase_Test {


    ////////////////////////////////////////////
    String sql_GameTaskList = """                                       
-- Список всех заданий в планах со всей историей их выдачи пользователю
with Tab_GameTaskList as (
 
select
    PlanTask.id,
    PlanTask.plan,
    PlanTask.task,
    GameTask.id gameTask,
    GameTask.game,
    GameTask.usr,
    GameTask.dtTask,
    GameTask.dtAnswer,
    (case when GameTask.dtTask is not null then 1 else 0 end) wasAsked,
    (case when GameTask.dtAnswer is not null then 1 else 0 end) wasAnswered,
    (extract('epoch' from GameTask.dtAnswer) - extract('epoch' from GameTask.dtTask)) as answerDuration,
    GameTask.wasTrue,
    GameTask.wasFalse,
    GameTask.wasHint,
    GameTask.wasSkip
from
    PlanTask
    left join GameTask on (
       PlanTask.task = GameTask.task and
       GameTask.usr = :usr 
    )
    
order by
    PlanTask.plan,
    PlanTask.task,
    GameTask.game
)
"""

    ////////////////////////////////////////////
    String sql_PlanStatistic = """
${sql_GameTaskList}
  
-- Статистика по каждому заданию в плане
select 
    Tab_GameTaskList.plan,
    Tab_GameTaskList.task,
    count(Tab_GameTaskList.id) cnt,
    sum(Tab_GameTaskList.wasAsked) cntAsked,
    sum(Tab_GameTaskList.wasAnswered) cntAnswered,
    sum(Tab_GameTaskList.answerDuration) answerDuration,
    sum(Tab_GameTaskList.wasTrue) cntTrue,
    sum(Tab_GameTaskList.wasFalse) cntFalse,
    sum(Tab_GameTaskList.wasHint) cntHint,
    sum(Tab_GameTaskList.wasSkip) cntSkip

from
    Tab_GameTaskList

where
    Tab_GameTaskList.plan = :plan

group by
    Tab_GameTaskList.plan,
    Tab_GameTaskList.task
    
"""

    @Test
    void xxx() {
        Store st1 = mdb.loadQuery(sql_PlanStatistic, [
                plan: 1000,
                usr : 1000,
        ])
        mdb.outTable(st1, 15)
    }

}
