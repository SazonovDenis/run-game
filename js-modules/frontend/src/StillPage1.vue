<template>

    <MenuContainer title="Capture file">

        <div>
            info: {{ info }}
        </div>


        <div class="contentarea">

            <div :class="'photo ' + getClassPhoto">
                <img id="photo" alt="The screen capture will appear in this box." src="">
            </div>

        </div>


        <q-input
            v-model="words"
            filled
            autogrow
            type="textarea"
        />


        <q-page-sticky position="bottom-right" :offset="[18, 18]">

            <label>
                <input id="myFileInput"
                       type="file"
                       accept="image/*"
                       capture="camera"
                >

<!--
                <div _v-if="isCaptured===true"
                >
                    <span>Выберите!</span>
                </div>

-->

                    <q-btn v-if="isCaptured===true"
                           style="height: 4em; width: 4em;"
                           round
                           color="primary"
                           icon="add"                     @click="onTakePicture"

                    />

<!--
                <q-btn v-if="isCaptured===true"
                       style="height: 4em; width: 4em;"
                       round
                       color="primary"
                       icon="add"
                       @click="onTakePicture"
                />
-->

<!--
                <span>Выберите файл</span>
-->

            </label>

            <q-btn v-if="isCaptured===false"
                   style="height: 4em; width: 4em;"
                   round
                   color="secondary"
                   icon="del"
                   @click="onNewPicture"
            />

        </q-page-sticky>


    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import {daoApi} from "./dao"
import ctx from "./gameplayCtx"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"

export default {

    components: {
        MenuContainer
    },

    data() {
        return {
            globalState: ctx.getGlobalState(),

            width: 1024, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            canvas: null,
            photo: null,
            //startbutton: null,

            isCaptured: null,
            words: null,
            info: null,

            emptyImage: "data:image/gif;base64,R0lGODlhAQABAIAAAP7//wAAACH5BAAAAAAALAAAAAABAAEAAAICRAEAOw==",
        }
    },

    computed: {

        getClassPhoto() {
            return ""

            if (this.isCaptured === false) {
                return ""
            } else {
                return "hidden"
            }
        },

    },

    methods: {
        onTakePicture() {
            photo.setAttribute('src', this.emptyImage);

            //this.takepicture();
            this.isCaptured = false

            console.log("onTakePicture")
        },

        onNewPicture() {
            photo.setAttribute('src', this.emptyImage);

            this.isCaptured = true
            this.words = null

            console.log("onNewPicture")
        },


        startup() {
            var myInput = document.getElementById('myFileInput');
            myInput.addEventListener('change', this.sendPic, false);

            //
            this.photo = document.getElementById('photo');
            photo.setAttribute('src', this.emptyImage);

            //
            this.isCaptured = true
        },


        async sendPic() {
            var myInput = document.getElementById('myFileInput');

            if (myInput.files.length == 0) {
                this.isCaptured = true
                return
            }

            var file = myInput.files[0];
            console.log(file);


            let reader = new FileReader();
            reader.readAsDataURL(file);

            let th = this

            reader.onload = async function() {
                console.log("reader.result");

                var dataImage = reader.result;
                photo.setAttribute('src', dataImage);

                // -----------------
                let res = await daoApi.invoke('m/Ocr/ParseStill', [dataImage])
                th.words = res
            };

            reader.onerror = function() {
                console.log(reader.error);
            };
        },

    },

    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        this.startup()
    },


}
</script>

<style>

.video {
    display: inline-block;
}

.photo {
    display: inline-block;
}

.contentarea {
    font-size: 16px;
    text-align: center;
}

#video {
    border: 1px solid black;
    width: 100%;
    height: auto;
}

#photo {
    border: 1px solid black;

    width: 100%;
    height: auto;
    _aspect-ratio: auto;
}

#canvas {
    display: none;
}

#myFileInput {
    display: none;
}


.hidden {
    display: none;
}

</style>