let jcTools = require("@jandcode/tools")
let path = require('path')
let fs = require('fs')

let WpbApxPlugin = require('@jandcode/apx/wpb-plugin')

let builder = new jcTools.WebpackBuilder(__dirname)

builder.merge(new WpbApxPlugin({
    tstModules: [
    ],
    apxModules: [
        '@jandcode/apx-map',
        '@jandcode/apx-chart',
        '@jandcode/apx-datagrid',
    ],
    themes: [
        'kis-std',
        'apx-base',
        'apx-std',
    ],
    themeDefault: 'apx-std',
}))

builder.merge({
    entry: {},
    plugins: [],
    optimization: {
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: /node_modules/,
                    name: 'vendors',
                    chunks: 'all',
                },
            },
        },
    },
    module: {
        exprContextCritical: false,
    }
})

// перекрытие для конкретного разработчика
let devConfig = path.resolve(__dirname, '_dev.config.js')
if (fs.existsSync(devConfig)) {
    let fn = require(devConfig)
    fn(builder)
}

//
module.exports = builder.build()
