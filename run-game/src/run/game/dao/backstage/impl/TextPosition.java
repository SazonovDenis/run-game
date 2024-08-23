package run.game.dao.backstage.impl;


public class TextPosition {

    public String text;
    public Position position;
    public PagePosition pagePosition;

    public TextPosition() {
        this.position = new Position();
    }

    public TextPosition(String text) {
        this.text = text;
        this.position = new Position();
        this.pagePosition = new PagePosition();
    }

    public TextPosition(String text, TextPosition textPosition) {
        super();
        this.text = text;
        this.position = textPosition.position;
        this.pagePosition = textPosition.pagePosition;
    }

    public TextPosition(String text, Position position, PagePosition pagePosition) {
        super();
        this.text = text;
        this.position = position;
        this.pagePosition = pagePosition;
    }

    public String toString() {
        return "text: '" + this.text + "', position: " + this.position + "', page: " + this.pagePosition;
    }

}
