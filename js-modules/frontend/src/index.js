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
        {path: '/start', frame: import('./StartPage')},
    ]

    apx.app.onBeforeRun(async () => {
    })

    apx.app.run(() => {
        apx.app.frameRouter.addRoutes(routes)

        let vueApp = apx.createVueApp(App)
        vueApp.mount(apx.jcBase.dom.getAppElement())
    })

}
