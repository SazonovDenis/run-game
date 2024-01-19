<template>

    <MenuContainer title="Главная">
        <div class="q-pa-md row items-start q-gutter-md">

            <div v-for="plan in plans" class="plan">

                <q-card class="my-card">
                    <q-img :src="imgSrc(plan.img)">
                    </q-img>

                    <div class="absolute-top text-h3">
                        <q-icon color="red"  style="margin: 0.6em" name="mail"/>
                    </div>

                    <div class="absolute-top text-right text-h3">
                        <q-badge color="green-5" style="margin: 0.6em"
                                 :label="plan.raiting"
                                 v-if="plan.raiting"/>
                    </div>

                    <div class="plan-text absolute-bottom text-h5">
                        {{ plan.text }}
                    </div>

                    <div class="absolute-bottom text-right text-h3">
                        <q-badge color="gray-10"  style="margin: 0.6em"
                                 :label="plan.count"/>
                    </div>

                </q-card>

            </div>

        </div>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"

export default {

    name: "PlanList",

    components: {
        MenuContainer, gameplay, auth, ctx
    },

    methods: {
        imgSrc(img) {
            if (img) {
                return apx.url.ref("run/game/web/img/plans/" + img + ".png")
            } else {
                return apx.url.ref("run/game/web/img/plans/no-image.png")
            }
        },
    },

    data() {

        return {
            plans: [
                {
                    img: "fruits",
                    text: "Овощи и фрукты",
                    count: 38,
                    raiting: null,
                },
                {
                    img: "materials",
                    text: "Материалы",
                    count: 21,
                    raiting: 1.3,
                },
                {
                    img: null,
                    text: "Части тела",
                    count: 38,
                    raiting: 3,
                },
                {
                    img: "products",
                    text: "Продукты",
                    count: 17,
                    raiting: 3.5,
                },
                {
                    img: "colors",
                    text: "Цвета и оттенки",
                    count: 13,
                    raiting: null,
                },
                {
                    img: "animals",
                    text: "Животные",
                    count: 24,
                    raiting: 12,
                },
                {
                    img: "plants",
                    text: "Растения",
                    count: 200,
                    raiting: 12,
                },
                {
                    img: null,
                    text: "Dumb ways to die",
                    count: 200,
                    raiting: 12,
                },
            ]

        }
    }

}

</script>

<style scoped>

.plan {
    max-width: 20em;
    min-width: 10em;
    width: 100% !important;
}

.plan-img img {
    _width: 8em;
}

.plan-text {
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    padding: 0.8em 0.6em;
}

</style>