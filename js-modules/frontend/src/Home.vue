<template>
    <div>
        <UserTaskPanel :usrTask="usrTask"/>
    </div>
</template>

<script>

import {apx} from './vendor'
import UserTaskPanel from "../src/comp/UserTaskPanel"

export default {

    extends: apx.JcFrame,

    components: {
        UserTaskPanel,
    },

/*
    data() {
        return {
        }
    },
*/


    created() {
        console.info("===")

        //let res = await kisBase.daoApi.loadStore('m/Game/choiceTask', [1001])

        //let res = await kisBase.daoApi.invoke("m/Game/choiceTask", [1001])

        let data = {
            task: {
                text: "В чем смысл жизни HOME",
                dataType: "string",
            },
            taskOptions: [
                {
                    text: "Радоваться  HOME"
                },
                {
                    text: "Грустить  HOME"
                },
                {
                    text: "Дожди  HOME"
                },
                {
                    text: "Туманы  HOME"
                },
                {
                    text: "Холода  HOME и ветра не должны нас пугать, ведь все есть движение духа"
                }
            ]
        }

        //
        this.usrTask = data
    },

}
</script>
