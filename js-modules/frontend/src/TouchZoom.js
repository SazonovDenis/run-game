import ctx from "./gameplayCtx"

export class TouchZoom {

    constructor() {
        // Определяем точку начала жеста
        this.initialDistance = null
        //
        this.targetElement = null
    }

    // Функция для вычисления расстояния между двумя точками
    getDistance(touches) {
        const dx = touches[0].clientX - touches[1].clientX
        const dy = touches[0].clientY - touches[1].clientY
        return Math.trunc(Math.sqrt(dx * dx + dy * dy))
    }

    // Обработчик события начала касания (touchstart)
    handleTouchStart(event) {
        let touches = this.getTouchesEvent(event)

        if (touches.length === 2) {
            this.initialDistance = this.getDistance(touches)
            //console.log("TouchStart, initialDistance", this.initialDistance)
            ctx.eventBus.emit("zoomStart")
        }
    }

    // Обработчик события движения пальцев (touchmove)
    getTouchesEvent(event) {
        let touches = []

        //touches.push({clientX: 0, clientY: 0})
        for (let touchEvent of event.touches) {
            touches.push({clientX: touchEvent.clientX, clientY: touchEvent.clientY})
        }

        return touches
    }

    handleTouchMove(event) {
        //console.log('TouchMove')

        let touches = this.getTouchesEvent(event)

        if (this.initialDistance !== null && touches.length === 2) {
            const currentDistance = this.getDistance(touches)
            if (currentDistance > this.initialDistance) {
                // Жест zoom-in (увеличение масштаба)
                //console.log('Zoom-in, currentDistance', currentDistance, "initialDistance", this.initialDistance)
                let zoomInfo = {
                    zoomRate: currentDistance / this.initialDistance,
                    currentDistance: currentDistance,
                    initialDistance: this.initialDistance,
                }
                ctx.eventBus.emit("zoomIn", zoomInfo)

            } else if (currentDistance < this.initialDistance) {
                // Жест zoom-out (уменьшение масштаба)
                //console.log('Zoom-out, currentDistance', currentDistance, "initialDistance", this.initialDistance)
                let zoomInfo = {
                    zoomRate: currentDistance / this.initialDistance,
                    currentDistance: currentDistance,
                    initialDistance: this.initialDistance,
                }
                ctx.eventBus.emit("zoomOut", zoomInfo)
            }
            // this.initialDistance = currentDistance
        }
    }

    // Обработчик события окончания касания (touchend)
    handleTouchEnd(event) {
        //console.log('TouchEnd')

        this.initialDistance = null
    }

    // Добавляем обработчики событий к элементу, на который применяется жест
    connect(targetElement) {
        if (this.targetElement) {
            this.disconnect()
        }

        //
        targetElement.addEventListener('touchstart', this.handleTouchStart.bind(this))
        targetElement.addEventListener('touchmove', this.handleTouchMove.bind(this))
        targetElement.addEventListener('touchend', this.handleTouchEnd.bind(this))

        //
        this.targetElement = targetElement
    }

    //
    disconnect() {
        if (this.targetElement) {
            this.targetElement.removeEventListener('touchstart', this.handleTouchStart)
            this.targetElement.removeEventListener('touchmove', this.handleTouchMove)
            this.targetElement.removeEventListener('touchend', this.handleTouchEnd)
            //
            this.targetElement = null
        }
    }

}