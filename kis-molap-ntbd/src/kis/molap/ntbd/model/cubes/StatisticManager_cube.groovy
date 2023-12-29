package kis.molap.ntbd.model.cubes

import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

public class StatisticManager_cube extends BaseMdbUtils {

    public StoreRecord loadGameStatistic(long game, long usr) throws Exception {
        Map params = new HashMap()
        params.put("usr", usr)
        params.put("game", game)
        StoreRecord rec = getMdb().loadQueryRecord(sql_UsrGameStatistic, params)
        return rec
    }

    public static String sql_GameTaskList = """                                       
-- Список заданий (в любых планах) со всей историей их выдачи пользователю
with Tab_GameTaskList as (
 
select
    GameTask.id,
    GameTask.task,
    GameTask.id gameTask,
    GameTask.game,
    GameTask.usr,
    GameTask.dtTask,
    GameTask.dtAnswer,
    (case when GameTask.dtTask is not null then 1 else 0 end) wasAsked,
    (case when GameTask.dtAnswer is not null then 1 else 0 end) wasAnswered,
    GameTask.wasTrue,
    GameTask.wasFalse,
    GameTask.wasHint,
    GameTask.wasSkip,
    (extract('epoch' from GameTask.dtAnswer) - extract('epoch' from GameTask.dtTask)) as answerDuration

from
    GameTask

where
    GameTask.usr = :usr
 
order by
    GameTask.task,
    GameTask.game
)
"""

    public static String sql_TaskStatistic = """
${StatisticManager_cube.sql_GameTaskList}
  
-- Статистика по каждому заданию 
select
    Tab_GameTaskList.usr,
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
    Tab_GameTaskList.task = :task

group by
    Tab_GameTaskList.usr,
    Tab_GameTaskList.task
    
"""

    String sql_UsrGameStatistic = """
${sql_GameTaskList}
  
-- Статистика по каждому заданию
select
    Tab_GameTaskList.usr,
    Tab_GameTaskList.game,
    count(Tab_GameTaskList.id) cntTask,
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
    Tab_GameTaskList.game = :game

group by
    Tab_GameTaskList.usr,
    Tab_GameTaskList.game
    
"""


}
