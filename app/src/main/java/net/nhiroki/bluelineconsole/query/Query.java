package net.nhiroki.bluelineconsole.query;

public class Query{
    protected static final String SPACE = " ";

    public QueryType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    private final QueryType type;
    private final String text;

    public Query(QueryType type, String text) {
        this.type = type;
        this.text = text;
    }

    private Query(String text) {
        String[] typeAndText = text.split(Query.SPACE, 2);
        if(typeAndText.length == 2){
            this.type = QueryType.fromSymbol(typeAndText[0]);
            this.text = typeAndText[1];
            return;
        }
        this.text = text;
        this.type = QueryType.NONE;
    }

    public boolean isSpecialised() {
        return type != null && type != QueryType.NONE;
    }

    public static Query from(String text){
        return new Query(text);
    }
}
