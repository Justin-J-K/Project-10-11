import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class JackTokenizer {
    private BufferedReader reader;
    private Queue<String> tokens;
    private boolean insideMultiLineComment;

    private String token;
    private TokenType tokenType;
    private Keyword keyword;
    private int intValue; 
    
    public JackTokenizer(File jackFile) {
        try {
            reader = new BufferedReader(new FileReader(jackFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found or is directory!");
        }
        tokens = new LinkedList<>();
    }

    public boolean hasMoreTokens() {
        if (tokens.isEmpty()) 
            while(tokenizeLine());
        
        return !tokens.isEmpty();
    }

    public void advance() {
        if (tokens.isEmpty())
            throw new IllegalStateException("Call hasMoreTokens before advancing!");

        token = tokens.poll();
        char firstChar = token.charAt(0);

        // check if string constant
        if (firstChar == '\"') {
            token = token.substring(1, token.length());
            tokenType = TokenType.STRING_CONST;
            return;
        }

        // check if symbol
        if (token.length() == 1 && Symbol.isSymbol(firstChar)) {
            tokenType = TokenType.SYMBOL;
            return;
        }

        // check if keyword
        keyword = Keyword.getValueByMnemonic(token);

        if (keyword != null) {
            tokenType = TokenType.KEYWORD;
            return;
        }

        // check if integer constant
        try {
            intValue = Integer.parseInt(token);
            tokenType = TokenType.INT_CONST;
            return;
        } catch (NumberFormatException ignored) {}

        // check if identifier
        if (Character.isLetter(firstChar) || firstChar == '_') {
            tokenType = TokenType.IDENTIFIER;
            return;
        }

        // token is not valid
        tokenType = null;
    }

    public TokenType tokenType() {
        return tokenType;
    } 

    public Keyword keyWord() {
        if (tokenType != TokenType.KEYWORD)
            throw new IllegalStateException("Current token is not a keyword!");

        return keyword;
    }

    public char symbol() {
        if (tokenType != TokenType.SYMBOL || token.length() != 1)
            throw new IllegalStateException("Current token is not a symbol!");

        return token.charAt(0);
    }

    public String identifier() {
        if (tokenType != TokenType.IDENTIFIER)
            throw new IllegalStateException("Current token is not an identifier!");

        return token;
    }

    public int intVal() {
        if (tokenType != TokenType.INT_CONST)
            throw new IllegalStateException("Current token is not an integer constant!");

        return intValue;
    }

    public String stringVal() {
        if (tokenType != TokenType.STRING_CONST)
            throw new IllegalStateException("Current token is not a string constant!");

        return token;
    }

    private boolean tokenizeLine() {
        String line = readLine();

        if (line == null) return false;

        StringBuilder currentToken = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            char nextChar = i < line.length() - 1 ? line.charAt(i + 1) : 0;

            // check for comments and ignore them
            if (currentChar == '/' && nextChar == '/') break;
            // ignore multiline comments
            if (insideMultiLineComment) {
                if (currentChar == '*' && nextChar == '/') {
                    i++;
                    insideMultiLineComment = false;
                }
                continue;
            } else if (currentChar == '/' && nextChar == '*') {
                i++;
                insideMultiLineComment = true;
                continue;
            }

            // check if quotes 
            if (currentChar == '\"') {
                insideQuotes = !insideQuotes;

                // add to queue if starting quotes or ending quotes
                if (currentToken.length() > 0 || !insideQuotes) {
                    tokens.offer(currentToken.toString());
                    currentToken.setLength(0);
                }

                // add quotes to start of string constant token
                if (insideQuotes) currentToken.append(currentChar);

                continue;
            }

            // if inside quotes always append to current token
            if (insideQuotes) {
                currentToken.append(currentChar);
                continue;
            }

            // skip whitespace and add to tokens queue
            if (Character.isWhitespace(currentChar)) {
                appendToken(currentToken);
                continue;
            }

            // if symbol add to queue and add symbol to queue
            if (Symbol.isSymbol(currentChar)) {
                appendToken(currentToken);

                tokens.offer("" + currentChar);

                continue;
            }

            // check if not letter, digit, or underscore
            if (!Character.isLetterOrDigit(currentChar) && currentChar != '_')
                throw new IllegalStateException("Invalid character: '" + currentChar + "'!");

            currentToken.append(currentChar);
        }

        appendToken(currentToken);

        return true;
    }

    private String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file!");
        }
    }

    private void appendToken(StringBuilder builder) {
        if (builder.length() > 0) {
            tokens.offer(builder.toString());
            builder.setLength(0);
        }
    }
}