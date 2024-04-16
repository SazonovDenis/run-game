/* вызов dao
----------------------------------------------------------------------------- */

import {apx} from '../vendor'
import * as cnv from '@jandcode/base/src/cnv'
import * as ajax from '@jandcode/base/src/ajax'
import * as error from '@jandcode/base/src/error'

// уровень вызова для timer
let _levelInvoke = 0

/**
 * Json Rpc клиент для выполнения запросов к api, предоставленному модулем
 * jandcode-core-apx и приправленного особенностями МОИМИ.
 */
export class MyJsonRpcClient extends apx.ApxJsonRpcClient {

    constructor(options) {
        super(options)
    }

    //////

    /**
     * Вызвать реальный метод json rpc
     * Порлная копия библиотечного метода JsonRpcClient.__invokeReal,
     * за исключением возможности добавить перехватчик ошибок onAfterInvokeError
     */
    __invokeReal(id, methodName, methodParams) {
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

            // уведомляем
            th.onBeforeInvoke(data)

            ajax.request({
                url: th.url,
                params: params,
                data: data,
                waitShow: th.waitShow,
            }).then((resp) => {
                try {
                    // уведомляем
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
                if (th.onAfterInvokeError && th.onAfterInvokeError(jsonResp)) {
                    // Ошибку обработал тот, кто захотел, дальнейшая обработка не нужна.
                    console.error(jsonResp)
                    return
                }
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


}

/**
 * JsonRpc клиент для вызова dao в точке 'api'
 * @type {MyJsonRpcClient}
 */
export let daoApi = new MyJsonRpcClient({url: 'api'})


