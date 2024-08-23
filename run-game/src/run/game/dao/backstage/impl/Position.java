package run.game.dao.backstage.impl;

public class Position {

    public int left;
    public int top;
    public int width;
    public int height;


    public Position() {

    }

    public Position(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public int hashCode() {
        return this.left * this.top * this.width * this.height;
    }

    public boolean equals(Object other) {
        if (other instanceof Position otherPosition) {
            return this.left == otherPosition.left &&
                    this.top == otherPosition.top &&
                    this.width == otherPosition.width &&
                    this.height == otherPosition.height;
        } else {
            return false;
        }
    }

    public String toString() {
        return "{left:" + this.left + ", top:" + this.top + ", " + this.width + "x" + this.height + "}";
    }

}