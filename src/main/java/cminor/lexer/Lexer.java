package cminor.lexer;

import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.syntax.SyntaxError;
import cminor.token.Location;
import cminor.token.Position;
import cminor.token.Token;
import cminor.token.TokenType;
import cminor.utilities.PrettyPrint;
import cminor.utilities.Vector;

//TODO: There seems to be an error with '<>' and '<=>' at least in the parser! \(O-o)/

/**
 * This is the {@code Lexer} which is responsible for tokenizing a C
 * Minor program. An instance of the lexer will be contained in the parser,
 * and it will be continually called to generate tokens for the parser until
 * either an error occurs or we have tokenized the EOF symbol.
 * @author Daniel Levy
 */
public class Lexer {

    /** Custom end-of-file character. */
    public static final char EOF = '\0'; // EOF

    /** Current C Minor program we are tokenizing. */
    private final String file;

    /** The file name we are tokenizing */
    private final String fileName;

    /** Current position we are at in {@link Lexer#file}. */
    private int currPos;

    /** Current lookahead character we are checking.*/
    private char lookChar;

    /** Current location we are at in {@link Lexer#file}.
     *  This keeps track of both row and column position.*/
    private final Location currLoc;

    /** Current text that will be stored when a {@code token} is generated.*/
    private String currText;

    /** A {@code Vector} that stores a C Minor program. This will be used by
     *  the parser in order to generate syntax error messages.*/
    private final Vector<String> lines;

    /** A {@link MessageHandler} to create lexer errors. */
    private final MessageHandler handler;

    /** Creates a new {@code Lexer} instance in interpretation mode.*/
    public Lexer(final String file) { this(file,""); }

    /**
     * Creates a new {@code Lexer} instance, will be called by the parser.
     * @param file C Minor program that will be tokenized.
     */
    public Lexer(final String file, String fileName) {
        this.file = file;

        // Get the actual file name depending on the file system.
        if(fileName.contains("/"))
            this.fileName = fileName.substring(fileName.lastIndexOf("/")+1);
        else if(fileName.contains("\\"))
            this.fileName = fileName.substring(fileName.lastIndexOf("\\")+1);
        else
            this.fileName = fileName;

        this.currPos = 0;
        this.lookChar = file.charAt(currPos);
        this.currLoc = new Location();
        this.currText = "";
        this.lines = new Vector<>();
        this.handler = this.fileName.isEmpty() ? new MessageHandler() : new MessageHandler(this.fileName);
    }

    public String getFileName() { return fileName; }

    public void setText(Token startToken, Token endToken) {
        Position start = startToken.getStartPos();
        Position end = endToken.getEndPos();
        StringBuilder sb = new StringBuilder();

        start.line--;
        end.line--;
        start.column--;
        end.column--;

        int strSize;
        int startCol = start.column;

        for(int i = start.line; i <= end.line; i++) {
            String curr = lines.get(i);

            if(i != end.line) { strSize = curr.length(); }
            else { strSize = end.column; }

            for(int j = startCol; j < strSize; j++) { sb.append(curr.charAt(j)); }
            startCol = 0;
        }

        startToken.setText(sb.toString());
    }

    /**
     * Sets a token text to be between its starting and ending {@code positions}.
     * @param tokenForAST This represents the token we are saving into an AST node.
     */
    public void setText(Token tokenForAST) {
        Position start = tokenForAST.getStartPos();
        Position end = tokenForAST.getEndPos();
        StringBuilder sb = new StringBuilder();

        start.line--;
        end.line--;
        start.column--;
        end.column--;

        int strSize;
        int startCol = start.column;

        for(int i = start.line; i <= end.line; i++) {
            String curr = lines.get(i);

            if(i != end.line) { strSize = curr.length(); }
            else { strSize = end.column; }

            for(int j = startCol; j < strSize; j++) { sb.append(curr.charAt(j)); }
            startCol = 0;
        }

        tokenForAST.setText(sb.toString());
    }

