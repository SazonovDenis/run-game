export default {

    intersectRect(rectA, rectB) {
        let bxMin = this.getXMin(rectB)
        let bxMax = this.getXMax(rectB)
        let axMin = this.getXMin(rectA)
        let axMax = this.getXMax(rectA)

        let ayMin = this.getYMin(rectA)
        let ayMax = this.getYMax(rectA)
        let byMin = this.getYMin(rectB)
        let byMax = this.getYMax(rectB)

        return (
            bxMin >= axMin && bxMin <= axMax ||
            bxMax >= axMin && bxMax <= axMax ||
            bxMin <= axMin && bxMax >= axMax
        ) && (
            byMin >= ayMin && byMin <= ayMax ||
            byMax >= ayMin && byMax <= ayMax ||
            byMin <= ayMin && byMax >= ayMax
        );
    },

    getXMin(rect) {
        if (rect.x1 < rect.x2) {return rect.x1} else {return rect.x2}
    },

    getXMax(rect) {
        if (rect.x1 > rect.x2) {return rect.x1} else {return rect.x2}
    },

    getYMin(rect) {
        if (rect.y1 < rect.y2) {return rect.y1} else {return rect.y2}
    },

    getYMax(rect) {
        if (rect.y1 > rect.y2) {return rect.y1} else {return rect.y2}
    },

    getElRect(el) {
        let rect = {
            x1: el.offsetLeft,
            x2: el.offsetLeft + el.offsetWidth,
            y1: el.offsetTop,
            y2: el.offsetTop + el.offsetHeight,
        }
        return rect
    },

    intersectEl(elA, elB) {
        let rectA = this.getElRect(elA)
        let rectB = this.getElRect(elB)

        return this.intersectRect(rectA, rectB)
    },


}