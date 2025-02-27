import {apx} from './vendor'
import App from './App'
import theme from 'theme/default'
import icons from 'all/icons'

import WordsPage from './WordsPage'

import gameplay from "./gameplay"

export async function run() {
    apx.jcBase.applyTheme(theme)
    apx.icons.registerIcons(icons)

    let routes = [
        {path: '', frame: import('./PlansPage')},
        {path: '/words', frame: WordsPage},
        {path: '/plans', frame: import('./PlansPage')},
        {path: '/statistic', frame: import('./StatisticPage')},
        {path: '/about', frame: import('./AboutPage')},
        {path: '/login', frame: import('./LoginPage')},
        {path: '/user', frame: import('./UserInfoPage')},
        {path: '/game', frame: import('./GamePage')},
        {path: '/link', frame: import('./LinkPage')},
        {path: '/linkAdd', frame: import('./LinkAddPage')},
        {path: '/plan', frame: import('./PlanPage')},
        {path: '/planEdit', frame: import('./PlanEditPage')},
        {path: '/planUsrPlan', frame: import('./PlanUsrPlanPage')},
        {path: '/planStatistic', frame: import('./PlanStatisticPage')},
        {path: '/gameStatistic', frame: import('./GameStatisticPage')},
        {path: '/test', frame: import('./TestPage')},
        {path: '/exit', frame: import('./ExitPage')},
    ]

    apx.app.onBeforeRun(async () => {
    })

    apx.app.run(() => {
        apx.app.frameRouter.addRoutes(routes)

        let vueApp = apx.createVueApp(App)
        vueApp.mount(apx.jcBase.dom.getAppElement())
    })


    // Загрузим данные пользователя сами, не полагаясь на данные
    // из gsp-страницы - они там устаревшие, т.к. берутся из СЕССИИ сервера.
    // На сервере СЕССИЯ обновляется только после логина, а при обновлении страницы - нет.
    // К примеру: обновление данных userInfo.settings.helpState методом
    // run.game.dao.auth.Usr_upd.updSettings обновляет только данные в БД,
    // но не обновляет данные в СЕССИИ сервера, таким образом, если изменить
    // settings.helpState, а потом перезагрузить страницу мы поллучим старое состояние.
    try {
        let userInfo = await gameplay.api_getCurrentUser()
        gameplay.setAuthUserInfo(userInfo)
    } catch(e) {
        console.warn(e)
    }
}