    /** Prints out the line an error occurs at. This will be called by the {@code parser}.*/
    public void printSyntaxError(Position start) { System.out.println(start.line + "| " + lines.get(start.line-1)); }

    /** Updates the lookahead character and program text every time there is a valid match.*/
    private void consume() {
        currText += file.charAt(currPos);

        try {
            lines.get(currLoc.end.line-1);
            lines.set(currLoc.end.line-1,currText);
        }
        catch(Exception e) { lines.add(currLoc.end.line-1,currText); }

        currPos += 1;
        currLoc.addCol();

        if(currPos < file.length()) { lookChar = file.charAt(currPos); }
        else { lookChar = EOF; }
    }

    /** Updates the lookahead character without updating the program buffer. */
    private void update() {
        currPos += 1;
        currLoc.addCol();
        if(currPos < file.length()) { lookChar = file.charAt(currPos); }
        else { lookChar = EOF; }
    }

    /** Checks if the lookahead character matches the expected character we need to see.*/
    private boolean match(char expectedChar) {
        if(lookChar != expectedChar)
            return false;
        consume();
        return true;
    }

    /** Consumes all whitespace characters before continuing to tokenize program.*/
    private void consumeWhitespace() {
        while(lookChar == ' ' || lookChar == '\t' || lookChar == '\r' || lookChar == '\n') {
            if(lookChar == ' ' || lookChar == '\t') { consume(); }
            else {
                consume();
                currText = "";
                currLoc.addLine();
            }
        }
        currLoc.resetStart();
    }

    /** Consumes a single line comment that starts with {@code //}.*/
    private void consumeComment() {
        while(lookChar != '\n' && lookChar != EOF)
            consume();
        consumeWhitespace();
    }

    /** Consumes a multi-line comment that starts with {@code /*}.*/
    private void consumeMultiLineComment() {
        while(lookChar != EOF) {
            consume();
            if(match('*') && match('/')) {
                consumeWhitespace();
                return;
            }

        }

        handler.createErrorBuilder(SyntaxError.class)
                .addErrorNumber(MessageNumber.SYNTAX_ERROR_100)
                .generateError();
    }

    /** Checks if EOF was reached.*/
    private boolean isEOF() { return currPos == file.length(); }

    /** Checks if current lookahead character is a letter.*/
    private boolean isLetter() {
        return (lookChar >= 'A' && lookChar <= 'Z') || (lookChar >= 'a' && lookChar <= 'z');
    }

    /** Checks if current lookahead character is a number.*/
    private boolean isDigit() { return lookChar >= '0' && lookChar <= '9'; }

    /**
     *    Any time a '\' appears in a Char or String literal, we have to check
     *    if this forms a valid escape sequence. In C Minor, we currently
     *    support all escape sequences that Java supports. If the escape
     *    sequence is not valid, we will output an error.
     * @param sb This represents the current Character/String literal we are tokenizing.
     */
    private void escapeSequence(StringBuilder sb) {
        update();
        switch(lookChar) {
            case '\'':
                sb.append('\'');
                currText += '\'';
                break;
            case '\"':
                sb.append('\"');
                currText += '\"';
                break;
            case '\\':
                sb.append('\\');
                currText += '\\';
                break;
            case 'b':
                sb.append('\b');
                currText += '\b';
                break;
            case 'f':
                sb.append('\f');
                currText += '\f';
                break;
            case 'n':
                sb.append('\n');
                currText += '\n';
                break;
            case 'r':
                sb.append('\r');
                currText += '\r';
                break;
            case 't':
                sb.append('\t');
                currText += '\t';
                break;
            case '0':
                sb.append('\0');
                currText += '\0';
                break;
            default:
                System.out.println(PrettyPrint.RED + "Error! Invalid escape sequence written at positions "
                                                   + currLoc.toString() + ".");
                System.exit(1);
        }
        update();
        currLoc.removeCol();
    }

