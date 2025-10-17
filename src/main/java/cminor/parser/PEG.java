package cminor.parser;

import cminor.ast.AST;
import cminor.ast.classbody.ClassBody;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.expressions.Literal.ConstantType;
import cminor.ast.misc.*;
import cminor.ast.misc.TypeParam.TypeAnnotation;
import cminor.ast.operators.*;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.ast.operators.BinaryOp.BinaryType;
import cminor.ast.operators.LoopOp.LoopType;
import cminor.ast.operators.UnaryOp.UnaryType;
import cminor.ast.statements.*;
import cminor.ast.statements.ListStmt.Commands;
import cminor.ast.topleveldecls.*;
import cminor.ast.types.*;
import cminor.ast.types.ScalarType.Scalars;
import cminor.lexer.Lexer;
import cminor.messages.CompilationMessage;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.syntax.SyntaxError;
import cminor.token.Token;
import cminor.token.TokenType;
import cminor.utilities.Vector;

public class PEG {

    /**
     * The {@link Lexer} responsible for generating tokens for the parser.
     */
    private final Lexer input;

    /**
     * A {@link Vector} of tokens representing all the lookaheads we have so far.
     */
    private final Vector<Token> lookaheads;

    /**
     * A {@link Vector} keeping track of all parsing positions we are at.
     */
    private final Vector<Integer> positions;

    /**
     * Handler responsible for dealing with error messages.
     */
    private final MessageHandler handler;

    /**
     * The starting position denoting the token we began the parsing for.
     */
    private int pos;

    private boolean test = false;

    /**
     * Default constructor for {@link PEG}.
     * @param input {@link Lexer} object to store into {@link #input}.
     */
    public PEG(Lexer input) {
        this.input = input;
        this.lookaheads = new Vector<>();
        this.positions = new Vector<>();
        this.pos = 0;
        this.handler = new MessageHandler();
    }

    /**
     * Consumes an input token when a successful match is found.
     */
    private void consume() {
        pos++; // Update the internal starting position

        if(pos == lookaheads.size() && !expects()) {
            pos = 0;            // Reset the starting position back to 0
            lookaheads.clear(); // Clear out the token array to save on memory... Is this needed?
        }

        synchronize(1); // Add the token to the vector if it's not there!
    }

    /**
     * Checks if we expect anymore input based on the grammar rule being parsed.
     * <p>
     *     If the {@link #positions} vector is empty, then this implies we have
     *     successfully parsed a grammar rule. Thus, we can reset our lookahead
     *     tokens and starting position, so we can parse a different rule.
     * </p>
     * @return {@code True} if there are more tokens to parse for a given grammar rule, {@code False} otherwise.
     */
    private boolean expects() { return !positions.isEmpty(); }

    /**
     * Checks if {@link #lookaheads} contains a lookahead at a given index.
     * <p>
     *     This method will ensure that if a lookahead token is not yet present in the
     *     {@link #lookaheads} vector, then it will be added to the vector, so we can
     *     continue to parse.
     * </p>
     * @param index The index we want to check for a token inside {@link #lookaheads}.
     */
    private void synchronize(int index) {
        if((pos+index)-1 > lookaheads.size()-1) // Is the next lookahead available at the index?
            fill(( (pos+index)-1) - (lookaheads.size()-1) ); // Add lookahead to the vector!
    }

    /**
     * Updates {@link #lookaheads} to include the next tokens needed by the parser.
     * <p>
     *     This method is only called from {@link #synchronize(int)} when we need to
     *     update {@link #lookaheads} to include tokens not yet present in the vector.
     * </p>
     * @param tokenCount The amount of tokens we want to add to {@link #lookaheads}.
     */
    private void fill(int tokenCount) {
        // Add 'tokenCount' amount of tokens to vector
        for(int i = 0; i <= tokenCount; i++)
            lookaheads.add(input.nextToken());
    }

    /**
     * Returns the current value of {@link #pos}.
     * <p>
     *     This denotes the starting position in {@link #lookaheads} where we began
     *     to parse a grammar rule.
     *     vector where
     * </p>
     * @return {@code Int} representing the starting position for parsing a grammar rule.
     */
    private int mark() {
        positions.add(pos);
        return pos;
    }

    /**
     * Removes the last position inserted into {@link #positions}.
     */
    private void remove() { positions.removeLast(); }

    /**
     * Resets the {@link #pos} if an error occurs while parsing.
     * <p>
     *     This method is only triggered when an error occurs during parsing. If we are not
     *     able to parse a given grammar rule, an exception is thrown, and we have to go back
     *     to the previous starting position in order to parse a separate grammar rule. If no
     *     more rules are available, then we have to generate a parsing error.
     * </p>
     */
    private void reset() {
        pos = positions.removeLast();
        while(!positions.isEmpty() && positions.getLast() >= pos)
            positions.removeLast();
    }

    /**
     * Resets the {@link #pos} based on a passed position value. See {@link #reset()}.
     * @param pos The position we wish to reset {@link #positions} to.
     */
    private void reset(int pos) {
        this.pos = pos;
        while(!positions.isEmpty() && positions.getLast() >= pos)
            positions.removeLast();
    }

    /**
     * Retrieves the current lookahead token.
     * @return {@link Token} representing the current lookahead token.
     */
    private Token currentLA() {
        synchronize(1);        // Make sure the token is present in the vector
        return lookaheads.get(pos); // Retrieve the lookahead based on starting position
    }

    /**
     * Checks if the next lookahead in {@link #lookaheads} matches the passed {@link TokenType}.
     * @param expected The {@link TokenType} we are expecting to see next.
     * @return {@code True} if the next lookahead matches what we are expecting, {@code False} otherwise.
     */
    private boolean nextLA(TokenType expected) { return currentLA().getTokenType() == expected; }

    /**
     * Checks if the current lookahead matches the expected token we wish to see.
     * @param expectedToken The {@link TokenType} that is expected in the input.
     */
    private void match(TokenType expectedToken) {
        if(!nextLA(expectedToken)) {
            handler.createErrorBuilder(SyntaxError.class)
                   .addErrorNumber(MessageNumber.SYNTAX_ERROR_101)
                   .addErrorArgs(expectedToken,currentLA().getTokenType())
                   .asSyntaxErrorBuilder()
                   .addLocation(currentLA(),input)
                   .generateError();
        }

        consume();
    }

    /**
     * Returns the starting token that denotes an AST node.
     * @return {@link Token}
     */
    private Token getStartToken() { return lookaheads.get(positions.removeLast()); }

    /**
     * Generates the metadata associated with a given {@link AST} node.
     * @return {@link Token} representing the metadata for an {@link AST} node.
     */
    private Token metadata() {return input.generateMetaData(getStartToken(),lookaheads.get(pos-1)); }

