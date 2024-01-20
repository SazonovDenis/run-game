<template>
    <div class="rgm-icon-bg"
         :style="{backgroundColor: bgColor, borderRadius: borderRadius}">
        <div>
            <q-icon class="rgm-icon-icon"
                    size="2em"
                    :color="color"
                    :name="name">
            </q-icon>
        </div>
        <div v-if="text"
             class="rgm-icon-text"
             :style="{color: color}"
            >
            {{ text }}
        </div>
    </div>

</template>

<script>


export default {

    name: "RgmIcon",

    props: {
        text: {type: String},
        name: {type: String, default: "circle"},
        color: {type: String, default: "white"},
        bgColor: {type: String, default: "red"},
        bgShape: {type: String},
    },

    computed: {
        borderRadius() {
            if (this.bgShape === "rounded") {
                // Квадрат со скруглением
                return "0.5rem"
            } else if (this.bgShape === "square") {
                // Квадрат
                return "0"
            } else {
                // Круг
                return "1rem"
            }
        }
    }

}

</script>

<style>

.rgm-icon-bg {
    _position: relative;
    _font-size: 3em;
    _width: 2em;
    height: 2em;

    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    width: fit-content;
}

.rgm-icon-icon {
    _left: 0.15em;
    _top: 0.15em;
}

.rgm-icon-text {
    padding-top: 0.2em;
    padding-left: 0.2em;
    padding-right: 0.8em;
}

</style>