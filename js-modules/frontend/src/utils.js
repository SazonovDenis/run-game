import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import dbConst from "./dao/dbConst"

export default {

    /* View in fullscreen */
    openFullscreen() {
        /* Get the documentElement (<html>) to display the page in fullscreen */
        var elem = document.documentElement;

        if (elem.requestFullscreen) {
            elem.requestFullscreen();
        } else if (elem.webkitRequestFullscreen) { /* Safari */
            elem.webkitRequestFullscreen();
        } else if (elem.msRequestFullscreen) { /* IE11 */
            elem.msRequestFullscreen();
        }
    },

    /* Close fullscreen */
    closeFullscreen() {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) { /* Safari */
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) { /* IE11 */
            document.msExitFullscreen();
        }
    },


    getAudioSrc(task) {
        if (!task || !task.valueSound) {
            return ""
        }

        return apx.url.ref("sound/" + task.valueSound)
    },

    loginIsGenerated(login) {
        if (!login) {
            return true
        }

        return login.length === 32
    },


    ratingText(rating) {
        rating = Math.abs(Math.trunc(rating))
        if (rating >= 10 && rating <= 20) {
            return "баллов"
        }
        let r = rating % 10
        if (r === 1) {
            return "балл"
        } else if (r === 2 || r === 3 || r === 4) {
            return "балла"
        } else {
            return "баллов"
        }
    },

    ratingTextInc(rating) {
        rating = Math.abs(Math.trunc(rating))
        if (rating >= 10 && rating <= 20) {
            return "заработано"
        }
        let r = rating % 10
        if (r === 1) {
            return "заработан"
        } else {
            return "заработано"
        }
    },

    ratingTextDec(rating) {
        rating = Math.abs(Math.trunc(rating))
        if (rating >= 10 && rating <= 20) {
            return "потеряно"
        }
        let r = rating % 10
        if (r === 1) {
            return "потерян"
        } else {
            return "потеряно"
        }
    },

    wordsText(wordCount) {
        wordCount = Math.abs(Math.trunc(wordCount))
        if (wordCount >= 10 && wordCount <= 20) {
            return "слов"
        }
        let r = wordCount % 10
        if (r === 1) {
            return "слово"
        } else if (r === 2 || r === 3 || r === 4) {
            return "слова"
        } else {
            return "слов"
        }
    },

    getHelpKeysArr(helpKey) {
        let helpKeysArr = []
        if (typeof (helpKey) === "string") {
            helpKeysArr.push(helpKey)
        } else {
            helpKeysArr = helpKey
        }
        return helpKeysArr
    },

    helpItemVisibleCount(helpKey) {
        if (!helpKey) {
            return 0
        }

        let res = 0
        let helpKeysArr = this.getHelpKeysArr(helpKey)
        for (let helpKey of helpKeysArr) {
            if (!this.isHelpItemHidden(helpKey)) {
                res = res + 1
            }
        }

        return res
    },


    // Чтобы внести единообразие:
    // показывать только явно запрошенную помощь
    // или всю явно не скрытую.
    isHelpItemHidden(helpValueKey) {
        let helpState = ctx.getGlobalState().helpState
        if (!helpState) {
            return false
        }

        if (false) {
            // Показывать только явно запрошенную помощь
            if (helpState[helpValueKey] !== true) {
                return true
            } else {
                return false
            }
        } else {
            // Показывать всю явно не скрытую
            if (helpState[helpValueKey] === false) {
                return true
            } else {
                return false
            }
        }
    },

    periodOptions: [
        {label: 'Сегодня', value: 'day'},
        {label: 'Неделя', value: 'week'},
        {label: 'Месяц', value: 'month'},
        {label: 'Три месяца', value: 'month3'},
    ],

    periodOptions_text: {
        day: "сегодня",
        week: "неделю",
        month: "месяц",
        month3: "три месяца",
    },

    Langs_text: {
        eng: "Анг", // "en",
        rus: "Рус", // "ru",
        kaz: "Каз", // "kz",
    },

    Langs_color_badge: {
        eng: "deep-purple-2",
        kaz: "deep-purple-2",
        rus: "blue-grey-2",
        mainLanguage: "blue-grey-1"
    },

    Langs_color_bth: {
        eng: "deep-purple-2",
        kaz: "deep-purple-2",
        rus: "blue-grey-2",
    },

    Langs_color_btn_outline: {
        eng: "deep-purple-6",
        kaz: "deep-purple-6",
        rus: "blue-grey-6",
    },

    Langs_color_btn_outline_dark: {
        eng: "deep-purple-9",
        kaz: "deep-purple-9",
        rus: "blue-grey-9",
    },

    // Названия для ссылок ОТ пользователя
    // для одной записи
    dictLinkTypeFromRec: {
        [dbConst.LinkType_friend]: "Ваш друг",
        [dbConst.LinkType_parent]: "Ваш родитель",
        [dbConst.LinkType_child]: "Ваш ребенок",
        [dbConst.LinkType_teacher]: "Ваш учитель",
        [dbConst.LinkType_student]: "Ваш ученик",
        [dbConst.LinkType_blocked]: "Заблокирован",
    },
    // для добавления записи
    dictLinkTypeFromAdd: [
        dbConst.LinkType_friend,
        dbConst.LinkType_parent,
        dbConst.LinkType_child,
        dbConst.LinkType_teacher,
        dbConst.LinkType_student,
    ],
    dictLinkTypeFromAdd_text: {
        [dbConst.LinkType_friend]: "Как друга",
        [dbConst.LinkType_parent]: "Как родителя",
        [dbConst.LinkType_child]: "Как ребенка",
        [dbConst.LinkType_teacher]: "Как учителя",
        [dbConst.LinkType_student]: "Как ученика",
    },
    // Поменяны местами "Для родителя"/"Для ребенка" и т.д.
    // Это специально, т.к. ссылка будет применятся получателем,
    // а для получателя роли меняются ролями.
    dictLinkTypeFromLinkAdd_text: {
        [dbConst.LinkType_friend]: "Как друга",
        [dbConst.LinkType_child]: "Как родителя",
        [dbConst.LinkType_parent]: "Как ребенка",
        [dbConst.LinkType_student]: "Как учителя",
        [dbConst.LinkType_teacher]: "Как ученика",
    },
    // Названия для ссылок ОТ пользователя
    dictLinkTypeFrom_text: {
        [dbConst.LinkType_friend]: "Ваши друзья",
        [dbConst.LinkType_parent]: "Ваши родители",
        [dbConst.LinkType_child]: "Ваши дети",
        [dbConst.LinkType_teacher]: "Ваши учителя",
        [dbConst.LinkType_student]: "Ваши ученики",
        [dbConst.LinkType_blocked]: "Заблокированные",
    },
    // Названия для ссылок К пользователю
    dictLinkTypeTo_text: {
        [dbConst.LinkType_friend]: "Ваши друзья",
        [dbConst.LinkType_parent]: "Ваши дети",
        [dbConst.LinkType_child]: "Ваши родители",
        [dbConst.LinkType_teacher]: "Ваши ученики",
        [dbConst.LinkType_student]: "Ваши учителя",
    },

    getPeriodText(value) {
        return this.periodOptions_text[value]
    },

    itemPosInItems(item, itemsList) {
        if (!itemsList) {
            return -1
        }

        for (let p = 0; p < itemsList.length; p++) {
            let itemEl = itemsList[p]
            if (itemEl.factAnswer === item.factAnswer &&
                itemEl.factQuestion === item.factQuestion
            ) {
                return p
            }
        }

        return -1
    },

}