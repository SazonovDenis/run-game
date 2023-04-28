<template>
    <tst-panel>
        <UserTaskPanel :usrTask="usrTask"/>
    </tst-panel>
</template>

<script>

//import {apx, kisBase} from '../vendor'

import RepairWellInfoWidget from "../../src/comp/UserTaskPanel"
import UserTaskPanel from "../../src/comp/UserTaskPanel"

function data() {
    return {}
}

export default {
    components: {
        UserTaskPanel,
        RepairWellInfoWidget,
    },

    data() {
        return {
            usrTask: {
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
            }
        }
    },


    async created() {
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
    }
}
</script>
