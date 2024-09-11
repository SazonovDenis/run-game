import utils from "../utils"

export default class AudioTask {

    soundLoading = false
    soundLoaded = false
    soundPlaying = false

    onSoundState = null

    constructor(task) {
        this.task = task
        this.audio = new Audio()
        this.audio.addEventListener('loadeddata', this.onSoundLoaded.bind(this))
        this.audio.addEventListener('error', this.onSoundError.bind(this))
        this.audio.addEventListener('play', this.onSoundPlay.bind(this))
        this.audio.addEventListener('pause', this.onSoundPause.bind(this))
    }

    destroy() {
        if (this.audio != null) {
            this.audio.removeEventListener('loadeddata', this.onSoundLoaded)
            this.audio.removeEventListener('error', this.onSoundError)
            this.audio.removeEventListener('play', this.onSoundPlay)
            this.audio.removeEventListener('pause', this.onSoundPause)
            this.audio = null
        }
    }

    setTask(task) {
        this.task = task
        this.soundLoaded = false
    }

    play() {
        if (this.canPlay()) {
            try {
                this.audio.play()
            } catch(e) {
                console.error(e)
            }
        } else {
            if (!this.soundLoaded) {
                this.loadAudioTask()
            }
        }
    }

    loadAudioTask() {
        // Останавливаем текущий звук
        try {
            this.audio.pause()
        } catch(e) {
            console.error(e)
        }

        // Новый звук
        this.audio.src = utils.getAudioSrc(this.task)

        //
        this.soundLoading = true
        this.soundLoaded = false
        this.emit("loading")
    }

    onSoundLoaded() {
        this.soundLoading = false
        this.soundLoaded = true
        this.emit("loaded")
    }

    onSoundError() {
        this.soundLoading = false
        this.soundLoaded = false
        this.emit("error")
    }

    onSoundPlay() {
        this.soundPlaying = true
        this.emit("play")
    }

    onSoundPause() {
        this.soundPlaying = false
        this.emit("pause")
    }

    canPlay() {
        return (
            this.task != null &&
            this.task.valueSound != null &&
            this.soundLoaded
        )
    }

    emit(event) {
        if (this.onSoundState) {
            let state = {
                soundLoaded: this.soundLoaded,
                soundLoading: this.soundLoading,
                soundPlaying: this.soundPlaying,
            }
            this.onSoundState(state, event)
        }
    }

}