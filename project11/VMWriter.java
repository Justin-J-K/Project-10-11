import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private File outputFile;
    private BufferedWriter writer;

    public VMWriter(File file) {
        outputFile = file;
        // open output file
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write to file \"" +
                    outputFile.getName() + "\"!");
        }
    }

    // write push command
    public void writePush(Segment seg, int index) {
        writeLine("push " + seg.getMnemonic() + " " + index);
    }

    // write pop command
    public void writePop(Segment seg, int index) {
        writeLine("pop " + seg.getMnemonic() + " " + index);
    }

    // overloaded
    public void writePush(Kind seg, int index) {
        writePush(getSegmentFromKind(seg), index);
    }

    // overloaded
    public void writePop(Kind seg, int index) {
        writePop(getSegmentFromKind(seg), index);
    }

    // write arithmetic command
    public void writeArithmetic(Command com) {
        writeLine(com.getMnemonic());
    }

    // write label command
    public void writeLabel(String label) {
        writeLine("label " + label);
    }

    // write goto command
    public void writeGoto(String label) {
        writeLine("goto " + label);
    }

    // write goto-if command
    public void writeIf(String label) {
        writeLine("if-goto " + label);
    }

    // write call command
    public void writeCall(String name, int nArgs) {
        writeLine("call " + name + " " + nArgs);
    }

    // write function command
    public void writeFunction(String name, int nArgs) {
        writeLine("function " + name + " " + nArgs);
    }

    // write return
    public void writeReturn() {
        writeLine("return");
    }

    // close and flush
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to flush and close the file!");
        }
    }

    // translate kind into segment
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
