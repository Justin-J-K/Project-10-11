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

    // get mnemonic of segment
    public String getMnemonic() {
        return mnemonic;
    }
}
