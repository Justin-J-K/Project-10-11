import java.io.File;

public class CompilationEngine {
    private File outputFile;
    private VMWriter writer;
    private JackTokenizer jackTokenizer;
    private SymbolTable symbolTable;
    private String className;
    private String subroutineName;
    private int labelIndex;

    public CompilationEngine(JackTokenizer tokenizer, File file) {
        jackTokenizer = tokenizer;
        outputFile = file;
        symbolTable = new SymbolTable();
        writer = new VMWriter(outputFile);
        advanceTokenizer();
    }

    public void compileClass() {
        // class
        if (jackTokenizer.tokenType() != TokenType.KEYWORD || 
                jackTokenizer.keyWord() != Keyword.CLASS
        ) {
            throw new IllegalStateException("syntax error: expected class declaration");
        }

        advanceTokenizer();

        // className (identifier)
        className = compileIdentifier();

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
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol() != '}'
        ) {
            throw new IllegalStateException("syntax error: expected '}'");
        }

        // close and flush VM writer
        writer.close();
    }

    private void compileClassVarDec() {
        // static or field
        Keyword keyword = jackTokenizer.keyWord();
        Kind kind = keyword == Keyword.STATIC ? Kind.STATIC : Kind.FIELD;
        advanceTokenizer();

        // type
        String type = compileType();

        // varName
        String varName = compileIdentifier();

        // add to symbol table
        symbolTable.define(varName, type, kind);

        // (',' varName)*
        while (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == ',') {
            advanceTokenizer();
            varName = compileIdentifier();
            symbolTable.define(varName, type, kind);
        }

        // ;
        compileSymbol(';');
    }

    private void compileSubroutine() {
        symbolTable.startSubroutine();

        // constructor or function or method
        Keyword keyword = jackTokenizer.keyWord();
        advanceTokenizer();

        // type
        compileVoidOrType();

        // subroutineName
        subroutineName = compileIdentifier();

        // check if method
        if (keyword == Keyword.METHOD) {
            symbolTable.define("this", className, Kind.ARG);
        }

        // (
        compileSymbol('(');
        
        // parameterList
        compileParameterList();

        // )
        compileSymbol(')');

        // subroutineBody
        compileSubRoutineBody();
    }

    private void compileSymbol(char symbol) {
        // check if valid
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol()  != symbol
        ) {
            throw new IllegalStateException("syntax error: expected '" + symbol + "'");
        }

        advanceTokenizer();
    }

    private String compileIdentifier() {
        if (jackTokenizer.tokenType() != TokenType.IDENTIFIER) 
            throw new IllegalStateException("syntax error: expected identifier");
        
        String identifier = jackTokenizer.identifier();

        advanceTokenizer();

        return identifier;
    }

    private String compileVoidOrType() {
        // check if void
        if (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.VOID
        ) {
            advanceTokenizer();
            return "void";
        } else {
            return compileType();
        }
    }

    private String compileType() {
        String result = null;

        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {
            if (!currentTokenIsPrimitive()) {
                throw new IllegalStateException("syntax error: expected type");
            }

            // compile the keyword type
            result = jackTokenizer.keyWord().getMnemonic();
        } else if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            // compile the identifier type
            result = jackTokenizer.identifier();
        } else {
            throw new IllegalStateException("syntax error: expected type or identifier");
        }

        advanceTokenizer();
        return result;
    }

    private void compileParameterList() {
        if (currentTokenIsType()) {
            // type varName
            String type = compileType();
            String identifier = compileIdentifier();

            symbolTable.define(identifier, type, Kind.ARG);

            // (',' type varName)*
            while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                    jackTokenizer.symbol() == ','
            ) {
                compileSymbol(',');
                type = compileType();
                identifier = compileIdentifier();

                symbolTable.define(identifier, type, Kind.ARG);
            }
        }
    }

    private void compileSubRoutineBody() {
        // {
        compileSymbol('{');
        
        // varDec
        while (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.VAR
        ) {
            compileVarDec();
        }

        // write function declaration
        writer.writeFunction(className + "." + subroutineName, symbolTable.varCount(Kind.VAR));

        // statements
        compileStatements();

        // }
        compileSymbol('}');
    }

    private void compileVarDec() {
        // 'var'
        advanceTokenizer();

        // type varName
        String type = compileType();
        String varName = compileIdentifier();

        symbolTable.define(varName, type, Kind.VAR);

        // (',' varName)*
        while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == ','
        ) {
            compileSymbol(',');
            varName = compileIdentifier();
            symbolTable.define(varName, type, Kind.VAR);
        }

        compileSymbol(';');
    }

    private void compileStatements() {
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
    }

    private void compileKeyword(Keyword keyword) {
        if (jackTokenizer.tokenType() != TokenType.KEYWORD ||
                jackTokenizer.keyWord() != keyword
        ) {
            throw new IllegalArgumentException("syntax error: expected " + keyword.toString());
        }

        advanceTokenizer();
    }

    private void compileDo() {
        // do
        compileKeyword(Keyword.DO);
        
        // subroutineCall
        compileSubroutineCall();

        // ;
        compileSymbol(';');
    }

    private void compileSubroutineCallNoIdentifier(String identifier) {
        String objectName = null;
        String name = identifier;

        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == '.'
        ) {
            // .
            compileSymbol('.');
            // new subroutine name
            objectName = name;
            name = compileIdentifier();
        }

        int numArgs = 0;

        // determine type
        String objectType = className;

        if (objectName != null) {
            String typeOf = symbolTable.typeOf(objectName);

            if (typeOf != null) {   // method
                objectType = typeOf;
                numArgs++;
                writer.writePush(symbolTable.kindOf(objectName), symbolTable.indexOf(objectName));
            } else { // otherwise function or constructor
                objectType = objectName;
            }
        }

        // (
        compileSymbol('(');

        // expressionList
        numArgs += compileExpressionList();

        // )
        compileSymbol(')');

        // write function as VM code
        writer.writeCall(objectType + "." + name, numArgs);
    }

    private void compileSubroutineCall() {
        compileSubroutineCallNoIdentifier(compileIdentifier());
    }

    private void compileLet() {
        // let
        compileKeyword(Keyword.LET);

        // varName
        String varName = compileIdentifier();

        boolean isArray = false;

        // ('[' expression ']')?
        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == '['
        ) {
            compileSymbol('[');
            compileExpression();
            compileSymbol(']');
            isArray = true;
        }

        // =
        compileSymbol('=');
        
        // expression
        compileExpression();

        // ;
        compileSymbol(';');

        if (isArray) {
            writer.writePop(Segment.TEMP, 0);
            writer.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
            writer.writeArithmetic(Command.ADD);
            writer.writePop(Segment.POINTER, 1);
            writer.writePush(Segment.TEMP, 0);
            writer.writePop(Segment.THAT, 0);
        } else {
            writer.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
        }
    }

    private void compileWhile() {
        String startLabel = "WHILE_START_" + labelIndex,
                endLabel = "WHILE_END_" + labelIndex;
        labelIndex++;

        // while
        compileKeyword(Keyword.WHILE);
        // (
        compileSymbol('(');

        writer.writeLabel(startLabel);

        // expression
        compileExpression();

        writer.writeArithmetic(Command.NOT);
        writer.writeIf(endLabel);

        // )
        compileSymbol(')');
        // {
        compileSymbol('{');

        // statements
        compileStatements();

        // }
        compileSymbol('}');

        writer.writeGoto(startLabel);
        writer.writeLabel(endLabel);
    }

    private void compileReturn() {
        // return
        compileKeyword(Keyword.RETURN);

        // expression?
        if (jackTokenizer.tokenType() != TokenType.SYMBOL ||
                jackTokenizer.symbol() != ';'
        ) {
            compileExpression();
        } else {
            writer.writePush(Segment.CONST, 0);
        }

        writer.writeReturn();

        // ;
        compileSymbol(';');
    }

    private void compileIf() {
        String startLabel = "IF_START_" + labelIndex, endLabel = "IF_END_" + labelIndex,
        elseLabel = "ELSE_" + labelIndex;
        labelIndex++;

        // if
        compileKeyword(Keyword.IF);
        // (
        compileSymbol('(');

        writer.writeLabel(startLabel);

        // expression
        compileExpression();

        writer.writeArithmetic(Command.NOT);
        writer.writeIf(elseLabel);

        // )
        compileSymbol(')');
        // {
        compileSymbol('{');

        // statements
        compileStatements();

        writer.writeGoto(endLabel);

        // }
        compileSymbol('}');

        writer.writeLabel(elseLabel);
        
        // else { statements }
        if (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                jackTokenizer.keyWord() == Keyword.ELSE
        ) {
            compileKeyword(Keyword.ELSE);
            compileSymbol('{');
            compileStatements();
            compileSymbol('}');
        }

        writer.writeLabel(endLabel);
    }

    private void compileExpression() {
        compileTerm();
        
        // op term
        while (currentTokenIsOp()) {
            char op = jackTokenizer.symbol();
            advanceTokenizer();
            compileTerm();

            System.out.println(op);

            switch (op) {
                case '+':
                    writer.writeArithmetic(Command.ADD);
                    break;
                case '-':
                    writer.writeArithmetic(Command.SUB);
                    break;
                case '*':
                    writer.writeCall("Math.multiply", 2);
                    break;
                case '/':
                    writer.writeCall("Math.divide", 2);
                    break;
                case '&':
                    writer.writeArithmetic(Command.AND);
                    break;
                case '|':
                    writer.writeArithmetic(Command.OR);
                    break;
                case '<':
                    System.out.println("writing");
                    writer.writeArithmetic(Command.LT);
                    break;
                case '>':
                    writer.writeArithmetic(Command.GT);
                    break;
                case '=':
                    writer.writeArithmetic(Command.EQ);
                    break;
            }
        }
    }

    private void compileTerm() {
        switch (jackTokenizer.tokenType()) {
            case IDENTIFIER:
                // compile identifier
                String identifier = compileIdentifier();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL) {
                    // compile method call
                    if (jackTokenizer.symbol() == '(' ||
                            jackTokenizer.symbol() == '.'
                    ) {
                        compileSubroutineCallNoIdentifier(identifier);
                    } else if (jackTokenizer.symbol() == '[') {
                        // compile array access
                        compileSymbol('[');
                        compileExpression();
                        compileSymbol(']');
                        writer.writePush(symbolTable.kindOf(identifier), symbolTable.indexOf(identifier));
                        writer.writeArithmetic(Command.ADD);
                        writer.writePop(Segment.POINTER, 1);
                        writer.writePush(Segment.THAT, 0);
                    } else {
                        writer.writePush(symbolTable.kindOf(identifier), symbolTable.indexOf(identifier));
                    }
                } else {
                    writer.writePush(symbolTable.kindOf(identifier), symbolTable.indexOf(identifier));
                }
                break;
            case INT_CONST:
                // compile integer
                writer.writePush(Segment.CONST, jackTokenizer.intVal());
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
                    writer.writeArithmetic(jackTokenizer.symbol() == '~' ? Command.NOT :
                            Command.NEG);
                } else {
                    throw new IllegalStateException("syntax error: unexpected symbol");
                }
                break;
            case STRING_CONST:
                // compile string constants
                String stringValue = jackTokenizer.stringVal();

                writer.writePush(Segment.CONST, stringValue.length());
                writer.writeCall("String.new", 1);

                for (char c : stringValue.toCharArray()) {
                    writer.writePush(Segment.CONST, c);
                    writer.writeCall("String.appendChar", 2);
                }

                advanceTokenizer();
                break;
            case KEYWORD:
                // compiles keywords
                if (currentTokenKeywordConstant()) {
                    compileKeyword(jackTokenizer.keyWord());

                    switch (jackTokenizer.keyWord()) {
                        case TRUE:
                            writer.writePush(Segment.CONST, -1);
                            break;
                        case FALSE: case NULL:
                            writer.writePush(Segment.CONST, 0);
                            break;
                        case THIS:
                            writer.writePush(Segment.POINTER, 0);
                        default:
                            break;
                    }
                } else {
                    throw new IllegalStateException("syntax error: expected true, false, null, or this");
                }
                break;
        }
    }

    private int compileExpressionList() {
        int numExpressions = 0;

        if (currentTokenIsStartExpression()) {
            // expression
            compileExpression();
            numExpressions++;

            // (, expression)*
            while (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                    jackTokenizer.symbol() == ','
            ) {
                advanceTokenizer();
                compileExpression();
                numExpressions++;
            }
        }

        return numExpressions;
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
}
