public enum Command {
    ADD("add"),
    SUB("sub"),
    NEG("neg"),
    EQ("eq"),
    GT("gt"),
    LT("lt"),
    AND("and"),
    OR("or"),
    NOT("not");

    private String mnemonic;

    Command(String mne) {
        mnemonic = mne;
    }

    // get mnemonnic of command
    public String getMnemonic() {
        return mnemonic;
    }
}
