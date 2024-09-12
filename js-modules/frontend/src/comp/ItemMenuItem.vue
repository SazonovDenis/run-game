<template>

    <template v-if="menuItem.label">

        <q-btn v-if="menuItem.hidden !== true"
               no-caps dense rounded unelevated
               size="0.9em"
               :label="menuItem.label"
               :_flat="!itemMenuOutline(menuItem, item)"
               :outline="itemMenuOutline(menuItem, item)"
               :icon="itemMenuIcon(menuItem, item)"
               :color="itemMenuColor(menuItem, item)"
               :text-color="itemMenuTextColor(menuItem, item)"
               @click.stop="itemMenuClick(menuItem, item)"
        />

    </template>

    <template v-else>

        <q-btn v-if="!itemMenuHidden(menuItem, item)"
               dense round
               size="1.2em"
               :flat="!itemMenuOutline(menuItem, item)"
               :outline="itemMenuOutline(menuItem, item)"
               :icon="itemMenuIcon(menuItem, item)"
               :color="itemMenuColor(menuItem, item)"
               @click.stop="itemMenuClick(menuItem, item)"
        />

    </template>

</template>

<script>

export default {

    name: "ItemMenuItem",

    props: {
        item: {},
        menuItem: {},
    },

    methods: {

        itemMenuOutline(menuItem, item) {
            if (menuItem.outline) {
                if (menuItem.outline instanceof Function) {
                    return menuItem.outline(item)
                } else {
                    return menuItem.outline
                }
            } else {
                return false
            }

        },

        itemMenuIcon(menuItem, item) {
            if (menuItem.icon) {
                if (menuItem.icon instanceof Function) {
                    return menuItem.icon(item)
                } else {
                    return menuItem.icon
                }
            } else {
                return undefined
            }
        },

        itemMenuColor(menuItem, item) {
            if (menuItem.color) {
                if (menuItem.color instanceof Function) {
                    return menuItem.color(item)
                } else {
                    return menuItem.color
                }
            } else {
                return "primary"
            }

        },

        itemMenuTextColor(menuItem, item) {
            if (menuItem.textColor) {
                if (menuItem.textColor instanceof Function) {
                    return menuItem.textColor(item)
                } else {
                    return menuItem.textColor
                }
            } else {
                return "black"
            }

        },

        itemMenuHidden(menuItem, item) {
            if (menuItem.hidden) {
                if (menuItem.hidden instanceof Function) {
                    return menuItem.hidden(item)
                } else {
                    return menuItem.hidden
                }
            } else {
                return false
            }
        },

        itemMenuClick(menuItem, item) {
            if (menuItem.onClick) {
                menuItem.onClick(item)
            }
        },

    }
}
</script>

<style scoped>

</style>