    /**
     * Generates the metadata associated with a given {@link Expression} node.
     * <p>
     *     Unlike {@link #metadata()}, this method will not return tokens from {@link #positions}. We do
     *     this to avoid removing the starting token for long complex expressions since we will need it
     *     for multiple {@link AST} nodes!
     * </p>
     * @return {@link Token} representing the metadata for an {@link Expression} node.
     */
    private Token exprMetadata() {
        return input.generateMetaData(lookaheads.get(positions.getLast()),lookaheads.get(pos-1));
    }

    /**
     * Tries to parse C Minor code written inside the {@link cminor.interpreter.VM}.
     * @return A {@link Vector} of {@link AST} nodes representing the code that a user wrote.
     */
    public Vector<? extends AST> parse() {
        Vector<AST> nodes = new Vector<>();
        int start = 0;

        while(true) {
            switch(currentLA().getTokenType()) {
                // Case 1) When EOF is found, the parsing is over!
                case EOF:
                    return nodes;
                // Case 2) Special case to handle any imported files
                case INCLUDE:
                    break;  //TODO: Add support for imports
                // Case 3) If a construct begins with the 'def' keyword, there are 4 possible paths the parser can take.
                case DEF:
                    // 2.1) Parse an enumeration
                    try {
                        start = mark();
                        nodes.add(enumType());
                        break;
                    } catch(CompilationMessage msg) { reset(start); }

                    // 2.2) Parse a global variable
                    try {
                        start = mark();
                        nodes.merge(globalVariable());
                        break;
                    } catch(CompilationMessage msg) { reset(start); }

                    // 2.3) Parse a local variable
                    try {
                        start = mark();
                        nodes.merge(declaration());
                        break;
                    } catch(CompilationMessage msg) { reset(start); }

                    // 2.4) Parse a function
                    // If all of the above fails, then output the error message!
                    try {
                        mark();
                        nodes.add(function());
                        break;
                    } catch(CompilationMessage msg) { msg.printMessage(); }
                // Case 4) Case to handle classes
                case CLASS:
                case ABSTR:
                case FINAL:
                    mark();
                    nodes.add(classType());
                    break;
                // Case 5) Everything else should represent some type of statement!
                default:
                    mark();
                    nodes.add(statement());
            }
        }
    }

    //TODO: Add rules
    // compilation ::= import_stmt* enum_type* global_variable* class_type* function* main_function ;
    // import_stmt ::= '#include' String_literal ;

    // enum_type ::= 'def' Name 'type' '=' '{' enum_field (',' enum_field)* '}' ;
    private EnumDecl enumType() {
        match(TokenType.DEF);
        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.TYPE);
        match(TokenType.EQ);
        match(TokenType.LBRACE);

