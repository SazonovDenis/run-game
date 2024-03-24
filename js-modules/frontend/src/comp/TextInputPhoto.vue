<template>

    <div style="position: relative">
        <div v-if="isDev" style="position: absolute; z-index: 10; padding: 5px">
            info: {{ info }}
        </div>

        <div class="camera-container" :class="getClassCamera">

            <div _class="contentarea" @click="onPictureClick">

                <div :class="'video-container ' + getClassVideo">
                    <video id="video">Video stream will appear in this box</video>
                </div>

                <canvas id="canvas"></canvas>

                <div :class="'photo-container ' + getClassPhoto">
                    <img
                        ref="photo"
                        id="photo"
                        alt="The screen capture will appear in this box">

                    <div class="photo-word-positions">
                        <div v-for="position in itemPositions"
                             class="photo-word-position"
                             @click="onItemPositionClick(position)"
                             :style="getItemPositionSyle(position)"
                        >

                        </div>
                    </div>
                </div>

            </div>


            <div class="photo-btn-div">

                <q-btn v-if="isCameraCapturing===true"
                       rounded
                       class="photo-btn photo-btn-still"
                       color="accent"
                       icon="image"
                       no-caps
                       label="Распознать с камеры"
                       @click="onTakePicture"
                />

                <q-btn v-if="isCameraCapturing===false && searchDone===true"
                       rounded
                       class="photo-btn photo-btn-clear"
                       color="primary"
                       _icon="del"
                       no-caps
                       label="Новый снимок"
                       @click="onNewPicture"
                />

            </div>

        </div>

        <div :class="'rgm-state-text photo-state-wait ' + getClassCameraInit">
            Инициализация камеры
        </div>

        <div :class="'rgm-state-text photo-state-error ' + getClassCameraInitFalil">
            Нет доступа к камере
        </div>


        <div class="row">

            <slot name="toolbar"></slot>

        </div>

    </div>

</template>

<script>

