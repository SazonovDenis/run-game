<template>

    <div class="q-gutter-x-xs" v-if="tagTranslateDirection">

        <q-badge
            :text-color="textColor(tagTranslateDirection.from)"
            :color="backgroundColor(tagTranslateDirection.from)">
            {{ tagTranslateDirection.fromText }}
        </q-badge>
        <q-icon v-if="tagTranslateDirection.fromAudio"
                name="headphones" outline color="orange-10" size="1.2rem"/>
        <span class="tag-arrow">&rarr;</span>
        <q-badge
            :text-color="textColor(tagTranslateDirection.to)"
            :color="backgroundColor(tagTranslateDirection.to)">
            {{ tagTranslateDirection.toText }}
        </q-badge>
        <q-icon v-if="tagTranslateDirection.toAudio"
                name="headphones" outline color="orange-10" size="1.2rem"/>
    </div>

</template>

<script>

import dbConst from "../dao/dbConst"
import appConst from "../dao/appConst"

/**
 * Показывает тэги плана: направление перевода (например: "eng->rus") и наличие звука.
 */
export default {

    name: "TagTranslateDirection",

    props: {
        tags: Object
    },

    computed: {
        tagTranslateDirection() {
            if (!this.tags) {
                return null
            }

            //
            let tagValueDirection = this.tags[dbConst.TagType_word_translate_direction]
            if (!tagValueDirection) {
                return null
            }
            let tagValueDirectionPair = tagValueDirection.split("-")

            //
            let tagQuestionDatatype = this.tags[dbConst.TagType_plan_question_datatype]
            let tagAnswerDatatype = this.tags[dbConst.TagType_plan_answer_datatype]

            //
            let langs = {
                "rus": "Рус",
                "eng": "Анг",
                "kaz": "Каз",
            }

            //
            return {
                from: tagValueDirectionPair[0],
                fromText: langs[tagValueDirectionPair[0]],
                fromAudio: "word-sound" === tagQuestionDatatype,
                to: tagValueDirectionPair[1],
                toText: langs[tagValueDirectionPair[1]],
                toAudio: "word-sound" === tagAnswerDatatype,
            }
        },
    },

    methods: {
        textColor(lang) {
            if (lang === appConst.settings.mainLanguage) {
                return "black"
            } else {
                return "white"
            }
        },
        backgroundColor(lang) {
            if (lang === appConst.settings.mainLanguage) {
                return "grey-3"
            } else {
                return "primary"
            }
        },
    },

}
</script>

<style scoped>

.tag-arrow {
    font-size: 1.2em;
}

</style>