package run.game

class SearchIndexEl {

    SearchIndexEl(String value) {
        this.value = value
        this.tags = new ArrayList<>()
        this.facts = new ArrayList<>()
    }

    public String value
    public List<Long> tags
    public List<Long> facts

    String toString() {
        return "value: " + value + ", tags: " + tags + ", facts: " + facts
    }

}
