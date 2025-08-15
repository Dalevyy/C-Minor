package cminor.parser;

import cminor.ast.AST;
import cminor.ast.classbody.ClassBody;
import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.expressions.Literal.ConstantType;
import cminor.ast.misc.TypeParam;
import cminor.ast.misc.TypeParam.TypeAnnotation;
import cminor.ast.misc.*;
import cminor.ast.operators.AssignOp;
import cminor.ast.operators.AssignOp.AssignType;
import cminor.ast.operators.BinaryOp;
import cminor.ast.operators.BinaryOp.BinaryType;
import cminor.ast.operators.LoopOp;
import cminor.ast.operators.LoopOp.LoopType;
import cminor.ast.operators.Operator;
import cminor.ast.operators.UnaryOp;
import cminor.ast.operators.UnaryOp.UnaryType;
import cminor.ast.statements.AssignStmt;
import cminor.ast.statements.BlockStmt;
import cminor.ast.statements.CaseStmt;
import cminor.ast.statements.ChoiceStmt;
import cminor.ast.statements.DoStmt;
import cminor.ast.statements.ExprStmt;
import cminor.ast.statements.ForStmt;
import cminor.ast.statements.IfStmt;
import cminor.ast.statements.ListStmt;
import cminor.ast.statements.LocalDecl;
import cminor.ast.statements.ReturnStmt;
import cminor.ast.statements.RetypeStmt;
import cminor.ast.statements.Statement;
import cminor.ast.statements.StopStmt;
import cminor.ast.statements.WhileStmt;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.EnumDecl;
import cminor.ast.topleveldecls.FuncDecl;
import cminor.ast.topleveldecls.GlobalDecl;
import cminor.ast.topleveldecls.ImportDecl;
import cminor.ast.topleveldecls.MainDecl;
import cminor.ast.types.ArrayType;
import cminor.ast.types.ClassType;
import cminor.ast.types.DiscreteType;
import cminor.ast.types.ListType;
import cminor.ast.types.ScalarType;
import cminor.ast.types.ScalarType.Scalars;
import cminor.ast.types.Type;
import cminor.ast.types.VoidType;
import cminor.lexer.Lexer;
import cminor.messages.CompilationMessage;
import cminor.messages.MessageHandler;
import cminor.micropasses.ImportHandler;
import cminor.token.Token;
import cminor.token.TokenType;
import cminor.utilities.PrettyPrint;
import cminor.utilities.Vector;

/**
 * A parser class responsible for checking if a C Minor program is syntactically valid.
 * <p><br>
 *     This is a handwritten LL(3) parser that interfaces with the {@link Lexer} to
 *     determine if a user correctly wrote a program. The parser is handwritten as
 *     suppose to tool-generated in order to facilitate the generation of more specific
 *     and meaningful error messages in the future.
 * </p>
 * @author Daniel Levy
 */
public class Parser {

    /**
     * Instance of the {@link Lexer} that the parser requests tokens from.
     */
    private final Lexer input;

    /**
     * Number of lookaheads the parser uses, currently set to 3.
     */
    private final int k = 3;

    /**
     * {@link Vector} of tokens containing the current lookaheads we are using.
     */
    private final Vector<Token> lookaheads;

    /**
     * Current lookahead position in the {@link #lookaheads} vector.
     */
    private int lookPos;

    /**
     * Stack that keeps track of all tokens we are currently parsing.
     */
    private final Vector<Token> tokenStack;

    /**
     * {@link MessageHandler} to generate syntax errors.
     */
    private final MessageHandler handler;

    /**
     * Flag set when a user wants to see every token that is parsed.
     */
    private static boolean printTokens = false;

    /**
     * Flag set when the parser begins to parse an imported file.
     */
    private boolean importMode;

    // Hacks to get IO statements to be parsed correctly... :(
    private boolean insideParen;
    private boolean insideIO;
    private boolean insideField;

    public Parser(Lexer input) {
        this.input = input;
        this.lookPos = 0;
        this.lookaheads = new Vector<>();
        this.tokenStack = new Vector<>();
        this.handler = new MessageHandler();
        this.importMode = false;
        this.insideParen = false;
        this.insideIO = false;
        this.insideField = false;

        for(int i = 0; i < k; i++) {
            this.lookaheads.add(new Token());
            consume();
        }
    }

    public static void setPrintTokens() { printTokens = !printTokens; }

    private String errorPosition(int start, int end) {
        return PrettyPrint.RED
                + " ".repeat(Math.max(0, start + 2))
                + "^".repeat(Math.max(0, end - start))
                + PrettyPrint.RESET;
    }

    private void consume() {
        lookaheads.set(lookPos,this.input.nextToken());
        lookPos = (lookPos + 1) % k;
    }

    private void match(TokenType expectedTok) {
        tokenStack.top().setEndLocation(currentLA().getEndPos());

        if(nextLA(expectedTok)) {
            if(printTokens) { System.out.println(currentLA().toString()); }
            consume();
        }
        else {
            //System.out.println(handler.createError().header());
            input.printSyntaxError(currentLA().getStartPos());
            System.out.println(errorPosition(currentLA().getStartPos().column,currentLA().getEndPos().column));

            throw new RuntimeException();
        }
    }

    private Token currentLA() { return lookaheads.get(lookPos%k); }
    private Token currentLA(int nextPos) { return lookaheads.get((lookPos+nextPos)%k); }

    private boolean nextLA(TokenType expectedTok) { return currentLA().getTokenType() == expectedTok; }
    private boolean nextLA(String expectedLexeme) { return currentLA().equals(expectedLexeme); }
    private boolean nextLA(TokenType expectedTok, int nextPos) {
        return currentLA(nextPos).getTokenType() == expectedTok;
    }

    public Token nodeToken() {
        Token t = tokenStack.pop();
        input.setText(t);

        if(tokenStack.top() != null)
            tokenStack.top().setEndLocation(t.getEndPos());

        return t;
    }

    public Token nodeTokenTop() {
        Token t = tokenStack.top();
        input.setText(t);
        return t;
    }

    /*
    _________________________________________
                    FIRST SETS
    _________________________________________
    */
    private boolean inScalarTypeFIRST() {
        return nextLA(TokenType.STRING)
                || nextLA(TokenType.REAL)
                || nextLA(TokenType.BOOL)
                || nextLA(TokenType.INT)
                || nextLA(TokenType.CHAR);
    }

    private boolean inDataDeclFIRST() {
        return nextLA(TokenType.PROPERTY)
                || nextLA(TokenType.PROTECTED)
                || nextLA(TokenType.PUBLIC);
    }

    private boolean inStatementFIRST() {
        return inConstantFIRST()
                || inScalarTypeFIRST()
                || nextLA(TokenType.LBRACK)
                || nextLA(TokenType.NEW)
                || nextLA(TokenType.ID)
                || nextLA(TokenType.LPAREN)
                || nextLA(TokenType.NOT)
                || nextLA(TokenType.BNOT)
                || nextLA(TokenType.LBRACE)
                || nextLA(TokenType.RETURN)
                || nextLA(TokenType.SET)
                || nextLA(TokenType.RETYPE)
                || nextLA(TokenType.IF)
                || nextLA(TokenType.WHILE)
                || nextLA(TokenType.FOR)
                || nextLA(TokenType.DO)
                || nextLA(TokenType.CHOICE)
                || nextLA("append")
                || nextLA("remove")
                || nextLA("insert")
                || nextLA(TokenType.CIN)
                || nextLA(TokenType.COUT)
                || nextLA(TokenType.BREAK)
                || nextLA(TokenType.CONTINUE)
                || nextLA(TokenType.STOP)
                || nextLA(TokenType.PARENT);
    }

