package cminor.lexer;

import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.syntax.SyntaxError;
import cminor.token.Location;
import cminor.token.Position;
import cminor.token.Token;
import cminor.token.TokenType;
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
    public static final char EOF = '\0';

    /** Current C Minor program we are tokenizing. */
    private final String file;

    /** The file name we are tokenizing */
    private final String fileName;

    /** Current position we are at in {@link Lexer#file}. */
    private int pos;

    /** Current lookahead character we are checking.*/
    private char lookahead;

    /** Current location we are at in {@link Lexer#file}.
     *  This keeps track of both row and column position.*/
    private final Location location;

    /** Current text that will be stored when a {@code token} is generated.*/
    private String text;

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

        this.pos = 0;
        this.lookahead = file.charAt(pos);
        this.location = new Location();
        this.text = "";
        this.lines = new Vector<>();
        this.handler = this.fileName.isEmpty() ? new MessageHandler() : new MessageHandler(this.fileName);
    }

    public String getFileName() { return fileName; }

    public String getText() { return lines.getLast(); }

    /**
     * Generates the metadata for an {@link cminor.ast.AST} node.
     * <p>
     *     This method is called by the {@link cminor.parser.Parser} whenever a new
     *     {@link cminor.ast.AST} node is created.
     * </p>
     * @param start The {@link Token} that contains the metadata starting location.
     * @param end The {@link Token} that contains the metadata ending location.
     * @return A {@link Token} that contains the metadata info for an {@link cminor.ast.AST} node.
     */
    public Token generateMetaData(Token start, Token end) {
        // If the tokens are the exact same, then we already have the metadata, so let's reuse one of them! :)
        if(start.equals(end))
            return start;

        // Copy the positions where the metadata starts and ends.
        Token metadata = new Token();
        metadata.setStartLocation(start.getStartPos().copy());
        metadata.setEndLocation(end.getEndPos().copy());

        // Get all the lines and columns. This determines which parts of the program will be
        // saved into the next AST node. (Make sure to decrement by 1 for proper indexing!)
        int startLine = start.getStartPos().line - 1, endLine = end.getEndPos().line - 1;
        int startCol = start.getStartPos().column - 1, endCol = end.getEndPos().column - 1;

        String currentLine;
        StringBuilder text = new StringBuilder();

        currentLine = lines.get(startLine);
        if(startLine == endLine)
            text.append(currentLine,startCol,endCol);
        else {
            text.append(currentLine,startCol,currentLine.length());
            // Add each line associated with the AST node.
            for(int i = startLine+1; i < endLine; i++) {
                currentLine = lines.get(i); // Get the current line
                text.append(currentLine);   // Store that line into the text field
            }

            currentLine = lines.get(endLine);
            text.append(currentLine,0,endCol);
        }

        metadata.setText(text.toString());
        return metadata;
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
    public String syntaxError(Position start) { return start.line + "| " + lines.get(start.line-1); }

    /** Updates the lookahead character and program text every time there is a valid match.*/
    private void consume() {
        try { text += file.charAt(pos); }
        // ERROR CHECK #1: If we are not able to get anymore text, then we have a general tokenizer error!
        catch(Exception _) {
            handler.createErrorBuilder(SyntaxError.class)
                    .addErrorNumber(MessageNumber.SYNTAX_ERROR_106)
                    .asSyntaxErrorBuilder()
                    .addLocation(location.start.line + "| " + text)
                    .generateError();
        }

        try {
            lines.get(location.end.line-1);
            lines.set(location.end.line-1, text);
        }
        catch(Exception _) { lines.add(location.end.line-1, text); }

        pos += 1;
        location.addCol();

        if(pos < file.length()) { lookahead = file.charAt(pos); }
        else { lookahead = EOF; }
    }

    /** Updates the lookahead character without updating the program buffer. */
    private void update() {
        pos += 1;
        location.addCol();
        if(pos < file.length()) { lookahead = file.charAt(pos); }
        else { lookahead = EOF; }
    }

    /** Checks if the lookahead character matches the expected character we need to see.*/
    private boolean match(char expectedChar) {
        if(lookahead != expectedChar)
            return false;
        consume();
        return true;
    }

    private boolean nextLA(char expected) { return lookahead == expected; }

    /** Consumes all whitespace characters before continuing to tokenize program.*/
    private void consumeWhitespace() {
        while(lookahead == ' ' || lookahead == '\t' || lookahead == '\r' || lookahead == '\n') {
            if(lookahead == ' ' || lookahead == '\t') { consume(); }
            else {
                consume();
                text = "";
                location.addLine();
            }
        }
        location.resetStart();
    }

    /** Consumes a single line comment that starts with {@code //}.*/
    private void consumeComment() {
        while(lookahead != '\n' && lookahead != EOF)
            consume();
        consumeWhitespace();
    }

    /** Consumes a multi-line comment that starts with {@code /*}.*/
    private void consumeMultiLineComment() {
        while(lookahead != EOF) {
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
    private boolean isEOF() { return pos == file.length(); }

    /** Checks if current lookahead character is a letter.*/
    private boolean isLetter() {
        return (lookahead >= 'A' && lookahead <= 'Z') || (lookahead >= 'a' && lookahead <= 'z');
    }

    /** Checks if current lookahead character is a number.*/
    private boolean isDigit() { return lookahead >= '0' && lookahead <= '9'; }

    /**
     *    Any time a '\' appears in a Char or String literal, we have to check
     *    if this forms a valid escape sequence. In C Minor, we currently
     *    support all escape sequences that Java supports. If the escape
     *    sequence is not valid, we will output an error.
     * @param sb This represents the current Character/String literal we are tokenizing.
     */
    private void escapeSequence(StringBuilder sb) {
        update();
        text += "\\";
        switch(lookahead) {
            case '\'':
                sb.append('\'');
                text += '\'';
                break;
            case '\"':
                sb.append('\"');
                text += '"';
                break;
            case '\\':
                sb.append('\\');
                text += '\\';
                break;
            case 'b':
                sb.append('\b');
                text += 'b';
                break;
            case 'f':
                sb.append('\f');
                text += 'f';
                break;
            case 'n':
                sb.append('\n');
                text += 'n';
                break;
            case 'r':
                sb.append('\r');
                text += 'r';
                break;
            case 't':
                sb.append('\t');
                text += 't';
                break;
            case '0':
                sb.append('\0');
                text += '0';
                break;
            default:
                handler.createErrorBuilder(SyntaxError.class)
                       .addErrorNumber(MessageNumber.SYNTAX_ERROR_103)
                       .asSyntaxErrorBuilder()
                       .addLocation("<insert>") //TODO: FIX
                       .generateError();
        }

        update();
        location.removeCol();
    }

    /**
     * Tokenizes a character literal.
     * @return {@link Token} representing a character literal.
     */
    private Token charLit() {
        StringBuilder sb = new StringBuilder();
        match('\'');
        sb.append('\'');

        // Case 1: Checks if we have an empty string or a text literal
        if(nextLA('\'')) {
            match('\'');
            sb.append('\'');

            if(nextLA('\'')) {
                match('\'');
                sb.append('\'');
                return strLit(sb);
            }

            // SPECIAL CASE: A '' denotes an empty string, NOT an empty character!
            return new Token(TokenType.STR_LIT,sb.toString(), location.copy());
        }
        // Case 2: Checks for escape sequences
        else if(nextLA('\\'))
            escapeSequence(sb);
        // Case 3: Appends anything else to the character
        else {
            sb.append(lookahead);
            consume();
        }

        // If we don't have an ending quote, the character should now be treated as a string!
        if(!match('\''))
            return strLit(sb);

        sb.append('\'');
        return new Token(TokenType.CHAR_LIT, sb.toString(), location.copy());
    }

    /**
     * Tokenizes a string literal.
     * <p>
     *     A string literal is a string inside {@code ".*?"} or {@code """.*?"""}
     * </p>
     * @param str {@link StringBuilder} responsible for creating the string literal.
     * @return {@link Token} representing a string literal.
     */
    private Token strLit(StringBuilder str) {
        // We will infinitely consume until we see the next non-escape sequenced quote!
        while(!match('\'')) {
            // ERROR #1: If we found EOF in this method, then a user forgot an ending quote...
            if(isEOF()) {
                handler.createErrorBuilder(SyntaxError.class)
                       .addErrorNumber(MessageNumber.SYNTAX_ERROR_100)
                       .addSuggestionNumber(MessageNumber.SYNTAX_SUGGEST_1000)
                       .asSyntaxErrorBuilder()
                       .addLocation(location,getText())
                       .setLexerMode()
                       .generateError();
            }
            else if(nextLA('\\'))
                escapeSequence(str);
            else {
                str.append(lookahead);
                consume();
            }
        }

        // By default, all text literals will be treated as strings!
        if(match('\'')) {
            str.append('\"');
            if(!match('\'')) {
                handler.createErrorBuilder(SyntaxError.class)
                        .addErrorNumber(MessageNumber.SYNTAX_ERROR_106)
                        .addSuggestionNumber(MessageNumber.SYNTAX_SUGGEST_1000)
                        .asSyntaxErrorBuilder()
                        .addLocation(location.start.line + "| " + text)
                        .generateError();
            }
            str.append('\'');
        }

        return new Token(TokenType.STR_LIT, str.toString(), location.copy());
    }

    /**
     * Tokenizes a real number literal.
     * @param newReal The real number we are trying to build
     * @return Real number token.
     */
    private Token realLit(StringBuilder newReal) {
        while(isDigit()) {
            newReal.append(lookahead);
            consume();
        }
        return new Token(TokenType.REAL_LIT, newReal.toString(), location.copy());
    }

    /**
     * Creates an identifier token.
     * @param newID The identifier we are trying to create.
     * @return Identifier token.
     */
    private Token createID(StringBuilder newID) {
        while(isDigit() || isLetter() || lookahead == '_') {
            newID.append(lookahead);
            consume();
        }
        return new Token(TokenType.ID, newID.toString(), location.copy());
    }

    /**
     * Tokenizes a name.
     * @return Identifier or keyword token.
     */
    private Token name() {
        StringBuilder createStr = new StringBuilder();

        while(isLetter() || lookahead == '_' || match('#')) {
            createStr.append(lookahead);
            consume();
        }

        if(isDigit()) { return createID(createStr); }

        String nextStr = createStr.toString();

        if(nextStr.equals("as") && match('?')) {
            return new Token(TokenType.AS, "as?", location.copy());
        }

        return switch(nextStr) {
            // -----------------------------------------------------------------------------------------------
            //                                            KEYWORDS
            // -----------------------------------------------------------------------------------------------

            case "abstr" -> new Token(TokenType.ABSTR, "abstr", location.copy());
            case "and" -> new Token(TokenType.AND, "and", location.copy());
            case "Array" -> new Token(TokenType.ARRAY, "Array", location.copy());
            case "Bool" -> new Token(TokenType.BOOL, "Bool", location.copy());
            case "break" -> new Token(TokenType.BREAK, "break", location.copy());
            case "cast" -> new Token(TokenType.CAST, "cast", location.copy());
            case "Char" -> new Token(TokenType.CHAR, "Char", location.copy());
            case "choice" -> new Token(TokenType.CHOICE, "choice", location.copy());
            case "cin" -> new Token(TokenType.CIN, "cin", location.copy());
            case "class" -> new Token(TokenType.CLASS, "class", location.copy());
            case "const" -> new Token(TokenType.CONST, "const", location.copy());
            case "continue" -> new Token(TokenType.CONTINUE, "continue", location.copy());
            case "cout" -> new Token(TokenType.COUT, "cout", location.copy());
            case "def" -> new Token(TokenType.DEF, "def", location.copy());
            case "discr" -> new Token(TokenType.DISCR, "discr", location.copy());
            case "do" -> new Token(TokenType.DO, "do", location.copy());
            case "else" -> new Token(TokenType.ELSE, "else", location.copy());
            case "endl" -> new Token(TokenType.ENDL, "endl", location.copy());
            case "except" -> new Token(TokenType.EXCEPT, "except", location.copy());
            case "final" -> new Token(TokenType.FINAL, "final", location.copy());
            case "for" -> new Token(TokenType.FOR, "for", location.copy());
            case "global" -> new Token(TokenType.GLOBAL, "global", location.copy());
            case "if" -> new Token(TokenType.IF, "if", location.copy());
            case "in" -> new Token(TokenType.IN, "in", location.copy());
            case "Int" -> new Token(TokenType.INT, "Int", location.copy());
            case "inherits" -> new Token(TokenType.INHERITS, "inherits", location.copy());
            case "inout" -> new Token(TokenType.INOUT, "inout", location.copy());
            case "inrev" -> new Token(TokenType.INREV, "inrev", location.copy());
            case "instanceof" -> new Token(TokenType.INSTANCEOF, "instanceof", location.copy());
            case "List" -> new Token(TokenType.LIST, "List", location.copy());
            case "local" -> new Token(TokenType.LOCAL, "local", location.copy());
            case "loop" -> new Token(TokenType.LOOP, "loop", location.copy());
            case "main" -> new Token(TokenType.MAIN, "main", location.copy());
            case "method" -> new Token(TokenType.METHOD, "method", location.copy());
            case "new" -> new Token(TokenType.NEW, "new", location.copy());
            case "not" -> new Token(TokenType.NOT, "not", location.copy());
            case "on" -> new Token(TokenType.ON, "on", location.copy());
            case "only" -> new Token(TokenType.ONLY, "only", location.copy());
            case "operator" -> new Token(TokenType.OPERATOR, "operator", location.copy());
            case "or" -> new Token(TokenType.OR, "or", location.copy());
            case "other" -> new Token(TokenType.OTHER, "other", location.copy());
            case "out" -> new Token(TokenType.OUT, "out", location.copy());
            case "overload" -> new Token(TokenType.OVERLOAD, "overload", location.copy());
            case "override" -> new Token(TokenType.OVERRIDE, "override", location.copy());
            case "parent" -> new Token(TokenType.PARENT, "parent", location.copy());
            case "property" -> new Token(TokenType.PROPERTY, "property", location.copy());
            case "protected" -> new Token(TokenType.PROTECTED, "protected", location.copy());
            case "public" -> new Token(TokenType.PUBLIC, "public", location.copy());
            case "pure" -> new Token(TokenType.PURE, "pure", location.copy());
            case "Real" -> new Token(TokenType.REAL, "Real", location.copy());
            case "recurs" -> new Token(TokenType.RECURS, "recurs", location.copy());
            case "ref" -> new Token(TokenType.REF, "ref", location.copy());
            case "rename" -> new Token(TokenType.RENAME, "rename", location.copy());
            case "return" -> new Token(TokenType.RETURN, "return", location.copy());
            case "retype" -> new Token(TokenType.RETYPE, "retype", location.copy());
            case "scalar" -> new Token(TokenType.SCALAR, "scalar", location.copy());
            case "set" -> new Token(TokenType.SET, "set", location.copy());
            case "slice" -> new Token(TokenType.SLICE, "slice", location.copy());
            case "stop" -> new Token(TokenType.STOP, "stop", location.copy());
            case "String" -> new Token(TokenType.STRING, "String", location.copy());
            case "then" -> new Token(TokenType.THEN, "then", location.copy());
            case "Tuple" -> new Token(TokenType.TUPLE, "Tuple", location.copy());
            case "type" -> new Token(TokenType.TYPE, "type", location.copy());
            case "uninit" -> new Token(TokenType.UNINIT, "uninit", location.copy());
            case "until" -> new Token(TokenType.UNTIL, "until", location.copy());
            case "Void" -> new Token(TokenType.VOID, "Void", location.copy());
            case "while" -> new Token(TokenType.WHILE, "while", location.copy());

            // -----------------------------------------------------------------------------------------------
            //                                      BOOLEAN LITERALS
            // -----------------------------------------------------------------------------------------------

            case "True" -> new Token(TokenType.BOOL_LIT, "True", location.copy());
            case "False" -> new Token(TokenType.BOOL_LIT, "False", location.copy());
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
            newNum.append(lookahead);
            consume();
        }

        // Only tokenize a real number when we don't have a loop operator
        // Sorta a hack, but it is what it is... :')
        if(lookahead == '.' && pos +1 < file.length() && file.charAt(pos +1) != '.') {
                match('.');
                newNum.append('.');
                return realLit(newNum);
        }
        return new Token(TokenType.INT_LIT, newNum.toString(), location.copy());
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
        while(lookahead != EOF) {
            switch(lookahead) {
                case ' ', '\t', '\r', '\n':
                    consumeWhitespace();
                    break;
                case '=':
                    consume();
                    if(match('=')) { return new Token(TokenType.EQEQ, "==", location.copy()); }
                    if(match('>')) { return new Token(TokenType.ARROW, "=>", location.copy()); }
                    return new Token(TokenType.EQ, "=", location.copy());
                case '+':
                    consume();
                    if(match('=')) { return new Token(TokenType.PLUSEQ, "+=", location.copy()); }
                    return new Token(TokenType.PLUS, "+", location.copy());
                case '-':
                    consume();
                    if(match('=')) { return new Token(TokenType.MINUSEQ, "-=", location.copy()); }
                    return new Token(TokenType.MINUS, "-", location.copy());
                case '*':
                    consume();
                    if(match('*')) {
                        if(match('=')) { return new Token(TokenType.EXPEQ, "**=", location.copy()); }
                        return new Token(TokenType.EXP, "**", location.copy());
                    }
                    if(match('=')) { return new Token(TokenType.MULTEQ, "*=", location.copy()); }
                    return new Token(TokenType.MULT, "*", location.copy());
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
                    if(match('=')) { return new Token(TokenType.DIVEQ, "/=", location.copy()); }
                    return new Token(TokenType.DIV, "/", location.copy());
                case '~':
                    consume();
                    return new Token(TokenType.BNOT, "~", location.copy());
                case '%':
                    consume();
                    if(match('=')) { return new Token(TokenType.MODEQ, "%=", location.copy()); }
                    return new Token(TokenType.MOD, "%", location.copy());
                case '!':
                    consume();
                    if(match('=')) { return new Token(TokenType.NEQ, "!=", location.copy()); }
                    if(isLetter()) {
                        StringBuilder sb = new StringBuilder();
                        while(isLetter()) {
                            sb.append(lookahead);
                            consume();
                        }
                        if(sb.toString().equals("instanceof")) {
                            return new Token(TokenType.NINSTANCEOF, "!instanceof", location.copy());
                        }
                    }
                    return new Token(TokenType.ERROR, "ERROR", location.copy());
                case '#':
                    consume();
                    if(isLetter()) {
                        StringBuilder sb = new StringBuilder();
                        while(isLetter()) {
                            sb.append(lookahead);
                            consume();
                        }
                        if(sb.toString().equals("include")) {
                            return new Token(TokenType.INCLUDE, "#include", location.copy());
                        }
                        if(sb.toString().equals("exclude")) {
                            return new Token(TokenType.EXCLUDE, "#exclude", location.copy());
                        }
                    }
                    return new Token(TokenType.ERROR, "ERROR", location.copy());
                case '<':
                    consume();
                    if(match('=')) {
                        if(match('>')) { return new Token(TokenType.UFO, "<=>", location.copy()); }
                        return new Token(TokenType.LTEQ, "<=", location.copy());
                    }
                    if(match('>')) { return new Token(TokenType.LTGT, "<>", location.copy()); }
                    if(match(':')) { return new Token(TokenType.MIN, "<:", location.copy()); }
                    if(match('<')) { return new Token(TokenType.SLEFT, "<<", location.copy()); }
                    return new Token(TokenType.LT, "<", location.copy());
                case '>':
                    consume();
                    if(match('=')) { return new Token(TokenType.GTEQ, ">=", location.copy()); }
                    if(match('>')) { return new Token(TokenType.SRIGHT, ">>", location.copy()); }
                    return new Token(TokenType.GT, ">", location.copy());
                case ':':
                    consume();
                    if(match('>')) { return new Token(TokenType.MAX, ":>", location.copy()); }
                    return new Token(TokenType.COLON, ":", location.copy());
                case '.':
                    consume();
                    if(isDigit()) { return realLit(new StringBuilder(".")); }
                    if(match('.')) { return new Token(TokenType.INC, "..", location.copy()); }
                    return new Token(TokenType.PERIOD, ".", location.copy());
                case ',':
                    consume();
                    return new Token(TokenType.COMMA, ",", location.copy());
                case '(':
                    consume();
                    return new Token(TokenType.LPAREN, "(", location.copy());
                case ')':
                    consume();
                    return new Token(TokenType.RPAREN, ")", location.copy());
                case '{':
                    consume();
                    return new Token(TokenType.LBRACE, "{", location.copy());
                case '}':
                    consume();
                    return new Token(TokenType.RBRACE, "}", location.copy());
                case '[':
                    consume();
                    return new Token(TokenType.LBRACK, "[", location.copy());
                case ']':
                    consume();
                    return new Token(TokenType.RBRACK, "]", location.copy());
                case '@':
                    consume();
                    return new Token(TokenType.AT, "@", location.copy());
                case '\'': return charLit();
                case '?':
                    consume();
                    if(match('.')) { return new Token(TokenType.ELVIS, "?.", location.copy()); }
                    else return new Token(TokenType.ERROR, "ERROR", location.copy());
                case '|':
                    consume();
                    return new Token(TokenType.BOR, "|", location.copy());
                case '&':
                    consume();
                    return new Token(TokenType.BAND, "&", location.copy());
                case '^':
                    consume();
                    return new Token(TokenType.XOR, "^", location.copy());
                default:
                    if(isLetter() || lookahead == '_') { return name(); }
                    if(isDigit()) { return number(new StringBuilder()); }
                    consume();
                    return new Token(TokenType.ERROR, "ERROR", location.copy());
            }
        }
        return new Token(TokenType.EOF, "EOF", location.copy());
    }
}
