import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private File outputFile;
    private BufferedWriter writer;

    public VMWriter(File file) {
        outputFile = file;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write to file \"" +
                    outputFile.getName() + "\"!");
        }
    }

    public void writePush(Segment seg, int index) {
        writeLine("push " + seg.getMnemonic() + " " + index);
    }

    public void writePop(Segment seg, int index) {
        writeLine("pop " + seg.getMnemonic() + " " + index);
    }

    public void writePush(Kind seg, int index) {
        writePush(getSegmentFromKind(seg), index);
    }

    public void writePop(Kind seg, int index) {
        writePop(getSegmentFromKind(seg), index);
    }

    public void writeArithmetic(Command com) {
        writeLine(com.getMnemonic());
    }

    public void writeLabel(String label) {
        writeLine("label " + label);
    }

    public void writeGoto(String label) {
        writeLine("goto " + label);
    }

    public void writeIf(String label) {
        writeLine("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        writeLine("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nArgs) {
        writeLine("function " + name + " " + nArgs);
    }

    public void writeReturn() {
        writeLine("return");
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to flush and close the file!");
        }
    }

    private Segment getSegmentFromKind(Kind kind) {
        Segment result = null;

        switch (kind) {
            case ARG:
                result = Segment.ARG;
                break;
            case FIELD:
                result = Segment.THIS;
                break;
            case STATIC:
                result = Segment.STATIC;
                break;
            case VAR:
                result = Segment.LOCAL;
                break;
        }

        return result;
    }

    private void writeLine(String line) {
        // write line to file
        try {
            writer.write(line + "\n");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to \"" + outputFile.getName() + "\"!");
        }
    }
}
