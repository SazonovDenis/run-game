<template>
    <!--
            ^c функционал с фотографирования вынести отдельно и распросранить на все режимы
        просмотр всех списков (скрытые, показанные, удаленные, добавленные)
        + возможность удалить план (также через промежуточное стстояние с кнопкой подтверждение "Сохранить")
    -->

    <div>
        <div>
            info: {{ info }}
        </div>

        <div class="photo-container" :class="getClassCamera">

            <div _class="contentarea" @click="onPictureClick">

                <div :class="'video ' + getClassVideo">
                    <video id="video">Video stream not available.</video>
                </div>

                <canvas id="canvas"></canvas>

                <div :class="'photo ' + getClassPhoto">
                    <img id="photo"
                         alt="The screen capture will appear in this box.">
                </div>

            </div>


            <div class="photo-btn-div">

                <q-btn v-if="isCameraCapturing===true"
                       round
                       class="photo-btn"
                       color="accent"
                       icon="image"
                       @click="onTakePicture"
                />

                <q-btn v-if="isCameraCapturing===false && dataLoaded===true"
                       round
                       class="photo-btn"
                       color="primary"
                       icon="del"
                       @click="onNewPicture"
                />

            </div>

        </div>

        <div :class="'photo-camera-state photo-state-wait ' + getClassCameraInit">
            Инициализация камеры
        </div>

        <div :class="'photo-camera-state photo-state-error ' + getClassCameraInitFalil">
            Нет доступа к камере
        </div>


        <div class="row">
            <q-btn
                v-if="items.length > 0"
                no-caps
                class="q-ma-sm"
                icon="quasar.stepper.done"
                label="Выбрать все"
                @click="selectAll()"
            />


            <slot name="toolbar"></slot>


        </div>

    </div>

</template>

<script>

import {daoApi} from "../dao"
import TaskList from "./TaskList"
import MenuContainer from "./MenuContainer"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
    },

    data() {
        return {
            width: 1024, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            wasCameraInit: false,
            wasCanplay: false,
            wasCameraInitFail: false,
            wasCameraInitOk: false,
            isCameraCapturing: false,

            dataLoaded: false,

            video: null,
            canvas: null,
            photo: null,

            info: null,
        }
    },

    computed: {

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

        /*
                itemHideMenuIcon(taskItem) {
                    if (taskItem.isHidden) {
                        return "visibility-off"
                    } else {
                        return "visibility"
                    }
                },

                itemHideMenuColor(taskItem) {
                    if (taskItem.isHidden) {
                        let posItemsHide = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                        if (posItemsHide === -1) {
                            return "grey-6"
                        } else {
                            return "red-8"
                        }
                    } else {
                        let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)
                        if (posItemsHideDel === -1) {
                            return "grey-6"
                        } else {
                            return "red-8"
                        }
                    }
                },

                itemHideMenuClick(taskItem) {
                    taskItem.isHidden = !taskItem.isHidden
                    //
                    if (taskItem.isHidden) {
                        taskItem.isKnownGood = false
                        taskItem.isKnownBad = false
                    }
                    //
                    //ctx.gameplay.api_saveUsrFact(taskItem.factQuestion, taskItem.factAnswer, taskItem)

                    //
                    let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
                    let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                    let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)

                    //
                    if (taskItem.isHidden) {
                        if (posItemsHideAdd === -1 && posItemsHideDel === -1) {
                            this.itemsHideAdd.push(taskItem)
                        }
                        if (posItemsHideDel !== -1) {
                            this.itemsHideDel.splice(posItemsHideDel, 1)
                        }
                        if (posItemsAdd !== -1) {
                            this.itemsAdd.splice(posItemsAdd, 1)
                            taskItem.isInItemsAdd = false
                        }
                    } else {
                        if (posItemsHideAdd !== -1) {
                            this.itemsHideAdd.splice(posItemsHideAdd, 1)
                        }
                        if (posItemsHideDel === -1 && posItemsHideAdd === -1) {
                            this.itemsHideDel.push(taskItem)
                        }
                    }

                },


                itemDeleteMenuIcon(taskItem) {
                    //let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    //if (p !== -1) {
                    if (taskItem.isInItemsAdd) {
                        return "quasar.stepper.done"
                    } else {
                        return "add"
                    }
                },

                itemDeleteMenuColor(taskItem) {
                    //let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    //if (p !== -1) {
                    if (taskItem.isInItemsAdd) {
                        return "green-6"
                    } else {
                        return "grey-7"
                    }
                },

                itemDeleteMenuClick(taskItem) {
                    let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    if (p !== -1) {
                        this.itemsAdd.splice(p, 1)
                        taskItem.isInItemsAdd = false
                    } else {
                        this.itemsAdd.push(taskItem)
                        taskItem.isInItemsAdd = true
                        //
                        if (taskItem.isHidden) {
                            this.itemHideMenuClick(taskItem)
                        }
                    }
                },
        */

        selectAll() {
            for (let taskItem of this.items) {
                if (!taskItem.isHidden && !taskItem.isInItemsAdd) {
                    this.itemsAdd.push(taskItem)
                    taskItem.isInItemsAdd = true
                }
            }
        },


        /* =============================== */

        onPictureClick() {
            console.log("onPictureClick")
            if (this.isCameraCapturing) {
                this.onTakePicture()
            } else {
                this.onNewPicture()
            }
        },

        onTakePicture() {
            console.log("onTakePicture")
            this.takePicture();
            this.isCameraCapturing = false
        },

        onNewPicture() {
            console.log("onNewPicture")
            this.clearphoto();
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
            th.clearphoto();
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

        clearphoto() {
            var context = canvas.getContext('2d');
            context.fillStyle = "#AAA";
            context.fillRect(0, 0, canvas.width, canvas.height);

            var data = canvas.toDataURL('image/png');
            photo.setAttribute('src', data);

            this.dataLoaded = false
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
                this.clearphoto();
            }
        },

        async loadData(dataImage) {
            //
            let resApi = await daoApi.loadStore('m/Game/findStill', [dataImage])

            // Заполним родительский список
            let items = resApi.records
            //
            this.items.length = 0
            for (let item of items) {
                this.items.push(item)
            }

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange()
            }

            /*
                        //
                        this.hiddenCount = 0
                        for (let item of this.items) {
                            if (item.isHidden) {
                                this.hiddenCount = this.hiddenCount + 1
                            }
                        }
                        // Для новой порции слов учтем, какие мы только что скрыли и добавили
                        for (let item of this.items) {
                            let posItemsHideAdd = utils.itemPosInItems(item, this.itemsHideAdd)
                            if (posItemsHideAdd !== -1) {
                                item.isHidden = true
                            }

                            let posItemsAdd = utils.itemPosInItems(item, this.itemsAdd)
                            if (posItemsAdd !== -1) {
                                item.isInItemsAdd = true
                            }
                        }
            */

            //
            this.dataLoaded = true
        },

        clearData() {
            // Очистим родительский список
            this.items.length = 0

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange()
            }

        },

    },


    async mounted() {
        this.startup()
    },


}

</script>

<style scoped>

.video {
    display: inline-block;
}

.photo {
    display: inline-block;
}

.photo-container {
    position: relative;
}

.photo-btn-div {
    position: absolute;
    bottom: 1.5rem;
    right: 1rem;
    opacity: 0.8;
}

.photo-btn {
    height: 4rem;
    width: 4rem;
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

.photo-camera-state {
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
    color: grey;
}

.photo-state-error {
    color: #850000;
}

</style>