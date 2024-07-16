package kis.molap.ntbd.model.cubes

import groovy.transform.*
import jandcode.commons.*
import jandcode.core.store.*

@CompileStatic
class UtCubeRating {

    // Расчет рейтингов: вес результата текущего, предыдущего и пред-предыдущего ответа
    //protected double[] ratingWeight = [0.5, 0.3, 0.2]
    protected double[] ratingWeight = [5, 3, 2]

    // Расчет рейтинга за скорость: время ответа и баллы за него
    protected double[] ratingDurationGrade = [2, 3, 5, 8]
    //protected double[] ratingDurationWeight = [1, 0.8, 0.5, 0.2]
    protected double[] ratingDurationWeight = [10, 8, 5, 2]

    // Период анализа ответов для расчета рейтинга - количество дней назад от даты последней игры
    public static int RAITING_ANALYZE_DAYS_DIFF = 10

    // Максимально возможный рейтинг одного факта
    //public static int RATING_FACT_MAX = 1
    public static int RATING_FACT_MAX = 10


    /**
     * Выполняет анализ списка GameTask, расчитывает рейтинг для каждого factQuestion+factAnswer
     *
     * @param stGameTask список GameTask+factQuestion+factAnswer (по определенным критериям)
     * Список должен быть отсортированн по factQuestion+factAnswer а внутри них - по дате выдачи.
     * @param pos начальная позиция
     * @param res
     * @return новая позиция
     */
    public int stepCollectRaiting(Store stGameTask, int pos, HashMap res) {
        List<Boolean> taskAnswers = []
        List<Double> taskAnswersDuration = []

        //
        while (pos < stGameTask.size()) {
            // Текущая и следующая запись
            StoreRecord recGameTask = stGameTask.get(pos)
            StoreRecord recGameTaskNext = null
            if (pos < stGameTask.size() - 1) {
                recGameTaskNext = stGameTask.get(pos + 1)
            }

            // Накопление rating по очередному GameTask
            if (taskAnswers.size() < ratingWeight.size()) {
                if (recGameTask.getLong("wasTrue")) {
                    taskAnswers.add(true)
                } else {
                    taskAnswers.add(false)
                }
                //
                Double duration = null
                if (!UtDateTime.isEmpty(recGameTask.getDateTime("dtAnswer"))) {
                    double durationMsec = recGameTask.getDateTime("dtAnswer").diffMSec(recGameTask.getDateTime("dtTask"))
                    duration = durationMsec / 1000
                }
                taskAnswersDuration.add(duration)
            }

            // Накопление по очередному task закончено
            if (recGameTaskNext == null ||
                    recGameTaskNext.getLong("factQuestion") != recGameTask.getLong("factQuestion") ||
                    recGameTaskNext.getLong("factAnswer") != recGameTask.getLong("factAnswer")
            ) {
                // ---
                // Считаем рейтинг

                // Если не хватает последних rating (мало раз выполнено task) -
                // считаем, что предыдущие были отвечены неправильно
                while (taskAnswers.size() < ratingWeight.size()) {
                    taskAnswers.add(false)
                    taskAnswersDuration.add(null)
                }

                //
                double ratingTask = 0
                for (int i = 0; i < ratingWeight.size(); i++) {
                    double ratingAnswer = getRatingAnswer(taskAnswers.get(i))
                    ratingTask = ratingTask + ratingAnswer * ratingWeight[i]
                }
                //
                double ratingQuickness = 0
                for (int i = 0; i < ratingWeight.size(); i++) {
                    double ratingQuicknessAnswer = getRatingQuickness(taskAnswersDuration.get(i))
                    ratingQuickness = ratingQuickness + ratingQuicknessAnswer * ratingWeight[i]
                }

                // ---
                res.put("factQuestion", recGameTask.getValue("factQuestion"))
                res.put("factAnswer", recGameTask.getValue("factAnswer"))
                res.put("ratingTask", ratingTask)
                res.put("ratingQuickness", ratingQuickness)


                //
                return pos + 1
            }


            // ---
            // Продолжение накопления
            pos++
        }


        //
        return pos
    }


    /**
     * Бал за ответ
     * @param taskAnswer результат ответа
     * @return Значение от 0 до 1
     */
    protected double getRatingAnswer(boolean taskAnswer) {
        if (taskAnswer == true) {
            return 1
        } else {
            return 0
        }
    }

    /**
     * Бал за скорость
     * @param taskAnswerDuration время ответа
     * @return Значение от 0 до 1
     */
    protected double getRatingQuickness(Double taskAnswerDuration) {
        double ratingQuickness = 0

        if (taskAnswerDuration == null) {
            return ratingQuickness
        }

        for (int i = 0; i < ratingDurationGrade.size(); i++) {
            if (taskAnswerDuration <= ratingDurationGrade[i]) {
                ratingQuickness = ratingDurationWeight[i]
                break
            }
        }

        return ratingQuickness
    }


}
