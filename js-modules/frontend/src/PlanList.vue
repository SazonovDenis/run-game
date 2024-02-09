<template>

    <MenuContainer title="Добавление уровней">


        <div class="plans q-pa-sm q-gutter-md">

            <template v-for="plan in plans">

                <q-card class="plan-item" @click="onClickStar(plan)">
                    <q-img :src="imgSrc(plan.img)">
                        <template v-slot:error>
                            <div
                                class="absolute-full flex flex-center bg-grey-2 text-black">
                                Cannot load image
                            </div>
                        </template>
                        <template v-slot:loading>
                            <div>&nbsp;</div>
                        </template>
                    </q-img>


                    <div class="row absolute-bottom plan-item-info">
                        <div class="plan-item-added-info">
                            <rgm-icon v-if="plan.added"
                                      bg-color="white"
                                      color="green"
                                      name="star"
                                      text="Добавлен"
                            />
                            <rgm-icon v-if="!plan.added"
                                      bg-color="white"
                                      color="grey-5"
                                      name="star"
                            />
                        </div>

                        <div class="row plan-item-added-rating">
                            <q-badge v-if="plan.raiting"
                                     color="green-5"
                                     :label="plan.raiting + ' / ' + plan.count"
                            />
                            <q-badge v-if="!plan.raiting"
                                     color="grey-8"
                                     :label="plan.count"
                            />
                        </div>

                    </div>


                    <q-card-section class="plan-item-text text-h4">
                        {{ plan.text }}
                    </q-card-section>


                </q-card>

            </template>

        </div>


    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import RgmIcon from "./comp/RgmIcon"

export default {

    name: "PlanList",

    components: {
        MenuContainer, RgmIcon, gameplay, auth, ctx
    },

    methods: {
        imgSrc(img) {
            if (img) {
                return apx.url.ref("run/game/web/img/plans/" + img + ".png")
            } else {
                return apx.url.ref("run/game/web/img/plans/no-image.png")
            }
        },

        onClickStar(plan) {
            if (plan.added) {
                plan.added = false
            } else {
                plan.added = true
            }
        },

    },

    data() {
        return {
            plans: []
        }
    },

    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        this.plans = await gameplay.api_getPlansPublic()
    },

}

</script>

<style>


.plans {
    display: grid;
    grid-template-columns: 1fr 1fr;
    __grid-template-columns: repeat(auto-fill, minmax(10em, 25em));
}

@media (max-width: 360px) {
    .plans {
        grid-template-columns: 1fr;
    }
}

.plan-item {
    justify-self: stretch;
    align-self: stretch;
    aspect-ratio: 1;
}

.plan-img img {
    _width: 8em;
}

.plan-item-info {
    top: -1em;
    position: relative;
}

.plan-item-added-info {
    flex-grow: 100;
    margin-left: 0.3em;
}

.plan-item-added-rating {
    font-size: 1.5em;
    width: 3em;
    align-content: center;
    justify-content: end;
    margin-right: 0.3em
}

.plan-item-text {
    top: -1em;
    position: relative;
    padding-bottom: 0;
}


</style>