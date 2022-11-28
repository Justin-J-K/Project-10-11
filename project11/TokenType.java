public enum TokenType {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    private final String typeString;

    TokenType(String type) {
        typeString = type;
    }

    @Override
    public String toString() {
        return typeString;
    }
}
