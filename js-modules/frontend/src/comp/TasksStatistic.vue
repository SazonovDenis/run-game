<template>

    <div>
        <div class="game-plan-item-progress">
            <div v-for="(item, index) in tasksStatistic"
                 :class="'game-plan-item-progress-el game-plan-item-progress-bar ' + getClass(item, index, tasksStatistic.length)"
                 :style="{height: getHeight(item)}">
            </div>
        </div>
        <div class="game-plan-item-progress">
            <div v-for="(item, index) in tasksStatistic"
                 class="game-plan-item-progress-el game-plan-item-progress-num">
                {{ getItemCount(item) }}
            </div>
        </div>
    </div>

</template>

<script>

/**
 * Статистика заданий в плане.
 */
export default {
    components: {},

    props: {
        tasksStatistic: {},
    },

    computed: {
        countTasks() {
            let count = 0
            for (let taskStatistic of this.tasksStatistic) {
                count = count + taskStatistic.count
            }
            return count
        },
    },

    methods: {
        getClass(item, index, count) {
            if (index === 0) {
                return "game-plan-todo"
            } else if (index === (count - 1)) {
                return "game-plan-done"
            } else {
                return "game-plan-inprogress"
            }
        },

        getHeight(item) {
            if (this.countTasks === 0) {
                return "0em"
            }
            let perc = 100 * item.count / this.countTasks
            let em = perc / 50
            return em + 'em'
        },

        getItemCount(item) {
            if (item.count > 0) {
                return item.count
            } else {
                return ""
            }
        },
    },

    data() {
        return {}
    },

}
</script>


<style lang="less" scoped>

.game-plan-item-progress {
    display: flex;
    flex-direction: row;
    align-items: flex-end;
}

.game-plan-item-progress-el {
    margin: 0.1rem;
    padding: 0.1rem 0.2rem;
    min-width: 3rem;
}

.game-plan-item-progress-bar {
    border: 1px solid silver;
    border-radius: 5px;
}

.game-plan-item-progress-num {
    text-align: center;
    font-size: 90%;
    _border: 1px solid silver;
}

.game-plan-done {
    background-color: #e6ffda;
}

.game-plan-inprogress {
    background-color: #fff2c8;
}

.game-plan-todo {
    background-color: #e0e0e0;
}


</style>
