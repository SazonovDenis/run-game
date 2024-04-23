<template>

    <MenuContainer title="Добавление уровней">


        <div class="plans q-pa-sm q-gutter-md">

            <template v-for="plan in plans">

                <q-card class="plan-item">
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


                    <div class="row absolute-bottom plan-item-info"
                         @click="onClickStar(plan)">
                        <div class="plan-item-added-info">
                            <rgm-icon v-if="plan.isAllowed"
                                      bg-color="white"
                                      color="green"
                                      name="star"
                                      text="Добавлен"
                            />
                            <rgm-icon v-if="!plan.isAllowed"
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


                    <q-card-section class="plan-item-text text-h4"
                                    @click="onClickPlan(plan)">
                        {{ plan.planText }}
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
import {daoApi} from "run-game-frontend/src/dao"

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

        async onClickStar(plan) {
            if (plan.isAllowed) {
                plan.isAllowed = false
            } else {
                plan.isAllowed = true
            }

            //
            if (plan.isAllowed) {
                await daoApi.invoke('m/Plan/addUsrPlan', [plan.id])
            } else {
                await daoApi.invoke('m/Plan/delUsrPlan', [plan.id])
            }
        },

        async onClickPlan(plan) {
            apx.showFrame({
                frame: '/plan', props: {planId: plan.id}
            })
        },

    },

    data() {
        return {
            plans: []
        }
    },

    async mounted() {
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