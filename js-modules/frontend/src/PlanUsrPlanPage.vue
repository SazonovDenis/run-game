<template>
    <MenuContainer
        title="Поделиться уровнем"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :helpKey="getHelpKey()"
    >


        <!-- -->

        <HelpPanel class="q-mt-xs q-mb-none" :helpKey="getHelpKey()"/>

        <!-- -->


        <div class="rgm-state-text q-my-md">
            Уровень: {{ plan.planText }}
        </div>


        <q-list v-if="usrs.length > 0">

            <template v-for="item in usrs">

                <q-item class=""
                        clickable v-ripple
                >

                    <q-item-section top avatar>

                        <q-avatar
                            icon="usr-link"
                            color="grey-2"
                            text-color="yellow-8">
                        </q-avatar>

                    </q-item-section>


                    <q-item-section>

                        <q-item-label>
                            <div>
                                {{ item.text }}
                            </div>
                            <div class="text-caption text-grey-9">
                                {{ getDictLinkTypeFromRec(item) }}
                            </div>
                        </q-item-label>

                    </q-item-section>


                    <q-item-section top side>

                        <q-btn
                            v-if="item.isAllowed"
                            dense round flat no-caps
                            class="item-list-bitton"
                            icon="star-filled"
                            color="green-9"
                            :text-color="itemTextColor(item)"
                            @click="onUsrClick(item)"
                        />

                        <q-btn
                            v-else
                            dense round flat no-caps
                            class="item-list-bitton"
                            icon="star"
                            color="green-9"
                            :text-color="itemTextColor(item)"
                            @click="onUsrClick(item)"
                        />

                    </q-item-section>

                </q-item>

            </template>

        </q-list>


        <div v-else
             class="q-pt-md rgm-state-text">
            У вас нет пользователей, с которыми можно поделиться
        </div>


    </MenuContainer>

</template>

<script>


import {daoApi} from "./dao"
import utils from "./utils"
import MenuContainer from "./comp/MenuContainer"
import HelpPanel from "./comp/HelpPanel"

export default {

    name: "PlanUsrPlanPage",

    components: {
        MenuContainer, HelpPanel,
    },

    props: {
        plan: {},

        frameReturn: null,
        frameReturnProps: null,
    },

    data() {
        return {
            usrs: [],
        }
    },

    methods: {

        getHelpKey() {
            return ["help.planUsrPlan"]
        },

        async onUsrClick(item) {
            if (!item.isAllowed) {
                await daoApi.invoke("m/Plan/addUsrPlanUsr", [this.plan.id, item.usrTo], {waitShow: false})
                item.isAllowed = true
            } else {
                await daoApi.invoke("m/Plan/delUsrPlanUsr", [this.plan.id, item.usrTo], {waitShow: false})
                item.isAllowed = false
            }
        },

        itemTextColor(item) {
            if (item.isAllowed) {
                return "green"
            } else {
                return "green-9"
            }
        },

        getDictLinkTypeFromRec(usr) {
            return utils.dictLinkTypeFromRec[usr.linkType]
        },

        async doLoad() {
            let resApi = await daoApi.loadStore('m/Plan/getUsrPlanByPlan', [this.plan.id])
            this.usrs = resApi.records
        }

    },

    async mounted() {
        this.dataLoaded = false
        this.usrs = []

        //
        await this.doLoad()

        //
        this.dataLoaded = true
    },

}

</script>

<style scoped>

</style>