    /**
     * Tokenizes a character literal.
     * @return Character token.
     */
    private Token charLit() {
        StringBuilder newChar = new StringBuilder();
        newChar.append('\'');

        if(lookChar == '\\')
            escapeSequence(newChar);
        else {
            newChar.append(lookChar);
            consume();
        }

        if(!match('\'')) { return strLit(newChar); }

        newChar.append('\'');
        return new Token(TokenType.CHAR_LIT, newChar.toString(), currLoc.copy());
    }

    /**
     * Tokenizes a string literal.
     * @param newStr Current String literal we are tokenizing
     * @return String or Text token.
     */
    private Token strLit(StringBuilder newStr) {
        while(!match('\'') && !isEOF()) {
            if(lookChar == '\\')
                escapeSequence(newStr);
            else {
                newStr.append(lookChar);
                consume();
            }
        }

        if(match('\'') && match('\''))
            return new Token(TokenType.TEXT_LIT, newStr.toString(), currLoc.copy());

        newStr.append('\'');
        return new Token(TokenType.STR_LIT, newStr.toString(), currLoc.copy());
    }

    /**
     * Tokenizes a real number literal.
     * @param newReal The real number we are trying to build
     * @return Real number token.
     */
    private Token realLit(StringBuilder newReal) {
        while(isDigit()) {
            newReal.append(lookChar);
            consume();
        }
        return new Token(TokenType.REAL_LIT, newReal.toString(), currLoc.copy());
    }

    /**
     * Creates an identifier token.
     * @param newID The identifier we are trying to create.
     * @return Identifier token.
     */
    private Token createID(StringBuilder newID) {
        while(isDigit() || isLetter() || lookChar == '_') {
            newID.append(lookChar);
            consume();
        }
        return new Token(TokenType.ID, newID.toString(), currLoc.copy());
    }

