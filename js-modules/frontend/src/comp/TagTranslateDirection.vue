<template>

    <div class="q-gutter-x-xs" v-if="tagTranslateDirection">

        <q-badge
            :text-color="badgeTextColor(tagTranslateDirection.from)"
            :color="badgeBackgroundColor(tagTranslateDirection.from)">
            {{ tagTranslateDirection.fromText }}
        </q-badge>
        <q-icon v-if="tagTranslateDirection.fromAudio"
                name="headphones" outline color="orange-10" size="1.2rem"/>

        <span class="tag-arrow">&rarr;</span>

        <q-badge
            :text-color="badgeTextColor(tagTranslateDirection.to)"
            :color="badgeBackgroundColor(tagTranslateDirection.to)">
            {{ tagTranslateDirection.toText }}
        </q-badge>
        <q-icon v-if="tagTranslateDirection.toAudio"
                name="headphones" outline color="orange-10" size="1.2rem"/>
    </div>

</template>

<script>

import dbConst from "../dao/dbConst"
import appConst from "../dao/appConst"
import utils from "../utils"

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
            let tagValueDirection = this.tags[dbConst.TagType_translate_direction]
            if (!tagValueDirection) {
                return null
            }
            let tagValueDirectionPair = tagValueDirection.split("-")

            //
            let tagQuestionDatatype = this.tags[dbConst.TagType_plan_question_factType]
            let tagAnswerDatatype = this.tags[dbConst.TagType_plan_answer_factType]

            //

            //
            return {
                from: tagValueDirectionPair[0],
                fromText: utils.Langs_text_short[tagValueDirectionPair[0]],
                fromAudio: "word-sound" === tagQuestionDatatype,
                to: tagValueDirectionPair[1],
                toText: utils.Langs_text_short[tagValueDirectionPair[1]],
                toAudio: "word-sound" === tagAnswerDatatype,
            }
        },
    },

    methods: {
        badgeTextColor(lang) {
            if (lang === appConst.settings.mainLanguage) {
                return "black"
            } else {
                return "black"
            }
        },
        badgeBackgroundColor(lang) {
            if (lang === appConst.settings.mainLanguage) {
                return utils.Langs_color_badge.mainLanguage
            } else {
                return utils.Langs_color_badge[lang]
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