    private boolean inPrimaryExpressionFIRST() {
        return inConstantFIRST()
                || nextLA(TokenType.ARRAY)
                || nextLA(TokenType.LIST)
                || nextLA(TokenType.TUPLE)
                || nextLA(TokenType.LBRACK)
                || nextLA(TokenType.LPAREN)
                || nextLA(TokenType.ID)
                || nextLA(TokenType.SLICE)
                || nextLA("length")
                || nextLA(TokenType.CAST)
                || nextLA(TokenType.BREAK)
                || nextLA(TokenType.CONTINUE)
                || nextLA(TokenType.PARENT);
    }

    private boolean inConstantFIRST() {
        return nextLA(TokenType.MINUS)
                || nextLA(TokenType.STR_LIT)
                || nextLA(TokenType.TEXT_LIT)
                || nextLA(TokenType.REAL_LIT)
                || nextLA(TokenType.BOOL_LIT)
                || nextLA(TokenType.INT_LIT)
                || nextLA(TokenType.CHAR_LIT)
                || nextLA(TokenType.ARRAY)
                || nextLA(TokenType.LIST)
                || nextLA(TokenType.NEW);
    }

    /*
    _________________________________________
                    FOLLOW SETS
    _________________________________________
    */

    private boolean inTypeFOLLOW() {
        return nextLA(TokenType.INT,1)
                || nextLA(TokenType.CHAR,1)
                || nextLA(TokenType.BOOL,1)
                || nextLA(TokenType.REAL,1)
                || nextLA(TokenType.STRING,1)
                || nextLA(TokenType.ID,1)
                || nextLA(TokenType.LIST,1)
                || nextLA(TokenType.ARRAY);
    }

    private boolean inPrimaryExpressionFOLLOW() {
        return nextLA(TokenType.LBRACK)
                || nextLA(TokenType.AT)
                || nextLA(TokenType.LPAREN)
                || nextLA(TokenType.ELVIS)
                || nextLA(TokenType.PERIOD);
    }

    private boolean inPowerExpressionFOLLOW() {
        return nextLA(TokenType.MULT)
                || nextLA(TokenType.DIV)
                || nextLA(TokenType.MOD);
    }

    private boolean inShiftExpressionFOLLOW() {
        return nextLA(TokenType.LT)
                || nextLA(TokenType.GT)
                || nextLA(TokenType.LTEQ)
                || nextLA(TokenType.GTEQ);
    }

    private Vector<AST> handleImports() {
        ImportHandler importHandler = new ImportHandler(input.getFileName());

        while(nextLA(TokenType.INCLUDE))
            importHandler.enqueue(importStmt());

        return importHandler.analyzeImports();
    }

    // ThisStmt will be the main method used for parsing when a user
    // runs C Minor through the virtual machine
    public Vector<? extends AST> nextNode() throws CompilationMessage {
        Vector<AST> nodes = new Vector<>();

        while(!nextLA(TokenType.EOF)) {
            if(nextLA(TokenType.INCLUDE))
                nodes.merge(handleImports());
            else if(nextLA(TokenType.DEF)
                    && nextLA(TokenType.ID,1)
                    && !(nextLA(TokenType.LT,2)
                    || nextLA(TokenType.LPAREN,2)
                    || nextLA(TokenType.COLON,2))) {
                nodes.add(enumType());
            }
            else if(nextLA(TokenType.DEF) && ((nextLA(TokenType.CONST, 1) || nextLA(TokenType.GLOBAL, 1))))
                nodes.merge(globalVariable());
            else if(nextLA(TokenType.ABSTR) || nextLA(TokenType.FINAL) || nextLA(TokenType.CLASS)) {
                nodes.add(classType());
            }
            // Parse FuncDecl
            else if((nextLA(TokenType.DEF))
                    && (nextLA(TokenType.PURE,1)
                    || nextLA(TokenType.RECURS,1)
                    || ((nextLA(TokenType.ID,1)
                    && (nextLA(TokenType.LPAREN,2)
                    || nextLA(TokenType.LT,2)))
                    && ((!nextLA(TokenType.MAIN,1))
                    && (!nextLA(TokenType.MAIN,2)))))) {
                nodes.add(function());

            }
            // Parse LocalDecl
            else if((nextLA(TokenType.DEF) && nextLA(TokenType.ID,1) && nextLA(TokenType.COLON,2))
                    || (nextLA(TokenType.DEF) && nextLA(TokenType.LOCAL,1))) {
                nodes.merge(declaration());
            }
            // Parse Statement | Expression
            else { nodes.add(statement()); }
        }

        return nodes;
    }

    /*
    ____________________________________________________________

                          COMPILATION UNIT
    ____________________________________________________________
    */

    // 1. compilation ::= import_stmt* enum_type* global_variable* class_type* function* main
    public CompilationUnit compilation() {
        tokenStack.add(currentLA());

        Vector<ImportDecl> imports = new Vector<>();
        if(nextLA(TokenType.INCLUDE)) {
            Vector<AST> im = handleImports();
            for(AST p : im)
                imports.add(p.asTopLevelDecl().asImport());
        }

        Vector<EnumDecl> enums = new Vector<>();
        while(nextLA(TokenType.DEF)
                && nextLA(TokenType.ID,1)
                && !(nextLA(TokenType.LT,2)
                || nextLA(TokenType.LPAREN,2))) {
            enums.add(enumType());
        }

        Vector<GlobalDecl> globals = new Vector<>();
        while(nextLA(TokenType.DEF) && (nextLA(TokenType.CONST, 1) || nextLA(TokenType.GLOBAL, 1))) {
            Vector<AST> globe = globalVariable();
            for(AST g : globe)
                globals.merge(g.asTopLevelDecl().asGlobalDecl());
        }

        Vector<ClassDecl> classes = new Vector<>();
        while(nextLA(TokenType.ABSTR) || nextLA(TokenType.FINAL) || nextLA(TokenType.CLASS)) {
            classes.add(classType());
        }

        Vector<FuncDecl> funcs = new Vector<>();
        while((nextLA(TokenType.DEF)) && !nextLA(TokenType.MAIN,1)) { funcs.add(function()); }

        MainDecl md = null;
        if(!importMode)
            md = mainFunc();

        if(!nextLA(TokenType.EOF)) {
            System.out.println(PrettyPrint.CYAN + "Syntax Error Detected! Unexpected End of File in " + input.getFileName() + ".");
            System.exit(1);
        }
        else if(printTokens) { System.out.println(currentLA().toString()); }

        return new CompilationUnit(nodeToken(),input.getFileName(),imports,enums,globals,classes,funcs,md);
    }

    /*
    ____________________________________________________________
                        COMPILER DIRECTIVES
    ____________________________________________________________
    */

    // 2. file-merge ::= '#include' STRING_LITERAL
    private ImportDecl importStmt() {
        tokenStack.add(currentLA());

        match(TokenType.INCLUDE);
        Name fileName = new Name(currentLA());
        match(TokenType.STR_LIT);

        return new ImportDecl(nodeToken(),fileName);
    }

    /*
    ____________________________________________________________
                          ENUM DECLARATION
    ____________________________________________________________
    */

    // 3. enum_type ::= 'def' ID 'type' '=' '{' enum_field ( ',' , enum_field)* '}'
    private EnumDecl enumType() {
        tokenStack.add(currentLA());

        match(TokenType.DEF);
        Name n = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.TYPE);
        match(TokenType.EQ);
        match(TokenType.LBRACE);

