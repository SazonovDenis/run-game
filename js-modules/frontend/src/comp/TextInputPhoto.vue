<template>

    <div style="position: relative; justify-content: center;"
         :class="getClassComponent()">

        <div v-if="isDev" style="position: absolute; z-index: 10; padding: 5px">
            info: {{ info }}
        </div>


        <div class="camera-container">

            <!-- Захват с камеры и canvas для стоп-кадра с неё -->
            <div v-show="this.isCameraCapturing === true" style="text-align: center;">

                <div class="video-container">
                    <video id="videoStream">Video stream will appear in this box</video>
                </div>

                <canvas id="canvas"></canvas>

            </div>


            <!-- Полученное изображение с показом позиций -->
            <div class="image-container">

                <div v-show="isStillImage === true"
                     ref="photoContainer"
                     class="photo-container"
                >

                    <!-- Полученное изображение -->
                    <img
                        ref="photoStill"
                        id="photoStill"
                        alt="The screen capture will appear in this box"
                        :style="getStylePhotoStill()"
                        @click="onPictureClick"
                    >

                    <!-- Показ позиций -->
                    <div class="photo-word-positions">
                        <div v-for="position in itemPositions"
                             class="photo-word-position"
                             :style="getItemPositionSyle(position)"
                             @click="onItemPositionClick(position)"
                        >

                        </div>
                    </div>
                </div>


                <!-- Кнопки управления: фото/новый снимок и т.д. -->
                <q-icon v-if="isCameraCapturing === true"
                        class="photo-btn photo-btn-still"
                        name="circle"
                        color="grey-2"
                        size="3.5em"
                        @click="onTakePicture"
                />

                <q-btn v-if="isCameraCapturing === false && searchDone === true"
                       rounded
                       class="photo-btn photo-btn-clear"
                       color="accent"
                       no-caps
                       label="Новый снимок"
                       @click="onNewPicture"
                />

                <q-btn v-if="isCameraCapturing === true || searchDone === true"
                       rounded
                       class="photo-btn photo-btn-file"
                       color="secondary"
                       no-caps
                       icon="image"
                       :label="labelChooseFile()"
                       @click="onChooseFile"
                />

                <div v-if="isCameraCapturing === false && searchDone === true"
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


        </div>


        <div v-if="this.isCameraIniting === true"
             class="rgm-state-text photo-state-wait">
            Инициализация камеры
        </div>

        <div v-if="this.wasCameraInitFail === true"
             class="rgm-state-text photo-state-error">
            Нет доступа к камере
        </div>


        <div v-if="searchDone" class="selected-list-container col">

            <TaskList
                :showEdit="true"
                :tasks="itemsSelectedList"
                :itemsMenu="itemsMenu"
                messageNoItems="Выберите слово на фото"
            />

        </div>


        <!-- slot: toolbar -->
        <div class="row">

            <slot name="toolbar"></slot>

        </div>

    </div>

</template>

<script>

import {apx} from "../vendor"
import {daoApi} from "../dao"
import ctx from "../gameplayCtx"
import {TouchZoom} from "../TouchZoom"
import TaskList from "./TaskList"

