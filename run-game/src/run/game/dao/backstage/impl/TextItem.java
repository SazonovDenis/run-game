package run.game.dao.backstage.impl;

public class TextItem {

    public String text;
    public long item;

    public TextItem(String text) {
        super();
        this.text = text;
        this.item = -1;
    }

    public TextItem(String text, long item) {
        super();
        this.text = text;
        this.item = item;
    }

    public int hashCode() {
        return this.text.hashCode() * (int) this.item;
    }

    public boolean equals(Object other) {
        if (other instanceof TextItem otherTextItem) {
            return this.text.equals(otherTextItem.text) &&
                    this.item == otherTextItem.item;
        } else {
            return false;
        }
    }

    public String toString() {
        return "text: '" + this.text + "', item: " + this.item + "";
    }

}
