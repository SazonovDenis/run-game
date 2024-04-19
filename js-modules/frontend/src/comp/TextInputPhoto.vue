<template>

    <div style="position: relative">
        <div v-if="isDev" style="position: absolute; z-index: 10; padding: 5px">
            info: {{ info }}
        </div>

        <div class="camera-container" :class="getClassCamera">

            <div>

                <div :class="'video-container ' + getClassVideo">
                    <video id="video">Video stream will appear in this box</video>
                </div>

                <canvas id="canvas"></canvas>

                <div :class="'photo-container ' + getClassPhoto">
                    <img
                        ref="photoStill"
                        id="photoStill"
                        alt="The screen capture will appear in this box"
                        :style="getStylePhotoStill()"
                        @click="onPictureClick"
                    >

                    <div class="photo-word-positions">
                        <div v-for="position in itemPositions"
                             class="photo-word-position"
                             :style="getItemPositionSyle(position)"
                             @click="onItemPositionClick(position)"
                        >

                        </div>
                    </div>
                </div>

            </div>


            <q-icon v-if="isCameraCapturing===true"
                    class="photo-btn photo-btn-still"
                    name="circle"
                    color="white"
                    size="3.5em"
                    @click="onTakePicture"
            />

            <q-btn v-if="isCameraCapturing===false && searchDone===true"
                   rounded
                   class="photo-btn photo-btn-clear"
                   color="accent"
                   no-caps
                   label="Новый снимок"
                   @click="onNewPicture"
            />

            <div v-if="isCameraCapturing===false && searchDone===true"
                 class="col zoom-btn"
            >

                <q-btn
                    round
                    color="grey-3"
                    text-color="grey-10"
                    icon="add"
                    size="1em"
                    @click="onPhotoStillInc()"
                />
                <q-btn round
                       color="grey-3"
                       text-color="grey-10"
                       style="margin-top: 0.2em"
                       icon="quasar.editor.hr"
                       size="1em"
                       @click="onPhotoStillDec()"
                />
            </div>


        </div>


        <div :class="'rgm-state-text photo-state-wait ' + getClassCameraInit">
            Инициализация камеры
        </div>

        <div :class="'rgm-state-text photo-state-error ' + getClassCameraInitFalil">
            Нет доступа к камере
        </div>


        <TaskList
            v-if="searchDone"
            :showEdit="true"
            :tasks="itemsSelectedList"
            :itemsMenu="itemsMenu"
            messageNoItems="Выберите слово на фото"
        />


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
import ctx from "../gameplayCtx"
import {TouchZoom} from "../TouchZoom"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        planId: null,
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
        itemOnClick: {type: Function},
        itemsMenu: {type: Array, default: []},
    },

    data() {
        return {
            width: 1280, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            imageClientWidth: null,
            imageClientHeight: null,

            stillVisibleWidth: null,

            wasCameraInit: false,
            wasCanplay: false,
            wasCameraInitFail: false,
            wasCameraInitOk: false,
            isCameraCapturing: false,

            searchDone: false,

            video: null,
            canvas: null,
            photoStill: null,

            info: null,

            itemsSelectedList: [],

            itemPositions: [],
            itemsPositionsIdx: {},
            itemsPositionsIdxList: {},
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
            let imageRealWidth = this.width
            let imageRealHeight = this.height
            //
            let imageWidth = this.imageClientWidth
            let imageHeight = this.imageClientHeight

            //
            //let kWidth = imageWidth / videoWidth
            let kWidth = this.stillVisibleWidth / imageRealWidth

            //
            let itemId = itemPosition.item
            //let item = this.itemsPositionsIdx[itemId]
            let itemsLst = this.itemsPositionsIdxList[itemId]

            //
            let isItem = false
            let isHidden = false
            let isInPlan = false
            //
            if (itemsLst) {
                for (let i = 0; i < itemsLst.length; i++) {
                    let item = itemsLst[i]
                    isItem = true
                    isHidden = isHidden || item.isHidden
                    isInPlan = isInPlan || item.isInPlan
                }
            }

            let borderColor
            let backgroundColor

            if (isItem) {
                if (isInPlan) {
                    borderColor = "rgba(20, 230, 20, 0.3)"
                    backgroundColor = "rgba(20, 230, 20, 0.3)"
                } else if (isHidden) {
                    borderColor = "rgba(20, 20, 230, 0.2)"
                    backgroundColor = "rgba(20, 20, 230, 0.2)"
                } else {
                    borderColor = "rgba(20, 230, 20, 0.4)"
                    backgroundColor = "rgba(20, 230, 20, 0.04)"
                }
            } else {
                borderColor = "rgba(230, 22, 22, 0.4)"
                backgroundColor = "none"
            }


            //
            let res = {
                borderColor: borderColor,
                backgroundColor: backgroundColor,
                left: Math.round(itemPosition.left * kWidth) + "px",
                top: Math.round(itemPosition.top * kWidth) + "px",
                width: Math.round(itemPosition.width * kWidth) + "px",
                height: Math.round(itemPosition.height * kWidth) + "px",
            }

            return res
        },


        getStylePhotoStill() {
            return {width: this.stillVisibleWidth + "px"}
        },


        onPhotoStillZoomStart() {
            console.info("onPhotoStillZoomStart")
            this.stillVisibleWidth_ZoomStart = this.stillVisibleWidth
        },


        onPhotoStillZoom(zoomInfo) {
            console.info("onPhotoStillZoom", zoomInfo)

            this.stillVisibleWidth = Math.trunc(this.stillVisibleWidth_ZoomStart * zoomInfo.zoomRate)

            if (this.stillVisibleWidth > this.width * 3) {
                this.stillVisibleWidth = this.width
            }
            if (this.stillVisibleWidth < this.imageClientWidth) {
                this.stillVisibleWidth = this.imageClientWidth
            }
        },

        onPhotoStillInc() {
            this.stillVisibleWidth = Math.trunc(this.stillVisibleWidth + this.stillVisibleWidth * 0.25)
            if (this.stillVisibleWidth > this.width * 3) {
                this.stillVisibleWidth = this.width
            }
        },

        onPhotoStillDec() {
            this.stillVisibleWidth = Math.trunc(this.stillVisibleWidth - this.stillVisibleWidth * 0.25)
            if (this.stillVisibleWidth < this.imageClientWidth) {
                this.stillVisibleWidth = this.imageClientWidth
            }
        },

        onPictureClick() {
            //console.info("onPictureClick")
            this.itemsSelectedList = []
        },

        onItemPositionClick(itemPosition) {
            let itemsLst = this.findItemsList(itemPosition)
            //console.info("onItemPosition click", itemsLst)

            //
            this.itemsSelectedList = itemsLst

            //
            if (this.itemOnClick) {
                for (let item1 of itemsLst) {
                    this.itemOnClick(item1)
                }
            }

            return false
        },

        findItemsList(itemPosition) {
            let itemId = itemPosition.item
            let item = this.itemsPositionsIdx[itemId]
            let itemsLst = this.itemsPositionsIdxList[itemId]

            //
            if (!itemsLst) {
                itemsLst = []
            }

            //
            return itemsLst
        },

        onTakePicture() {
            this.takePicture();
            this.isCameraCapturing = false
        },

        onNewPicture() {
            this.clearData(true)
            this.clearPhoto();
            this.isCameraCapturing = true
        },

        onItemsCleared() {
            this.clearData(false)
            this.clearPhoto();
            this.isCameraCapturing = true
        },

        onEditModeChanged() {
            if (!this.wasCameraInit) {
                this.startup()
            }
        },

        startup() {
            let th = this

            try {
                th.video = document.getElementById('video');
                th.canvas = document.getElementById('canvas');
                th.photoStill = document.getElementById('photoStill');

                //
                navigator.mediaDevices.getUserMedia({
                    video: {
                        width: {ideal: 1280},
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

                    //
                    th.touchZoom.connect(th.photoStill)

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
            ///////////////////////////////
            console.info("video: " + video.videoWidth + "x" + video.videoHeight)
            this.info = "video: " + video.videoWidth + "x" + video.videoHeight + ", this: " + this.width + "x" + this.height
            ///////////////////////////////

            //
            this.height = video.videoHeight / (video.videoWidth / this.width);

            //
            video.setAttribute('width', this.width);
            video.setAttribute('height', this.height);
            canvas.setAttribute('width', this.width);
            canvas.setAttribute('height', this.height);
        },

        clearPhoto() {
            var context = canvas.getContext('2d');
            context.fillStyle = "#AAA";
            context.fillRect(0, 0, canvas.width, canvas.height);

            var data = canvas.toDataURL('image/png');
            photoStill.setAttribute('src', data);

            this.searchDone = false
        },

        async takePicture() {
            if (!this.wasCameraInitOk) {
                return
            }

            //
            this.imageClientWidth = video.clientWidth
            this.imageClientHeight = video.clientHeight

            //
            this.stillVisibleWidth = video.clientWidth

            //
            var canvasContext = canvas.getContext('2d');
            canvasContext.drawImage(video, 0, 0, this.width, this.height);

            var dataImage = canvas.toDataURL('image/png');
            photoStill.setAttribute('src', dataImage);

            ///////////////////////////////
            this.info = "size: " + dataImage.length + ", " + this.width + "x" + this.height
            ///////////////////////////////

            //
            await this.loadData(dataImage)
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
            this.itemsPositionsIdxList = {}
            for (let item of this.items) {
                let itemId = item.item
                this.itemsPositionsIdx[itemId] = item
                //
                let lst = this.itemsPositionsIdxList[itemId]
                if (!lst) {
                    lst = []
                    this.itemsPositionsIdxList[itemId] = lst
                }
                lst.push(item)
            }


            // --- Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange(this.searchDone)
            }
        },

        clearData(doEmitParent) {
            this.searchDone = false

            // Очистим родительский список
            this.items.length = 0

            // Очистим свои списки
            this.itemsSelectedList = []
            //
            this.itemPositions = []
            this.itemsPositionsIdx = {}
            this.itemsPositionsIdxList = {}

            // Уведомим родителя
            if (this.itemsOnChange && doEmitParent) {
                this.itemsOnChange(this.searchDone)
            }

        },

    },

    mounted() {
        ctx.eventBus.on('itemsCleared', this.onItemsCleared)
        ctx.eventBus.on('editModeChanged', this.onEditModeChanged)

        //
        this.touchZoom = new TouchZoom()
        //
        ctx.eventBus.on('zoomStart', this.onPhotoStillZoomStart)
        ctx.eventBus.on('zoomIn', this.onPhotoStillZoom)
        ctx.eventBus.on('zoomOut', this.onPhotoStillZoom)
    },

    unmounted() {
        ctx.eventBus.off('itemsCleared', this.onItemsCleared)
        ctx.eventBus.off('editModeChanged', this.onEditModeChanged)

        //
        this.touchZoom.disconnect()
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
    overflow: scroll;
    width: 100%;
    height: 30em;
}

.photo-btn {
    position: absolute;
}

.photo-btn-still {
    left: 50%;
    margin-left: -0.5em;
    bottom: 0.2em;

    opacity: 0.6;

    border: 2px solid;
    border-radius: 10em;
}

.photo-btn-clear {
    height: 3em;
    right: 0.5em;
    bottom: 0.9em;

    padding: 0 1.5em;

    opacity: 0.7;
}

.zoom-btn {
    opacity: 0.6;
    position: absolute;
    right: 1em;
    top: 6.5em;
    width: 2.5em;
}

#video {
    border: 1px solid black;
    width: 100%;
    height: auto;
}

#photoStill {
    border: 1px solid black;
    _width: 100%;
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