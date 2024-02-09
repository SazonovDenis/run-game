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


        <TaskList
            :showEdit="true"
            :tasks="items"
            :itemsMenu="itemsMenu"
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

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        // Куда складывать выбранные Item
        itemsAdd: {type: Array, default: []},
    },

    data() {
        return {
            items: [],
            itemsMenu: [
                {
                    icon: "quasar.stepper.done",
                    itemMenuColor: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                }
            ],

            //
            width: 1024, // We will scale the photo width to this
            height: 0,  // This will be computed based on the input stream

            wasInit: false,
            isCapturing: null,

            video: null,
            canvas: null,
            photo: null,

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
        itemDeleteMenuColor(taskItem) {
            let p = this.itemPosInItemsAdd(taskItem)
            if (p !== -1) {
                return "green-6"
            } else {
                return "grey-7"
            }
        },

        itemDeleteMenuClick(taskItem) {
            console.info("itemDeletedItemMenuClick", taskItem)
            console.info("this.itemsAdd", this.itemsAdd)

            let p = this.itemPosInItemsAdd(taskItem)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            } else {
                this.itemsAdd.push(taskItem)
            }
        },

        itemPosInItemsAdd(taskItem) {
            for (let p = 0; p < this.itemsAdd.length; p++) {
                let itemAdd = this.itemsAdd[p]
                if (itemAdd.factAnswer === taskItem.factAnswer &&
                    itemAdd.factQuestion === taskItem.factQuestion
                ) {
                    return p
                }
            }

            return -1
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