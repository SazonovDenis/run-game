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

    data() {
        return {
            usrTask: {
                /*
                                task: {
                                    text: "В чем смысл жизни",
                                    dataType: "string",
                                },
                                taskOptions: [
                                    {
                                        text: "Радоваться"
                                    },
                                    {
                                        text: "Грустить"
                                    },
                                    {
                                        text: "Дожди"
                                    },
                                    {
                                        text: "Туманы"
                                    },
                                    {
                                        text: "Холода и ветра не должны нас пугать, ведь все есть движение духа"
                                    }
                                ]
                */
            }
        }
    },


    created() {
        console.info("===")

        //let res = await kisBase.daoApi.loadStore('m/Game/choiceTask', [1001])

        //let res = await kisBase.daoApi.invoke("m/Game/choiceTask", [1001])

        let data = {
            task: {
                text: "В чем смысл жизни1",
                dataType: "string",
            },
            taskOptions: [
                {
                    text: "Радоваться 23"
                },
                {
                    text: "Грустить 54"
                },
                {
                    text: "Дожди 454"
                },
                {
                    text: "Туманы 455"
                },
                {
                    text: "Холода 54 и ветра не должны нас пугать, ведь все есть движение духа"
                }
            ]
        }

        //
        this.usrTask = data
    },

}
</script>
