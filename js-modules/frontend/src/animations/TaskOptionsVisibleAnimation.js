import AnimationBase from "../AnimationBase"

class TaskOptionsVisibleAnimation extends AnimationBase {

    interval = 200
    name = "TaskOptionsVisibleAnimation"

    MAX_ShowTaskOptions = 1500
    MAX_TaskOptionVisible = 300

    valShowTaskOptions = 0
    valTaskOptionVisible = 0
    indexTaskOptionVisible = 0


    onStart(data, cfg) {
        data.showTaskOptions = false;
        //
        data.taskOptionVisible.length = 0;
        for (let i = 0; i < cfg.taskOptionsLength; i++) {
            data.taskOptionVisible.push(false)
        }

        //
        this.indexTaskOptionVisible = 0
        this.valShowTaskOptions = 0
    }

    onStep(frames) {
        let data = this.data
        let cfg = this.cfg


        // ---
        // Пауза в MAX_ShowTaskOptions перед показом вариантов
        this.valShowTaskOptions = this.valShowTaskOptions + this.interval * frames
        if (this.valShowTaskOptions < this.MAX_ShowTaskOptions) {
            // Еще не время
            return
        }

        // Разрешим показ вариантов
        data.showTaskOptions = true;


        // ---
        // Показ вариантов с паузой перед каждым вариантом
        for (let i = 0; i < frames; i++) {
            if (this.indexTaskOptionVisible >= cfg.taskOptionsLength) {
                this.stop()
                return
            }

            // Пауза в MAX_TaskOptionVisible перед показом очередного варианта
            this.valTaskOptionVisible = this.valTaskOptionVisible + this.interval * frames
            if (this.valTaskOptionVisible < this.MAX_TaskOptionVisible) {
                // Еще не время
                return
            }

            // Разрешим показ очередного варианта
            data.taskOptionVisible[this.indexTaskOptionVisible] = true

            // Новая пауза перед следующим
            this.indexTaskOptionVisible = this.indexTaskOptionVisible + 1
            this.valTaskOptionVisible = 0
        }
    }

}


export default TaskOptionsVisibleAnimation