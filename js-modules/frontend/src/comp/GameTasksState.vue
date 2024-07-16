<template>
    <div class="row" v-if="tasksResult">
        <template v-for="taskResult in tasksResult">
            <TaskAnswerResult :item="taskResult"/>
        </template>
    </div>
</template>

<script>

import TaskAnswerResult from "./TaskAnswerResult"

/**
 * Состояние заданий в игре (в виде закрашенных кружков)
 * Используется в виджете для таскбара.
 */
export default {
    components: {
        TaskAnswerResult,
    },

    props: {
        tasksResult: {},
    },

    methods: {},

    data() {
        return {}
    },

}
</script>


<style lang="less">

</style>