        Vector<Var> vars = new Vector<>(enumField());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            vars.add(enumField());
        }

        match(TokenType.RBRACE);
        
        return new EnumDecl(nodeToken(),n,vars);
    }

    // 4. enum_field ::= ID ( '=' constant )?
    private Var enumField() {
        tokenStack.add(currentLA());

        Name n = new Name(currentLA());
        match(TokenType.ID);

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            Expression e = constant();

            return new Var(nodeToken(),n,e);
        }

        return new Var(nodeToken(),n);
    }

    /*
    ____________________________________________________________
              GLOBAL VARIABLES AND VARIABLE DECLARATIONS
    ____________________________________________________________
    */

    // 5. global_variable ::= 'def' ( 'const' | 'global' ) variable_decl
    private Vector<AST> globalVariable() {
        tokenStack.add(currentLA());
        boolean isConstant = false;

        match(TokenType.DEF);
        if(nextLA(TokenType.CONST)) {
            match(TokenType.CONST);
            isConstant = true;
        }
        else { match(TokenType.GLOBAL); }

        Vector<Var> vars = variableDecl();
        Vector<AST> globals = new Vector<>();

        for(Var v : vars) {
            tokenStack.top().setEndLocation(v.getLocation().end);
            input.setText(tokenStack.top());
            globals.add(new GlobalDecl(tokenStack.top(),v,isConstant));
        }

        tokenStack.pop();
        return globals;
    }

    // 6. variable_decl ::= variable_decl_list
    private Vector<Var> variableDecl() { return variableDeclList(); }

    // 7. variable_decl_list ::= variable_decl_init ( ',' variable_decl_init )*
    private Vector<Var> variableDeclList() {
        Vector<Var> varList = new Vector<>(variableDeclInit());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            varList.add(variableDeclInit());
        }

        return varList;
    }

    // 8. variable_decl_init ::= ID ':' type ('=' (expression | 'uninit' ))?
    private Var variableDeclInit() {
        tokenStack.add(currentLA());

        Name n = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type t = type();

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            if(nextLA(TokenType.UNINIT)) {
                match(TokenType.UNINIT);
                return new Var(nodeToken(),n,new Literal(),t); // give it some random value
            }
            else {
                Expression e = expression();
                return new Var(nodeToken(),n,e,t);
            }
        }
        return new Var(nodeToken(),n,t);
    }

    /*
    ____________________________________________________________
                                TYPES
    ____________________________________________________________
    */

    // 9. type ::= scalar_type
    //           | class_name
    //           | 'List' '[' type ']'
    //           | 'Array' '[' type ']'
    private Type type() {
        if(nextLA(TokenType.ID)) { return className(); }
        else if(nextLA(TokenType.LIST)) {
            tokenStack.add(currentLA());

            match(TokenType.LIST);

            match(TokenType.LBRACK);
            Type ty = type();
            match(TokenType.RBRACK);

            if(ty.isList()) {
                ty.asList().dims += 1;
                ty.copyMetaData(nodeToken());
                return ty;
            }
            return new ListType(nodeToken(),ty,1);
        }
        else if(nextLA(TokenType.ARRAY)) {
            tokenStack.add(currentLA());

            match(TokenType.ARRAY);

            match(TokenType.LBRACK);
            Type ty = type();
            match(TokenType.RBRACK);

            input.setText(tokenStack.top());

            if(ty.isArray()) {
                ty.asArray().dims += 1;
                ty.copyMetaData(nodeToken());
                return ty;
            }
            return new ArrayType(nodeToken(),ty,1);
        }
        return scalarType();
    }

    // 10. scalar_type ::= discrete_type
    //                  | 'String' ( '[' Int_literal ']' )*
    //                  | 'Real'   ( '[' Int_literal ']' )*
    private Type scalarType() {
        if(nextLA(TokenType.STRING)) {
            tokenStack.add(currentLA());
            match(TokenType.STRING);

            if(nextLA(TokenType.LBRACK)) {
                ScalarType arrType = new ScalarType(tokenStack.top(),Scalars.STR);

                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK);
                    dims += 1;
                }

                return new ArrayType(nodeToken(),arrType,dims);
            }

            return new ScalarType(nodeToken(),Scalars.STR);
        }
        else if(nextLA(TokenType.REAL)) {
            tokenStack.add(currentLA());
            match(TokenType.REAL);

            if(nextLA(TokenType.LBRACK)) {
                ScalarType arrType = new ScalarType(tokenStack.top(),Scalars.REAL);

                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK);
                    dims += 1;
                }

                return new ArrayType(nodeToken(),arrType,dims);
            }

            return new ScalarType(nodeToken(),Scalars.REAL);
        }
        else { return discreteType(); }
    }

    // 11. discrete_type ::= 'Bool' ( '[' Int_literal ']' )*
    //                     | 'Int'  ( '[' Int_literal ']' )*
    //                     | 'Char' ( '[' Int_literal ']' )*
    private Type discreteType() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.BOOL)) {
            match(TokenType.BOOL);

            if(nextLA(TokenType.LBRACK)) {
                DiscreteType arrType = new DiscreteType(tokenStack.top(),Scalars.BOOL);

                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK);
                    dims += 1;
                }

                return new ArrayType(nodeToken(),arrType,dims);
            }

            return new DiscreteType(nodeToken(),Scalars.BOOL);
        }
        else if(nextLA(TokenType.INT)) {
            match(TokenType.INT);

            if(nextLA(TokenType.LBRACK)) {
                DiscreteType arrType = new DiscreteType(tokenStack.top(),Scalars.INT);

                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK);
                    dims += 1;
                }

                return new ArrayType(nodeToken(),arrType,dims);
            }

            return new DiscreteType(nodeToken(),Scalars.INT);
        }
        else {
            match(TokenType.CHAR);

            if(nextLA(TokenType.LBRACK)) {
                DiscreteType arrType = new DiscreteType(tokenStack.top(),Scalars.CHAR);

                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK);
                    dims += 1;
                }

                return new ArrayType(nodeToken(),arrType,dims);
            }

            return new DiscreteType(nodeToken(),Scalars.CHAR);
        }
    }

    // 12. class_name ::= ID ( type_params? )?
    private ClassType className() {
        tokenStack.add(currentLA());

        Name n = new Name(currentLA());
        match(TokenType.ID);

        if(nextLA(TokenType.LT)) {
            Vector<Type> types = typeParams();
            return new ClassType(nodeToken(),n,types);
        }
        return new ClassType(nodeToken(),n);
    }

    // --. type_params : '@' type ( ',' type )* '@'
    private Vector<Type> typeParams() {
        if(nextLA(TokenType.LT))
            match(TokenType.LT);
        else
            match(TokenType.AT);
        Vector<Type> types = new Vector<>(type());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            types.add(type());
        }

        if(nextLA(TokenType.GT))
            match(TokenType.GT);
        else
            match(TokenType.AT);
        return types;
    }

    /*
    ____________________________________________________________
                          CLASS DECLARATION
    ____________________________________________________________
    */

    // 13. class_type ::= ( 'abstr' | 'final' )? 'class' ID type_params? super_class? class_body
    private ClassDecl classType() {
        tokenStack.add(currentLA());

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
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> types = new Vector<>();
        if(nextLA(TokenType.LT)) { types = typeifierParams(); }

        ClassType ct = null;
        if(nextLA(TokenType.INHERITS)) { ct = superClass(); }

        ClassBody body = classBody();
        return new ClassDecl(nodeToken(),mod,n,types,ct,body);
    }

    // 14. type_params ::= '<' typeifier ( ',' typeifier )* '>'
    private Vector<TypeParam> typeifierParams() {
        match(TokenType.LT);
        Vector<TypeParam> typefs = new Vector<>(typeifier());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            typefs.add(typeifier());
        }
        match(TokenType.GT);

        return typefs;
    }

    // 15. typeifier ::= ( 'discr' | 'scalar' | 'class' )? ID
    private TypeParam typeifier() {
        tokenStack.add(currentLA());

        TypeAnnotation pt = null;
        if(nextLA(TokenType.DISCR)) {
            pt = TypeAnnotation.DISCR;
            match(TokenType.DISCR);
        }
        else if(nextLA(TokenType.SCALAR)) {
            pt = TypeAnnotation.SCALAR;
            match(TokenType.SCALAR);
        }
        else if(nextLA(TokenType.CLASS)) {
            pt = TypeAnnotation.CLASS;
            match(TokenType.CLASS);
        }

        Name n = new Name(currentLA());
        match(TokenType.ID);

        return new TypeParam(nodeToken(),pt,n);
    }

    // 16. super_class ::= 'inherits' ID type_params?
    private ClassType superClass() {
        match(TokenType.INHERITS);

        tokenStack.add(currentLA());
        Name superName = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> vectorOfTypes = new Vector<>();
        if(nextLA(TokenType.LT))
            vectorOfTypes = typeParams();

        return new ClassType(nodeToken(),superName,vectorOfTypes);
    }

    // 17. class_body ::= '{' data_decl* method_decl* '}'
    private ClassBody classBody() {
        tokenStack.add(currentLA());
        match(TokenType.LBRACE);

        Vector<FieldDecl> dataDecls = new Vector<>();
        while(inDataDeclFIRST() && nextLA(TokenType.ID,1)) { dataDecls.merge(dataDecl()); }

        Vector<MethodDecl> methodDecls = new Vector<>();
        while(nextLA(TokenType.PROTECTED) || nextLA(TokenType.PUBLIC)) { methodDecls.add(methodDecl()); }

        match(TokenType.RBRACE);
        return new ClassBody(nodeToken(),dataDecls,methodDecls);
    }

    /*
    ____________________________________________________________
                          FIELD DECLARATION
    ____________________________________________________________
    */

    // 18. data_decl ::= ( 'property' | 'protected' | 'public' ) variable_decl
    private Vector<FieldDecl> dataDecl() {
        tokenStack.add(currentLA());

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

        for(Var v : vars) {
            tokenStack.top().setEndLocation(v.getLocation().end);
            input.setText(tokenStack.top());
            fields.add(new FieldDecl(tokenStack.top(),mod,v));
        }

        tokenStack.pop();
        return fields;
    }

    /*
    ____________________________________________________________
                          METHOD DECLARATION
    ____________________________________________________________
    */

    // 19. method_decl ::= method_class | operator_class
    private MethodDecl methodDecl() {
        if(nextLA(TokenType.OPERATOR,1) || nextLA(TokenType.OPERATOR,2)) { return operatorClass(); }
        else { return methodClass(); }
    }

    // 20. method_class ::= method_modifier attribute 'override'? 'method' method_header '=>' return_type block_statement
    private MethodDecl methodClass() {
        tokenStack.add(currentLA());
        Modifier mod = methodModifier();

        while(nextLA(TokenType.FINAL) || nextLA(TokenType.PURE) || nextLA(TokenType.RECURS)) {
            tokenStack.add(currentLA());
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

        return new MethodDecl(nodeToken(),mod,mName,null,pd,rt,bs,override);
    }

    // 21. method_modifier : 'protected' | 'public' ;
    private Modifier methodModifier() {
        tokenStack.add(currentLA());

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
        tokenStack.add(currentLA());
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
        tokenStack.add(currentLA());

        Modifier m = paramModifier();

        Name n = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type ty = type();

        Vector<ParamDecl> pd = new Vector<>();
        pd.add(new ParamDecl(nodeToken(),m,n,ty));

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            tokenStack.add(currentLA());
            m = paramModifier();

            n = new Name(currentLA());
            match(TokenType.ID);
            match(TokenType.COLON);
            ty = type();

            pd.add(new ParamDecl(nodeToken(),m,n,ty));
        }

        return pd;
    }

    // 25. param_modifier : 'in' | 'out' | 'inout' | 'ref'
    private Modifier paramModifier() {
        tokenStack.add(currentLA());

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
            tokenStack.add(currentLA());
            match(TokenType.VOID);
            return new VoidType(nodeToken());
        }
        return type();
    }

    // 27. operator_class : operator_modifier 'final'? 'operator' operator_header '=>' return_type block_statement
    private MethodDecl operatorClass() {
        tokenStack.add(currentLA());
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

        return new MethodDecl(nodeToken(),mod,null,op,pd,rt,block,false);
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

    // 30. binary_operator ::= <= | < | > | >= | == | <> | <=> | <: | :> | + | - | * | / | % | **
    private BinaryOp binaryOperator() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.LTEQ)) {
            match(TokenType.LTEQ);
            return new BinaryOp(nodeToken(),BinaryType.LTEQ);
        }
        else if(nextLA(TokenType.LT)) {
            match(TokenType.LT);
            return new BinaryOp(nodeToken(),BinaryType.LT);
        }
        else if(nextLA(TokenType.GT)) {
            match(TokenType.GT);
            return new BinaryOp(nodeToken(),BinaryType.GT);
        }
        else if(nextLA(TokenType.GTEQ)) {
            match(TokenType.GTEQ);
            return new BinaryOp(nodeToken(),BinaryType.GTEQ);
        }
        else if(nextLA(TokenType.EQEQ)) {
            match(TokenType.EQEQ);
            return new BinaryOp(nodeToken(),BinaryType.EQEQ);
        }
        else if(nextLA(TokenType.LTGT)) {
            match(TokenType.LTGT);
            return new BinaryOp(nodeToken(),BinaryType.LTGT);
        }
        else if(nextLA(TokenType.UFO)) {
            match(TokenType.UFO);
            return new BinaryOp(nodeToken(),BinaryType.UFO);
        }
        else if(nextLA(TokenType.PLUS)) {
            match(TokenType.PLUS);
            return new BinaryOp(nodeToken(),BinaryType.PLUS);
        }
        else if(nextLA(TokenType.MINUS)) {
            match(TokenType.MINUS);
            return new BinaryOp(nodeToken(),BinaryType.MINUS);
        }
        else if(nextLA(TokenType.MULT)) {
            match(TokenType.MULT);
            return new BinaryOp(nodeToken(),BinaryType.MULT);
        }
        else if(nextLA(TokenType.DIV)) {
            match(TokenType.DIV);
            return new BinaryOp(nodeToken(),BinaryType.DIV);
        }
        else if(nextLA(TokenType.MOD)) {
            match(TokenType.MOD);
            return new BinaryOp(nodeToken(),BinaryType.MOD);
        }
        else {
            match(TokenType.EXP);
            return new BinaryOp(nodeToken(),BinaryType.EXP);
        }
    }

    // 31. unary-operator ::= ~ | not
    private UnaryOp unaryOperator() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.BNOT)) {
            match(TokenType.BNOT);
            return new UnaryOp(nodeToken(),UnaryType.NEGATE);
        }
        else {
            match(TokenType.NOT);
            return new UnaryOp(nodeToken(),UnaryType.NOT);
        }
    }

    /*
    ____________________________________________________________
                        FUNCTION DECLARATION
    ____________________________________________________________
    */

    // 32. function ::= 'def' ( 'pure' | 'recurs' )? function_header '=>' return_type block_statement
    private FuncDecl function() {
        tokenStack.add(currentLA());
        boolean isRecursive = false;
        match(TokenType.DEF);

        Modifier mod = new Modifier();
        if(nextLA(TokenType.PURE)) {
            mod.setPure();
            match(TokenType.PURE);
        }
        else if(nextLA(TokenType.RECURS)) {
            mod.setRecursive();
            match(TokenType.RECURS);
        }

        Vector<Object> header = functionHeader();
        Name n = (Name) header.get(0);

        Vector<TypeParam> typefs = (Vector<TypeParam>) header.get(1);
        Vector<ParamDecl> pd = (Vector<ParamDecl>) header.get(2);

        match(TokenType.ARROW);
        Type ret = returnType();
        BlockStmt b = blockStatement();

        return new FuncDecl(nodeToken(),mod,n,typefs,pd,ret,b);
    }

    // 33. function_header ::= ID function_type_params? '(' formal_params? ')'
    private Vector<Object> functionHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Vector<TypeParam> typefs = new Vector<>();
        if(nextLA(TokenType.LT)) { typefs = typeifierParams(); }

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF)) {
            params = formalParams();
        }
        match(TokenType.RPAREN);

        Vector<Object> header = new Vector<>();
        header.add(n);
        header.add(typefs);
        header.add(params);

        return header;
    }

    /*
    ____________________________________________________________
                          MAIN FUNCTION
    ____________________________________________________________
    */

    // 34. main ::= 'def' 'main' args? '=>' return_type block_statement
    private MainDecl mainFunc() {
        tokenStack.add(currentLA());

        match(TokenType.DEF);
        match(TokenType.MAIN);

        Vector<ParamDecl> args = new Vector<>();
        if(nextLA(TokenType.LPAREN)) { args.merge(args()); }

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt b = blockStatement();

        return new MainDecl(nodeToken(),args,rt,b);
    }

    // 35. args ::= '(' formal_params? ')'
    private Vector<ParamDecl> args() {
        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = new Vector<>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF)) {
            pd = formalParams();
        }
        match(TokenType.RPAREN);
        return pd;
    }

    // 36. block-statement ::= '{' declaration* statement* '}'
    private BlockStmt blockStatement() {
        tokenStack.add(currentLA());
        match(TokenType.LBRACE);

        Vector<LocalDecl> vd = new Vector<>();
        while(nextLA(TokenType.DEF)) {
            Vector<AST> local = declaration();
            for(AST l : local)
                vd.add(l.asStatement().asLocalDecl());
        }

        Vector<Statement> st = new Vector<>();
        while(inStatementFIRST()) { st.add(statement()); }

        match(TokenType.RBRACE);
        return new BlockStmt(nodeToken(),vd,st);
    }

    // 37. declaration ::= 'def' 'local'? variable_decl
    private Vector<AST> declaration() {
        tokenStack.add(currentLA());

        match(TokenType.DEF);
        if(nextLA(TokenType.LOCAL)) { match(TokenType.LOCAL); }

        Vector<Var> vars = variableDecl();
        Vector<AST> locals = new Vector<>();

        for(Var v : vars) {
            tokenStack.top().setEndLocation(v.getLocation().end);
            input.setText(tokenStack.top());
            locals.add(new LocalDecl(tokenStack.top(),v));
        }

        tokenStack.pop();
        return locals;
    }

    /*
    ____________________________________________________________
                             STATEMENTS
    ____________________________________________________________
    */

    /*
       38. statement ::= 'stop'
                       | return_statement
                       | assignment_statement
                       | block_statement
                       | if_statement
                       | while_statement
                       | do_while_statement
                       | for_statement
                       | choice_statement
                       | list_command_statement
     */
    private Statement statement() {
        if(nextLA(TokenType.STOP)) {
            tokenStack.add(currentLA());
            match(TokenType.STOP);
            return new StopStmt(nodeToken());
        }
        else if(nextLA(TokenType.RETURN))
            return returnStatement();
        else if(nextLA(TokenType.LBRACE))
            return blockStatement();
        else if(nextLA(TokenType.IF))
            return ifStatement();
        else if(nextLA(TokenType.WHILE))
            return whileStatement();
        else if(nextLA(TokenType.DO))
            return doWhileStatement();
        else if(nextLA(TokenType.FOR))
            return forStatement();
        else if(nextLA(TokenType.CHOICE))
            return choiceStatement();
        else if(nextLA("append") || nextLA("remove") || nextLA("insert"))
            return listCommandStatement();
        else
            return assignmentStatement();
    }

    // 39. return_statement ::= expression?
    private ReturnStmt returnStatement() {
        tokenStack.add(currentLA());

        match(TokenType.RETURN);
        if(inPrimaryExpressionFIRST()) {
            Expression e = expression();
            return new ReturnStmt(nodeToken(),e);
        }

        return new ReturnStmt(nodeToken(),null);
    }

    // 41. assignment_statement ::= 'set' expression assignment_operator expression
    //                            | 'retype' expression '=' object_constant
    //                            |  logical_or_expression
    private Statement assignmentStatement() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.SET)) {
            match(TokenType.SET);
            Expression LHS = expression();
            AssignOp op = assignmentOperator();
            Expression RHS = expression();

            return new AssignStmt(nodeToken(),LHS,RHS,op);
        }
        else if(nextLA(TokenType.RETYPE)) {
            match(TokenType.RETYPE);

            Expression e = expression();
            match(TokenType.EQ);
            NewExpr RHS = objectConstant();

            return new RetypeStmt(nodeToken(),e,RHS);
        }

        Expression e = logicalOrExpression();
        return new ExprStmt(nodeToken(),e);
    }

    // 40. assignment_operator ::= '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**='
    private AssignOp assignmentOperator() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            return new AssignOp(nodeToken(),AssignType.EQ);
        }
        else if(nextLA(TokenType.PLUSEQ)) {
            match(TokenType.PLUSEQ);
            return new AssignOp(nodeToken(),AssignType.PLUSEQ);
        }
        else if(nextLA(TokenType.MINUSEQ)) {
            match(TokenType.MINUSEQ);
            return new AssignOp(nodeToken(),AssignType.MINUSEQ);
        }
        else if(nextLA(TokenType.MULTEQ)) {
            match(TokenType.MULTEQ);
            return new AssignOp(nodeToken(),AssignType.MULTEQ);
        }
        else if(nextLA(TokenType.DIVEQ)) {
            match(TokenType.DIVEQ);
            return new AssignOp(nodeToken(),AssignType.DIVEQ);
        }
        else if(nextLA(TokenType.MODEQ)) {
            match(TokenType.MODEQ);
            return new AssignOp(nodeToken(),AssignType.MODEQ);
        }
        else {
            match(TokenType.EXPEQ);
            return new AssignOp(nodeToken(),AssignType.EXPEQ);
        }
    }

    // 41. if_statement ::= if expression block_statement ( elseif_statement )* ( 'else' block_statement)?
    private IfStmt ifStatement() {
        tokenStack.add(currentLA());

        match(TokenType.IF);
        Expression e = expression();

        BlockStmt b = blockStatement();

        Vector<IfStmt> elifStmts = new Vector<>();
        while(nextLA(TokenType.ELSE) && nextLA(TokenType.IF,1)) {
            IfStmt eIf = elseIfStatement();
            elifStmts.add(eIf);
        }

        BlockStmt elseBlock;
        if(nextLA(TokenType.ELSE)) {
            match(TokenType.ELSE);
            elseBlock = blockStatement();

            return new IfStmt(nodeToken(),e,b,elifStmts,elseBlock);
        }

        return new IfStmt(nodeToken(),e,b,elifStmts);
    }

    // 42. elseif_statement ::= 'else' 'if' expression block_statement
    private IfStmt elseIfStatement() {
        tokenStack.add(currentLA());

        match(TokenType.ELSE);
        match(TokenType.IF);

        Expression e = expression();
        BlockStmt b = blockStatement();

        return new IfStmt(nodeToken(),e,b);
    }

    // 43. while_statement ::= 'while' expression block_statement
    private WhileStmt whileStatement() {
        tokenStack.add(currentLA());

        match(TokenType.WHILE);
        Expression e = expression();
        BlockStmt b = blockStatement();

        return new WhileStmt(nodeToken(),e,b);
    }

    // 44. do_while_statement ::= 'do' block_statement 'while' expression
    private DoStmt doWhileStatement() {
        tokenStack.add(currentLA());

        match(TokenType.DO);
        BlockStmt b = blockStatement();

        match(TokenType.WHILE);
        Expression e = expression();

        return new DoStmt(nodeToken(),b,e);
    }

    // 45. for_statement : 'for' '(' range_iterator | array_iterator')' block_statement
    private ForStmt forStatement() {
        tokenStack.add(currentLA());

        match(TokenType.FOR);
        match(TokenType.LPAREN);

        Vector<AST> forCondition = rangeIterator();
        LocalDecl forVar = forCondition.get(0).asStatement().asLocalDecl();
        Expression LHS = forCondition.get(1).asExpression();
        Expression RHS = forCondition.get(3).asExpression();
        LoopOp loopOp = forCondition.get(2).asOperator().asLoopOp();

        match(TokenType.RPAREN);
        BlockStmt b = blockStatement();

        return new ForStmt(nodeToken(),forVar,LHS,RHS,loopOp,b);
    }

    // 46. range_iterator : 'def' Name 'in' expression range_operator expression ;
    private Vector<AST> rangeIterator() {
        tokenStack.add(currentLA());
        Vector<AST> forComponents = new Vector<>();

        match(TokenType.DEF);
        Var v = variableDeclInit();
        forComponents.add(new LocalDecl(nodeToken(),v));
        match(TokenType.IN);

        forComponents.add(expression());
        forComponents.add(rangeOperator());
        forComponents.add(expression());

        return forComponents;
    }

    // 47. range_operator : inclusive | exclusive_right | exclusive_left | exclusive ;
    private LoopOp rangeOperator() {
        if(nextLA(TokenType.INC) && nextLA(TokenType.LT,1)) { return exclusiveRight(); }
        else if(nextLA(TokenType.INC)) { return inclusive(); }
        else if(nextLA(TokenType.LT)
                && nextLA(TokenType.INC,1)
                && nextLA(TokenType.LT,2)) {
            return exclusive();
        }
        else { return exclusiveLeft(); }
    }

    // 48. inclusive : '..' ;
    private LoopOp inclusive() {
        tokenStack.add(currentLA());
        match(TokenType.INC);
        return new LoopOp(nodeToken(),LoopType.INCL);
    }

    // 49. exclusive_right : '..<' ;
    private LoopOp exclusiveRight() {
        tokenStack.add(currentLA());
        match(TokenType.INC);
        match(TokenType.LT);
        return new LoopOp(nodeToken(),LoopType.EXCL_R);

    }

    // 50. exclusive_left : '<..' ;
    private LoopOp exclusiveLeft() {
        tokenStack.add(currentLA());
        match(TokenType.LT);
        match(TokenType.INC);
        return new LoopOp(nodeToken(),LoopType.EXCL_L);
    }

    // 51. exclusive : '<..<'
    private LoopOp exclusive() {
        tokenStack.add(currentLA());
        match(TokenType.LT);
        match(TokenType.INC);
        match(TokenType.LT);
        return new LoopOp(nodeToken(),LoopType.EXCL);
    }

    // 52. choice_statement ::= 'choice' expression '{' case_statement* 'other' block_statement '}'
    private ChoiceStmt choiceStatement() {
        tokenStack.add(currentLA());

        match(TokenType.CHOICE);
        Expression e = expression();

        match(TokenType.LBRACE);
        Vector<CaseStmt> cStmts = new Vector<>();
        while(nextLA(TokenType.ON)) { cStmts.add(caseStatement()); }

        match(TokenType.OTHER);
        BlockStmt b = blockStatement();
        match(TokenType.RBRACE);

        return new ChoiceStmt(nodeToken(),e,cStmts,b);
    }

    // 53. case_statement ::= 'on' label block_statement
    private CaseStmt caseStatement() {
        tokenStack.add(currentLA());

        match(TokenType.ON);
        Label l = label();
        BlockStmt b = blockStatement();

        return new CaseStmt(nodeToken(),l,b);
    }

    // 54. label ::= scalar_constant ('..' scalar_constant)?
    private Label label() {
        tokenStack.add(currentLA());
        Literal lConstant = scalarConstant();

        if(nextLA(TokenType.INC)) {
            match(TokenType.INC);
            Literal rConstant = scalarConstant();
            return new Label(nodeToken(),lConstant,rConstant);
        }

        return new Label(nodeToken(),lConstant);
    }

    // 55. list_command_statement ::= 'append' '(' arguments? ')'
    //                              | 'remove' '(' arguments? ')'
    //                              | 'insert' '(' arguments? ')'
    private ListStmt listCommandStatement() {
        tokenStack.add(currentLA());
        ListStmt.Commands command;

        if(nextLA("append")) {
            match(TokenType.ID);
            command = ListStmt.Commands.APPEND;
        }
        else if(nextLA("remove")) {
            match(TokenType.ID);
            command = ListStmt.Commands.REMOVE;
        }
        else {
            match(TokenType.ID);
            command = ListStmt.Commands.INSERT;
        }

        match(TokenType.LPAREN);
        Vector<Expression> args = new Vector<>();
        if(!nextLA(TokenType.RPAREN))
            args = arguments();
        match(TokenType.RPAREN);
        return new ListStmt(nodeToken(),command,args);
    }

    // 56. input_statement ::= 'cin' ( '>>' expression )+
    private InStmt inputStatement() {
        tokenStack.add(currentLA());

        match(TokenType.CIN);
        match(TokenType.SRIGHT);

        boolean oldIO = insideIO;
        insideIO = true;
        Vector<Expression> inputExprs = new Vector<>(expression());

        while(nextLA(TokenType.SRIGHT)) {
            match(TokenType.SRIGHT);
            inputExprs.add(expression());
        }
        insideIO = oldIO;
        return new InStmt(nodeToken(),inputExprs);
    }

    // 57. output_statement ::= 'cout' ( '<<' expression )+
    private OutStmt outputStatement() {
        tokenStack.add(currentLA());

        match(TokenType.COUT);
        match(TokenType.SLEFT);

        boolean oldIO = insideIO;
        insideIO = true;
        Vector<Expression> outputExprs = new Vector<>(expression());

        while(nextLA(TokenType.SLEFT)) {
            match(TokenType.SLEFT);
            outputExprs.add(expression());
        }

        insideIO = oldIO;
        return new OutStmt(nodeToken(),outputExprs);
    }

    /*
    ____________________________________________________________
                            EXPRESSIONS
    ____________________________________________________________
    */

    // 58. primary_expression ::= ID
    //                          | constant
    //                          | '(' expression ')'
    //                          | input_statement
    //                          | output_statement
    //                          | 'break'
    //                          | 'continue'
    //                          | 'endl'
    //                          | 'parent'
    private Expression primaryExpression() {
        if(nextLA(TokenType.LPAREN)) {
            boolean oldParen = insideParen;
            insideParen = true;

            match(TokenType.LPAREN);
            Expression e = expression();
            match(TokenType.RPAREN);

            insideParen = oldParen;

            return e;
        }
        else if(nextLA(TokenType.CIN)) { return inputStatement(); }
        else if(nextLA(TokenType.COUT)) { return outputStatement(); }
        else if(inConstantFIRST()) { return constant(); }

        tokenStack.add(currentLA());

        if(nextLA(TokenType.ID)) {
            Name n = new Name(currentLA());
            match(TokenType.ID);

            return new NameExpr(nodeToken(),n);
        }
        else if(nextLA(TokenType.BREAK)) {
            match(TokenType.BREAK);
            return new BreakStmt(nodeToken());
        }
        else if(nextLA(TokenType.CONTINUE)) {
            match(TokenType.CONTINUE);
            return new ContinueStmt(nodeToken());
        }
        else if(nextLA(TokenType.ENDL)) {
            match(TokenType.ENDL);
            return new EndlStmt(nodeToken());
        }
        else {
            match(TokenType.PARENT);
            return new ParentStmt(nodeToken());
        }
    }

    // 59. postfix_expression ::= primary_expression ( '[' expression ']'
    //                                               | '(' arguments? ')'
    //                                               |  ( '.' | '?.' ) expression )*
    private Expression postfixExpression() {
        tokenStack.add(currentLA());
        Expression LHS = primaryExpression();

        if(inPrimaryExpressionFOLLOW()) {
            Expression RHS;
            while(inPrimaryExpressionFOLLOW()) {
                if(nextLA(TokenType.LBRACK)) {
                    Vector<Expression> indices = new Vector<>();

                    while(nextLA(TokenType.LBRACK)) {
                        match(TokenType.LBRACK);
                        indices.add(expression());
                        match(TokenType.RBRACK);
                    }

                    input.setText(tokenStack.top());
                    RHS = new ArrayExpr(tokenStack.top(),LHS,indices);
                }
                else if(nextLA(TokenType.LPAREN) || nextLA(TokenType.AT)) {
                    Vector<Expression> args = new Vector<>();
                    Vector<Type> types = new Vector<>();
                    if(nextLA(TokenType.AT) && inTypeFOLLOW())
                        types = typeParams();

                    match(TokenType.LPAREN);

                    if(!nextLA(TokenType.RPAREN))
                        args = arguments();

                    match(TokenType.RPAREN);

                    input.setText(tokenStack.top());
                    RHS = new Invocation(tokenStack.top(),LHS.asExpression().asNameExpr().getName(),types,args);
                }
                else {
                    boolean oldField = insideField;
                    insideField = true;
                    boolean nullCheck = false;

                    if(nextLA(TokenType.PERIOD))
                        match(TokenType.PERIOD);
                    else {
                        match(TokenType.ELVIS);
                        nullCheck = true;
                    }

                    Expression expr = expression();
                    input.setText(tokenStack.top());
                    RHS = new FieldExpr(tokenStack.top(),LHS.asExpression(),expr.asExpression(),nullCheck);
                    insideField = oldField;
                }
                LHS = RHS;
            }
        }

        nodeToken();
        return LHS;
    }

    // 60. arguments ::= expression ( ',' expression )*
    private Vector<Expression> arguments() {
        Vector<Expression> ex = new Vector<>();
        ex.add(expression());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            ex.add(expression());
        }
        return ex;
    }

    // 61. factor_expression ::= 'length' '(' arguments ')' | postfix_expression
    private Expression factorExpression() {
        if(nextLA("length")) {
            tokenStack.add(currentLA());
            match(TokenType.ID);
            match(TokenType.LPAREN);

            Vector<Expression> args = new Vector<>();
            if(!nextLA(TokenType.RPAREN)) { args = arguments(); }
            match(TokenType.RPAREN);

            return new Invocation(nodeToken(),new Name("length"),new Vector<>(),args);
        }
        return postfixExpression();
    }

    // 62. unary_expression ::= unary_operator cast_expression | factor_expression
    private Expression unaryExpression() {
        if((nextLA(TokenType.BNOT) || nextLA(TokenType.NOT)) && !insideField) {
            tokenStack.add(currentLA());

            UnaryOp uo = unaryOperator();
            Expression e = castExpression();

            return new UnaryExpr(nodeToken(),e,uo);
        }
        return factorExpression();
    }

    // 63. cast_expression ::= scalar_type '(' cast_expression ')' | unary_expression
    private Expression castExpression() {
        if(inScalarTypeFIRST() && !insideField) {
            tokenStack.add(currentLA());
            Type st = scalarType();

            match(TokenType.LPAREN);
            Expression e = castExpression();
            match(TokenType.RPAREN);

            return new CastExpr(nodeToken(),st,e);
        }

        return unaryExpression();
    }

    // 64. power_expression ::= cast_expression ( '**' cast_expression )*
    private Expression powerExpression() {
        tokenStack.add(currentLA());
        Expression left = castExpression();

        if(nextLA(TokenType.EXP) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.EXP)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.EXP);
                match(TokenType.EXP);

                Expression right = castExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 65. multiplication_expression ::= power_expression ( ( '*' | '/' | '%' ) power_expression )*
    private Expression multiplicationExpression() {
        tokenStack.add(currentLA());
        Expression left = powerExpression();

        if(inPowerExpressionFOLLOW() && !insideField) {
            BinaryExpr be;
            while(inPowerExpressionFOLLOW()) {
                BinaryOp bo;

                if(nextLA(TokenType.MULT)) {
                    bo = new BinaryOp(currentLA(),BinaryType.MULT);
                    match(TokenType.MULT);
                }
                else if(nextLA(TokenType.DIV)) {
                    bo = new BinaryOp(currentLA(),BinaryType.DIV);
                    match(TokenType.DIV);
                }
                else {
                    bo = new BinaryOp(currentLA(),BinaryType.MOD);
                    match(TokenType.MOD);
                }

                Expression right = powerExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 66. additive_expression ::= multiplication_expression ( ( '+' | '-' ) multiplication_expression )*
    private Expression additiveExpression() {
        tokenStack.add(currentLA());
        Expression left = multiplicationExpression();

        if((nextLA(TokenType.PLUS) || nextLA(TokenType.MINUS)) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.PLUS) || nextLA(TokenType.MINUS)) {
                BinaryOp bo;

                if(nextLA(TokenType.PLUS)) {
                    bo = new BinaryOp(currentLA(),BinaryType.PLUS);
                    match(TokenType.PLUS);
                }
                else {
                    bo = new BinaryOp(currentLA(),BinaryType.MINUS);
                    match(TokenType.MINUS);
                }

                Expression right = multiplicationExpression();

                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 67. shift_expression ::= additive_expression ( ( '<<' | '>>' ) additive_expression )*
    private Expression shiftExpression() {
        tokenStack.add(currentLA());
        Expression left = additiveExpression();

        if((nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) && (!insideIO || insideParen) && !insideField) {
            BinaryExpr be;
            while (nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) {
                BinaryOp bo;

                if (nextLA(TokenType.SLEFT)) {
                    bo = new BinaryOp(currentLA(), BinaryType.SLEFT);
                    match(TokenType.SLEFT);
                } else {
                    bo = new BinaryOp(currentLA(), BinaryType.SRIGHT);
                    match(TokenType.SRIGHT);
                }

                Expression right = additiveExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 68. relational_expression ::= shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )*
    private Expression relationalExpression() {
        tokenStack.add(currentLA());
        Expression left = shiftExpression();

        if(inShiftExpressionFOLLOW() && !nextLA(TokenType.INC,1) && !insideField) {
            BinaryExpr be;
            while(inShiftExpressionFOLLOW()) {
                BinaryOp bo;

                if(nextLA(TokenType.LT)) {
                    bo = new BinaryOp(currentLA(),BinaryType.LT);
                    match(TokenType.LT);
                }
                else if(nextLA(TokenType.GT)) {
                    bo = new BinaryOp(currentLA(),BinaryType.GT);
                    match(TokenType.GT);
                }
                else if(nextLA(TokenType.LTEQ)) {
                    bo = new BinaryOp(currentLA(),BinaryType.LTEQ);
                    match(TokenType.LTEQ);
                }
                else {
                    bo = new BinaryOp(currentLA(),BinaryType.GTEQ);
                    match(TokenType.GTEQ);
                }

                Expression right = shiftExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 69. instanceof_expression ::= relational_expression ( ( 'instanceof' | '!instanceof' | 'as?' ) relational_expression )*
    private Expression instanceOfExpression() {
        tokenStack.add(currentLA());
        Expression left = relationalExpression();

        if((nextLA(TokenType.INSTANCEOF) || nextLA(TokenType.NINSTANCEOF) || nextLA(TokenType.AS)) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.INSTANCEOF) || nextLA(TokenType.NINSTANCEOF) || nextLA(TokenType.AS)) {
                BinaryOp bo;

                if(nextLA(TokenType.INSTANCEOF)) {
                    bo = new BinaryOp(currentLA(),BinaryType.INSTOF);
                    match(TokenType.INSTANCEOF);
                }
                else if(nextLA(TokenType.NINSTANCEOF)) {
                    bo = new BinaryOp(currentLA(),BinaryType.NINSTOF);
                    match(TokenType.NINSTANCEOF);
                }
                else {
                    bo = new BinaryOp(currentLA(), BinaryType.AS);
                    match(TokenType.AS);
                }

                Expression right = relationalExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 70. equality_expression ::= instanceof_expression ( ( '==' | '!=' ) instanceof_expression )*
    private Expression equalityExpression() {
        tokenStack.add(currentLA());
        Expression left = instanceOfExpression();

        if((nextLA(TokenType.EQEQ) || nextLA(TokenType.NEQ)) && !insideField) {
            BinaryExpr be;
            while (nextLA(TokenType.EQEQ) || nextLA(TokenType.NEQ)) {
                BinaryOp bo;

                if (nextLA(TokenType.EQEQ)) {
                    bo = new BinaryOp(currentLA(), BinaryType.EQEQ);
                    match(TokenType.EQEQ);
                } else {
                    bo = new BinaryOp(currentLA(), BinaryType.NEQ);
                    match(TokenType.NEQ);
                }

                Expression right = instanceOfExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 71. and_expression ::= equality_expression ( '&' equality_expression )*
    private Expression andExpression() {
        tokenStack.add(currentLA());
        Expression left = equalityExpression();

        if(nextLA(TokenType.BAND) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.BAND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BAND);
                match(TokenType.BAND);

                Expression right = equalityExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 72. exclusive_or_expression ::= and_expression ( '^' and_expression )*
    private Expression exclusiveOrExpression() {
        tokenStack.add(currentLA());
        Expression left = andExpression();

        if(nextLA(TokenType.XOR) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.XOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.XOR);
                match(TokenType.XOR);

                Expression right = andExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 73. inclusive_or_expression ::= exclusive_or_expression ( '|' exclusive_or_expression )*
    private Expression inclusiveOrExpression() {
        tokenStack.add(currentLA());
        Expression left = exclusiveOrExpression();

        if(nextLA(TokenType.BOR) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.BOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BOR);
                match(TokenType.BOR);

                Expression right = exclusiveOrExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 74. logical_and_expression ::= inclusive_or_expression ( 'and' inclusive_or_expression )*
    private Expression logicalAndExpression() {
        tokenStack.add(currentLA());
        Expression left = inclusiveOrExpression();

        if(nextLA(TokenType.AND) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.AND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.AND);
                match(TokenType.AND);

                Expression right = inclusiveOrExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 75. logical_or_expression ::= logical_and_expression ( 'or' logical_and_expression )*
    private Expression logicalOrExpression() {
        tokenStack.add(currentLA());
        Expression left = logicalAndExpression();

        if(nextLA(TokenType.OR) && !insideField) {
            BinaryExpr be;
            while(nextLA(TokenType.OR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.OR);
                match(TokenType.OR);

                Expression right = logicalAndExpression();
                be = new BinaryExpr(nodeTokenTop(),left,right,bo);
                left = be;
            }
        }

        nodeToken();
        return left;
    }

    // 76. expression ::= logical_or_expression
    private Expression expression() { return logicalOrExpression(); }

    /*
    ____________________________________________________________
                                LITERALS
    ____________________________________________________________
    */

    // 77. constant ::= object_constant | array_constant | list_constant | scalar_constant
    private Expression constant() {
        if(nextLA(TokenType.NEW)) { return objectConstant(); }
        else if(nextLA(TokenType.ARRAY)) { return arrayConstant(); }
        else if(nextLA(TokenType.LIST)) { return listConstant(); }
        else { return scalarConstant(); }
    }

    // 78. object_constant ::= 'new' ID (type_params)? '(' (object_field ( ',' object_field )* ')'
    private NewExpr objectConstant() {
        tokenStack.add(currentLA());
        match(TokenType.NEW);

        Token nameTok = currentLA();
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> typeArgs = new Vector<>();
        if(nextLA(TokenType.LT))
            typeArgs = typeParams();

        match(TokenType.LPAREN);
        Vector<Var> vars = new Vector<>();
        if(nextLA(TokenType.ID)) {
            vars.add(objectField());
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                vars.add(objectField());
            }
        }
        match(TokenType.RPAREN);

        return new NewExpr(nodeToken(),new ClassType(nameTok,n,typeArgs),vars);
    }

    // 79. object_field ::= ID '=' expression
    private Var objectField() {
        tokenStack.add(currentLA());

        Name n = new Name(currentLA());
        match(TokenType.ID);

        match(TokenType.EQ);
        Expression e = expression();

        return new Var(nodeToken(),n,e);
    }

    // 80. array_constant ::= 'Array' ( '[' expression ']' )* '(' arguments ')'
    private ArrayLiteral arrayConstant() {
        tokenStack.add(currentLA());
        match(TokenType.ARRAY);

        Vector<Expression> exprs = new Vector<>();
        while(nextLA(TokenType.LBRACK)) {
            match(TokenType.LBRACK);
            exprs.add(expression());
            match(TokenType.RBRACK);
        }

        match(TokenType.LPAREN);
        Vector<Expression> args = arguments();
        match(TokenType.RPAREN);

        return new ArrayLiteral(nodeToken(),exprs,args);
    }

    // 81. list_constant ::= 'List' '(' expression (',' expression)* ')'
    private ListLiteral listConstant() {
        tokenStack.add(currentLA());

        match(TokenType.LIST);
        match(TokenType.LPAREN);

        Vector<Expression> exprs = new Vector<>();
        if(inPrimaryExpressionFIRST()) {
            exprs = new Vector<>(expression());
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                exprs.add(expression());
            }
        }
        match(TokenType.RPAREN);

        return new ListLiteral(nodeToken(),exprs);
    }

    // 82. scalar_constant ::= discrete_constant | STRING_LITERAL | TEXT_LITERAL | REAL_LITERAL
    private Literal scalarConstant() {
        if(nextLA(TokenType.STR_LIT)) {
            tokenStack.add(currentLA());
            match(TokenType.STR_LIT);
            return new Literal(nodeToken(), ConstantType.STR);
        }
        else if(nextLA(TokenType.TEXT_LIT)) {
            tokenStack.add(currentLA());
            match(TokenType.TEXT_LIT);
            return new Literal(nodeToken(), ConstantType.TEXT);
        }
        else if(nextLA(TokenType.REAL_LIT) || (nextLA(TokenType.MINUS) && nextLA(TokenType.REAL_LIT,1))) {
            tokenStack.add(currentLA());
            if(nextLA(TokenType.MINUS))
                match(TokenType.MINUS);
            match(TokenType.REAL_LIT);
            return new Literal(nodeToken(), ConstantType.REAL);
        }
        else { return discreteConstant(); }
    }

    // 83. discrete_constant ::= INT_LITERAL | CHAR_LITERAL | BOOL_LITERAL
    private Literal discreteConstant() {
        tokenStack.add(currentLA());

        if(nextLA(TokenType.INT_LIT) || (nextLA(TokenType.MINUS) && nextLA(TokenType.INT_LIT,1))) {
            if(nextLA(TokenType.MINUS))
                match(TokenType.MINUS);
            match(TokenType.INT_LIT);
            return new Literal(nodeToken(), ConstantType.INT);
        }
        else if(nextLA(TokenType.CHAR_LIT)) {
            match(TokenType.CHAR_LIT);
            return new Literal(nodeToken(), ConstantType.CHAR);
        }

        match(TokenType.BOOL_LIT);
        return new Literal(nodeToken(), ConstantType.BOOL);
    }
}
