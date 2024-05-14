/* вызов dao
----------------------------------------------------------------------------- */

import {apx} from '../vendor'
import * as cnv from '@jandcode/base/src/cnv'
import * as ajax from '@jandcode/base/src/ajax'
import * as base from '@jandcode/base/src/base'
import * as error from '@jandcode/base/src/error'
import {Store} from '@jandcode/apx/src/data/store'

// уровень вызова для timer
let _levelInvoke = 0

/**
 * Json Rpc клиент для выполнения запросов к api, предоставленному модулем
 * jandcode-core-apx и приправленного особенностями проекта.
 */
export class MyJsonRpcClient extends apx.ApxJsonRpcClient {

    constructor(options) {
        super(options)
    }

    /**
     * Полная копия библиотечного метода JsonRpcClient.loadStore,
     * за исключением возможности указать конфигурацию вызова
     */
    async loadStore(methodName, methodParams, cfg) {
        let cfgInvoke = {
            waitShow: this.waitShow
        }
        Object.assign(cfgInvoke, cfg);

        //
        let res = await this.invoke(methodName, methodParams, cfgInvoke)
        return new Store(res)
    }

    /**
     * Полная копия библиотечного метода JsonRpcClient.invoke,
     * за исключением возможности указать конфигурацию вызова
     */
    async invoke(methodName, methodParams, cfg) {
        let cfgInvoke = {
            waitShow: this.waitShow
        }
        Object.assign(cfgInvoke, cfg);

        //
        if (arguments.length < 2) {
            throw new Error('Не верное число агрументов')
        }
        if (!cnv.isArray(methodParams) && !cnv.isObject(methodParams)) {
            throw new Error('Параметры метода должны быть массивом или объектом')
        }
        let id = base.nextId('json-rpc-')

        //
        return await this.__invokeReal(id, methodName, methodParams, cfgInvoke)
    }


    /**
     * Полная копия библиотечного метода JsonRpcClient.__invokeReal,
     * за исключением возможности добавить перехватчик ошибок onAfterInvokeError
     */
    __invokeReal(id, methodName, methodParams, cfgInvoke) {
        let th = this
        let params = {}
        //
        if (Jc.cfg.envDev) {
            params._m = methodName.replace(/\//g, ':')
            _levelInvoke++
            console.time("time-" + _levelInvoke)
        }
        //
        let promise = new Promise(function(resolve, reject) {
            let data = {
                id: id,
                method: methodName,
                params: methodParams,
            }

            // ---
            // Уведомляем

            // Уведомим о событии того, кто указан в cfgInvoke
            // (это ситуативный подписчик)
            th.notifyRequestState("start", cfgInvoke)

            // Вызовем того, кто указан как обработчик в th.onBeforeInvoke
            th.onBeforeInvoke(data)

            //
            ajax.request({
                url: th.url,
                params: params,
                data: data,
                waitShow: cfgInvoke.waitShow,
            }).then((resp) => {
                try {
                    // ---
                    // Уведомляем

                    // Уведомим о событии того, кто указан в cfgInvoke
                    // (это ситуативный подписчик)
                    th.notifyRequestState("ok", cfgInvoke, resp.data)

                    // Вызовем того, кто указан как обработчик в th.onAfterInvoke
                    th.onAfterInvoke(resp.data)
                } finally {
                    if (Jc.cfg.envDev) {
                        console.groupCollapsed(`${id} [${th.url}] ${methodName}`)
                        console.info(`params:`, methodParams)
                        console.info(`result:`, resp.data.result)
                        console.info(`  resp:`, resp.data)
                        console.timeEnd("time-" + _levelInvoke)
                        console.groupEnd()
                        _levelInvoke--
                    }
                }
                // готово
                resolve(resp.data.result)
            }).catch((resp) => {
                let jsonResp = resp.response && resp.response.data

                // ------
                // Добавлено для проекта Rgm

                // ---
                // Уведомляем

                // Уведомим о событии того, кто указан в cfgInvoke
                // (это ситуативный подписчик)
                th.notifyRequestState("error", cfgInvoke, jsonResp)

                // Вызовем того, кто указан как обработчик в th.onAfterInvokeError
                // (это постоянный обработчик)
                if (th.onAfterInvokeError && th.onAfterInvokeError(jsonResp)) {
                    // Ошибку обработал тот, кто захотел, дальнейшая обработка не нужна.
                    console.error(jsonResp)
                    return
                }

                // Добавлено для проекта Rgm
                // ------

                if (Jc.cfg.envDev) {
                    console.groupCollapsed(`%c${id} [${th.url}] ${methodName}`, 'color: red;')
                    console.info(`params:`, methodParams)
                    if (jsonResp) {
                        console.info(`ERROR result:`, resp.response.data)
                    }
                    console.timeEnd("time-" + _levelInvoke)
                    console.groupEnd()
                    _levelInvoke--
                }
                let err
                if (jsonResp) {
                    err = resp.response.data
                    if (!cnv.isString(err)) {
                        if (err.error) {
                            err = err.error
                        }
                    }
                } else {
                    err = resp
                }
                reject(error.createError(err))
            })
        });

        // маркируем
        promise.jsonRpcId = id

        return promise
    }


    notifyRequestState(requestState, cfgInvoke, jsonResp) {
        if (cfgInvoke.onRequestState) {
            try {
                // Уведомляем
                cfgInvoke.onRequestState(requestState, jsonResp)
            } catch(e) {
                console.error("cfgInvoke.onAfterInvoke")
                console.error(e)
            }
        }
    }


}

/**
 * JsonRpc клиент для вызова dao в точке 'api'
 * @type {MyJsonRpcClient}
 */
export let daoApi = new MyJsonRpcClient({url: 'api'})


