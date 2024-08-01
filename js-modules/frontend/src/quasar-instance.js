import {Quasar} from 'quasar'
import {createApp} from 'vue'

let quasarInstance = null

export const setQuasarInstance = (app) => {
    quasarInstance = app.config.globalProperties.$q
}

export const getQuasarInstance = () => {
    if (!quasarInstance) {
        throw new Error('Quasar instance is not set')
    }
    return quasarInstance
}

export function initQiasarInstance() {
    const app = createApp()
    app.use(Quasar, {})
    setQuasarInstance(app)
    return quasarInstance
}