export default {

    components: {
        TaskList
    },

    props: {
        planId: null,
        image: null,
        items: {type: Array, default: []},
        itemsMenu: {type: Array, default: []},
    },

    data() {
        return {
            //
            imagePreferableWidth: 1024 * 2,

            // Размер кадра видеопотока с камеры
            videoStreamWidth: 0,
            videoStreamHeight: 0,

            // Размер снимка (с камеры или из файла)
            stillWidth: null,
            stillHeight: null,

            // Видимый размер области просмотра снимка
            elImageClientWidth: 0,
            elImageClientHeight: 0,
            // Видимый размер снимка (используется при zoom in/out)
            stillVisibleWidth: null,

            wasCameraInit: false,
            wasCameraInitFail: false,
            wasCameraInitOk: false,
            isCameraIniting: false,
            isCameraCapturing: false,

            isStillImage: false,
            stillImage: null,

            searchDone: false,

            videoStream: null,
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

    },

    methods: {

        getClassComponent() {
            if (window.screen.orientation.type === "portrait-primary") {
                return "col"
            } else {
                return "row"
            }
        },

        getItemPositionSyle(itemPosition) {
            let imageRealWidth = this.stillWidth
            let imageRealHeight = this.stillHeight
            //
            let imageWidth = this.stillWidth
            let imageHeight = this.stillHeight

            //
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

            if (this.stillVisibleWidth > this.stillWidth * 2) {
                this.stillVisibleWidth = this.stillWidth * 2
            }
            if (this.stillVisibleWidth < this.elImageClientWidth) {
                this.stillVisibleWidth = this.elImageClientWidth
            }
        },

        onPhotoStillInc() {
            this.stillVisibleWidth = Math.trunc(this.stillVisibleWidth + this.stillVisibleWidth * 0.25)
            if (this.stillVisibleWidth > this.stillWidth * 2) {
                this.stillVisibleWidth = this.stillWidth * 2
            }
        },

        onPhotoStillDec() {
            this.stillVisibleWidth = Math.trunc(this.stillVisibleWidth - this.stillVisibleWidth * 0.25)
            if (this.stillVisibleWidth < this.elImageClientWidth) {
                this.stillVisibleWidth = this.elImageClientWidth
            }
        },

        onPictureClick() {
            this.itemsSelectedList = []
        },

        onItemPositionClick(itemPosition) {
            let itemsLst = this.findItemsList(itemPosition)
            //console.info("onItemPosition click", itemsLst)

            //
            this.itemsSelectedList = itemsLst

            //
            for (let item1 of itemsLst) {
                this.$emit("itemClick", item1)
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

        async onNewPicture() {
            this.clearData(true)
            this.clearImage();
            //
            this.searchDone = false
            this.isStillImage = false
            //
            await this.checkCameraInit()
            //
            this.isCameraCapturing = true
        },

        labelChooseFile() {
            if (this.searchDone) {
                return "Новый файл"
            } else {
                if (Jc.cfg.is.mobile) {
                    return "Из галереи"
                } else {
                    return "Из файла"
                }
            }
        },

        onChooseFile() {
            this.clearData(true)
            this.clearImage();
            //
            this.searchDone = false
            this.isStillImage = false
            //
            this.$emit("fileChoose")
        },

        onItemsCleared() {
            this.clearData(false)
            this.clearImage();
            //
            this.searchDone = false
            this.isStillImage = false
        },

        async onEditModeChanged(frameMode) {
            if (frameMode === "addByPhoto") {
                await this.checkCameraInit()
                this.isCameraCapturing = true
            }
        },

        async checkCameraInit() {
            if (!this.wasCameraInit) {
                await this.initCamera()
            }
        },

        async initCamera() {
            let th = this

            try {
                th.isCameraIniting = true

                th.videoStream = document.getElementById('videoStream');
                th.canvas = document.getElementById('canvas');
                th.photoStill = document.getElementById('photoStill');

                //
                await navigator.mediaDevices.getUserMedia({
                    video: {
                        width: {ideal: this.imagePreferableWidth},
                        facingMode: 'environment'
                    },
                    audio: false

                }).then(function(stream) {
                    videoStream.srcObject = stream;
                    videoStream.play();

                    //
                    videoStream.addEventListener('canplay', th.onCanplay, false)

                    //
                    th.wasCameraInit = true
                    th.wasCameraInitOk = true
                    th.isCameraCapturing = true
                    th.isCameraIniting = false

                    //
                    th.touchZoom.connect(th.photoStill)

                }).catch(function(err) {
                    console.log("An error occurred: " + err);

                    //
                    th.wasCameraInit = true
                    th.wasCameraInitFail = true
                    th.isCameraCapturing = false
                    th.isCameraIniting = false

                });

            } catch(err) {
                console.log("An error occurred: " + err);

                th.info = err

                th.wasCameraInit = true
                th.wasCameraInitFail = true
                th.isCameraCapturing = false
                th.isCameraIniting = false
            }

            //
            th.clearImage();
        },

        clearImage() {
            var context = canvas.getContext('2d');
            context.fillStyle = "#AAA";
            context.fillRect(0, 0, canvas.width, canvas.height);

            var data = canvas.toDataURL('image/png');
            photoStill.setAttribute('src', data);
        },

        onCanplay() {
            // Запомним размер кадра видеопотока
            this.videoStreamHeight = videoStream.videoHeight;
            this.videoStreamWidth = videoStream.videoWidth;

            // Размер приёмника выставим по размеру кадра видеопотока
            canvas.setAttribute('width', this.videoStreamWidth);
            canvas.setAttribute('height', this.videoStreamHeight);

            // Запомним видимый размер области просмотра снимка
            this.elImageClientWidth = videoStream.clientWidth
            this.elImageClientHeight = videoStream.clientHeight

            //
            this.info = "video: " + videoStream.videoWidth + "x" + videoStream.videoHeight
            console.info(this.info)
        },

        async takePicture() {
            if (!this.wasCameraInitOk) {
                return
            }

            // Заберем картинку с окна захвата
            var canvasContext = canvas.getContext('2d');
            canvasContext.drawImage(videoStream, 0, 0, this.videoStreamWidth, this.videoStreamHeight);
            var dataImage = canvas.toDataURL('image/png');

            // Отреагируем
            await this.applyImage(dataImage)
        },

        async applyImage(dataImage) {
            this.info = "size: " + dataImage.length + ", " + this.videoStreamWidth + "x" + this.videoStreamHeight
            console.info(this.info)

            // Нарисуем себе
            photoStill.setAttribute('src', dataImage);

            // Переключаем режимы показа
            this.isCameraCapturing = false
            this.isStillImage = true
            this.stillImage = dataImage


            // Прочитаем фактический размер изображения и его контейнера.
            // Обязательно после await nextTick() и после изменения флагов, т.к.
            // свойство photoStill.naturalWidth будет правильно вычислятся только
            // после завершения обработки photoStill.setAttribute,
            // а photoContainer.clientWidth будет правильно вычислятся после рендеринга.
            await this.$nextTick()

            // Прочитаем фактический размер изображения.
            this.stillWidth = photoStill.naturalWidth
            this.stillHeight = photoStill.naturalHeight

            // Прочитаем фактический размер контейнера изображения на странице.
            let photoContainer = this.$refs.photoContainer
            this.elImageClientWidth = photoContainer.clientWidth
            //
            console.info("photoContainer: " + photoContainer.clientWidth + "x" + photoContainer.clientHeight)
            console.info("photoStill: " + photoStill.naturalWidth + "x" + photoStill.naturalHeight)


            // Исходно видимый размер снимка - по ширине контейнера изображения
            this.stillVisibleWidth = this.elImageClientWidth


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
            this.$emit("itemsChange", this.searchDone)
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
            if (doEmitParent) {
                this.$emit("itemsChange", this.searchDone)
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


#canvas {
    display: none;
}


/* --- */


.camera-container {
    position: relative;
}


.videoStream-container {
    display: inline-block;
}


/* --- */

.photo-container {
    display: inline-block;
    position: relative;
    overflow: scroll;
}

@media (orientation: landscape) {
    .photo-container {
        max-width: 50vw;
        height: calc(100vh - 3.125rem);
    }
}

@media (orientation: portrait) {
    .photo-container {
        width: 100vw;
        height: calc(70vh - 3.125rem);
    }
}


/* --- */


#videoStream {
    _border: 1px solid black;

    width: 100%;
    height: auto;
    max-height: calc(100vh - 3.125rem); /* чтобы не уходило ниже, чем высота экрана, за вычетом размера тулбара */
}


#photoStill {
    _border: 1px solid black;

    width: 100%;
    height: auto;
}


/* --- */


.selected-list-container {
    overflow: scroll;
}

@media (orientation: landscape) {
    .selected-list-container {
    }
}

@media (orientation: portrait) {
    .selected-list-container {
        height: calc(30vh - 3.125rem); /* чтобы не уходило ниже, чем высота экрана, за вычетом размера тулбара */
    }
}


/* --- */

.photo-btn {
    position: absolute;
}


.photo-btn-still {
    opacity: 0.7;

    border: 2px solid;
    border-radius: 10em;
}

@media (orientation: landscape) {
    .photo-btn-still {
        top: 50%;
        margin-top: -0.5em;
        right: 0.2em;
    }
}

@media (orientation: portrait) {
    .photo-btn-still {
        left: 50%;
        margin-left: -0.5em;
        bottom: 0.2em;
    }
}

.photo-btn-clear {
    height: 3em;

    right: 0.5em;
    bottom: 0.9em;

    padding: 0 1.5em;

    opacity: 0.7;
}

.photo-btn-file {
    height: 3em;

    left: 0.5em;
    bottom: 0.9em;

    padding: 0 1.5em;

    opacity: 0.7;
}

.zoom-btn {
    opacity: 0.6;
    position: absolute;

    right: 1em;
    margin-bottom: -1em;
    bottom: 50%;

    width: 2.5em;
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