import {apx} from './vendor'
import App from './App'
import Home from './HomePage'

import theme from 'theme/default'
import icons from 'all/icons'

export function run() {
    apx.jcBase.applyTheme(theme)
    apx.icons.registerIcons(icons)

    let routes = [
        {path: '', frame: Home},
        {path: '/login', frame: import('./LoginPage')},
        {path: '/game', frame: import('./GamePage')},
/*
        {path: '/start', frame: import('./StartPage')},
*/
        {path: '/levels', frame: import('./LevelsPage')},
        {path: '/gameInfo', frame: import('./GameInfoPage')},
        {path: '/user', frame: import('./UserInfoPage')},
    ]

    apx.app.onBeforeRun(async () => {
    })

    apx.app.run(() => {
        apx.app.frameRouter.addRoutes(routes)

        let vueApp = apx.createVueApp(App)
        vueApp.mount(apx.jcBase.dom.getAppElement())
    })

}
