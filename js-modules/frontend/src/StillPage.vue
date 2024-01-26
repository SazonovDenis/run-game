<template>

    <MenuContainer title="Capture photo">

        <div>
            info: {{ info }}
        </div>


        <div class="contentarea">
            <div :class="'video ' + getClassVideo">
                <video id="video">Video stream not available.</video>
            </div>

            <canvas id="canvas"></canvas>

            <div :class="'photo ' + getClassPhoto">
                <img id="photo" alt="The screen capture will appear in this box.">
            </div>
        </div>


        <q-input
            v-model="words"
            filled
            autogrow
            type="textarea"
        />


        <q-page-sticky position="bottom-right" :offset="[18, 18]">

            <q-btn v-if="isCapturing===true"
                   style="height: 4em; width: 4em;"
                   round
                   color="accent"
                   icon="add"
                   @click="onTakePicture"
            />

            <q-btn v-if="isCapturing===false"
                   style="height: 4em; width: 4em;"
                   round
                   color="primary"
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

            streaming: false,

            video: null,
            canvas: null,
            photo: null,
            //startbutton: null,

            isCapturing: null,
            words: null,
            info: null,
        }
    },

    computed: {

        getClassVideo() {
            if (this.isCapturing === true) {
                return ""
            } else {
                return "hidden"
            }
        },

        getClassPhoto() {
            if (this.isCapturing === false) {
                return ""
            } else {
                return "hidden"
            }
        },

    },

    methods: {
        onTakePicture() {
            this.takepicture();
            this.isCapturing = false
            console.log("onTakePicture")
        },

        onNewPicture() {
            this.clearphoto();
            this.isCapturing = true
            this.words = null
            console.log("onNewPicture")
        },


        startup() {
            this.video = document.getElementById('video');
            this.canvas = document.getElementById('canvas');
            this.photo = document.getElementById('photo');

            navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: 'environment'
                },
                audio: false
            }).then(function(stream) {
                console.info("stream", stream)
                video.srcObject = stream;
                video.play();
            }).catch(function(err) {
                console.log("An error occurred: " + err);
            });

            video.addEventListener('canplay', this.canplay, false)

            this.clearphoto();

            //
            this.isCapturing = true
        },

        canplay() {
            if (!this.streaming) {
                ///////////////////////////////
                console.info("video: " + video.videoWidth + "x" + video.videoHeight)
                this.info = "video: " + video.videoWidth + "x" + video.videoHeight
                ///////////////////////////////

                this.height = video.videoHeight / (video.videoWidth / this.width);
                //
/*
                if (isNaN(this.height)) {
                    this.height = this.width / (4 / 3);
                }
*/

                this.info = "video: " + video.videoWidth + "x" + video.videoHeight + ", this: " + this.width + "x" + this.height

                //
                video.setAttribute('width', this.width);
                video.setAttribute('height', this.height);
                canvas.setAttribute('width', this.width);
                canvas.setAttribute('height', this.height);

                //
                this.streaming = true;
            }
        },

        clearphoto() {
            var context = canvas.getContext('2d');
            context.fillStyle = "#AAA";
            context.fillRect(0, 0, canvas.width, canvas.height);

            var data = canvas.toDataURL('image/png');
            photo.setAttribute('src', data);
        },

        async takepicture() {
            var canvasContext = canvas.getContext('2d');
            if (this.width && this.height) {
                //canvas.width = this.width;
                //canvas.height = this.height;
                canvasContext.drawImage(video, 0, 0, this.width, this.height /*, 0, 0, this.width, this.height*/);

                var dataImage = canvas.toDataURL('image/png');
                photo.setAttribute('src', dataImage);

                ///////////////////////////////
                this.info = "size: " + dataImage.length + ", " + this.width + "x" + this.height
                ///////////////////////////////

                // -----------------
                let res = await daoApi.invoke('m/Ocr/ParseStill', [dataImage])
                this.words = res
                // -----------------
                /*
                                let th = this

                                //
                                let url = apx.url.ref("api")
                                let data = {
                                    //id: id,
                                    method: "m/Ocr/ParseStill",
                                    params: [dataImage],
                                }

                                await apx.ajax.request({
                                    url: url,
                                    method: "post",
                                    params: {},
                                    data: data,
                                    waitShow: true,
                                }).then((resp) => {
                                    console.info(`  resp:`, resp.data)
                                    console.info(`result:`, resp.data.result)
                                    // готово
                                    th.words = resp.data.result
                                })
                */
            } else {
                this.clearphoto();
            }
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


.hidden {
    display: none;
}

</style>