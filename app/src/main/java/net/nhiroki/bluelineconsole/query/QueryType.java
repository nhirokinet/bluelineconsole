package net.nhiroki.bluelineconsole.query;


public enum QueryType {
    NONE(""),
    APP_ONLY("a"),
    CONTACT_ONLY("c")
    ;

    private final String symbol;

    QueryType(String symbol) {
        this.symbol = symbol;
    }

    public static QueryType fromSymbol(String symbol){
        if(symbol == null){
            return NONE;
        }
        for (QueryType type: values()) {
            if(type.symbol.equals(symbol.trim())){
                return type;
            }
        }
        return NONE;
    }
}
