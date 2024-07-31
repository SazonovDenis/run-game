import {apx} from "./vendor"

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


    /**
     * Возвращает куки с указанным name,
     * или undefined, если ничего не найдено
     */
    getCookie(name, json = false) {
        if (!name) {
            return undefined;
        }
        /*
        Returns cookie with specified name (str) if exists, else - undefined
        if returning value is JSON and json parameter is true, returns json, otherwise str
        */
        let matches = document.cookie.match(new RegExp(
            "(?:^|; )" + name.replace(/([.$?*|{}()\[\]\\\/+^])/g, '\\$1') + "=([^;]*)"
        ));
        if (matches) {
            let res = decodeURIComponent(matches[1]);
            if (json) {
                try {
                    return JSON.parse(res);
                } catch(e) {}
            }
            return res;
        }

        return undefined;
    },

    /**
     * Возвращает куки в виде массива пар name:value
     */
    getCookiesKeys() {
        let res = []

        let list = document.cookie.split("; ");
        for (let pair of list) {
            let key = pair.split("=")[0]
            res.push(key)
        }

        return res
    },

    setCookie(name, value, options = {path: '/'}) {
        /*
        Sets a cookie with specified name (str), value (str) & options (dict)

        options keys:
          - path (str) - URL, for which this cookie is available (must be absolute!)
          - domain (str) - domain, for which this cookie is available
          - expires (Date object) - expiration date&time of cookie
          - max-age (int) - cookie lifetime in seconds (alternative for expires option)
          - secure (bool) - if true, cookie will be available only for HTTPS.
                            IT CAN'T BE FALSE
          - samesite (str) - XSRF protection setting.
                             Can be strict or lax
                             Read https://web.dev/samesite-cookies-explained/ for details
          - httpOnly (bool) - if true, cookie won't be available for using in JavaScript
                              IT CAN'T BE FALSE
        */
        if (!name) {
            return;
        }

        options = options || {};

        if (options.expires instanceof Date) {
            options.expires = options.expires.toUTCString();
        }

        if (value instanceof Object) {
            value = JSON.stringify(value);
        }
        let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);
        for (let optionKey in options) {
            updatedCookie += "; " + optionKey;
            let optionValue = options[optionKey];
            if (optionValue !== true) {
                updatedCookie += "=" + optionValue;
            }
        }
        document.cookie = updatedCookie;
    },

    deleteCookie(name) {
        this.setCookie(name, "", {
            'max-age': -1
        })
    },


    isLocalUserCookeName(name) {
        return name && name.startsWith("user_")
    },

    getLocalUserCookeName(iserId) {
        return "user_" + iserId
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

    // Чтобы внести единообразие:
    // показывать только явно запрошенную помощь
    // или всю явно не скрытую.
    isHelpItemHidden(helpState, helpValueKey) {
        if (true) {
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