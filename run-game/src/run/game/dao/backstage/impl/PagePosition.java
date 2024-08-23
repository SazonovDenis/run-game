package run.game.dao.backstage.impl;

public class PagePosition {

    public int wordNum;
    public int lineNum;
    public int paragraphNum;

    public PagePosition() {
    }

    public PagePosition(int paragraphNum, int lineNum, int wordNum) {
        this.paragraphNum = paragraphNum;
        this.lineNum = lineNum;
        this.wordNum = wordNum;
    }

/*
    public int hashCode() {
        return this.wordNum * this.lineNum * this.paragraphNum;
    }

    public boolean equals(Object other) {
        if (other instanceof PagePosition otherPosition) {
            return this.wordNum == otherPosition.wordNum &&
                    this.lineNum == otherPosition.lineNum &&
                    this.paragraphNum == otherPosition.paragraphNum;
        } else {
            return false;
        }
    }
*/

    public String toString() {
        return "{word:" + this.wordNum + ", line:" + this.lineNum + ", paragraph:" + this.paragraphNum + "}";
    }

}