    /**
     * Tokenizes a name.
     * @return Identifier or keyword token.
     */
    private Token name() {
        StringBuilder createStr = new StringBuilder();

        while(isLetter() || lookChar == '_' || match('#')) {
            createStr.append(lookChar);
            consume();
        }

        if(isDigit()) { return createID(createStr); }

        String nextStr = createStr.toString();

        if(nextStr.equals("as") && match('?')) {
            return new Token(TokenType.AS, "as?", currLoc.copy());
        }

        return switch(nextStr) {
            // -----------------------------------------------------------------------------------------------
            //                                            KEYWORDS
            // -----------------------------------------------------------------------------------------------

            case "abstr" -> new Token(TokenType.ABSTR, "abstr", currLoc.copy());
            case "and" -> new Token(TokenType.AND, "and", currLoc.copy());
            case "Array" -> new Token(TokenType.ARRAY, "Array", currLoc.copy());
            case "Bool" -> new Token(TokenType.BOOL, "Bool", currLoc.copy());
            case "break" -> new Token(TokenType.BREAK, "break", currLoc.copy());
            case "cast" -> new Token(TokenType.CAST, "cast", currLoc.copy());
            case "Char" -> new Token(TokenType.CHAR, "Char", currLoc.copy());
            case "choice" -> new Token(TokenType.CHOICE, "choice", currLoc.copy());
            case "cin" -> new Token(TokenType.CIN, "cin", currLoc.copy());
            case "class" -> new Token(TokenType.CLASS, "class", currLoc.copy());
            case "const" -> new Token(TokenType.CONST, "const", currLoc.copy());
            case "continue" -> new Token(TokenType.CONTINUE, "continue", currLoc.copy());
            case "cout" -> new Token(TokenType.COUT, "cout", currLoc.copy());
            case "def" -> new Token(TokenType.DEF, "def", currLoc.copy());
            case "discr" -> new Token(TokenType.DISCR, "discr", currLoc.copy());
            case "do" -> new Token(TokenType.DO, "do", currLoc.copy());
            case "else" -> new Token(TokenType.ELSE, "else", currLoc.copy());
            case "endl" -> new Token(TokenType.ENDL, "endl", currLoc.copy());
            case "except" -> new Token(TokenType.EXCEPT, "except", currLoc.copy());
            case "final" -> new Token(TokenType.FINAL, "final", currLoc.copy());
            case "for" -> new Token(TokenType.FOR, "for", currLoc.copy());
            case "global" -> new Token(TokenType.GLOBAL, "global", currLoc.copy());
            case "if" -> new Token(TokenType.IF, "if", currLoc.copy());
            case "in" -> new Token(TokenType.IN, "in", currLoc.copy());
            case "Int" -> new Token(TokenType.INT, "Int", currLoc.copy());
            case "inherits" -> new Token(TokenType.INHERITS, "inherits", currLoc.copy());
            case "inout" -> new Token(TokenType.INOUT, "inout", currLoc.copy());
            case "inrev" -> new Token(TokenType.INREV, "inrev", currLoc.copy());
            case "instanceof" -> new Token(TokenType.INSTANCEOF, "instanceof", currLoc.copy());
            case "List" -> new Token(TokenType.LIST, "List", currLoc.copy());
            case "local" -> new Token(TokenType.LOCAL, "local", currLoc.copy());
            case "loop" -> new Token(TokenType.LOOP, "loop", currLoc.copy());
            case "main" -> new Token(TokenType.MAIN, "main", currLoc.copy());
            case "method" -> new Token(TokenType.METHOD, "method", currLoc.copy());
            case "new" -> new Token(TokenType.NEW, "new", currLoc.copy());
            case "not" -> new Token(TokenType.NOT, "not", currLoc.copy());
            case "on" -> new Token(TokenType.ON, "on", currLoc.copy());
            case "only" -> new Token(TokenType.ONLY, "only", currLoc.copy());
            case "operator" -> new Token(TokenType.OPERATOR, "operator", currLoc.copy());
            case "or" -> new Token(TokenType.OR, "or", currLoc.copy());
            case "other" -> new Token(TokenType.OTHER, "other", currLoc.copy());
            case "out" -> new Token(TokenType.OUT, "out", currLoc.copy());
            case "overload" -> new Token(TokenType.OVERLOAD, "overload", currLoc.copy());
            case "override" -> new Token(TokenType.OVERRIDE, "override", currLoc.copy());
            case "parent" -> new Token(TokenType.PARENT, "parent", currLoc.copy());
            case "property" -> new Token(TokenType.PROPERTY, "property", currLoc.copy());
            case "protected" -> new Token(TokenType.PROTECTED, "protected", currLoc.copy());
            case "public" -> new Token(TokenType.PUBLIC, "public", currLoc.copy());
            case "pure" -> new Token(TokenType.PURE, "pure", currLoc.copy());
            case "Real" -> new Token(TokenType.REAL, "Real", currLoc.copy());
            case "recurs" -> new Token(TokenType.RECURS, "recurs", currLoc.copy());
            case "ref" -> new Token(TokenType.REF, "ref", currLoc.copy());
            case "rename" -> new Token(TokenType.RENAME, "rename", currLoc.copy());
            case "return" -> new Token(TokenType.RETURN, "return", currLoc.copy());
            case "retype" -> new Token(TokenType.RETYPE, "retype", currLoc.copy());
            case "scalar" -> new Token(TokenType.SCALAR, "scalar", currLoc.copy());
            case "set" -> new Token(TokenType.SET, "set", currLoc.copy());
            case "slice" -> new Token(TokenType.SLICE, "slice", currLoc.copy());
            case "stop" -> new Token(TokenType.STOP, "stop", currLoc.copy());
            case "String" -> new Token(TokenType.STRING, "String", currLoc.copy());
            case "then" -> new Token(TokenType.THEN, "then", currLoc.copy());
            case "Tuple" -> new Token(TokenType.TUPLE, "Tuple", currLoc.copy());
            case "type" -> new Token(TokenType.TYPE, "type", currLoc.copy());
            case "uninit" -> new Token(TokenType.UNINIT, "uninit", currLoc.copy());
            case "until" -> new Token(TokenType.UNTIL, "until", currLoc.copy());
            case "Void" -> new Token(TokenType.VOID, "Void", currLoc.copy());
            case "while" -> new Token(TokenType.WHILE, "while", currLoc.copy());

            // -----------------------------------------------------------------------------------------------
            //                                      BOOLEAN LITERALS
            // -----------------------------------------------------------------------------------------------

            case "True" -> new Token(TokenType.BOOL_LIT, "True", currLoc.copy());
            case "False" -> new Token(TokenType.BOOL_LIT, "False", currLoc.copy());
            default -> createID(createStr);
        };
    }

