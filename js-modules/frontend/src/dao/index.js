/* вызов dao
----------------------------------------------------------------------------- */

import {apx} from '../vendor'

/**
 * Json Rpc клиент для выполнения запросов к api, предоставленному модулем
 * jandcode-core-apx и приправленного особенностями МОИМИ.
 */
export class MyJsonRpcClient extends apx.ApxJsonRpcClient {

    constructor(options) {
        super(options)
    }

    //////

}

/**
 * JsonRpc клиент для вызова dao в точке 'api'
 * @type {MyJsonRpcClient}
 */
export let daoApi = new MyJsonRpcClient({url: 'api'})


