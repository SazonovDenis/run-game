<template>

    <div :class="'input-container ' + getClassComponent()">

        <div v-if="isDev" style="position: absolute; z-index: 10; padding: 5px">
            info: {{ info }}
        </div>

        <div v-if="this.isCameraIniting === true && this.isStillImage !== true"
             class="rgm-state-text photo-state-wait">
            Инициализация камеры
        </div>

        <div v-if="this.wasCameraInitFail === true && this.isStillImage !== true"
             class="rgm-state-text photo-state-error">
            Нет доступа к камере
        </div>


        <div v-show="this.wasCameraInitOk === true || this.isStillImage === true"
             class="camera-container">

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


        <div v-if="searchDone" class="selected-list-container col">

            <div
                v-if="textSelected"
                class="q-pa-md rgm-state-text"
            >
                Слова &laquo;{{ textSelected }}&raquo; нет в словаре
            </div>

            <TaskList
                v-else
                :showEdit="true"
                :tasks="itemsListSelected"
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

import {jcBase} from "../vendor"
import {daoApi} from "../dao"
import ctx from "../gameplayCtx"
import {TouchZoom} from "../TouchZoom"
import TaskList from "./TaskList"

export default {

    components: {
        TaskList,
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

            itemsListSelected: [],
            textSelected: null,

            itemPositions: [],
            itemsIdIdx: {},
        }
    },

    computed: {

        isDev() {
            return jcBase.cfg.envDev
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
            // ---
            // Нахождение в списках

            //
            let isItem = false
            let isHidden = false
            let isInPlan = false
            //
            let positionItems = itemPosition.items
            if (positionItems) {
                for (let itemId of positionItems) {
                    let item = this.itemsIdIdx[itemId]
                    if (item) {
                        isItem = true
                        // Хоть один из positionItems находится в плане (скрыт) - считаем, что вся позиция в плане (скрыта)
                        isHidden = isHidden || item.isHidden
                        isInPlan = isInPlan || item.isInPlan
                    }
                }
            }

            // Окраска в зависимости от нахождения в списках
            let borderColor
            let backgroundColor
            //
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


            // ---
            // Расположение

            //
            let position = itemPosition.position
            //
            let imageRealWidth = this.stillWidth
            let kWidth = this.stillVisibleWidth / imageRealWidth
            //
            let left = Math.round(position.left * kWidth)
            let top = Math.round(position.top * kWidth)
            let width = Math.round(position.width * kWidth)
            let height = Math.round(position.height * kWidth)


            // ---
            let res = {
                borderColor: borderColor,
                backgroundColor: backgroundColor,
                left: left + "px",
                top: top + "px",
                width: width + "px",
                height: height + "px",
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
            this.itemsListSelected = []
            this.textSelected = null
        },

        onItemPositionClick(itemPosition) {
            let itemsLst = this.findItemsList(itemPosition.items)

            //
            if (itemsLst.length !== 0) {
                this.itemsListSelected = itemsLst
                this.textSelected = null
            } else {
                this.itemsListSelected = []
                this.textSelected = itemPosition.text
            }

            //
            for (let item of itemsLst) {
                this.$emit("itemClick", item)
            }

            return false
        },

        findItemsList(itemIds) {
            let items = []

            for (let itemId of itemIds) {
                let item = this.itemsIdIdx[itemId]

                //
                if (item) {
                    items.push(item)
                }
            }

            //
            return items
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
            this.clearData()
            await this.loadData(dataImage)

        },

        async loadData(dataImage) {
            //
            let resApi
            try {
                resApi = await daoApi.loadStore('m/Item/findStill', [dataImage, this.planId, {}])
            } finally {
                // Чтобы после ошибки кнопки снова появились
                this.searchDone = true
            }
            let resItems = resApi.facts.records
            let resItemsPositions = resApi.positions.records


            // --- Заполним родительский список

            // items
            this.items.length = 0
            for (let item of resItems) {
                this.items.push(item)
            }


            // ---

            // Получим список positions
            this.itemPositions.length = 0
            for (let position of resItemsPositions) {
                this.itemPositions.push(position)
            }

            // Создадим индекс по itemId - для обработки кликов
            this.itemsIdIdx = {}
            for (let item of this.items) {
                let itemId = item.item
                //
                this.itemsIdIdx[itemId] = item
            }

            // --- Уведомим родителя
            this.$emit("itemsChange", this.searchDone)
        },

        clearData(doEmitParent) {
            this.searchDone = false

            // Очистим родительский список
            this.items.length = 0

            // Очистим свои списки
            this.itemsListSelected = []
            this.textSelected = null
            //
            this.itemPositions = []
            this.itemsIdIdx = {}

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
.input-container {
    position: relative;
    justify-content: center;
}

@media (orientation: landscape) {
    .input-container {
        flex-direction: row-reverse;
    }
}

/* --- */


.camera-container {
    position: relative;
    z-index: 3000;
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
        height: calc(100vh - 4.4rem);
    }
}

@media (orientation: portrait) {
    .photo-container {
        width: 100vw;
        height: calc(70vh - 4.4rem);
    }
}


/* --- */


#videoStream {
    _border: 1px solid black;

    width: 100%;
    height: auto;
    max-height: calc(100vh - 4.4rem); /* чтобы не уходило ниже, чем высота экрана, за вычетом размера тулбара */
}


#photoStill {
    _border: 1px solid black;

    width: 100%;
    height: auto;
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

    left: 0.5em;
    bottom: 0.9em;

    padding: 0 1.5em;

    opacity: 0.8;
}

.photo-btn-file {
    height: 3em;

    left: 0.5em;
    bottom: 0.9em;

    padding: 0 1.5em;

    opacity: 0.8;
}

.zoom-btn {
    opacity: 0.6;
    position: absolute;

    right: 1em;
    margin-bottom: -1em;
    bottom: 50%;

    width: 2.5em;
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