import {apx} from "../vendor"
import {daoApi} from "../dao"
import TaskList from "./TaskList"
import MenuContainer from "./MenuContainer"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        planId: null,
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
        itemOnClick: {type: Function},
    },

    data() {
        return {
            width: 1280, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            wasCameraInit: false,
            wasCanplay: false,
            wasCameraInitFail: false,
            wasCameraInitOk: false,
            isCameraCapturing: false,

            searchDone: false,

            video: null,
            canvas: null,
            photo: null,

            info: null,

            itemPositions: [],
            itemsPositionsIdx: {},
        }
    },

    computed: {

        isDev() {
            return apx.jcBase.cfg.envDev
        },

        getClassCameraInit() {
            if (this.wasCameraInit === false) {
                return ""
            } else {
                return "hidden"
            }
        },

        getClassCameraInitFalil() {
            if (this.wasCameraInitFail === true) {
                return ""
            } else {
                return "hidden"
            }
        },

        getClassCamera() {
            if (this.wasCameraInitOk === true) {
                return ""
            } else {
                return "hidden"
            }
        },

        getClassVideo() {
            if (this.isCameraCapturing === true) {
                return ""
            } else {
                return "hidden"
            }
        },

        getClassPhoto() {
            if (this.isCameraCapturing === false) {
                return ""
            } else {
                return "hidden"
            }
        },

    },

    methods: {

        getItemPositionSyle(itemPosition) {
            let videoWidth = this.width
            let videoHeight = this.height
            //
            let image = this.$refs.photo
            let imageWidth = image.clientWidth
            let imageHeight = image.clientHeight

            //
            let kWidth = imageWidth / videoWidth

            //
            let itemId = itemPosition.item
            let item = this.itemsPositionsIdx[itemId]
            //
            let isItem
            let isHidden
            let isInPlan
            //
            if (item) {
                isItem = true
                isHidden = item.isHidden
                isInPlan = item.isInPlan
            } else {
                isItem = false
                isInPlan = false
                isHidden = false
            }

            let borderColor
            let backgroundColor

            if (isHidden) {
                borderColor = "rgba(20, 20, 230, 0.2)"
                backgroundColor = "rgba(20, 20, 230, 0.2)"
            } else if (isItem) {
                if (isInPlan) {
                    borderColor = "rgba(20, 230, 20, 0.4)"
                    backgroundColor = "rgba(20, 230, 20, 0.4)"
                } else {
                    borderColor = "rgb(20, 230, 20)"
                    backgroundColor = "rgba(20, 230, 20, 0.04)"
                }
            } else {
                borderColor = "rgba(230, 22, 22, 0.4)"
                backgroundColor = "none"
            }


            return {
                borderColor: borderColor,
                backgroundColor: backgroundColor,
                left: Math.round(itemPosition.left * kWidth) + "px",
                top: Math.round(itemPosition.top * kWidth) + "px",
                width: Math.round(itemPosition.width * kWidth) + "px",
                height: Math.round(itemPosition.height * kWidth) + "px",
            }
        },

        onItemPositionClick(itemPosition) {
            console.info(itemPosition)
            // //
            let itemId = itemPosition.item
            let item = this.itemsPositionsIdx[itemId]
            if (item && this.itemOnClick) {
                this.itemOnClick(item)
            }
        },

        onPictureClick() {
            //console.log("onPictureClick")
        },

        onTakePicture() {
            //console.log("onTakePicture")
            this.takePicture();
            this.isCameraCapturing = false
        },

        onNewPicture() {
            //console.log("onNewPicture")
            this.clearPhoto();
            this.clearData()
            this.isCameraCapturing = true
        },


        startup() {
            let th = this

            try {
                th.video = document.getElementById('video');
                th.canvas = document.getElementById('canvas');
                th.photo = document.getElementById('photo');

                //
                navigator.mediaDevices.getUserMedia({
                    video: {
                        width: { ideal: 1280 },
                        facingMode: 'environment'
                    },
                    audio: false

                }).then(function(stream) {
                    video.srcObject = stream;
                    video.play();

                    //
                    video.addEventListener('canplay', th.onCanplay, false)

                    //
                    th.wasCameraInit = true
                    th.wasCameraInitOk = true
                    th.isCameraCapturing = true

                }).catch(function(err) {
                    console.log("An error occurred: " + err);

                    //
                    th.wasCameraInit = true
                    th.wasCameraInitFail = true
                    th.isCameraCapturing = false
                });

            } catch(e) {
                console.log("An error occurred: " + e);

                th.info = e

                th.wasCameraInit = true
                th.wasCameraInitFail = true
                th.isCameraCapturing = false
            }

            //
            th.clearPhoto();
        },

        onCanplay() {
            if (!this.wasCanplay) {
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
            }
        },

        clearPhoto() {
            var context = canvas.getContext('2d');
            context.fillStyle = "#AAA";
            context.fillRect(0, 0, canvas.width, canvas.height);

            var data = canvas.toDataURL('image/png');
            photo.setAttribute('src', data);

            this.searchDone = false
        },

        async takePicture() {
            var canvasContext = canvas.getContext('2d');
            if (this.width && this.height) {
                //canvas.width = this.width;
                //canvas.height = this.height;
                canvasContext.drawImage(video, 0, 0, this.width, this.height);

                var dataImage = canvas.toDataURL('image/png');
                photo.setAttribute('src', dataImage);

                ///////////////////////////////
                this.info = "size: " + dataImage.length + ", " + this.width + "x" + this.height
                ///////////////////////////////

                //
                await this.loadData(dataImage)
            } else {
                this.clearPhoto();
            }
        },

        async loadData(dataImage) {
            //
            let resApi = await daoApi.loadStore('m/Game/findStill', [dataImage, this.planId])
            let resItems = resApi.facts.records
            let resItemsPositions = resApi.positions.records

            //
            this.searchDone = true


            // --- Заполним родительский список

            // items
            this.items.length = 0
            for (let item of resItems) {
                this.items.push(item)
            }

            // positions
            let plainPositions = []
            for (let item of resItemsPositions) {
                let positions = item.positions
                for (let itemPosition of positions) {
                    itemPosition.item = item.item
                    plainPositions.push(itemPosition)
                }
            }
            //
            this.itemPositions.length = 0
            for (let p of plainPositions) {
                this.itemPositions.push(p)
            }

            // Создадим индекс по item - для обработки кликов
            this.itemsPositionsIdx = {}
            for (let item of this.items) {
                let itemId = item.item
                this.itemsPositionsIdx[itemId] = item
            }


            // --- Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange(this.searchDone)
            }
        },

        clearData() {
            this.searchDone = false

            // Очистим родительский список
            this.items.length = 0

            // Очистим свои списки
            this.itemPositions = []
            this.itemsPositionsIdx = {}

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange(this.searchDone)
            }

        },

    },


    async mounted() {
        this.startup()
    },


}

</script>

<style scoped>

.camera-container {
    position: relative;
}

.video-container {
    display: inline-block;
}

.photo-container {
    display: inline-block;
    position: relative;
}

.photo-btn-div {
    position: absolute;
    bottom: 1em;
    right: 0.5em;
}

.photo-btn {
    height: 3em;
    _width: 4em;
}

.photo-btn-still {
    opacity: 0.7;
}

.photo-btn-clear {
    opacity: 0.8;
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
}

#canvas {
    display: none;
}

.hidden {
    display: none;
}

.state-text {
    font-size: 3em;
    text-align: center;

    animation-name: fade;
    animation-delay: .2s;
    animation-duration: 0.5s;
    animation-fill-mode: both;
}

@keyframes fade {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

.photo-state-wait {
    padding: 1em 0;
    color: grey;
}

.photo-state-error {
    padding: 1em 0;
    color: #850000;
}

.photo-word-positions {
    position: absolute;
    top: 0;
    left: 0;
}

.photo-word-position {
    position: absolute;
    color: rgba(192, 192, 192, 0.4);
    border-radius: 3px;
    border-width: 1px;
    border-style: solid;
}

</style>