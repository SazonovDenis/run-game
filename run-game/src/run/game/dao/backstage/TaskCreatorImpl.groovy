package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*

public class TaskCreatorImpl extends BaseMdbUtils implements TaskCreator {

    Store st_wordRus

    void setMdb(Mdb mdb) {
        super.setMdb(mdb)

        // Источники неправильных ответов
        Store stDataType = mdb.loadQuery("select * from DataType")
        StoreIndex idxDataType = stDataType.getIndex("code")
        st_wordRus = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-translate").getLong("id"))
    }

    public DataBox createTask(long idItem, String tagQuestion, String tagAnswer) {
        DataBox res = new DataBox()

        Store stQuestion = mdb.loadQuery(sql(), [id: idItem, dataType: tagQuestion])
        Store stAnswer = mdb.loadQuery(sql(), [id: idItem, dataType: tagAnswer])

        //
        if (stQuestion.size() == 0) {
            throw new Exception("Не найден tagQuestion: " + tagQuestion)
        }
        if (stAnswer.size() == 0) {
            throw new Exception("Не найден tagAnswer: " + tagAnswer)
        }

        // ---
        // Типа запросили пару "word-translate -> word-spelling"
        // ---

        //
        Rnd rnd = new RndImpl()

        // Выбираем "факт вопрос" и "факт ответ"
        StoreRecord recQuestion = stQuestion.get(rnd.num(0, stQuestion.size() - 1))
        StoreRecord recAnswer = stAnswer.get(rnd.num(0, stAnswer.size() - 1))
        StoreIndex idxAnswer = stAnswer.getIndex("factValue")

        //
        String valueTrue = recAnswer.getValue("factValue")


        // ---
        // Генерим варианты ответов
        int optionsCount = 6
        int valuesFalseChoiceCount = 20

        // Подберем неправильные варианты
        Map valuesFalseMap = UtWordDistance.getJaroWinklerMatch(valueTrue, st_wordRus, valuesFalseChoiceCount)
        Set valuesFalseSet = valuesFalseMap.keySet()


        // ---
        // Фильтруем неправильные варианты

        // Синонимы правильного ответа - чтобы исключить их из "ложных" вариантов
        // (иначе пользователь запутается)
        Set set_synonyms = new HashSet()
        for (String valueTrueWord : valueTrue.split("[ ,;]")) {
            if (valueTrueWord.length() == 0) {
                continue
            }
            StoreRecord rec_synonyms = mdb.loadQueryRecord("select * from WordSynonym where word = :word and lang = :lang", [word: valueTrueWord, lang: "rus"], false)
            if (rec_synonyms != null) {
                String sSynonyms = new String(rec_synonyms.getValue("synonyms"), "utf-8")
                set_synonyms.addAll(UtJson.fromJson(sSynonyms))
            }
        }

        // Фильтруем
        List<String> valuesFalseArr = new ArrayList<>()
        for (String falseValue : valuesFalseSet) {
            if (!set_synonyms.contains(falseValue)) {
                valuesFalseArr.add(falseValue)
            }
        }


        // ---
        // Всё нормально?
        int falseOptionsTotalCount = valuesFalseArr.size()
        if (falseOptionsTotalCount <= optionsCount) {
            throw new Exception("Не нашлось достаточного количества неправильных ответов")
        }


        // ---
        // Гтоовим правильный и неправильные ответы

        // Выбираем неправильные ответы (и их порядок)
        int n = 0
        int[] falseOntionsIndexes = new int[optionsCount]
        Set<Integer> falseOptionsIndexesUsed = new HashSet<>()
        while (falseOptionsIndexesUsed.size() < optionsCount) {
            int falseOptionIndex = rnd.num(0, falseOptionsTotalCount - 1)
            // Обеспечим, что среди неправильных вариантов не окажется совпадающего с
            // одним из правильных (он может оказаться отобраным
            // функцией UtWordDistance.getJaroWinklerMatch по похожести)
            String valueFalse = valuesFalseArr.get(falseOptionIndex)
            boolean valueFalseIsCoincideTrue = idxAnswer.get(valueFalse) != null
            //
            if (!falseOptionsIndexesUsed.contains(falseOptionIndex) && !valueFalseIsCoincideTrue) {
                falseOptionsIndexesUsed.add(falseOptionIndex)
                falseOntionsIndexes[n] = falseOptionIndex
                n = n + 1
            }
        }

        // Выбираем позицию правильного варианта в задании
        int trueValuePos = rnd.num(0, optionsCount - 1)

        // Заполняем stOption вариантами, среди которых один правильный
        Store stOption = mdb.createStore("TaskOption")
        for (int i = 0; i < optionsCount; i++) {
            StoreRecord recOption = stOption.add()
            if (i == trueValuePos) {
                // Правильный ответ
                recOption.setValue("trueFact", recAnswer.getValue("fact"))
                recOption.setValue("dataType", recAnswer.getValue("factDataType"))
                recOption.setValue("value", valueTrue)
            } else {
                // Неправильные варианты
                int falseOptionIndex = falseOntionsIndexes[i]
                String valueFalse = valuesFalseArr[falseOptionIndex]
                //
                recOption.setValue("dataType", recAnswer.getValue("factDataType"))
                recOption.setValue("value", valueFalse)
            }

        }

        //
        res.put("question", recQuestion)
        res.put("option", stOption)

        //
        return res
    }


    public DataBox loadTask(long idTask) {
        return null;
    }

    String sql() {
        return """
select
    Item.id item,
    Item.value itemValue,
    Fact.id fact,

    Fact_DataType.code factDataType,
    Fact.value factValue,

    FactTag.id factTagId,
    FactTag_TagType.code factTag_code,
    FactTag_Tag.value factTag,

    FactTagValue.id factTagValueId,
    FactTagValue_TagType.code factTagValue_code,
    FactTagValue.value factTagValue_Value
from
    Item

    left join Fact on (Fact.item = Item.id)
    left join DataType Fact_DataType on (Fact.dataType = Fact_DataType.id)

    left join FactTag on (FactTag.fact = Fact.id)
    left join Tag FactTag_Tag on (FactTag.tag = FactTag_Tag.id)
    left join TagType FactTag_TagType on (FactTag_Tag.tagType = FactTag_TagType.id)

    left join FactTagValue on (FactTagValue.fact = Fact.id)
    left join TagType FactTagValue_TagType on (FactTagValue.tagType = FactTagValue_TagType.id)
where
    Item.id = :id and
    Fact_DataType.code = :dataType

order by
    Item.id,
    Fact.dataType,
    Fact.id,
    FactTag.tag
"""

    }

}
