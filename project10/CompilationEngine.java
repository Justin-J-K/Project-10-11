import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private File outputFile;
    private BufferedWriter writer;
    private JackTokenizer jackTokenizer;

    public CompilationEngine(JackTokenizer tokenizer, File file) {
        jackTokenizer = tokenizer;
        outputFile = file;
        // try to create a new file writer
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write to file \"" +
                    outputFile.getName() + "\"!");
        }
        advanceTokenizer();
    }

    public void compileClass() {
        // class
        if (jackTokenizer.tokenType() != TokenType.KEYWORD || 
                jackTokenizer.keyWord() != Keyword.CLASS
        ) {
            throw new IllegalStateException("syntax error: expected class declaration");
        }

        writeStartTag("class");

        // class
        compileKeyword(jackTokenizer.keyWord());

        // className (identifier)
        compileIdentifier();

        // symbol {
        compileSymbol('{');

        // classVarDec*
        while (jackTokenizer.tokenType() == TokenType.KEYWORD && 
                (jackTokenizer.keyWord() == Keyword.STATIC || 
                jackTokenizer.keyWord() == Keyword.FIELD)) {
            compileClassVarDec();
        }

        // subroutineDec*
        while (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                (jackTokenizer.keyWord() == Keyword.CONSTRUCTOR ||
                jackTokenizer.keyWord() == Keyword.FUNCTION ||
                jackTokenizer.keyWord() == Keyword.METHOD)) {
            compileSubroutine();
        }
        
        // symbol }
        writeTag(TokenType.SYMBOL, '}');

        writeEndTag("class");

        // close and flush buffered writer
        try {
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to flush and close the file!");
        }
    }

    private void compileClassVarDec() {
        writeStartTag("classVarDec");

        // static or field
        writeTag(TokenType.KEYWORD, jackTokenizer.keyWord());

        // type
        advanceTokenizer();
        compileType();

        // varName
        compileIdentifier();

        // (',' varName)*
        while (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == ',') {
            writeTag(TokenType.SYMBOL, ',');
            advanceTokenizer();
            compileIdentifier();
        }

        // ;
        compileSymbol(';');

        writeEndTag("classVarDec");
    }

    private void compileSubroutine() {
        writeStartTag("subroutineDec");

        // constructor or function or method
        writeTag(TokenType.KEYWORD, jackTokenizer.keyWord());

        // type
        advanceTokenizer();
        compileVoidOrType();

        // subroutineName
        compileIdentifier();

        // (
        compileSymbol('(');
        
        // parameterList
        compileParameterList();

        // )
        compileSymbol(')');

        // subroutineBody
        compileSubRoutineBody();

        writeEndTag("subroutineDec");
    }

    private void compileSymbol(char symbol) {
        // check if valid
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol()  != symbol
        ) {
            throw new IllegalStateException("syntax error: expected '" + symbol + "'");
        }
        
        writeTag(TokenType.SYMBOL, symbol);

        advanceTokenizer();
    }

    private void compileIdentifier() {
        if (jackTokenizer.tokenType() != TokenType.IDENTIFIER) 
            throw new IllegalStateException("syntax error: expected identifier");
        
        // write identifier tag
        writeTag(TokenType.IDENTIFIER, jackTokenizer.identifier());
        advanceTokenizer();
    }

    private void compileVoidOrType() {
        // check if void
        if (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.VOID
        ) {
            writeTag(TokenType.KEYWORD, jackTokenizer.keyWord());
            advanceTokenizer();
        } else {
            compileType();
        }
    }

    private void compileType() {
        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {
            if (!currentTokenIsPrimitive()) {
                throw new IllegalStateException("syntax error: expected type");
            }

            // compile the keyword type
            writeTag(TokenType.KEYWORD, jackTokenizer.keyWord());
        } else if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            // compile the identifier type
            writeTag(TokenType.IDENTIFIER, jackTokenizer.identifier());
        } else {
            throw new IllegalStateException("syntax error: expected type or identifier");
        }

        advanceTokenizer();
    }

    private void compileParameterList() {
        writeStartTag("parameterList");

        if (currentTokenIsType()) {
            // type varName
            compileType();
            compileIdentifier();

            // (',' type varName)*
            while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                    jackTokenizer.symbol() == ','
            ) {
                compileSymbol(',');
                compileType();
                compileIdentifier();
            }
        }

        writeEndTag("parameterList");
    }

    private void compileSubRoutineBody() {
        writeStartTag("subroutineBody");

        // {
        compileSymbol('{');
        
        // varDec
        while (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.VAR
        ) {
            compileVarDec();
        }

        // statements
        compileStatements();

        // }
        compileSymbol('}');

        writeEndTag("subroutineBody");
    }

    private void compileVarDec() {
        writeStartTag("varDec");

        // 'var'
        writeTag(TokenType.KEYWORD, Keyword.VAR);

        // type varName
        advanceTokenizer();
        compileType();
        compileIdentifier();

        // (',' varName)*
        while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == ','
        ) {
            compileSymbol(',');
            compileIdentifier();
        }

        compileSymbol(';');

        writeEndTag("varDec");
    }

    private void compileStatements() {
        writeStartTag("statements");

        boolean done = false;

        // statement*
        while (jackTokenizer.tokenType() == TokenType.KEYWORD && !done) {
            switch (jackTokenizer.keyWord()) {
                case LET:
                    compileLet();
                    break;
                case IF:
                    compileIf();
                    break;
                case WHILE:
                    compileWhile();
                    break;
                case DO:
                    compileDo();
                    break;
                case RETURN:
                    compileReturn();
                    break;
                default:
                    done = true;
                    break;
            }
        }
        
        writeEndTag("statements");
    }

    private void compileKeyword(Keyword keyword) {
        if (jackTokenizer.tokenType() != TokenType.KEYWORD ||
                jackTokenizer.keyWord() != keyword
        ) {
            throw new IllegalArgumentException("syntax error: expected " + keyword.toString());
        }
        // write the keyword
        writeTag(TokenType.KEYWORD, keyword);
        advanceTokenizer();
    }

    private void compileDo() {
        writeStartTag("doStatement");

        // do
        compileKeyword(Keyword.DO);
        
        // subroutineCall
        compileIdentifier();
        compileSubroutineCallNoIdentifier();

        // ;
        compileSymbol(';');

        writeEndTag("doStatement");
    }

    private void compileSubroutineCallNoIdentifier() {
        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == '.'
        ) {
            // .
            compileSymbol('.');

            // subroutineName
            compileIdentifier();
        }

        // (
        compileSymbol('(');

        // expressionList
        compileExpressionList();

        // )
        compileSymbol(')');
    }

    private void compileLet() {
        writeStartTag("letStatement");

        // let
        compileKeyword(Keyword.LET);

        // varName
        compileIdentifier();

        // ('[' expression ']')?
        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == '['
        ) {
            compileSymbol('[');
            compileExpression();
            compileSymbol(']');
        }

        // =
        compileSymbol('=');
        
        // expression
        compileExpression();

        // ;
        compileSymbol(';');

        writeEndTag("letStatement");
    }

    private void compileWhile() {
        writeStartTag("whileStatement");

        // while
        compileKeyword(Keyword.WHILE);

        // (
        compileSymbol('(');

        // expression
        compileExpression();

        // )
        compileSymbol(')');

        // {
        compileSymbol('{');

        // statements
        compileStatements();

        // }
        compileSymbol('}');

        writeEndTag("whileStatement");
    }

    private void compileReturn() {
        writeStartTag("returnStatement");

        // return
        compileKeyword(Keyword.RETURN);

        // expression?
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol() != ';'
        ) {
            compileExpression();
        }

        // ;
        compileSymbol(';');

        writeEndTag("returnStatement");
    }

    private void compileIf() {
        writeStartTag("ifStatement");

        // if
        compileKeyword(Keyword.IF);

        // (
        compileSymbol('(');

        // expression
        compileExpression();

        // )
        compileSymbol(')');

        // {
        compileSymbol('{');

        // statements
        compileStatements();

        // }
        compileSymbol('}');
        
        // else { statements }
        if (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.ELSE
        ) {
            compileKeyword(Keyword.ELSE);
            compileSymbol('{');
            compileStatements();
            compileSymbol('}');
        }

        writeEndTag("ifStatement");
    }

    private void compileExpression() {
        writeStartTag("expression");

        compileTerm();
        
        // op term
        while (currentTokenIsOp()) {
            writeTag(TokenType.SYMBOL, jackTokenizer.symbol());
            advanceTokenizer();
            compileTerm();
        }

        writeEndTag("expression");
    }

    private void compileTerm() {
        writeStartTag("term");

        switch (jackTokenizer.tokenType()) {
            case IDENTIFIER:
                // compile identifier
                compileIdentifier();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL) {
                    // compile method call
                    if (jackTokenizer.symbol() == '(' ||
                            jackTokenizer.symbol() == '.'
                    ) {
                        compileSubroutineCallNoIdentifier();
                    } else if (jackTokenizer.symbol() == '[') {
                        // compile array access
                        compileSymbol('[');
                        compileExpression();
                        compileSymbol(']');
                    }
                }
                break;
            case INT_CONST:
                // compile integer
                writeTag(TokenType.INT_CONST, jackTokenizer.intVal() + "");
                advanceTokenizer();
                break;
            case SYMBOL:
                // compile parentheses
                if (jackTokenizer.symbol() == '(') {
                    compileSymbol('(');
                    compileExpression();
                    compileSymbol(')');
                } else if (jackTokenizer.symbol() == '-' ||
                        jackTokenizer.symbol() == '~'
                ) {
                    // compile unary operations
                    compileSymbol(jackTokenizer.symbol());
                    compileTerm();
                } else {
                    throw new IllegalStateException("syntax error: unexpected symbol");
                }
                break;
            case STRING_CONST:
                // compile string constants
                writeTag(TokenType.STRING_CONST, jackTokenizer.stringVal());
                advanceTokenizer();
                break;
            case KEYWORD:
                // compiles keywords
                if (currentTokenKeywordConstant()) {
                    compileKeyword(jackTokenizer.keyWord());
                } else {
                    throw new IllegalStateException("syntax error: expected true, false, null, or this");
                }
                break;
        }

        writeEndTag("term");
    }

    private void compileExpressionList() {
        writeStartTag("expressionList");

        if (currentTokenIsStartExpression()) {
            // expression
            compileExpression();

            // (, expression)*
            while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                    jackTokenizer.symbol() == ','
            ) {
                writeTag(TokenType.SYMBOL, jackTokenizer.symbol());
                advanceTokenizer();
                compileExpression();
            }
        }

        writeEndTag("expressionList");
    }

    private void advanceTokenizer() {
        if (!jackTokenizer.hasMoreTokens())
            throw new IllegalStateException("syntax error: expected additional tokens");

        jackTokenizer.advance();
    }

    private boolean currentTokenIsStartExpression() {
        // check if current token is the start of an expression
        boolean isKeywordConstant = currentTokenKeywordConstant(), 
                startsWithIdentifier = jackTokenizer.tokenType() == TokenType.IDENTIFIER,
                startsWithUnaryOp = jackTokenizer.tokenType() == TokenType.SYMBOL &&
                    (jackTokenizer.symbol() == '-' ||
                    jackTokenizer.symbol() == '~' ||
                    jackTokenizer.symbol() == '(');
        
        return jackTokenizer.tokenType() == TokenType.INT_CONST || 
                jackTokenizer.tokenType() == TokenType.STRING_CONST ||
                isKeywordConstant || startsWithIdentifier || startsWithUnaryOp;
    }

    private boolean currentTokenIsOp() {
        // check if current topen is an operator
        if (jackTokenizer.tokenType() != TokenType.SYMBOL)
            return false;
        char symbol = jackTokenizer.symbol();
        return symbol == '+' || symbol == '-' || symbol == '*' || symbol == '/' ||
                symbol == '&' || symbol == '|' || symbol == '<' || symbol == '>' ||
                symbol == '=';
    }

    private boolean currentTokenIsPrimitive() {
        // check if current token is a primitive
        return jackTokenizer.tokenType() == TokenType.KEYWORD && 
                (jackTokenizer.keyWord() == Keyword.INT ||
                jackTokenizer.keyWord() == Keyword.CHAR ||
                jackTokenizer.keyWord() == Keyword.BOOLEAN);
    }

    private boolean currentTokenKeywordConstant() {
        // check if current token is a keyword constant
        return jackTokenizer.tokenType() == TokenType.KEYWORD &&
                (jackTokenizer.keyWord() == Keyword.TRUE ||
                jackTokenizer.keyWord() == Keyword.FALSE ||
                jackTokenizer.keyWord() == Keyword.NULL ||
                jackTokenizer.keyWord() == Keyword.THIS);
    }

    private boolean currentTokenIsType() {
        // check if current token is a type
        return jackTokenizer.tokenType() == TokenType.IDENTIFIER ||
                currentTokenIsPrimitive();
    }

    private void writeLine(String line) {
        // write line to file
        try {
            writer.write(line + "\n");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to \"" + outputFile.getName() + "\"!");
        }
    }

    private void writeStartTag(String tagName) {
        // write <tagName>
        writeLine(createStartTag(tagName));
    }

    private void writeEndTag(String tagName) {
        // write </tagName>
        writeLine(createEndTag(tagName));
    }

    private void writeTag(String tagName, String text) {
        // write <tagName> text </tagName
        writeLine(createStartTag(tagName) + text + createEndTag(tagName));
    }

    // overloaded method
    private void writeTag(String tagName, char text) {
        // compile special symbol cases
        switch (text) {
            case '<':
                writeTag(TokenType.SYMBOL, "&lt;");
                break;
            case '>':
                writeTag(TokenType.SYMBOL, "&gt;");
                break;
            case '&':
                writeTag(TokenType.SYMBOL, "&amp;");
                break;
            default:
                writeTag(tagName, "" + text);
        }
    }

    // overloaded method
    private void writeTag(TokenType tagName, String text) {
        writeTag(tagName.toString(), text);
    }

    // overloaded method
    private void writeTag(TokenType tagName, char text) {
        writeTag(tagName.toString(), text);
    }

    // overloaded method
    private void writeTag(TokenType tagName, Keyword text) {
        writeTag(tagName, text.toString());
    }

    private String createStartTag(String tagName) {
        return "<" + tagName + ">";
    }

    private String createEndTag(String tagName) {
        return "</" + tagName + ">";
    }
}
