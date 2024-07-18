<template>

    <q-circular-progress
        show-value
        rounded
        size="8rem"
        :font-size="fontSize"
        :value="circularValue"
        :thickness="thickness"
        :color="color"
        track-color="grey-3"
        :class="'q-mx-sm circular ' + getClass"
    >
        <div class="col">
            <div>
                <span class="value-value">
                    {{ value }}
                </span>
            </div>

            <div :class="'value-title' + getClassSuffixTotal">
                <span v-html="title">
                </span>
            </div>

            <div class="total" v-if="valueTotal">
                <span class="total-title">из </span>
                <span class="total-value">{{ valueTotal }}</span>
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
export default {

    name: "CircularValue",

    props: {
        value: Number,
        valueType: String,
        title: String,
        valueTotal: Number,
    },

    computed: {
        circularValue() {
            if (!this.valueTotal) {
                return null
            } else {
                return 100 * this.value / this.valueTotal
            }
        },
        fontSize() {
            if (this.valueTotal) {
                return "0.8rem"
            } else {
                return "1rem"
            }
        },
        thickness() {
            if (this.valueTotal) {
                return 0.2
            } else {
                return 0
            }
        },
        color() {
            if (this.valueType === "learned") {
                return "green-7"
            } else if (this.valueType === "rating") {
                return "blue-10"
            } else {
                return "grey-7"
            }
        },
        getClassSuffixTotal() {
            if (this.valueTotal) {
                return "-with-total"
            } else {
                return "-no-total"
            }
        },
        getClass() {
            if (this.valueTotal) {
                return "type-" + this.valueType
            } else {
                return "type-" + this.valueType + " type-" + this.valueType + "-no-total"
            }
        }
    }

}
</script>

<style scoped>

.circular {
    text-align: center;
    font-weight: bold;
    border-radius: 10em;
}

.value-value {
    font-weight: bold;
    font-size: 3em;
}

.value-title {
    font-size: 1em;
}

.value-title-with-total {
    font-size: 1.3em;
}

.total {
    position: relative;
    top: 0.1em;
    color: #303030;
}

.total-title {
    font-weight: normal;
    font-size: 1em;
}

.total-value {
    font-weight: normal;
    font-size: 1.7em;
}


.type-learned {
    color: #109010;
}

.type-repeated {
    color: #008080;
}

.type-rating {
    color: #003090;
}

.type-learned-no-total {
    background-color: #10901010;
}

.type-repeated-no-total {
    background-color: #00808010;
}


</style>