    /**
     * Tokenizes a number.
     * @param newNum The number we are trying to build
     * @return Integer or real token.
     */
    private Token number(StringBuilder newNum) {
        while(isDigit()) {
            newNum.append(lookChar);
            consume();
        }

        // Only tokenize a real number when we don't have a loop operator
        // Sorta a hack, but it is what it is... :')
        if(lookChar == '.' && currPos+1 < file.length() && file.charAt(currPos+1) != '.') {
                match('.');
                newNum.append('.');
                return realLit(newNum);
        }
        return new Token(TokenType.INT_LIT, newNum.toString(), currLoc.copy());
    }

    /**
     * This is the main method for the C Minor lexer.<br><br>
     * <p>
     *   We will use a greedy algorithm to determine which token to generate next
     *   based on the current input lookahead character. Error tokens will only be
     *   generated if we can not determine which valid token a string of characters
     *   should generate.
     * </p>
     * @return Any token.
     */
    public Token nextToken() {
        while(lookChar != EOF) {
            switch(lookChar) {
                case ' ', '\t', '\r', '\n':
                    consumeWhitespace();
                    break;
                case '=':
                    consume();
                    if(match('=')) { return new Token(TokenType.EQEQ, "==", currLoc.copy()); }
                    if(match('>')) { return new Token(TokenType.ARROW, "=>", currLoc.copy()); }
                    return new Token(TokenType.EQ, "=", currLoc.copy());
                case '+':
                    consume();
                    if(match('=')) { return new Token(TokenType.PLUSEQ, "+=", currLoc.copy()); }
                    return new Token(TokenType.PLUS, "+", currLoc.copy());
                case '-':
                    consume();
                    if(match('=')) { return new Token(TokenType.MINUSEQ, "-=", currLoc.copy()); }
                    return new Token(TokenType.MINUS, "-", currLoc.copy());
                case '*':
                    consume();
                    if(match('*')) {
                        if(match('=')) { return new Token(TokenType.EXPEQ, "**=", currLoc.copy()); }
                        return new Token(TokenType.EXP, "**", currLoc.copy());
                    }
                    if(match('=')) { return new Token(TokenType.MULTEQ, "*=", currLoc.copy()); }
                    return new Token(TokenType.MULT, "*", currLoc.copy());
                case '/':
                    consume();
                    if(match('/')) {
                        consumeComment();
                        break;
                    }
                    if(match('*')) {
                        consumeMultiLineComment();
                        break;
                    }
                    if(match('=')) { return new Token(TokenType.DIVEQ, "/=", currLoc.copy()); }
                    return new Token(TokenType.DIV, "/", currLoc.copy());
                case '~':
                    consume();
                    return new Token(TokenType.BNOT, "~", currLoc.copy());
                case '%':
                    consume();
                    if(match('=')) { return new Token(TokenType.MODEQ, "%=", currLoc.copy()); }
                    return new Token(TokenType.MOD, "%", currLoc.copy());
                case '!':
                    consume();
                    if(match('=')) { return new Token(TokenType.NEQ, "!=", currLoc.copy()); }
                    if(isLetter()) {
                        StringBuilder sb = new StringBuilder();
                        while(isLetter()) {
                            sb.append(lookChar);
                            consume();
                        }
                        if(sb.toString().equals("instanceof")) {
                            return new Token(TokenType.NINSTANCEOF, "!instanceof", currLoc.copy());
                        }
                    }
                    return new Token(TokenType.ERROR, "ERROR", currLoc.copy());
                case '#':
                    consume();
                    if(isLetter()) {
                        StringBuilder sb = new StringBuilder();
                        while(isLetter()) {
                            sb.append(lookChar);
                            consume();
                        }
                        if(sb.toString().equals("include")) {
                            return new Token(TokenType.INCLUDE, "#include", currLoc.copy());
                        }
                        if(sb.toString().equals("exclude")) {
                            return new Token(TokenType.EXCLUDE, "#exclude", currLoc.copy());
                        }
                    }
                    return new Token(TokenType.ERROR, "ERROR", currLoc.copy());
                case '<':
                    consume();
                    if(match('=')) {
                        if(match('>')) { return new Token(TokenType.UFO, "<=>", currLoc.copy()); }
                        return new Token(TokenType.LTEQ, "<=", currLoc.copy());
                    }
                    if(match('>')) { return new Token(TokenType.LTGT, "<>", currLoc.copy()); }
                    if(match(':')) { return new Token(TokenType.MIN, "<:", currLoc.copy()); }
                    if(match('<')) { return new Token(TokenType.SLEFT, "<<", currLoc.copy()); }
                    return new Token(TokenType.LT, "<", currLoc.copy());
                case '>':
                    consume();
                    if(match('=')) { return new Token(TokenType.GTEQ, ">=", currLoc.copy()); }
                    if(match('>')) { return new Token(TokenType.SRIGHT, ">>", currLoc.copy()); }
                    return new Token(TokenType.GT, ">", currLoc.copy());
                case ':':
                    consume();
                    if(match('>')) { return new Token(TokenType.MAX, ":>", currLoc.copy()); }
                    return new Token(TokenType.COLON, ":", currLoc.copy());
                case '.':
                    consume();
                    if(isDigit()) { return realLit(new StringBuilder(".")); }
                    if(match('.')) { return new Token(TokenType.INC, "..", currLoc.copy()); }
                    return new Token(TokenType.PERIOD, ".", currLoc.copy());
                case ',':
                    consume();
                    return new Token(TokenType.COMMA, ",", currLoc.copy());
                case '(':
                    consume();
                    return new Token(TokenType.LPAREN, "(", currLoc.copy());
                case ')':
                    consume();
                    return new Token(TokenType.RPAREN, ")", currLoc.copy());
                case '{':
                    consume();
                    return new Token(TokenType.LBRACE, "{", currLoc.copy());
                case '}':
                    consume();
                    return new Token(TokenType.RBRACE, "}", currLoc.copy());
                case '[':
                    consume();
                    return new Token(TokenType.LBRACK, "[", currLoc.copy());
                case ']':
                    consume();
                    return new Token(TokenType.RBRACK, "]", currLoc.copy());
                case '@':
                    consume();
                    return new Token(TokenType.AT, "@", currLoc.copy());
                case '\'':
                    consume();
                    if(match('\'') && match('\'')) { return strLit(new StringBuilder()); }
                    return charLit();
                case '?':
                    consume();
                    if(match('.')) { return new Token(TokenType.ELVIS, "?.", currLoc.copy()); }
                    else return new Token(TokenType.ERROR, "ERROR", currLoc.copy());
                case '|':
                    consume();
                    return new Token(TokenType.BOR, "|", currLoc.copy());
                case '&':
                    consume();
                    return new Token(TokenType.BAND, "&", currLoc.copy());
                case '^':
                    consume();
                    return new Token(TokenType.XOR, "^", currLoc.copy());
                default:
                    if(isLetter() || lookChar == '_') { return name(); }
                    if(isDigit()) { return number(new StringBuilder()); }
                    consume();
                    return new Token(TokenType.ERROR, "ERROR", currLoc.copy());
            }
        }
        return new Token(TokenType.EOF, "EOF", currLoc.copy());
    }
}
