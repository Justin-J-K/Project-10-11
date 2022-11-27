import java.util.HashMap;
import java.util.Map;

public enum Keyword {
    CLASS("class"),
    METHOD("method"),
    FUNCTION("function"),
    CONSTRUCTOR("constructor"),
    INT("int"),
    BOOLEAN("boolean"),
    CHAR("char"),
    VOID("void"),
    VAR("var"),
    STATIC("static"),
    FIELD("field"),
    LET("let"),
    DO("do"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    RETURN("return"),
    TRUE("true"),
    FALSE("false"),
    NULL("null"),
    THIS("this");

    private static final Map<String, Keyword> MNEMONIC_MAP = new HashMap<>();

    private final String mnemonic;

    Keyword(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return mnemonic;
    }

    public static Keyword getValueByMnemonic(String mnemonic) {
        if (MNEMONIC_MAP.size() != values().length)
            initMnemonicMap();

        return MNEMONIC_MAP.get(mnemonic);
    }

    private static void initMnemonicMap() {
        for (Keyword keyword : values())
            MNEMONIC_MAP.put(keyword.getMnemonic(), keyword);
    }
}
