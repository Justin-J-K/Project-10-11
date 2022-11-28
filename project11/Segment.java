public enum Segment {
    CONST("constant"),
    ARG("argument"),
    LOCAL("local"),
    STATIC("static"),
    THIS("this"),
    THAT("that"),
    POINTER("pointer"),
    TEMP("temp");

    private String mnemonic;

    Segment(String mne) {
        mnemonic = mne;
    }

    public String getMnemonic() {
        return mnemonic;
    }
}
