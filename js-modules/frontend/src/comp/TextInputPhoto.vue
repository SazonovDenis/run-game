<template>

    <div>
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


        <div>
            <q-toggle
                v-if="this.hiddenCount > 0"
                v-model="showHidden"
                :label="'Скрытые (' + this.hiddenCount + ')'"
            />
        </div>


        <TaskList
            :showEdit="true"
            :tasks="items"
            :itemsMenu="itemsMenu"
            :filter="filter"
        />


        <q-page-sticky position="bottom-right" :offset="[10, 70]">

            <q-btn v-if="isCapturing===true"
                   style="height: 4em; width: 4em;"
                   round
                   color="accent"
                   icon="image"
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

    </div>

</template>

<script>

import {daoApi} from "../dao"
import TaskList from "./TaskList"
import MenuContainer from "./MenuContainer"
import utils from "../utils"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        // Куда складывать выбранные Item
        itemsAdd: {type: Array, default: []},

        itemsHideAdd: {type: Array, default: []},

        itemsHideDel: {type: Array, default: []},
    },

    data() {
        return {
            items: [],
            itemsMenu: [
                {
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    itemMenuClick: this.itemHideMenuClick,
                },
                {
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                },
            ],

            //
            width: 1024, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            wasInit: false,
            isCapturing: null,

            video: null,
            canvas: null,
            photo: null,

            showHidden: false,
            hiddenCount: false,

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
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "quasar.stepper.done"
            } else {
                return "add"
            }
        },

        itemDeleteMenuColor(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "green-6"
            } else {
                return "grey-7"
            }
        },

        itemDeleteMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            } else {
                this.itemsAdd.push(taskItem)
                //
                if (taskItem.isHidden) {
                    this.itemHideMenuClick(taskItem)
                }
            }
        },


        /* =============================== */

        filter(planTask) {
            if (planTask.isHidden && !this.showHidden) {
                return false
            }

            return true
        },

        /* =============================== */

        onTakePicture() {
            this.takepicture();
            this.isCapturing = false
            console.log("onTakePicture")
        },

        onNewPicture() {
            this.clearphoto();
            this.isCapturing = true
            this.items = []
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
            if (!this.wasInit) {
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
                this.wasInit = true;
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

                //
                let resApi = await daoApi.loadStore('m/Game/findStill', [dataImage])
                //
                this.items = resApi.records

                //
                this.hiddenCount = 0
                for (let item of this.items) {
                    if (item.isHidden) {
                        this.hiddenCount = this.hiddenCount + 1
                    }
                }


            } else {
                this.clearphoto();
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