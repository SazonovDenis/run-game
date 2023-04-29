<template>
    <tst-panel>
        <UserTaskPanel :usrTask="usrTask"/>
    </tst-panel>
</template>

<script>

//import {apx, kisBase} from '../vendor'

import UserTaskPanel from "../../src/comp/UserTaskPanel"

function data() {
    return {}
}

export default {
    components: {
        UserTaskPanel,
    },

    data() {
        return {
            usrTask: {
                task: {
                    text: "смысл жизни",
                    dataType: "string",
                },
                taskOptions: [
                    {
                        text: "радоваться"
                    },
                    {
                        text: "грустить"
                    },
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
                    id: 1039,
                    text: "Радоваться 23"
                },
                {
                    id: 8503,
                    text: "Грустить 54"
                },
                {
                    id: 2034,
                    text: "Дожди 454"
                },
                {
                    id: 5103,
                    text: "Туманы 455"
                },
                {
                    id: 4022,
                    text: "Холода 54 и ветра не должны нас пугать, ведь все есть движение духа"
                }
            ]
        }

        //
        this.usrTask = data
    }
}
</script>