        Vector<Var> constants = new Vector<>(enumField());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            constants.add(enumField());
        }

        match(TokenType.RBRACE);
        return new EnumDecl(metadata(),name,constants);
    }

    // enum_field ::= Name ('=' constant)? ;
    private Var enumField() {
        mark();
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Expression expr = null;
        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            expr = constant();
            return new Var(metadata(),name,expr);
        }
        return new Var(metadata(),name);
    }

    // global_variable ::= 'def' ('const' | 'global') variable_decl ;
    private Vector<AST> globalVariable() {
        match(TokenType.DEF);

        boolean constant = false;
        switch(currentLA().getTokenType()) {
            case CONST:
                match(TokenType.CONST);
                constant = true;
                break;
            default:
                match(TokenType.GLOBAL);
        }

        Vector<Var> vars = variableDecl();
        Vector<AST> globals = new Vector<>();
        Token metadata = metadata(); // Use the same metadata for multiple global declarations in a single line!
        for(Var var : vars)
            globals.add(new GlobalDecl(metadata,var,constant));

        return globals;
    }

    // variable_decl ::= variable_decl_list ;
    private Vector<Var> variableDecl() { return variableDeclList(); }

    // variable_decl_list ::= variable_decl_init (',' variable_decl_init)* ;
    private Vector<Var> variableDeclList() {
        Vector<Var> vars = new Vector<>(variableDeclInit());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            vars.add(variableDeclInit());
        }
        return vars;
    }

    // variable_decl_init ::= Name ':' type ('=' (expression | 'uninit'))? ;
    private Var variableDeclInit() {
        mark();
        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type type = type();

        Expression init = null;
        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            if(nextLA(TokenType.UNINIT))
                match(TokenType.UNINIT);
            else
                init = expression();
            return new Var(metadata(),name,init,type);
        }

        return new Var(metadata(),name,type);
    }

    // type ::= scalar_type | class_name | 'List' '[' type ']' | 'Array' '[' type ']' ;
    private Type type() {
        mark();
        switch(currentLA().getTokenType()) {
            case ID:
                return className();
            case LIST: {
                match(TokenType.LIST);
                match(TokenType.LBRACK);
                Type baseType = type();
                match(TokenType.RBRACK);

                if(baseType.isList()) {
                    baseType.asList().dims += 1;
                    baseType.copyMetaData(metadata());
                    return baseType;
                }
                return new ListType(metadata(),baseType,1);
            }
            case ARRAY: {
                match(TokenType.ARRAY);
                match(TokenType.LBRACK);
                Type baseType = type();
                match(TokenType.RBRACK);

                if(baseType.isArray()) {
                    baseType.asArray().dims += 1;
                    baseType.copyMetaData(metadata());
                    return baseType;
                }
                return new ArrayType(metadata(),baseType,1);
            }
            default:
                return scalarType();
        }
    }

    // scalar_type ::= discrete_type | 'String' | 'Real' ;
    private ScalarType scalarType() {
        switch(currentLA().getTokenType()) {
            case STRING:
                match(TokenType.STRING);
                return new ScalarType(metadata(),Scalars.STR);
            case REAL:
                match(TokenType.REAL);
                return new ScalarType(metadata(),Scalars.REAL);
            default:
                return discreteType();
        }
    }

    // discrete_type ::= 'Bool' | 'Int' | 'Char' ;
    private DiscreteType discreteType() {
        switch(currentLA().getTokenType()) {
            case BOOL:
                match(TokenType.BOOL);
                return new DiscreteType(metadata(),Scalars.BOOL);
            case INT:
                match(TokenType.INT);
                return new DiscreteType(metadata(),Scalars.INT);
            default:
                match(TokenType.CHAR);
                return new DiscreteType(metadata(),Scalars.CHAR);
        }
    }

    // class_name ::= Name ('<' type (',' type)* '>')? ;
    private ClassType className() {
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> types = new Vector<>();
        if(nextLA(TokenType.LT)) {
            match(TokenType.LT);
            types.add(type());

            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                types.add(type());
            }
            match(TokenType.GT);
        }

        return new ClassType(metadata(),name,types);
    }

    // class_type ::= ( 'abstr' | 'final' )? 'class' ID typefier_params? super_class? class_body ;
    private ClassDecl classType() {
        Modifier mod = new Modifier();
        switch(currentLA().getTokenType()) {
            case ABSTR:
                match(TokenType.ABSTR);
                mod.setAbstract();
                break;
            case FINAL:
                match(TokenType.FINAL);
                mod.setFinal();
                break;
            default:
                break;
        }

        match(TokenType.CLASS);
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> typeParams = new Vector<>();
        if(nextLA(TokenType.LT))
            typeParams = typeifierParams();

        ClassType superClass = null;
        if(nextLA(TokenType.INHERITS))
            superClass = superClass();

        ClassBody body = classBody();
        return new ClassDecl(metadata(),mod,name,typeParams,superClass,body);
    }

    // typeifier_params ::= '<' typeifier ( ',' typeifier )* '>' ;
    private Vector<TypeParam> typeifierParams() {
        match(TokenType.LT);

        Vector<TypeParam> typeParams = new Vector<>(typeifier());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            typeParams.add(typeifier());
        }

        match(TokenType.GT);
        return typeParams;
    }

    // typeifier ::= ( 'discr' | 'scalar' | 'class' )? ID ;
    private TypeParam typeifier() {
        mark();
        TypeAnnotation annotation = switch(currentLA().getTokenType()) {
            case DISCR -> {
                match(TokenType.DISCR);
                yield TypeParam.TypeAnnotation.DISCR;
            }
            case SCALAR -> {
                match(TokenType.SCALAR);
                yield TypeAnnotation.SCALAR;
            }
            case CLASS -> {
                match(TokenType.CLASS);
                yield TypeAnnotation.CLASS;
            }
            default -> null;
        };

        Name name = new Name(currentLA());
        match(TokenType.ID);
        return new TypeParam(metadata(),annotation,name);
    }

    // super_class ::= 'inherits' ID type_params? ;
    private ClassType superClass() {
        match(TokenType.INHERITS);

        mark();
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> types = new Vector<>();
        if(nextLA(TokenType.LT))
            types = typeParams();

        return new ClassType(metadata(),name,types);
    }

    // class_body ::= '{' field_decl* method_decl* '}' ;
    private ClassBody classBody() {
        mark();
        match(TokenType.LBRACE);

        Vector<FieldDecl> fields = new Vector<>();
        int start = 0; // This is the position we will reset the parser to if we can't parse another field declaration!
        try {
            while(true) {
                start = mark();
                fields.merge(fieldDecl());
            }
        } catch(CompilationMessage msg) { reset(start); }

        Vector<MethodDecl> methods = new Vector<>();
        // Try to parse methods until the end of the class is reached!
        while(!nextLA(TokenType.RBRACE))
            methods.add(methodDecl());
        match(TokenType.RBRACE);
        return new ClassBody(metadata(),fields,methods);
    }

    // field_decl ::= ('property' | 'protected' | 'public') variable_decl ;
    private Vector<FieldDecl> fieldDecl() {
        Modifier mod = new Modifier();
        switch(currentLA().getTokenType()) {
            case PROPERTY:
                match(TokenType.PROPERTY);
                mod.setProperty();
                break;
            case PROTECTED:
                match(TokenType.PROTECTED);
                mod.setProtected();
                break;
            default:
                match(TokenType.PUBLIC);
                mod.setPublic();
                break;
        }

        Vector<Var> vars = variableDecl();
        Vector<FieldDecl> fields = new Vector<>();

        Token metadata = metadata(); // Use the same metadata for multiple field declarations in a single line!
        for(Var v : vars)
            fields.add(new FieldDecl(metadata,mod,v));

        return fields;
    }

    // method_decl ::= method_class | operator_class ;
    private MethodDecl methodDecl() {
        // First, try to parse a method
        try {
            mark();
            return methodClass();
        }
        catch(CompilationMessage msg) { reset(); }

        // Then, try to parse an operator overload. If this fails, then there
        // is for sure an error, so we will print out an error.
        try {
            mark();
            return operatorClass();
        }
        catch(CompilationMessage msg) { msg.printMessage(); }

        return null; // Here to stop Java compiler complaining... \(-_-)/
    }

    // method_class ::= method_modifier attribute 'override'? 'method' method_header '=>' return_type block_statement ;
    private MethodDecl methodClass() {
        Modifier mod = methodModifier();

        while(nextLA(TokenType.FINAL) || nextLA(TokenType.PURE) || nextLA(TokenType.RECURS))
            mod.add(attribute());

        boolean override = false;
        if(nextLA(TokenType.OVERRIDE)) {
            match(TokenType.OVERRIDE);
            override = true;
        }

        match(TokenType.METHOD);
        Vector<Object> header = methodHeader();
        Name name = (Name) header.get(0);
        Vector<ParamDecl> params = (Vector<ParamDecl>) header.get(1);
        if(params == null)
            params = new Vector<>();

        match(TokenType.ARROW);
        Type returnType = returnType();
        BlockStmt block = blockStatement();

        return new MethodDecl(metadata(),mod,name,null,params,returnType,block,override);
    }

    // method_modifier ::= 'protected' | 'public' ;
    private Modifier methodModifier() {
        Modifier mod = new Modifier();
        if(nextLA(TokenType.PROTECTED)) {
            mod.setProtected();
            match(TokenType.PROTECTED);
        }
        else {
            mod.setPublic();
            match(TokenType.PUBLIC);
        }
        return mod;
    }

    // attribute ::= 'final' | 'pure' | 'recurs' ;
    private Modifier attribute() {
        Modifier mod = new Modifier();
        switch(currentLA().getTokenType()) {
            case FINAL:
                match(TokenType.FINAL);
                mod.setFinal();
                break;
            case PURE:
                match(TokenType.PURE);
                mod.setPure();
                break;
            case RECURS:
                match(TokenType.RECURS);
                mod.setRecursive();
        }

        return mod;
    }

    // method-header ::= ID '(' formal-params? ')' ;
    private Vector<Object> methodHeader() {
        Name name = new Name(currentLA());
        match(TokenType.ID);

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(name);
        header.add(params);

        return header;
    }

    // formal_params ::= param_modifier Name ':' type ( ',' param_modifier Name ':' type)* ;
    private Vector<ParamDecl> formalParams() {
        mark();
        Modifier mod = paramModifier();

        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type paramType = type();

        Vector<ParamDecl> params = new Vector<>();
        params.add(new ParamDecl(metadata(),mod,name,paramType));

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);

            mark();
            mod = paramModifier();

            name = new Name(currentLA());
            match(TokenType.ID);
            match(TokenType.COLON);
            paramType = type();

            params.add(new ParamDecl(metadata(),mod,name,paramType));
        }

        return params;
    }

    // param_modifier : 'in' | 'out' | 'inout' | 'ref' ;
    private Modifier paramModifier() {
        Modifier mod = new Modifier();
        switch(currentLA().getTokenType()) {
            case IN:
                match(TokenType.IN);
                mod.setInMode();
                break;
            case OUT:
                match(TokenType.OUT);
                mod.setOutMode();
                break;
            case INOUT:
                match(TokenType.INOUT);
                mod.setInOutMode();
                break;
            default:
                match(TokenType.REF);
                mod.setRefMode();
        }

        return mod;
    }

    // return-type ::= Void | type ;
    private Type returnType() {
        if(nextLA(TokenType.VOID)) {
            VoidType voidType = new VoidType(currentLA());
            match(TokenType.VOID);
            return voidType;
        }

        return type();
    }

    // operator_class ::= method_modifier 'final'? 'operator' operator_header '=>' return_type block_statement ;
    private MethodDecl operatorClass() {
        Modifier mod = methodModifier();
        if(nextLA(TokenType.FINAL)) {
            mod.setFinal();
            match(TokenType.FINAL);
        }

        match(TokenType.OPERATOR);
        Vector<Object> header = operatorHeader();
        Operator op = (Operator) header.get(0);
        Vector<ParamDecl> pd = (Vector<ParamDecl>) header.get(1);

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt block = blockStatement();

        return new MethodDecl(metadata(),mod,null,op,pd,rt,block,false);
    }

    // operator_header ::= operator_symbol '(' formal-params? ')' ;
    private Vector<Object> operatorHeader() {
        Operator op = operatorSymbol();
        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(op);
        header.add(params);
        return header;
    }

    // operator_symbol ::= binary_operator | unary_operator ;
    private Operator operatorSymbol() {
        if(nextLA(TokenType.BNOT) || nextLA(TokenType.NOT))
            return unaryOperator();
        else
            return binaryOperator();
    }

    // binary_operator ::= <= | < | > | >= | == | <> | <=> | + | - | * | / | % | ** ;
    private BinaryOp binaryOperator() {
        return switch (currentLA().getTokenType()) {
            case TokenType.LTEQ -> {
                match(TokenType.LTEQ);
                yield new BinaryOp(currentLA(), BinaryType.LTEQ);
            }
            case TokenType.LT -> {
                match(TokenType.LT);
                yield new BinaryOp(currentLA(), BinaryType.LT);
            }
            case TokenType.GT -> {
                match(TokenType.GT);
                yield new BinaryOp(currentLA(), BinaryType.GT);
            }
            case TokenType.GTEQ -> {
                match(TokenType.GTEQ);
                yield new BinaryOp(currentLA(), BinaryType.GTEQ);
            }
            case TokenType.EQEQ -> {
                match(TokenType.EQEQ);
                yield new BinaryOp(currentLA(), BinaryType.EQEQ);
            }
            case TokenType.LTGT -> {
                match(TokenType.LTGT);
                yield new BinaryOp(currentLA(), BinaryType.LTGT);
            }
            case TokenType.UFO -> {
                match(TokenType.UFO);
                yield new BinaryOp(currentLA(), BinaryType.UFO);
            }
            case TokenType.PLUS -> {
                match(TokenType.PLUS);
                yield new BinaryOp(currentLA(), BinaryType.PLUS);
            }
            case TokenType.MINUS -> {
                match(TokenType.MINUS);
                yield new BinaryOp(currentLA(), BinaryType.MINUS);
            }
            case TokenType.MULT -> {
                match(TokenType.MULT);
                yield new BinaryOp(currentLA(), BinaryType.MULT);
            }
            case TokenType.DIV -> {
                match(TokenType.DIV);
                yield new BinaryOp(currentLA(), BinaryType.DIV);
            }
            case TokenType.MOD -> {
                match(TokenType.MOD);
                yield new BinaryOp(currentLA(), BinaryType.MOD);
            }
            default -> {
                match(TokenType.EXP);
                yield new BinaryOp(currentLA(), BinaryType.EXP);
            }
        };
    }

    // unary-operator ::= ~ | not ;
    private UnaryOp unaryOperator() {
        return switch(currentLA().getTokenType()) {
            case BNOT -> {
                match(TokenType.BNOT);
                yield new UnaryOp(currentLA(),UnaryType.BNOT);
            }
            default -> {
                match(TokenType.NOT);
                yield new UnaryOp(currentLA(),UnaryType.NOT);
            }
        };
    }

    // function ::= 'def' ( 'pure' | 'recurs' )? function_header '=>' return_type block_statement ;
    private FuncDecl function() {
        match(TokenType.DEF);

        Modifier mod = new Modifier();
        if(nextLA(TokenType.PURE)) {
            match(TokenType.PURE);
            mod.setPure();
        }
        else if(nextLA(TokenType.RECURS)) {
            match(TokenType.RECURS);
            mod.setRecursive();
        }

        Vector<Object> header = functionHeader();
        Name name = (Name) header.get(0);

        Vector<TypeParam> typeParams = (Vector<TypeParam>) header.get(1);
        Vector<ParamDecl> params = (Vector<ParamDecl>) header.get(2);

        match(TokenType.ARROW);
        Type returnType = returnType();
        BlockStmt block = blockStatement();

        return new FuncDecl(metadata(),mod,name,typeParams,params,returnType,block);
    }

    // function_header ::= ID function_type_params? '(' formal_params? ')' ;
    private Vector<Object> functionHeader() {
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> typeParams = new Vector<>();
        if(nextLA(TokenType.LT))
            typeParams = typeifierParams();

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();

        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(name);
        header.add(typeParams);
        header.add(params);

        return header;
    }

    // main_function ::= 'def' 'main' args '=>' return_type block_statement ;
    private MainDecl mainFunction() {
        match(TokenType.DEF);
        match(TokenType.MAIN);
        Vector<ParamDecl> params = args();
        match(TokenType.ARROW);

        Type returnType = returnType();
        BlockStmt block = blockStatement();
        return new MainDecl(metadata(),params,returnType,block);
    }

    // args ::= '(' formal_params? ')' ;
    private Vector<ParamDecl> args() {
        Vector<ParamDecl> params = new Vector<>();

        match(TokenType.LPAREN);
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();
        match(TokenType.RPAREN);

        return params;
    }

    // block_statement ::= '{' declaration* statement* '}' ;
    private BlockStmt blockStatement() {
        mark();
        match(TokenType.LBRACE);

        Vector<LocalDecl> locals = new Vector<>();
        while(nextLA(TokenType.DEF)) {
            mark();
            Vector<AST> vars = declaration();
            for(AST local : vars)
                locals.add(local.asStatement().asLocalDecl());
        }

        Vector<Statement> stmts = new Vector<>();
        while(!nextLA(TokenType.RBRACE)) {
            mark();
            stmts.add(statement());
        }

        match(TokenType.RBRACE);
        return new BlockStmt(metadata(),locals,stmts);
    }

    // declaration ::= 'def' 'local'? variable_decl ;
    private Vector<AST> declaration() {
        match(TokenType.DEF);

        if(nextLA(TokenType.LOCAL))
            match(TokenType.LOCAL);
        Vector<Var> vars = variableDecl();
        Vector<AST> locals = new Vector<>();
        Token metadata = metadata(); // Use the same metadata for multiple local declarations in a single line!

        for(Var v : vars)
            locals.add(new LocalDecl(metadata,v));

        return locals;
    }

    // statement ::= 'stop'
    //             | input_statement
    //             | output_statement
    //             | return_statement
    //             | block_statement
    //             | if_statement
    //             | while_statement
    //             | do_while_statement
    //             | for_statement
    //             | choice_statement
    //             | input_statement
    //             | output_statement
    //             | list_command_statement
    //             | expression_statement
    //             ;
    private Statement statement() {
        switch(currentLA().getTokenType()) {
            case STOP:
                match(TokenType.STOP);
                return new StopStmt(metadata());
            case CIN:
                return inputStatement();
            case COUT:
                return outputStatement();
            case RETURN: return returnStatement();
            case LBRACE: return blockStatement();
            case IF: return ifStatement();
            case WHILE: return whileStatement();
            case DO: return doWhileStatement();
            case FOR: return forStatement();
            case CHOICE: return choiceStatement();
            default:
                if(currentLA().equals("append") || currentLA().equals("insert") || currentLA().equals("remove"))
                    return listCommandStatement();
                else
                    return expressionStatement();
        }
    }

    // return_statement ::= 'return' expression? ;
    private ReturnStmt returnStatement() {
        match(TokenType.RETURN);
        Expression expr = null;

        try { expr = expression(); }
        catch(CompilationMessage msg) { reset(); }

        return new ReturnStmt(metadata(),expr);
    }

    // expression_statement ::= 'set' expression assignment_operator expression
    //                        | 'retype' expression '=' object_constant
    //                        |  expression ;
    private Statement expressionStatement() {
        if(nextLA(TokenType.SET)) {
            match(TokenType.SET);
            Expression LHS = expression();
            AssignOp assignOp = assignmentOperator();
            Expression RHS = expression();
            return new AssignStmt(metadata(),LHS,RHS,assignOp);
        }
        else if(nextLA(TokenType.RETYPE)) {
            match(TokenType.RETYPE);
            Expression LHS = expression();
            match(TokenType.EQ);

            mark();
            NewExpr RHS = objectConstant();
            return new RetypeStmt(metadata(),LHS,RHS);
        }
        else {
            Expression expr = expression();
            return new ExprStmt(metadata(),expr);
        }
    }

    // assignment_operator ::= '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**=' ;
    private AssignOp assignmentOperator() {
        return switch(currentLA().getTokenType()) {
            case EQ -> {
                match(TokenType.EQ);
                yield new AssignOp(currentLA(), AssignType.EQ);
            }
            case PLUSEQ -> {
                match(TokenType.PLUSEQ);
                yield new AssignOp(currentLA(), AssignType.PLUSEQ);
            }
            case MINUSEQ -> {
                match(TokenType.MINUSEQ);
                yield new AssignOp(currentLA(), AssignType.MINUSEQ);
            }
            case MULTEQ -> {
                match(TokenType.MULTEQ);
                yield new AssignOp(currentLA(), AssignType.MULTEQ);
            }
            case DIVEQ -> {
                match(TokenType.DIVEQ);
                yield new AssignOp(currentLA(), AssignType.DIVEQ);
            }
            case MODEQ -> {
                match(TokenType.MODEQ);
                yield new AssignOp(currentLA(), AssignType.MODEQ);
            }
            case EXPEQ -> {
                match(TokenType.EXPEQ);
                yield new AssignOp(currentLA(), AssignType.EXPEQ);
            }
            default -> null;
        };

    }

    // if_statement ::= 'if' expression block_statement (elif_statement)* ('else' block_statement)? ;
    private IfStmt ifStatement() {
        match(TokenType.IF);

        Expression condition = expression();
        BlockStmt block = blockStatement();

        Vector<IfStmt> elifs = new Vector<>();
        BlockStmt elseBlock = null;
        while(true) { //TODO: this is problemo why?
            try {
                mark();
                elifs.add(elifStatement());
            }
            catch(CompilationMessage msg) {
                reset();
                if(nextLA(TokenType.ELSE)) {
                    match(TokenType.ELSE);
                    elseBlock = blockStatement();
                }
                break;
            }
        }

        return new IfStmt(metadata(),condition,block,elifs,elseBlock);
    }

    // elif_statement ::= 'else' 'if' expression block_statement ;
    private IfStmt elifStatement() {
        match(TokenType.ELSE);
        match(TokenType.IF);

        Expression condition = expression();
        BlockStmt block = blockStatement();
        return new IfStmt(metadata(),condition,block);
    }

    // while_statement ::= 'while' expression block_statement ;
    private WhileStmt whileStatement() {
        match(TokenType.WHILE);
        Expression condition = expression();
        BlockStmt block = blockStatement();
        return new WhileStmt(metadata(),condition,block);
    }

    // do_while_statement ::= 'do' block_statement 'while' expression ;
    private DoStmt doWhileStatement() {
        match(TokenType.DO);
        BlockStmt block = blockStatement();
        match(TokenType.WHILE);
        Expression condition = expression();
        return new DoStmt(metadata(),block,condition);
    }

    // for_statement ::= 'for' '(' range_iterator | array_iterator ')' block_statement ;
    private ForStmt forStatement() {
        match(TokenType.FOR);
        match(TokenType.LPAREN);

        mark();
        Vector<AST> forHeader = rangeIterator();
        LocalDecl controlVar = new LocalDecl(metadata(),forHeader.getFirst().asSubNode().asVar());
        Expression LHS = forHeader.get(1).asExpression();
        LoopOp loopOp = forHeader.get(2).asOperator().asLoopOp();
        Expression RHS = forHeader.getLast().asExpression();
        match(TokenType.RPAREN);

        BlockStmt block = blockStatement();
        return new ForStmt(metadata(),controlVar,LHS,RHS,loopOp,block);
    }

    // range_iterator ::= 'def' Name ':' type 'in' expression range_operator expression ;
    private Vector<AST> rangeIterator() {
        Vector<AST> forHeader = new Vector<>();

        match(TokenType.DEF);
        Name name = new Name(currentLA());
        mark();
        match(TokenType.ID);
        match(TokenType.COLON);

        Type type = type();
        forHeader.add(new Var(metadata(),name,type));

        match(TokenType.IN);
        forHeader.add(expression());
        forHeader.add(rangeOperator());
        forHeader.add(expression());
        return forHeader;
    }

    //TODO: Implement once done! --> array_iterator ::= Name ( 'in' | 'inrev' ) expression ;

    // range_operator ::= '..' | '..<' | <..' | '<..<' ;
    private LoopOp rangeOperator() {
        mark();
        if(nextLA(TokenType.INC)) {
            match(TokenType.INC);
            if(nextLA(TokenType.LT)) {
                match(TokenType.LT);
                // Exclusive Right Case
                return new LoopOp(metadata(),LoopType.EXCL_R);
            }
            // Inclusive Case
            return new LoopOp(metadata(),LoopType.INCL);
        }

        match(TokenType.LT);
        match(TokenType.INC);
        if(nextLA(TokenType.LT)) {
            match(TokenType.LT);
            // Exclusive Case
            return new LoopOp(metadata(),LoopType.EXCL);
        }
        // Exclusive Left Case
        return new LoopOp(metadata(),LoopType.EXCL_L);
    }

    // choice_statement ::= 'choice' expression '{' case_statement* 'other' block_statement '}' ;
    private ChoiceStmt choiceStatement() {
        match(TokenType.CHOICE);

        Expression choice = expression();
        match(TokenType.LBRACE);
        Vector<CaseStmt> cases = new Vector<>();
        while(nextLA(TokenType.ON))
            cases.add(caseStatement());

        match(TokenType.OTHER);
        BlockStmt defaultBlock = blockStatement();
        match(TokenType.RBRACE);
        return new ChoiceStmt(metadata(),choice,cases,defaultBlock);
    }

    // case_statement ::= 'on' label block_statement ;
    private CaseStmt caseStatement() {
        mark();
        match(TokenType.ON);

        mark();
        Label label = label();

        BlockStmt block = blockStatement();
        return new CaseStmt(metadata(),label,block);
    }

    // label ::= scalar_constant ('..' scalar_constant)? ;
    private Label label() {
        mark();
        Literal LHS = scalarConstant(), RHS = null;

        if(nextLA(TokenType.INC)) {
            match(TokenType.INC);
            mark();
            RHS = scalarConstant();
        }

        return new Label(metadata(),LHS,RHS);
    }

    // list_command_statement ::= 'append' '(' arguments? ')'
    //                          | 'remove' '(' arguments? ')'
    //                          | 'insert' '(' arguments? ')' ;
    private ListStmt listCommandStatement() {
        Commands command = switch (currentLA().getText()) {
            case "append" -> Commands.APPEND;
            case "insert" -> Commands.INSERT;
            case "remove" -> Commands.REMOVE;
            default -> null;
        };

        match(TokenType.ID);
        match(TokenType.LPAREN);

        Vector<Expression> args = new Vector<>();
        if(!nextLA(TokenType.RPAREN))
            args = arguments();
        match(TokenType.RPAREN);
        return new ListStmt(metadata(),command,args);
    }

    // input_statement ::= 'cin' ('>>' expression)+ ;
    private InStmt inputStatement() {
        match(TokenType.CIN);
        match(TokenType.SRIGHT);
        test = true;
        Vector<Expression> expr = new Vector<>(expression());
        while(nextLA(TokenType.SRIGHT)) {
            match(TokenType.SRIGHT);
            expr.add(expression());
        }
        test = false;
        return new InStmt(metadata(),expr);
    }

    // output_statement ::= 'cout' ('<<' expression)+ ;
    private OutStmt outputStatement() {
        match(TokenType.COUT);
        match(TokenType.SLEFT);
        test = true;
        Vector<Expression> expr = new Vector<>(expression());
        while(nextLA(TokenType.SLEFT)) {
            match(TokenType.SLEFT);
            expr.add(expression());
        }
        test = false;
        return new OutStmt(metadata(),expr);
    }

    // primary_expression ::= 'break' | 'continue' | 'endl' | 'parent' | '(' expression ')' | Identifier | Constant
    private Expression primaryExpression() {
        switch(currentLA().getTokenType()) {
            case BREAK:
                BreakStmt bs = new BreakStmt(currentLA());
                match(TokenType.BREAK);
                return bs;
            case CONTINUE:
                ContinueStmt cs = new ContinueStmt(currentLA());
                match(TokenType.CONTINUE);
                return cs;
            case ENDL:
                EndlStmt es = new EndlStmt(currentLA());
                match(TokenType.ENDL);
                return es;
            case PARENT:
                ParentStmt ps = new ParentStmt(currentLA());
                match(TokenType.PARENT);
                return ps;
            case LPAREN:
                match(TokenType.LPAREN);
                boolean old = test;
                test = false;
                Expression expr = expression();
                test = old;
                match(TokenType.RPAREN);
                return expr;
            case ID:
                NameExpr ne = new NameExpr(currentLA(),new Name(currentLA()));
                match(TokenType.ID);
                return ne;
            default:
                return constant();
        }
    }

    /*
        postfix_expression::= primary_expression
                           ('[' expression ']'' | (type_params)? '(' arguments? ')' | ('.' | '?.') postfix_expression)*;
    */
    private Expression postfixExpression() {
        Expression primary = primaryExpression();

        while(!nextLA(TokenType.EOF)) {
            switch(currentLA().getTokenType()) {
                case LBRACK:
                    Vector<Expression> indices = new Vector<>();
                    while(nextLA(TokenType.LBRACK)) {
                        match(TokenType.LBRACK);
                        indices.add(expression());
                        match(TokenType.RBRACK);
                    }
                    primary = new ArrayExpr(exprMetadata(),primary,indices);
                    break;
                case LT:
                case LPAREN:
                    Vector<Type> typeArgs = new Vector<>();
                    /*
                        We will try to parse the input as type arguments if the user inputted
                        a "<" sign. If there is an issue, then the user actually wrote a relational
                        expression, so we will just return the current primary expression. Otherwise,
                        we have an invocation to a template function.
                     */
                    if(nextLA(TokenType.LT)) {
                        int pos = mark();
                        try { typeArgs = typeParams(); }
                        catch(CompilationMessage msg) {
                            reset(pos);
                            return primary;
                        }
                    }

                    match(TokenType.LPAREN);
                    Vector<Expression> args = arguments();
                    match(TokenType.RPAREN);
                    primary = new Invocation(exprMetadata(),primary,typeArgs,args);
                    break;
                case PERIOD:
                case ELVIS:
                    boolean nullCheck = false;
                    if(nextLA(TokenType.PERIOD)) {
                        match(TokenType.PERIOD);
                        nullCheck = true;
                    }
                    else { match(TokenType.ELVIS); }

                    Expression access = postfixExpression();
                    primary = new FieldExpr(exprMetadata(),primary,access,nullCheck);
                    break;
                default:
                    return primary;
            }
        }

        return primary;
    }

    // arguments ::= expression (',' expression)* ;
    private Vector<Expression> arguments() {
        if(nextLA(TokenType.RPAREN))
            return new Vector<>();

        Vector<Expression> args = new Vector<>(expression());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            args.add(expression());
        }

        return args;
    }

    // type_params ::= '<' type ( ',' type )* '>' ;
    private Vector<Type> typeParams() {
        match(TokenType.LT);
        Vector<Type> types = new Vector<>(type());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            types.add(type());
        }

        match(TokenType.GT);
        return types;
    }

    //factor_expression : postfix_expression | 'length' '(' arguments? ')' ;
    private Expression factorExpression() {
        if(currentLA().equals("length")) {
            Expression name = new NameExpr("length");
            match(TokenType.ID);
            match(TokenType.LPAREN);
            Vector<Expression> args = new Vector<>();

            try{ args = arguments(); }
            catch(CompilationMessage msg) { /* ######### DO NOTHING ######### */ }

            match(TokenType.RPAREN);
            return new Invocation(exprMetadata(),name,new Vector<>(),args);
        }

        return postfixExpression();
    }

    // unary_expression ::= unary_operator cast_expression | factor_expression ;
    private Expression unaryExpression() {
        if(nextLA(TokenType.BNOT) || nextLA(TokenType.NOT)) {
            UnaryOp uOp;
            switch(currentLA().getTokenType()) {
                case BNOT:
                    uOp = new UnaryOp(currentLA(),UnaryType.BNOT);
                    match(TokenType.BNOT);
                    break;
                default:
                    uOp = new UnaryOp(currentLA(),UnaryType.NOT);
                    match(TokenType.NOT);
            }

            Expression expr = castExpression();
            return new UnaryExpr(exprMetadata(),expr,uOp);
        }

        return factorExpression();
    }

    // cast_expression ::= scalar_type '(' cast_expression ')' | unary_expression ;
    private Expression castExpression() {
        if(nextLA(TokenType.REAL) || nextLA(TokenType.STRING) || nextLA(TokenType.INT)
                                  || nextLA(TokenType.CHAR) || nextLA(TokenType.BOOL)) {
            mark();
            Type castType = scalarType();
            match(TokenType.LPAREN);

            mark();
            Expression castExpr = castExpression();

            match(TokenType.RPAREN);
            return new CastExpr(metadata(),castType,castExpr);
        }

        return unaryExpression();
    }

    // power_expression ::= cast_expression ('**' cast_expression)* ;
    private Expression powerExpression() {
        /*
            Remember, exponentiation is right associative! We need to make sure
            the tree is built from right to left to ensure the correct binary
            expression is generated. This is why we are using stacks here. :)
        */
        Vector<Expression> stack = new Vector<>();
        Vector<BinaryOp> binOpStack = new Vector<>();
        stack.add(castExpression());

        while(nextLA(TokenType.EXP)) {
            binOpStack.add(new BinaryOp(currentLA(),BinaryType.EXP));
            match(TokenType.EXP);
            stack.add(castExpression());
        }

        while(!binOpStack.isEmpty()) {
            Expression RHS = stack.pop();
            Expression LHS = stack.pop(); // Java is weird...
            stack.add(new BinaryExpr(exprMetadata(),LHS,RHS,binOpStack.pop()));
        }

        return stack.pop();
    }

    // multiplication_expression ::= power_expression ( ('*' | '/' | '%') power_expression)* ;
    private Expression multiplicationExpression() {
        Expression LHS = powerExpression();

        while(nextLA(TokenType.MULT) || nextLA(TokenType.DIV) || nextLA(TokenType.MOD)) {
            BinaryOp binOp;
            switch(currentLA().getTokenType()) {
                case MULT:
                    binOp = new BinaryOp(currentLA(),BinaryType.MULT);
                    match(TokenType.MULT);
                    break;
                case DIV:
                    binOp = new BinaryOp(currentLA(),BinaryType.DIV);
                    match(TokenType.DIV);
                    break;
                default:
                    binOp = new BinaryOp(currentLA(),BinaryType.MOD);
                    match(TokenType.MOD);
            }

            Expression RHS = powerExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // additive_expression ::= multiplication_expression ( ('+' | '-') multiplication_expression)* ;
    private Expression additiveExpression() {
        Expression LHS = multiplicationExpression();
        while(nextLA(TokenType.PLUS) || nextLA(TokenType.MINUS)) {
            BinaryOp binOp;
            switch(currentLA().getTokenType()) {
                case PLUS:
                    binOp = new BinaryOp(currentLA(),BinaryType.PLUS);
                    match(TokenType.PLUS);
                    break;
                default:
                    binOp = new BinaryOp(currentLA(),BinaryType.MINUS);
                    match(TokenType.MINUS);
            }

            Expression RHS = multiplicationExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // shift_expression ::= additive_expression ( ('<<' | '>>') additive_expression)* ;
    private Expression shiftExpression() {
        Expression LHS = additiveExpression();

        while((nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) && !test) {
            BinaryOp binOp;
            switch(currentLA().getTokenType()) {
                case SLEFT:
                    binOp = new BinaryOp(currentLA(),BinaryType.SLEFT);
                    match(TokenType.SLEFT);
                    break;
                default:
                    binOp = new BinaryOp(currentLA(),BinaryType.SRIGHT);
                    match(TokenType.SRIGHT);
            }

            Expression RHS = additiveExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // relational_expression ::= shift_expression ( ('<' | '>' | '<=' | '>=') shift_expression)* ;
    private Expression relationalExpression() {
        Expression LHS = shiftExpression();

        while(nextLA(TokenType.LT) || nextLA(TokenType.GT) || nextLA(TokenType.LTEQ) || nextLA(TokenType.GTEQ)) {
            BinaryOp binOp;
            switch(currentLA().getTokenType()) {
                case LT:
                    binOp = new BinaryOp(currentLA(),BinaryType.LT);
                    match(TokenType.LT);
                    break;
                case GT:
                    binOp = new BinaryOp(currentLA(),BinaryType.GT);
                    match(TokenType.GT);
                    break;
                case LTEQ:
                    binOp = new BinaryOp(currentLA(),BinaryType.LTEQ);
                    match(TokenType.LTEQ);
                    break;
                default:
                    binOp = new BinaryOp(currentLA(),BinaryType.GTEQ);
                    match(TokenType.GTEQ);
            }

            Expression RHS = shiftExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // instanceof_expression ::= relational_expression (('instanceof' | '!instanceof' | 'as?') relational_expression)*;
    private Expression instanceofExpression() {
        Expression LHS = relationalExpression();

        while(nextLA(TokenType.INSTANCEOF) || nextLA(TokenType.NINSTANCEOF) || nextLA(TokenType.AS)) {
            BinaryOp binOp;
            switch(currentLA().getTokenType()) {
                case INSTANCEOF:
                    binOp = new BinaryOp(currentLA(),BinaryType.INSTOF);
                    match(TokenType.INSTANCEOF);
                    break;
                case NINSTANCEOF:
                    binOp = new BinaryOp(currentLA(),BinaryType.NINSTOF);
                    match(TokenType.NINSTANCEOF);
                    break;
                default:
                    binOp = new BinaryOp(currentLA(),BinaryType.AS);
                    match(TokenType.AS);
            }

            Expression RHS = relationalExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // equality_expression ::= instanceof_expression ( ('==' | '!=') instanceof_expression)* ;
    private Expression equalityExpression() {
        Expression LHS = instanceofExpression();

        while(nextLA(TokenType.EQEQ) || nextLA(TokenType.NEQ)) {
            BinaryOp binaryOp;
            switch(currentLA().getTokenType()) {
                case EQEQ:
                    binaryOp = new BinaryOp(currentLA(),BinaryType.EQEQ);
                    match(TokenType.EQEQ);
                    break;
                default:
                    binaryOp = new BinaryOp(currentLA(),BinaryType.NEQ);
                    match(TokenType.NEQ);
            }

            Expression RHS = instanceofExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binaryOp);
        }

        return LHS;
    }

    // and_expression ::= equality_expression ('&' equality_expression)* ;
    private Expression andExpression() {
        Expression LHS = equalityExpression();

        while(nextLA(TokenType.BAND)) {
            BinaryOp binOp = new BinaryOp(currentLA(),BinaryType.BAND);
            match(TokenType.BAND);

            Expression RHS = equalityExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // exclusive_or_expression ::= and_expression ('^' and_expression)* ;
    private Expression exclusiveOrExpression() {
        Expression LHS = andExpression();

        while(nextLA(TokenType.XOR)) {
            BinaryOp binOp = new BinaryOp(currentLA(),BinaryType.XOR);
            match(TokenType.XOR);

            Expression RHS = andExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // inclusive_or_expression ::= exclusive_or_expression ('|' exclusive_or_expression)* ;
    private Expression inclusiveOrExpression() {
        Expression LHS = exclusiveOrExpression();

        while(nextLA(TokenType.BOR)) {
            BinaryOp binOp = new BinaryOp(currentLA(),BinaryType.BOR);
            match(TokenType.BOR);

            Expression RHS = exclusiveOrExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // logical_and_expression ::= inclusive_or_expression ('and' inclusive_or_expression)* ;
    private Expression logicalAndExpression() {
        Expression LHS = inclusiveOrExpression();

        while(nextLA(TokenType.AND)) {
            BinaryOp binOp = new BinaryOp(currentLA(),BinaryType.AND);
            match(TokenType.AND);

            Expression RHS = inclusiveOrExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // logical_or_expression ::= logical_and_expression ('or' logical_and_expression)* ;
    private Expression logicalOrExpression() {
        Expression LHS = logicalAndExpression();

        while(nextLA(TokenType.OR)) {
            BinaryOp binOp = new BinaryOp(currentLA(),BinaryType.OR);
            match(TokenType.OR);

            Expression RHS = logicalAndExpression();
            LHS = new BinaryExpr(exprMetadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // expression ::= logical_or_expression
    private Expression expression() {
        mark();
        Expression expr = logicalOrExpression();
        remove();
        return expr;
    }

    // constant ::= object_constant | array_constant | list_constant | scalar_constant ;
    private Expression constant() {
        mark();
        return switch(currentLA().getTokenType()) {
            case NEW -> objectConstant();
            case ARRAY -> arrayConstant();
            case LIST -> listConstant();
            default -> scalarConstant();
        };
    }

    // object_constant ::= 'new' Name (type_params)? '(' object_field (',' object_field)* ')' ;
    private NewExpr objectConstant() {
        match(TokenType.NEW);
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> typeArgs = new Vector<>();
        if(nextLA(TokenType.LT))
            typeArgs = typeParams();

        match(TokenType.LPAREN);
        Vector<Var> fields = new Vector<>();
        if(!nextLA(TokenType.RPAREN))
            fields = new Vector<>(objectField());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            fields.add(objectField());
        }

        match(TokenType.RPAREN);
        return new NewExpr(metadata(),new ClassType(name,typeArgs),fields);
    }

    // object_field ::= Name '=' expression ;
    private Var objectField() {
        mark();
        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.EQ);
        Expression init = expression();
        return new Var(metadata(),name,init);
    }

    // array_constant ::= 'Array' ( '[' expression ']' )* '(' arguments ')' ;
    private ArrayLiteral arrayConstant() {
        match(TokenType.ARRAY);

        Vector<Expression> dims = new Vector<>();
        while(nextLA(TokenType.LBRACK)) {
            match(TokenType.LBRACK);
            dims.add(expression());
            match(TokenType.RBRACK);
        }

        match(TokenType.LPAREN);
        Vector<Expression> args = arguments();
        match(TokenType.RPAREN);
        return new ArrayLiteral(metadata(),dims,args);
    }

    // list_constant ::= 'List' '(' (expression (',' expression)*)? ')' ;
    private ListLiteral listConstant() {
        match(TokenType.LIST);
        match(TokenType.LPAREN);

        Vector<Expression> inits = new Vector<>();
        if(!nextLA(TokenType.RPAREN)) {
            inits.add(expression());

            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                inits.add(expression());
            }
        }

        match(TokenType.RPAREN);
        return new ListLiteral(metadata(),inits);
    }

    // scalar_constant ::= discrete_constant | String_literal | Real_literal
    private Literal scalarConstant() {
        //TODO: This is a test !!!
        if(nextLA(TokenType.MINUS)) {
            match(TokenType.MINUS);
            if(nextLA(TokenType.REAL_LIT)) {
                match(TokenType.REAL_LIT);
                return new Literal(metadata(),ConstantType.REAL);
            }
            match(TokenType.INT_LIT);
            return new Literal(metadata(),ConstantType.INT);
        }


        if(nextLA(TokenType.STR_LIT)) {
            match(TokenType.STR_LIT);
            return new Literal(metadata(),ConstantType.STR);
        }
        else if(nextLA(TokenType.REAL_LIT)) {
            match(TokenType.REAL_LIT);
            return new Literal(metadata(),ConstantType.REAL);
        }
        else
            return discreteConstant();
    }

    // discrete_constant ::= Int_literal | Bool_literal | Char_literal
    private Literal discreteConstant() {
        if(nextLA(TokenType.INT_LIT)) {
            match(TokenType.INT_LIT);
            return new Literal(metadata(),ConstantType.INT);
        }
        else if(nextLA(TokenType.BOOL_LIT)) {
            match(TokenType.BOOL_LIT);
            return new Literal(metadata(),ConstantType.BOOL);
        }
        else {
            match(TokenType.CHAR_LIT);
            return new Literal(metadata(),ConstantType.CHAR);
        }
    }
}
