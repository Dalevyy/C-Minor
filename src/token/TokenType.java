package token;

/**
 * Enumeration representing all possible tokens in C Minor.
 * <p><br>
 *     This is a list of every token that the C Minor lexer
 *     recognizes. Currently, there are 126 possible tokens
 *     that can be generated.
 * </p>
 * @author Daniel Levy
 */
public enum TokenType {
    EOF,        // $
    ERROR,      // Default Error

    /* ###################### KEYWORDS ###################### */

    ABSTR,       // abstr
    ARRAY,       // Array
    BOOL,        // Bool
    BREAK,       // break
    CAST,        // cast
    CHAR,        // Char
    CHOICE,      // choice
    CIN,         // cin
    CLASS,       // class
    CONST,       // const
    CONTINUE,    // continue
    COUT,        // cout
    DEF,         // def
    DISCR,       // discr
    DO,          // do
    ELSE,        // else
    ENDL,        // endl
    EXCEPT,      // except
    EXCLUDE,     // #exclude
    FINAL,       // final
    FOR,         // for
    GLOBAL,      // global
    IF,          // if
    IN,          // in
    INCLUDE,     // #include
    INT,         // Int
    INHERITS,    // inherits
    INOUT,       // inout
    INREV,       // inrev
    LIST,        // List
    LOCAL,       // local
    LOOP,        // loop
    MAIN,        // main
    METHOD,      // method
    NEW,         // new
    NOT,         // not
    ON,          // on
    ONLY,        // only
    OPERATOR,    // operator
    OTHER,       // other
    OUT,         // out
    OVERLOAD,    // overload
    OVERRIDE,    // override
    PARENT,      // parent
    PROPERTY,    // property
    PROTECTED,   // protected
    PUBLIC,      // public
    PURE,        // pure
    REAL,        // Real
    RECURS,      // recurs
    REF,         // ref
    RENAME,      // rename
    RETURN,      // return
    RETYPE,      // retype
    SCALAR,      // scalar
    SET,         // set
    SLICE,       // slice
    STOP,        // stop
    STRING,      // String
    THEN,        // then
    TUPLE,       // Tuple
    TYPE,        // type
    UNINIT,      // uninit
    UNTIL,       // until
    VOID,        // Void
    WHILE,       // while

    /* ###################### LITERALS ###################### */

    /*
        Additional RegEx Information:
            1) letter = a | ... | z | A | ... | Z
            2) digit = 0 | ... | 9
            3) letter-digit = letter | digit
    */
    ID,          // letter letter-digit*
    INT_LIT,     // digit+
    REAL_LIT,    // digit digit* . digit+ | . digit+
    CHAR_LIT,    // 'char'
    STR_LIT,     // 'char*'
    TEXT_LIT,    // '''char*'''
    BOOL_LIT,    // true || false


    /* ###################### OPERATORS ###################### */

    EQ,          // =
    PLUS,        // +
    MINUS,       // -
    MULT,        // *
    DIV,         // /
    MOD,         // %
    EXP,         // **
    BNOT,       // ~
    EQEQ,        // ==
    NEQ,         // !=
    LT,          // <
    LTEQ,        // <=
    GT,          // >
    GTEQ,        // >=
    LTGT,        // <>
    UFO,         // <=>
    MIN,         // <:
    MAX,         // :>
    INC,         // ..
    AS,          // as?
    INSTANCEOF,  // instanceof
    NINSTANCEOF, // !instanceof
    AND,         // and
    OR,          // or
    ELVIS,       // ?.
    BAND,        // &
    BOR,         // |
    XOR,         // ^
    SLEFT,       // <<
    SRIGHT,      // >>
    PLUSEQ,      // +=
    MINUSEQ,     // -=
    MULTEQ,      // *=
    DIVEQ,       // /=
    MODEQ,       // %=
    EXPEQ,       // **=

    /* ###################### SEPARATORS ###################### */

    LPAREN,      // (
    RPAREN,      // )
    LBRACE,      // {
    RBRACE,      // }
    LBRACK,      // [
    RBRACK,      // ]
    COLON,       // :
    PERIOD,      // .
    COMMA,       // ,
    AT,          // @
    ARROW,       // =>

    /* ###################### DEBUG INFO ###################### */

    SPACE,       // ' '
    TAB,         // '\t'
    RET,         // '\r'
    NEWLINE,     // '\n'
    SEMICOLON,   // ;
}
