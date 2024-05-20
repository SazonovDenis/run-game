import {apx} from './vendor'
import App from './App'
import theme from 'theme/default'
import icons from 'all/icons'

import Home from './HomePage'

export function run() {
    apx.jcBase.applyTheme(theme)
    apx.icons.registerIcons(icons)

    let routes = [
        {path: '', frame: Home},
        {path: '/test', frame: import('./TestPage')},
        {path: '/exit', frame: import('./ExitPage')},
        {path: '/about', frame: import('./AboutPage')},
        {path: '/login', frame: import('./LoginPage')},
        {path: '/game', frame: import('./GamePage')},
        {path: '/plans', frame: import('./PlansPage')},
        {path: '/statistic', frame: import('./StatisticPage')},
        {path: '/gameInfo', frame: import('./GameInfoPage')},
        {path: '/user', frame: import('./UserInfoPage')},
        {path: '/link', frame: import('./LinkPage')},
        {path: '/plan', frame: import('./PlanPage')},
        {path: '/planEdit', frame: import('./PlanEditPage')},
    ]

    apx.app.onBeforeRun(async () => {
    })

    apx.app.run(() => {
        apx.app.frameRouter.addRoutes(routes)

        let vueApp = apx.createVueApp(App)
        vueApp.mount(apx.jcBase.dom.getAppElement())
    })

}
