package cminor.parser;

import cminor.ast.AST;
import cminor.ast.expressions.*;
import cminor.ast.expressions.Literal.ConstantType;
import cminor.ast.types.*;
import cminor.ast.types.ScalarType.Scalars;
import cminor.ast.misc.*;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.ast.classbody.*;
import cminor.ast.operators.*;
import cminor.ast.operators.BinaryOp.BinaryType;
import cminor.ast.operators.UnaryOp.UnaryType;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.Name;
import cminor.ast.misc.Var;
import cminor.ast.topleveldecls.EnumDecl;
import cminor.lexer.Lexer;
import cminor.messages.CompilationMessage;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.syntax.SyntaxError;
import cminor.token.Token;
import cminor.token.TokenType;
import cminor.utilities.Vector;

import java.util.function.BinaryOperator;

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

    private final MessageHandler handler;

    /**
     * The starting position denoting the token we began the parsing for.
     */
    private int pos;

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
            pos = 0; // Reset the starting position back to 0
            lookaheads.clear(); // Clear out the token array to save on memory... Is this needed?
        }

        synchronize(1); // Add the token to the vector if it's not there!
    }

    /**
     * Retrieves the current lookahead token the parser is at.
     * @param index The index we want to access the token from (based on the current start position)
     * @return {@link Token} representing the current lookahead token.
     */
    private Token currentLA(int index) {
        synchronize(index); // Make sure the token is present in the vector
        return lookaheads.get(pos+index-1); // Retrieve the lookahead based on starting position
    }

    /**
     * Retrieves the current lookahead token.
     * @return {@link Token} representing the current lookahead token.
     */
    private Token currentLA() { return currentLA(1); }

    /**
     * Returns the next available lookahead based on a passed index.
     * @param index The index (x) we wish to check for a particular token from the start position.
     * @return {@link TokenType} that appears some x amount of spaces from the start position.
     */
    private TokenType nextLA(int index) { return currentLA(index).getTokenType(); }

    private boolean nextLA(TokenType expectedToken) { return currentLA(1).getTokenType() == expectedToken; }

    /**
     * Checks if the current lookahead matches the expected token we wish to see.
     * @param expectedToken The {@link TokenType} that is expected in the input.
     */
    private void match(TokenType expectedToken) {
        if(nextLA(1) == expectedToken)
            consume();
        else
            handler.createErrorBuilder(SyntaxError.class)
                   .addErrorNumber(MessageNumber.SYNTAX_ERROR_101)
                   .addErrorArgs(expectedToken,currentLA().getTokenType())
                   .asSyntaxErrorBuilder()
                   .addLocation(currentLA(),input)
                   .generateError();
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
        for(int i = 0; i < tokenCount; i++) {
            lookaheads.add(input.nextToken());
            System.out.println(lookaheads.getLast());

            // Add 'tokenCount' amount of tokens to vector
        }
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
     * Resets the {@link #pos} if an error occurs while parsing.
     * <p>
     *     This method is only triggered when an error occurs during parsing. If we are not
     *     able to parse a given grammar rule, an exception is thrown, and we have to go back
     *     to the previous starting position in order to parse a separate grammar rule. If no
     *     more rules are available, then we have to generate a parsing error.
     * </p>
     */
    private void reset() {
        pos = positions.getLast();
        positions.removeLast();
    }

    private void reset(int pos) {
        this.pos = pos;
        while(!positions.isEmpty() && positions.getLast() > pos)
            positions.removeLast();
    }

    private void release() { positions.removeLast(); }

    private Token metadata() {
        Token start = lookaheads.get(positions.getLast()); // Gets the first token for the grammar rule
        input.setText(start,lookaheads.get(pos-1));        // Saves metadata representing the grammar rule!
        return start;
    }

    /*
            while(!nextLA(TokenType.EOF)) {
            if(nextLA(TokenType.DEF)) {
                mark();
                try {
                    nodes.add(enumType());
                    continue;
                }
                catch(CompilationMessage msg) { reset(); }

                mark();
                try {
                    nodes.merge(globalVariable());
                }
                catch(CompilationMessage msg) { msg.printMessage(); }
            }

            mark();
            try {
                nodes.add(classType());
                continue;
            } catch(CompilationMessage msg) { msg.printMessage(); }
            reset();
        }
     */
    public Vector<? extends AST> parse() {
        Vector<AST> nodes = new Vector<>();

//        if(nextLA(TokenType.DEF)) {
//            mark();
//            try { nodes.add(enumType()); return nodes; }
//            catch(CompilationMessage msg) { /* ############ DO NOTHING ############ */ }
//            reset();
//
//            mark();
//            try { nodes.merge(globalVariable()); }
//            catch(CompilationMessage msg) { msg.printMessage(); }
//            reset();
//        }

        mark();
        try { nodes.add(expression()); }
        catch(CompilationMessage msg) { msg.printMessage(); reset();}

//        mark();
//        try { nodes.add(classType()); return nodes; }
//        catch(CompilationMessage msg) { /* ############ DO NOTHING ############ */ }
//        reset();

//        if(!nextLA(TokenType.EOF)){
//            handler.createErrorBuilder(SyntaxError.class)
//                   .addErrorNumber(MessageNumber.SYNTAX_ERROR_100)
//                   .asSyntaxErrorBuilder()
//                   .addLocation(currentLA(),input)
//                   .generateError();
//        }

        return nodes;
    }

//    // 1. compilation : import_stmt* enum_type* global_variable* class_type* function* main ;
//    public CompilationUnit compilation() {
//        // import_stmt : '#include' String_literal
//
//        if(nextLA(TokenType.DEF)) {
//            mark();
//        }
//        // enum_type : 'def' Name 'type' '=' '{' enum_field (',' enum_field)* '}' ;
//        // global_variable : 'def' ('const' | 'global') variable_decl ;
//        // class_type : ('abstr' | 'final')? 'class' name typefier_params? super_class? class_body ;
//        // function : 'def' ('pure' | 'recurs')? function_header '=>' return_type block_statement ;
//    }

    // 2. import_stmt ::= '#include' String_literal ;

    // 3. enum_type ::= 'def' Name 'type' '=' '{' enum_field (',' enum_field)* '}' ;
    private EnumDecl enumType() {
        match(TokenType.DEF);
        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.TYPE);
        match(TokenType.EQ);
        match(TokenType.LBRACE);

        mark();
        Vector<Var> constants = new Vector<>(enumField());
        release();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            mark();
            constants.add(enumField());
            release();
        }

        match(TokenType.RBRACE);
        return new EnumDecl(metadata(),name,constants);
    }

    // 4. enum_field ::= Name ('=' constant)? ;
    private Var enumField() {
        Name name = new Name(currentLA());
        match(TokenType.ID);

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            Expression expr = constant();
            return new Var(metadata(),name,expr);
        }

        return new Var(metadata(),name);
    }

    // 5. global_variable ::= 'def' ('const' | 'global') variable_decl ;
    private Vector<AST> globalVariable() {
        match(TokenType.DEF);

        boolean constant = false;
        if(nextLA(TokenType.CONST)) {
            match(TokenType.CONST);
            constant = true;
        }
        else
            match(TokenType.GLOBAL);

        Vector<Var> vars = variableDecl();
        Vector<AST> globals = new Vector<>();

        for(Var var : vars)
            globals.add(new GlobalDecl(metadata(),var,constant));

        return globals;
    }

    // 6. variable_decl ::= variable_decl_list ;
    private Vector<Var> variableDecl() { return variableDeclList(); }

    // 7. variable_decl_list ::= variable_decl_init (',' variable_decl_init)* ;
    private Vector<Var> variableDeclList() {
        mark();
        Vector<Var> vars = new Vector<>(variableDeclInit());
        release();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            mark();
            vars.add(variableDeclInit());
            release();
        }

        return vars;
    }

    // 8. variable_decl_init ::= Name ':' type ('=' (expression | 'uninit'))? ;
    private Var variableDeclInit() {
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
        }

        return new Var(metadata(),name,init,type);
    }

    // 9. type ::= scalar_type
    //           | class_name
    //           | 'List' '[' type ']'
    //           | 'Array' '[' type ']'
    //           ;
    private Type type() {
        Type type;
        mark();

        if(nextLA(TokenType.ID))
            type = className();
        else if(nextLA(TokenType.LIST)) {
            match(TokenType.LIST);
            match(TokenType.LBRACK);
            Type baseType = type();
            match(TokenType.RBRACK);

            if(baseType.isList()) {
                baseType.asList().dims += 1;
                baseType.copyMetaData(metadata());
                type = baseType;
            }
            else
                type = new ListType(metadata(),baseType,1);
        }
        else if(nextLA(TokenType.ARRAY)) {
            match(TokenType.ARRAY);
            match(TokenType.LBRACK);
            Type baseType = type();
            match(TokenType.RBRACK);

            if(baseType.isArray()) {
                baseType.asArray().dims += 1;
                baseType.copyMetaData(metadata());
                type = baseType;
            }
            else
                type = new ArrayType(metadata(),baseType,1);
        }
        else
            type = scalarType();

        release();
        return type;
    }

    // 10. scalar_type ::= discrete_type
    //                   | 'String'
    //                   | 'Real'
    //                   ;
    private ScalarType scalarType() {
        if(nextLA(TokenType.STRING)) {
            match(TokenType.STRING);
            return new ScalarType(metadata(),Scalars.STR);
        }
        else if(nextLA(TokenType.REAL)) {
            match(TokenType.REAL);
            return new ScalarType(metadata(),Scalars.REAL);
        }
        else { return discreteType(); }
    }

    // 11. discrete_type ::= 'Bool'
    //                     | 'Int'
    //                     | 'Char'
    //                     ;
    private DiscreteType discreteType() {
        if(nextLA(TokenType.BOOL)) {
            match(TokenType.BOOL);
            return new DiscreteType(metadata(),Scalars.BOOL);
        }
        else if(nextLA(TokenType.INT)) {
            match(TokenType.INT);
            return new DiscreteType(metadata(),Scalars.INT);
        }
        else {
            match(TokenType.CHAR);
            return new DiscreteType(metadata(),Scalars.CHAR);
        }
    }

    // 12. class_name ::= Name ('<' type (',' type)* '>')? ;
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

    // 13. class_type ::= ( 'abstr' | 'final' )? 'class' ID type_params? super_class? class_body
    private ClassDecl classType() {
        Modifier mod = new Modifier();
        if(nextLA(TokenType.ABSTR)) {
            mod.setAbstract();
            match(TokenType.ABSTR);
        }
        else if(nextLA(TokenType.FINAL)) {
            mod.setFinal();
            match(TokenType.FINAL);
        }

        match(TokenType.CLASS);
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> typeParams = new Vector<>();
        if(nextLA(TokenType.LT)) { typeParams = typeifierParams(); }

        ClassType superClass = null;
        if(nextLA(TokenType.INHERITS)) { superClass = superClass(); }

        ClassBody body = classBody();
        return new ClassDecl(metadata(),mod,name,typeParams,superClass,body);
    }

    // 14. type_params ::= '<' typeifier ( ',' typeifier )* '>'
    private Vector<TypeParam> typeifierParams() {
        match(TokenType.LT);

        mark();
        Vector<TypeParam> typeParams = new Vector<>(typeifier());
        release();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);

            mark();
            typeParams.add(typeifier());
            release();
        }

        match(TokenType.GT);
        return typeParams;
    }

    // 15. typeifier ::= ( 'discr' | 'scalar' | 'class' )? ID
    private TypeParam typeifier() {
        TypeParam.TypeAnnotation pt = null;
        if(nextLA(TokenType.DISCR)) {
            pt = TypeParam.TypeAnnotation.DISCR;
            match(TokenType.DISCR);
        }
        else if(nextLA(TokenType.SCALAR)) {
            pt = TypeParam.TypeAnnotation.SCALAR;
            match(TokenType.SCALAR);
        }
        else if(nextLA(TokenType.CLASS)) {
            pt = TypeParam.TypeAnnotation.CLASS;
            match(TokenType.CLASS);
        }

        Name name = new Name(currentLA());
        match(TokenType.ID);

        return new TypeParam(metadata(),pt,name);
    }

    // 16. super_class ::= 'inherits' ID type_params?
    private ClassType superClass() {
        match(TokenType.INHERITS);

        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> vectorOfTypes = new Vector<>();
        if(nextLA(TokenType.LT))
            vectorOfTypes = typeParams();

        return new ClassType(metadata(),name,vectorOfTypes);
    }

    // 17. class_body ::= '{' data_decl* method_decl* '}'
    private ClassBody classBody() {
        match(TokenType.LBRACE);

        Vector<FieldDecl> dataDecls = new Vector<>();
        while(true) {
            try {
                mark();
                dataDecls.merge(dataDecl());
            }
            catch(CompilationMessage msg) {
                reset();
                break;
            }
        }

        Vector<MethodDecl> methodDecls = new Vector<>();
        while(true) {
            try {
                mark();
                methodDecls.add(methodDecl());
            } catch(CompilationMessage msg) {
                release();
                break;
            }
        }

        match(TokenType.RBRACE);
        return new ClassBody(metadata(),dataDecls,methodDecls);
    }

    // 18. data_decl ::= ('property' | 'protected' | 'public') variable_decl ;
    private Vector<FieldDecl> dataDecl() {
        Modifier mod = new Modifier();
        if(nextLA(TokenType.PROPERTY)) {
            mod.setProperty();
            match(TokenType.PROPERTY);
        }
        else if(nextLA(TokenType.PROTECTED)) {
            mod.setProtected();
            match(TokenType.PROTECTED);
        }
        else {
            mod.setPublic();
            match(TokenType.PUBLIC);
        }

        Vector<Var> vars = variableDecl();
        Vector<FieldDecl> fields = new Vector<>();

        for(Var v : vars)
            fields.add(new FieldDecl(metadata(),mod,v));

        return fields;
    }

    // 19. method_decl ::= method_class | operator_class
    private MethodDecl methodDecl() {
        MethodDecl md = null;
        mark();
        try {
            md = methodClass();
            release();
            return md;
        }
        catch(CompilationMessage _) { /* We do not have a method, so see if we have an operator! */ }
        reset();

        mark();
        try {
            md = operatorClass();
            release();
        }
        catch(CompilationMessage msg) { msg.printMessage(); }

        return md;
    }

    // 20. method_class ::= method_modifier attribute 'override'? 'method' method_header '=>' return_type block_statement
    private MethodDecl methodClass() {
        Modifier mod = methodModifier();

        while(nextLA(TokenType.FINAL) || nextLA(TokenType.PURE) || nextLA(TokenType.RECURS)) {
            if(nextLA(TokenType.FINAL)) {
                mod.setFinal();
                match(TokenType.FINAL);
            }
            else if(nextLA(TokenType.PURE)) {
                mod.setPure();
                match(TokenType.PURE);
            }
            else {
                mod.setRecursive();
                match(TokenType.RECURS);
            }
        }

        boolean override = false;
        if(nextLA(TokenType.OVERRIDE)) {
            match(TokenType.OVERRIDE);
            override = true;
        }

        match(TokenType.METHOD);

        Vector<Object> header = methodHeader();
        Name mName = (Name) header.get(0);
        Vector<ParamDecl> pd = (Vector<ParamDecl>) header.get(1);
        if(pd == null) { pd = new Vector<>(); }

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt bs = blockStatement();

        return new MethodDecl(metadata(),mod,mName,null,pd,rt,bs,override);
    }

    // 21. method_modifier : 'protected' | 'public' ;
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

    // 22. attribute ::= 'final' | 'pure' | 'recurs'
    private Modifier attribute() {
        Modifier mod = new Modifier();
        if(nextLA(TokenType.FINAL)) {
            mod.setFinal();
            match(TokenType.FINAL);
        }
        else if(nextLA(TokenType.PURE)) {
            mod.setPure();
            match(TokenType.PURE);
        }
        else {
            mod.setRecursive();
            match(TokenType.RECURS);
        }

        return mod;
    }

    // 23. method-header ::= ID '(' formal-params? ')'
    private Vector<Object> methodHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);

        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF)) {
            pd = formalParams();
        }
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(n);
        header.add(pd);

        return header;
    }

    // 24. formal_params : param_modifier Name ':' type ( ',' param_modifier Name ':' type)*
    private Vector<ParamDecl> formalParams() {
        mark();
        Modifier mod = paramModifier();

        Name name = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type ty = type();

        Vector<ParamDecl> pd = new Vector<>();
        pd.add(new ParamDecl(metadata(),mod,name,ty));
        release();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            mark();
            mod = paramModifier();

            name = new Name(currentLA());
            match(TokenType.ID);
            match(TokenType.COLON);
            ty = type();

            pd.add(new ParamDecl(metadata(),mod,name,ty));
            release();
        }

        return pd;
    }

    // 25. param_modifier : 'in' | 'out' | 'inout' | 'ref'
    private Modifier paramModifier() {
        Modifier mod = new Modifier();

        if(nextLA(TokenType.IN)) {
            mod.setInMode();
            match(TokenType.IN);
        }
        else if(nextLA(TokenType.OUT)) {
            mod.setOutMode();
            match(TokenType.OUT);
        }
        else if(nextLA(TokenType.INOUT)) {
            mod.setInOutMode();
            match(TokenType.INOUT);
        }
        else {
            mod.setRefMode();
            match(TokenType.REF);
        }

        return mod;
    }

    // 26. return-type ::= Void | type
    private Type returnType() {
        if(nextLA(TokenType.VOID)) {
            match(TokenType.VOID);
            return new VoidType(metadata());
        }
        else
            return type();
    }

    // 27. operator_class : operator_modifier 'final'? 'operator' operator_header '=>' return_type block_statement
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

    // 28. operator_header ::= operator_symbol '(' formal-params? ')'
    private Vector<Object> operatorHeader() {
        Operator op = operatorSymbol();

        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF)) {
            pd = formalParams();
        }
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(op);
        header.add(pd);

        return header;
    }

    // 29. operator_symbol ::= binary_operator | unary_operator
    private Operator operatorSymbol() {
        if(nextLA(TokenType.BNOT) || nextLA(TokenType.NOT)) { return unaryOperator(); }
        else { return binaryOperator(); }
    }

    // 30. binary_operator ::= <= | < | > | >= | == | <> | <=> | + | - | * | / | % | **
    private BinaryOp binaryOperator() {
        return switch (currentLA().getTokenType()) {
            case TokenType.LTEQ -> {
                match(TokenType.LTEQ);
                yield new BinaryOp(metadata(), BinaryType.LTEQ);
            }
            case TokenType.LT -> {
                match(TokenType.LT);
                yield new BinaryOp(metadata(), BinaryType.LT);
            }
            case TokenType.GT -> {
                match(TokenType.GT);
                yield new BinaryOp(metadata(), BinaryType.GT);
            }
            case TokenType.GTEQ -> {
                match(TokenType.GTEQ);
                yield new BinaryOp(metadata(), BinaryType.GTEQ);
            }
            case TokenType.EQEQ -> {
                match(TokenType.EQEQ);
                yield new BinaryOp(metadata(), BinaryType.EQEQ);
            }
            case TokenType.LTGT -> {
                match(TokenType.LTGT);
                yield new BinaryOp(metadata(), BinaryType.LTGT);
            }
            case TokenType.UFO -> {
                match(TokenType.UFO);
                yield new BinaryOp(metadata(), BinaryType.UFO);
            }
            case TokenType.PLUS -> {
                match(TokenType.PLUS);
                yield new BinaryOp(metadata(), BinaryType.PLUS);
            }
            case TokenType.MINUS -> {
                match(TokenType.MINUS);
                yield new BinaryOp(metadata(), BinaryType.MINUS);
            }
            case TokenType.MULT -> {
                match(TokenType.MULT);
                yield new BinaryOp(metadata(), BinaryType.MULT);
            }
            case TokenType.DIV -> {
                match(TokenType.DIV);
                yield new BinaryOp(metadata(), BinaryType.DIV);
            }
            case TokenType.MOD -> {
                match(TokenType.MOD);
                yield new BinaryOp(metadata(), BinaryType.MOD);
            }
            default -> {
                match(TokenType.EXP);
                yield new BinaryOp(metadata(), BinaryType.EXP);
            }
        };
    }

    // 31. unary-operator ::= ~ | not
    private UnaryOp unaryOperator() {
        if(nextLA(TokenType.BNOT)) {
            match(TokenType.BNOT);
            return new UnaryOp(metadata(), UnaryOp.UnaryType.BNOT);
        }
        else {
            match(TokenType.NOT);
            return new UnaryOp(metadata(), UnaryOp.UnaryType.NOT);
        }
    }

    // 32. function ::= 'def' ( 'pure' | 'recurs' )? function_header '=>' return_type block_statement
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

    // 33. function_header ::= ID function_type_params? '(' formal_params? ')'
    private Vector<Object> functionHeader() {
        Name name = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> typeParams = new Vector<>();
        if(nextLA(TokenType.LT)) { typeParams = typeifierParams(); }

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF)) {
            params = formalParams();
        }
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(name);
        header.add(typeParams);
        header.add(params);

        return header;
    }

    private BlockStmt blockStatement() { return null; }

    private InStmt inputStatement() { return null; }

    // output_statement ::= 'cout' ('<<' expression)+ ;
    private OutStmt outputStatement() {
        match(TokenType.COUT);
        match(TokenType.SLEFT);
        Vector<Expression> expr = new Vector<>(expression());

        return new OutStmt(metadata(),expr);
    }

    /*
        primary_expression ::= output_statement
                             | input_statement
                             | 'break'
                             | 'continue'
                             | 'endl'
                             | 'parent'
                             | '(' expression ')'
                             | Identifier
                             | Constant
     */
    private Expression primaryExpression() {
        switch(currentLA().getTokenType()) {
            case COUT:
                return outputStatement();
            case CIN:
                return inputStatement();
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
                Expression expr = expression();
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
        postfix_expression::= primary_expression ( '[' expression ']''
                                                 | (type_params)? '(' arguments? ')'
                                                 | ('.' | '?.') postfix_expression
                                                 )* ;
     */
    private Expression postfixExpression() {
        mark();
        Expression primary = primaryExpression();
        release();

        while(!nextLA(TokenType.EOF)) {
            switch(currentLA().getTokenType()) {
                case LBRACK:
                    Vector<Expression> indices = new Vector<>();
                    while(nextLA(TokenType.LBRACK)) {
                        match(TokenType.LBRACK);
                        indices.add(expression());
                        match(TokenType.RBRACK);
                    }
                    primary = new ArrayExpr(metadata(),primary,indices);
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
                        try {
                            typeArgs = typeParams();
                            release();
                        }
                        catch(CompilationMessage msg) {
                            reset(pos);
                            return primary;
                        }
                    }

                    match(TokenType.LPAREN);
                    Vector<Expression> args = arguments();
                    match(TokenType.RPAREN);
                    primary = new Invocation(metadata(),primary,typeArgs,args);
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
                    primary = new FieldExpr(metadata(),primary,access,nullCheck);
                    break;
                default:
                    return primary;
            }
        }

        return primary;
    }

    // arguments ::= expression (',' expression)* ;
    private Vector<Expression> arguments() {
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
            return new Invocation(metadata(),name,new Vector<>(),args);
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
            return new UnaryExpr(metadata(),expr,uOp);
        }

        return factorExpression();
    }

    // cast_expression ::= scalar_type '(' cast_expression ')' | unary_expression ;
    private Expression castExpression() {
        if(nextLA(TokenType.REAL) || nextLA(TokenType.STRING)
                || nextLA(TokenType.INT) || nextLA(TokenType.CHAR) || nextLA(TokenType.BOOL)) {
            Type castType = scalarType();
            match(TokenType.LPAREN);
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
            stack.add(new BinaryExpr(metadata(),LHS,RHS,binOpStack.pop()));
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // shift_expression ::= additive_expression ( ('<<' | '>>') additive_expression)* ;
    private Expression shiftExpression() {
        Expression LHS = additiveExpression();

        while(nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) {
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binaryOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
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
            LHS = new BinaryExpr(metadata(),LHS,RHS,binOp);
        }

        return LHS;
    }

    // expression ::= logical_or_expression
    private Expression expression() { return logicalOrExpression(); }

    // constant ::= object_constant | array_constant | list_constant | scalar_constant ;
    private Expression constant() {
        Expression constant;
        mark();

        constant = switch(currentLA().getTokenType()) {
            case NEW -> objectConstant();
            case ARRAY -> arrayConstant();
            case LIST -> listConstant();
            default -> scalarConstant();
        };

        release();
        return constant;
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
        mark();
        Vector<Var> fields = new Vector<>(objectField());
        release();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            mark();
            fields.add(objectField());
            release();
        }

        match(TokenType.RPAREN);
        return new NewExpr(metadata(),new ClassType(name,typeArgs),fields);
    }

    // object_field ::= Name '=' expression ;
    private Var objectField() {
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
