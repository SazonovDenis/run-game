<template>

    <div class="row filter-bar">

        <q-space/>

        <div class="row q-mx-sm q-gutter-x-xs">

            <q-btn
                @click="toggleTag_TrueNull('kaz')"
                color="primary"
                :outline="getOutline('kaz')"
                label="Каз"
            />
            <q-btn
                @click="toggleTag_TrueNull('eng')"
                color="primary"
                :outline="getOutline('eng')"
                label="Анг"
            />

            <q-btn
                v-if="button_showHidden_show"
                @click="toggleShowHidden()"
                :color="showHidden !== true ? 'indigo-8' : 'indigo-2'"
                :outline="showHidden !== true"
                text-color="indigo-10"
                unelevated no-caps
                icon="quasar.chip.selected"
                label="Знаю"
            >
                <q-badge rounded floating color="indigo-8"
                         v-if="button_showHidden_show && showHidden !== true"
                         :label="button_showHidden_text"
                />
            </q-btn>


        </div>

    </div>

</template>

<script>


/**
 * Панель фильтрации по языкам и по известности
 * Полезна при поиске слов.
 */
export default {

    name: "TaskListFilterBarView",

    components: {},

    props: {
        tags: {type: Object, default: {}},
        showHidden: {type: Boolean, default: false},
        onTagsChange: {type: Function, default: null},
        button_showHidden_show: {type: Boolean, default: false},
        button_showHidden_text: {type: String, default: null},
    },

    data() {
        return {
            showSearch: false,
        }
    },

    methods: {

        getOutline(lang) {
            if (!this.tags) {
                return false
            }
            //
            return !this.tags[lang]
        },

        getOutline_WordSound() {
            let tag = "word-sound"
            if (this.tags[tag] === false) {
                return false
            } else if (this.tags[tag] === true) {
                return false
            } else {
                return true
            }
        },

        getIcon_WordSound() {
            let tag = "word-sound"
            if (this.tags[tag] === false) {
                return "headphones-cross"
            } else if (this.tags[tag] === true) {
                return "headphones"
            } else {
                return "headphones"
            }
        },

        toggleTag_TrueFalseNull(tag) {
            //
            let tags = this.tags
            //
            if (tags[tag] === false) {
                delete tags[tag]
            } else if (tags[tag] === true) {
                tags[tag] = false
            } else {
                tags[tag] = true
            }
            //
            tags.tagLastClicked = tag
            //
            this.updateParent("tags", tags)
        },

        toggleTag_TrueNull(tag) {
            let tags = this.tags
            //
            if (tags[tag] === true) {
                delete tags[tag]
            } else {
                tags[tag] = true
            }
            //
            tags.tagLastClicked = tag

            // Позволяет родительскому компоненту доработать напильником значения,
            // во исполнение его бизнес-логики
            if (this.onTagsChange) {
                this.onTagsChange(tags)
            }

            //
            this.updateParent("tags", tags)
        },

        toggleShowHidden() {
            let showHidden = this.showHidden
            if (showHidden === true) {
                showHidden = false
            } else {
                showHidden = true
            }
            //
            this.updateParent("showHidden", showHidden)
        },

        updateParent(valueModelName, value) {
            this.$emit("update:" + valueModelName, value)
        },

    },


}
</script>


<style scoped>

.filter-bar {
    height: 3.1em;
}

</style>