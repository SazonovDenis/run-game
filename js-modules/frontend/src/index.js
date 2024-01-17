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
        {path: '/login', frame: import('./LoginPage')},
        {path: '/game', frame: import('./GamePage')},
        {path: '/levels', frame: import('./LevelsPage')},
        {path: '/levelChoice', frame: import('./LevelChoicePage')},
        {path: '/gameInfo', frame: import('./GameInfoPage')},
        {path: '/user', frame: import('./UserInfoPage')},
        {path: '/plan', frame: import('./PlanPage')},
        {path: '/still', frame: import('./StillPage')},
        {path: '/still1', frame: import('./StillPage1')},
    ]

    apx.app.onBeforeRun(async () => {
    })

    apx.app.run(() => {
        apx.app.frameRouter.addRoutes(routes)

        let vueApp = apx.createVueApp(App)
        vueApp.mount(apx.jcBase.dom.getAppElement())
    })

}
