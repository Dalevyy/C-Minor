package parser;

import ast.*;
import ast.Typeifier.*;
import ast.Modifier.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.expressions.Literal.*;
import ast.operators.*;
import ast.operators.AssignOp.*;
import ast.operators.BinaryOp.*;
import ast.operators.LoopOp.*;
import ast.operators.UnaryOp.*;
import ast.statements.*;
import ast.top_level_decls.*;
import ast.types.*;
import ast.types.ScalarType.*;
import ast.types.DiscreteType.*;
import lexer.*;
import messages.errors.syntax_error.SyntaxErrorFactory;
import token.*;
import utilities.PrettyPrint;

/*
---------------------------------------------------------
                     C Minor Parser
---------------------------------------------------------
*/
public class Parser {

    private final Lexer input;         // Lexer
    private final int k = 3;           // k = # of lookaheads
    private int lookPos;               // Current lookahead position
    private final Token[] lookaheads;  // Array of k lookaheads
    private boolean printToks;         // Flag to print tokens to user
    private boolean interpretMode = false;
    private SyntaxErrorFactory generateSyntaxError;

    public Parser(Lexer input, boolean printTokens) {
        this.input = input;
        this.lookPos = 0;
        this.lookaheads = new Token[k];
        this.printToks = printTokens;
        for(int i = 0; i < k; i++)
            consume();
        this.generateSyntaxError = new SyntaxErrorFactory();
    }

    public Parser(Lexer input, boolean printTokens, boolean interpretMode) {
        this(input,false);
        this.interpretMode = interpretMode;
    }

    private String errorPosition(int start, int end) {
        StringBuilder sb = new StringBuilder();

        for(int i = -2; i < start; i++)
            sb.append(' ');

        for(int i = start; i < end; i++)
            sb.append('^');

        return sb.toString();
    }

    private void consume() {
        lookaheads[lookPos] = this.input.nextToken();
        lookPos = (lookPos + 1) % k;
    }

    private boolean match(TokenType expectedTok) {
        if(nextLA(expectedTok)) {
            if(printToks) System.out.println(currentLA().toString());
            consume();
            return true;
        }
        else {
            System.out.println(PrettyPrint.CYAN + "Syntax Error Detected!" + PrettyPrint.RESET);
            input.printSyntaxError(currentLA().getStartPos());
            System.out.println(PrettyPrint.RED + errorPosition(currentLA().getStartPos().column,currentLA().getEndPos().column) + PrettyPrint.RESET);
            if(interpretMode) { throw new RuntimeException(); }
            else { System.exit(1); }
        }
        return false;
    }

    private void match(TokenType expectedTok, Token t) {
        t.newEndLocation(currentLA().getLocation().end);
        if(nextLA(expectedTok)) {
            if(printToks) System.out.println(currentLA().toString());
            consume();
        }
        else {
            System.out.println(PrettyPrint.CYAN + "Syntax Error Detected!" + PrettyPrint.RESET);
            input.printSyntaxError(currentLA().getStartPos());
            System.out.println(PrettyPrint.RED + errorPosition(currentLA().getStartPos().column,currentLA().getEndPos().column) + PrettyPrint.RESET);
            if(interpretMode) { throw new RuntimeException(); }
            else { System.exit(1); }
        }
    }

    private Token currentLA() { return lookaheads[lookPos%k]; }
    private Token currentLA(int nextPos) { return lookaheads[(lookPos+nextPos)%k]; }

    private boolean nextLA(TokenType expectedTok) {
        return currentLA().getTokenType() == expectedTok;
    }

    private boolean nextLA(TokenType expectedTok, int nextPos) {
        return currentLA(nextPos).getTokenType() == expectedTok;
    }

    /*
    -----------------------------------------
                    FIRST Sets
    -----------------------------------------
    */
    private boolean isInTypeFIRST() {
        return isInScalarTypeFIRST() ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.LIST) ||
               nextLA(TokenType.TUPLE);
    }

    private boolean isInScalarTypeFIRST() {
        return nextLA(TokenType.STRING) ||
                nextLA(TokenType.REAL) ||
                nextLA(TokenType.BOOL) ||
                nextLA(TokenType.INT) ||
                nextLA(TokenType.CHAR);
    }

    private boolean isInDataDeclFIRST() {
        return nextLA(TokenType.PROPERTY) ||
                nextLA(TokenType.PROTECTED) ||
                nextLA(TokenType.PUBLIC);
    }

    private boolean isInStatementFIRST() {
        return isInConstantFIRST() ||
               isInScalarTypeFIRST() ||
               nextLA(TokenType.LBRACK) ||
               nextLA(TokenType.NEW) ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.LPAREN) ||
               nextLA(TokenType.NOT) ||
               nextLA(TokenType.TILDE) ||
               nextLA(TokenType.LBRACE) ||
               nextLA(TokenType.RETURN) ||
               nextLA(TokenType.SET) ||
               nextLA(TokenType.IF) ||
               nextLA(TokenType.WHILE) ||
               nextLA(TokenType.FOR) ||
               nextLA(TokenType.DO) ||
               nextLA(TokenType.CHOICE) ||
               nextLA(TokenType.CIN) ||
               nextLA(TokenType.COUT) ||
               nextLA(TokenType.BREAK) ||
               nextLA(TokenType.CONTINUE) ||
               nextLA(TokenType.STOP);
    }

    private boolean isInPrimaryExpressionFIRST() {
        return isInConstantFIRST() ||
               nextLA(TokenType.ARRAY) ||
               nextLA(TokenType.LIST) ||
               nextLA(TokenType.TUPLE) ||
               nextLA(TokenType.LBRACK) ||
               nextLA(TokenType.LPAREN) ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.SLICE) ||
               nextLA(TokenType.LENGTH) ||
               nextLA(TokenType.CAST) ||
               nextLA(TokenType.BREAK) ||
               nextLA(TokenType.CONTINUE);
    }

    private boolean isInConstantFIRST() {
        return nextLA(TokenType.STR_LIT) ||
                nextLA(TokenType.TEXT_LIT) ||
                nextLA(TokenType.REAL_LIT) ||
                nextLA(TokenType.BOOL_LIT) ||
                nextLA(TokenType.INT_LIT) ||
                nextLA(TokenType.CHAR_LIT) ||
                nextLA(TokenType.ARRAY);
    }

    /*
    -----------------------------------------
                    FOLLOW Sets
    -----------------------------------------
    */

    private boolean isInPrimaryExpressionFOLLOW() {
        return nextLA(TokenType.LBRACK) ||
                nextLA(TokenType.LPAREN) ||
                nextLA(TokenType.ELVIS) ||
                nextLA(TokenType.PERIOD);
    }

    private boolean isInPowerExpressionFOLLOW() {
        return nextLA(TokenType.MULT) ||
                nextLA(TokenType.DIV) ||
                nextLA(TokenType.MOD);
    }

    private boolean isInShiftExpressionFOLLOW() {
        return nextLA(TokenType.LT) ||
               nextLA(TokenType.GT) ||
               nextLA(TokenType.LTEQ) ||
               nextLA(TokenType.GTEQ);
    }

    public AST parseVM() {
        // Parse EnumDecl
        if(nextLA(TokenType.DEF) && nextLA(TokenType.ID,1) &&
                !(nextLA(TokenType.LT,2)||nextLA(TokenType.LPAREN,2) || nextLA(TokenType.COLON,2)))
            return enumType();
        // Parse GlobalDecl
        else if(nextLA(TokenType.DEF) && ((nextLA(TokenType.CONST, 1) || nextLA(TokenType.GLOBAL, 1))))
            return globalVariable();
        // Parse ClassDecl
        else if(nextLA(TokenType.ABSTR) || nextLA(TokenType.FINAL) || nextLA(TokenType.CLASS))
            return classType();
        // Parse FuncDecl
        else if((nextLA(TokenType.DEF)) && (nextLA(TokenType.PURE,1) || nextLA(TokenType.RECURS,1) || ((nextLA(TokenType.ID,1) && nextLA(TokenType.LPAREN,2))
                && ((!nextLA(TokenType.MAIN,1)) && (!nextLA(TokenType.MAIN,2))))))
            return function();
        // Parse LocalDecl
        else if((nextLA(TokenType.DEF) && nextLA(TokenType.ID,1) && nextLA(TokenType.COLON,2))
                || (nextLA(TokenType.DEF) && nextLA(TokenType.LOCAL,1)))
            return declaration();
        // Parse Statement | Expression
        else
            return statement();
    }

    /*
    ------------------------------------------------------------
                          COMPILATION UNIT
    ------------------------------------------------------------
    */

    // 1. compilation ::= file_merge* enum_type* global_variable* class_type* function* main
    public Compilation compilation() {
        Token t = currentLA();

        Vector<EnumDecl> enums = new Vector<EnumDecl>();
        while(nextLA(TokenType.DEF) && nextLA(TokenType.ID,1) && !(nextLA(TokenType.LT,2)||nextLA(TokenType.LPAREN,2)))
            enums.append(enumType());

        Vector<GlobalDecl> globals = new Vector<GlobalDecl>();
        while(nextLA(TokenType.DEF) && (nextLA(TokenType.CONST, 1) || nextLA(TokenType.GLOBAL, 1)))
            globals.merge(globalVariable());

        Vector<ClassDecl> classes = new Vector<ClassDecl>();
        while(nextLA(TokenType.ABSTR) || nextLA(TokenType.FINAL) || nextLA(TokenType.CLASS))
            classes.append(classType());

        Vector<FuncDecl> funcs = new Vector<FuncDecl>();
        while((nextLA(TokenType.DEF)) && !nextLA(TokenType.MAIN,1))
                funcs.append(function());

        MainDecl md = mainFunc();

        if(!nextLA(TokenType.EOF)) {
            System.out.println(PrettyPrint.CYAN + "Syntax Error Detected! Unexpected End of File.");
            System.exit(1);
        }
        else if(printToks) System.out.println(currentLA().toString());

        t.newEndLocation(md.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new Compilation(t,enums,globals,classes,funcs,md);
    }

    /*
    ------------------------------------------------------------
                        COMPILER DIRECTIVES
    ------------------------------------------------------------
    */

    // 2. file-merge ::= '#include' filename choice? rename
    private void fileMerge() {
        Token t = currentLA();

        if(nextLA(TokenType.INCLUDE)) {
            match(TokenType.INCLUDE);
            match(TokenType.STR_LIT);
        }
    }

    /*
    ------------------------------------------------------------
                          ENUM DECLARATION
    ------------------------------------------------------------
    */

    // 3. enum_type ::= 'def' ID type? 'type' '=' '{' enum_field ( ',' , enum_field)* '}'
    private EnumDecl enumType() {
        Token t = currentLA();

        match(TokenType.DEF);
        Name n = new Name(currentLA());
        if(!match(TokenType.ID)) {
            System.out.println("Hi this is an error");
        }

        Type ty = null;
        if(isInTypeFIRST()) ty = type();

        match(TokenType.TYPE);
        match(TokenType.EQ);
        match(TokenType.LBRACE);

        Vector<Var> vars = new Vector<Var>(enumField());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            vars.append(enumField());
        }

        match(TokenType.RBRACE, t);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new EnumDecl(t,n,ty,vars);
    }

    // 4. enum_field ::= ID ( '=' constant )?
    private Var enumField() {
        Token t = currentLA();
        Name n = new Name(t);
        match(TokenType.ID);

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            Expression e = constant();

            t.newEndLocation(e.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
            return new Var(t,n,e);
        }
        return new Var(t,n);
    }

    /*
    ------------------------------------------------------------
              GLOBAL VARIABLES AND VARIABLE DECLARATIONS
    ------------------------------------------------------------
    */

    // 5. global_variable ::= 'def' ( 'const' | 'global' ) variable_decl
    private Vector<GlobalDecl> globalVariable() {
        Token t = currentLA();
        boolean isConstant = false;

        match(TokenType.DEF);
        if(nextLA(TokenType.CONST)) {
            match(TokenType.CONST);
            isConstant = true;
        }
        else match(TokenType.GLOBAL);

        Vector<Var> vars = variableDecl();
        Vector<GlobalDecl> globals = new Vector<GlobalDecl>();

        for(int i = 0; i < vars.size(); i++) {
            Var v = vars.get(i).asVar();
            t.setText(input.getProgramInputForToken(t.getStartPos(),v.location.end));
            globals.append(new GlobalDecl(t,v,v.type(),isConstant));
        }

        return globals;
    }

    // 6. variable_decl ::= variable_decl_list
    private Vector<Var> variableDecl() { return variableDeclList(); }

    // 7. variable_decl_list ::= variable_decl_init ( ',' variable_decl_init )*
    private Vector<Var> variableDeclList() {
        Vector<Var> varList = new Vector<Var>(variableDeclInit());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            varList.append(variableDeclInit());
        }

        return varList;
    }

    // 8. variable_decl_init ::= ID ':' type ( '=' ( expression | 'uninit' ) )?
    private Var variableDeclInit() {
        Token t = currentLA();

        Name n = new Name(t);
        match(TokenType.ID,t);
        match(TokenType.COLON,t);
        Type type = type();

        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ, t);

            Expression e = null;
            if(nextLA(TokenType.UNINIT)) match(TokenType.UNINIT, t);
            else {
                e = expression();
                t.newEndLocation(e.getLocation().end);
            }
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
            return new Var(t,n,type,e);
        }
        return new Var(t,n,type);
    }

    /*
    ------------------------------------------------------------
                                TYPES
    ------------------------------------------------------------
    */

    // 9. type ::= scalar_type
    //           | class_name
    //           | 'List' '[' type ']'
    //           | 'Array' '[' type ']'
    private Type type() {
        if(nextLA(TokenType.ID))
            return className();
        else if(nextLA(TokenType.LIST)) {
            Token t = currentLA();

            match(TokenType.LIST);

            match(TokenType.LBRACK);
            Type ty = type();
            match(TokenType.RBRACK,t);

            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new ListType(t,ty);
        }
        else if(nextLA(TokenType.ARRAY)) {
            Token t = currentLA();

            match(TokenType.ARRAY);

            match(TokenType.LBRACK);
            Type ty = type();
            match(TokenType.RBRACK,t);

            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new ArrayType(t,ty,0);
        }
        return scalarType();
    }

    // 10. scalar_type ::= discrete_type
    //                  | 'String' ( '[' Int_literal ']' )*
    //                  | 'Real'   ( '[' Int_literal ']' )*
    private Type scalarType() {
        Token t = currentLA();
        if(nextLA(TokenType.STRING)) {
            match(TokenType.STRING);
            if(nextLA(TokenType.LBRACK)) {
                ScalarType arrType = new ScalarType(t, Scalars.STR);
                Token arrTok = t.copy();
                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK,arrTok);
                    dims += 1;
                }
               // arrTok.setText(input.getProgramInputForToken(arrTok));
                return new ArrayType(arrTok,arrType,dims);
            }
            return new ScalarType(t,Scalars.STR);
        }
        else if(nextLA(TokenType.REAL)) {
            match(TokenType.REAL);
            if(nextLA(TokenType.LBRACK)) {
                ScalarType arrType = new ScalarType(t, Scalars.REAL);
                Token arrTok = t.copy();
                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK,arrTok);
                    dims += 1;
                }
              //  arrTok.setText(input.getProgramInputForToken(arrTok));
                return new ArrayType(arrTok,arrType,dims);
            }
            return new ScalarType(t,Scalars.REAL);
        }
        return discreteType();
    }

    // 11. discrete_type ::= 'Bool' ( '[' Int_literal ']' )*
    //                     | 'Int'  ( '[' Int_literal ']' )*
    //                     | 'Char' ( '[' Int_literal ']' )*
    private Type discreteType() {
        Token t = currentLA();
        if(nextLA(TokenType.BOOL)) {
            match(TokenType.BOOL);
            if(nextLA(TokenType.LBRACK)) {
                DiscreteType arrType = new DiscreteType(t, Discretes.BOOL);
                Token arrTok = t.copy();
                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK,arrTok);
                    dims += 1;
                }
               // arrTok.setText(input.getProgramInputForToken(arrTok));
                return new ArrayType(arrTok,arrType,dims);
            }
            return new DiscreteType(t, Discretes.BOOL);
        }
        else if(nextLA(TokenType.INT)) {
            match(TokenType.INT);
            if(nextLA(TokenType.LBRACK)) {
                DiscreteType arrType = new DiscreteType(t, Discretes.INT);
                Token arrTok = t.copy();
                int dims = 0;
                while(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    match(TokenType.INT_LIT);
                    match(TokenType.RBRACK,arrTok);
                    dims += 1;
                }
              //  arrTok.setText(input.getProgramInputForToken(arrTok));
                return new ArrayType(arrTok,arrType,dims);
            }
            return new DiscreteType(t, Discretes.INT);
        }
        match(TokenType.CHAR,t);
        if(nextLA(TokenType.LBRACK)) {
            DiscreteType arrType = new DiscreteType(t, Discretes.CHAR);
            Token arrTok = t.copy();
            int dims = 0;
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK,arrTok);
                dims += 1;
            }
            //arrTok.setText(input.getProgramInputForToken(arrTok));
            return new ArrayType(arrTok,arrType,dims);
        }
        return new DiscreteType(t, Discretes.CHAR);
    }

    // 12. class_name ::= ID ( '<' type ( ',' type )* '>' )?
    private ClassType className() {
        Token t = currentLA();

        Name n = new Name(t);
        match(TokenType.ID);

        if(nextLA(TokenType.LT)) {
            match(TokenType.LT,t);

            Vector<Type> typeParams = new Vector<Type>(type());
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                typeParams.append(type());
            }

            match(TokenType.GT,t);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new ClassType(t,n,typeParams);
        }
        return new ClassType(t,n);
    }

    /*
    ------------------------------------------------------------
                          CLASS DECLARATION
    ------------------------------------------------------------
    */

    // 13. class_type ::= ( 'abstr' | 'final' )? 'class' ID type_params? super_class? class_body
    private ClassDecl classType() {
        Token t = currentLA();

        Modifier m = null;
        if(nextLA(TokenType.ABSTR)) {
            m = new Modifier(t,Mods.ABSTR);
            match(TokenType.ABSTR);
        }
        else if(nextLA(TokenType.FINAL)) {
            m = new Modifier(t,Mods.FINAL);
            match(TokenType.FINAL);
        }

        match(TokenType.CLASS);
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Vector<Type> types = null;
        if(nextLA(TokenType.LT)) types = typeParams();

        ClassType ct = null;
        if(nextLA(TokenType.INHERITS)) ct = superClass();

        ClassBody body = classBody();

        t.newEndLocation(body.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new ClassDecl(t,m,n,types,ct,body);
    }

    // 14. type_params ::= '<' type ( ',' type )* '>'
    private Vector<Type> typeParams() {
        Token t = currentLA();

        match(TokenType.LT);

        Vector<Type> types = new Vector<Type>(type());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            types.append(type());
        }
        match(TokenType.GT,t);

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return types;
    }

    // 15. super_class ::= 'inherits' ID type_params?
    private ClassType superClass() {
        match(TokenType.INHERITS);

        Token t = currentLA();
        Name superName = new Name(t);
        match(TokenType.ID);

        Vector<Type> vectorOfTypes = null;
        if(nextLA(TokenType.LT)) {
            vectorOfTypes = typeParams();
            t.setText(input.toString());
        }

        return new ClassType(t,superName,vectorOfTypes);
    }

    // 16. class_body ::= '{' data_decl* method_decl* '}'
    private ClassBody classBody() {
        Token t = currentLA();
        match(TokenType.LBRACE);

        Vector<FieldDecl> dataDecls = new Vector<FieldDecl>();
        while(isInDataDeclFIRST() && nextLA(TokenType.ID,1))
           dataDecls.merge(dataDecl());

        Vector<MethodDecl> methodDecls = new Vector<MethodDecl>();
        while(nextLA(TokenType.PROTECTED) || nextLA(TokenType.PUBLIC))
            methodDecls.append(methodDecl());

        match(TokenType.RBRACE,t);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new ClassBody(t,dataDecls,methodDecls);
    }

    /*
    ------------------------------------------------------------
                          FIELD DECLARATION
    ------------------------------------------------------------
    */

    // 17. data_decl ::= ( 'property' | 'protected' | 'public' ) variable_decl
    private Vector<FieldDecl> dataDecl() {
        Token t = currentLA();

        Modifier m;
        if(nextLA(TokenType.PROPERTY)) {
            match(TokenType.PROPERTY);
            m = new Modifier(t,Mods.PROPERTY);
        }
        else if(nextLA(TokenType.PROTECTED)) {
            match(TokenType.PROTECTED);
            m = new Modifier(t,Mods.PROTECTED);
        }
        else {
            match(TokenType.PUBLIC);
            m = new Modifier(t,Mods.PUBLIC);
        }

        Vector<Var> vars = variableDecl();
        Vector<FieldDecl> fields = new Vector<FieldDecl>();

        for(int i = 0; i < vars.size(); i++) {
            Var v = vars.get(i).asVar();
            t.setText(input.getProgramInputForToken(t.getStartPos(),v.location.end));
            fields.append(new FieldDecl(t,m,v,v.type()));
        }

        return fields;
    }

    /*
    ------------------------------------------------------------
                          METHOD DECLARATION
    ------------------------------------------------------------
    */

    // 18. method_decl ::= method_class | operator_class
    private MethodDecl methodDecl() {
        if(nextLA(TokenType.OPERATOR,1) || nextLA(TokenType.OPERATOR,2))
            return operatorClass();
        return methodClass();
    }

    // 19. method_class ::= method_modifier attribute* 'override'? 'method' method_header '=>' return_type block_statement
    private MethodDecl methodClass() {
        Token t = currentLA();
        Vector<Modifier> mods = new Vector<Modifier>(methodModifier());

        while(nextLA(TokenType.FINAL) || nextLA(TokenType.PURE) || nextLA(TokenType.RECURS))
            mods.append(attribute());

        boolean override = false;
        if(nextLA(TokenType.OVERRIDE)) {
            match(TokenType.OVERRIDE);
            override = true;
        }

        match(TokenType.METHOD);

        Vector<AST> header = methodHeader();
        Name mName = header.get(0).asName();
        Vector<ParamDecl> pd = header.get(1).asVector();
        if(pd == null)
            pd = new Vector<ParamDecl>();

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt bs = blockStatement();

        t.newEndLocation(bs.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new MethodDecl(t,mods,mName,null,pd,rt,bs,override);
    }

    // 20. method_modifier : 'protected' | 'public' ;
    private Modifier methodModifier() {
        Token t = currentLA();
        if(nextLA(TokenType.PROTECTED)) {
            match(TokenType.PROTECTED);
            return new Modifier(t, Mods.PROTECTED);
        }
        match(TokenType.PUBLIC);
        return new Modifier(t, Mods.PUBLIC);
    }

    // 21. attribute ::= 'final' | 'pure' | 'recurs'
    private Modifier attribute() {
        Token t = currentLA();
        if(nextLA(TokenType.FINAL)) {
            match(TokenType.FINAL);
            return new Modifier(t,Mods.FINAL);
        }
        else if(nextLA(TokenType.PURE)) {
            match(TokenType.PURE);
            return new Modifier(t,Mods.PURE);
        }
        match(TokenType.RECURS);
        return new Modifier(t,Mods.RECURS);
    }

    // 22. method-header ::= ID '(' formal-params? ')'
    private Vector<AST> methodHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);

        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = new Vector<ParamDecl>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
                pd = formalParams();
        match(TokenType.RPAREN);

        Vector<AST> header = new Vector<AST>();
        header.append(n);
        header.append(pd);

        return header;
    }

    // 23. formal_params : param_modifier Name:type ( ',' param_modifier Name:type)*
    private Vector<ParamDecl> formalParams() {
        Token t = currentLA();

        Modifier m = paramModifier();

        Name n = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.COLON);
        Type ty = type();

        t.newEndLocation(ty.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        Vector<ParamDecl> pd = new Vector<ParamDecl>();
        pd.append(new ParamDecl(t,m,n,ty));

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            t = currentLA();
            m = paramModifier();

            n = new Name(currentLA());
            match(TokenType.ID);
            match(TokenType.COLON);
            ty = type();

            t.newEndLocation(ty.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            pd.append(new ParamDecl(t,m,n,ty));
        }
        return pd;
    }

    // 24. param_modifier : 'in' | 'out' | 'inout' | 'ref'
    private Modifier paramModifier() {
        Token t = currentLA();
        if(nextLA(TokenType.IN)) {
            match(TokenType.IN);
            return new Modifier(t,Mods.IN);
        }
        else if(nextLA(TokenType.OUT)) {
            match(TokenType.OUT);
            return new Modifier(t,Mods.OUT);
        }
        else if(nextLA(TokenType.INOUT)) {
            match(TokenType.INOUT);
            return new Modifier(t,Mods.INOUT);
        }
        match(TokenType.REF);
        return new Modifier(t,Mods.REF);
    }

    // 25. return-type ::= Void | type
    private Type returnType() {
        if(nextLA(TokenType.VOID)) {
            Token t = currentLA();
            match(TokenType.VOID);
            return new VoidType(t);
        }
        return type();
    }

    // 26. operator_class : operator_modifier 'final'? 'operator' operator_header '=>' return_type block_statement
    private MethodDecl operatorClass() {
        Token t = currentLA();
        Vector<Modifier> mods = new Vector<Modifier>(methodModifier());

        if(nextLA(TokenType.FINAL)) {
            mods.append(new Modifier(currentLA(),Mods.FINAL));
            match(TokenType.FINAL);
        }

        match(TokenType.OPERATOR);
        Vector<AST> header = operatorHeader();
        Operator op = header.get(0).asOperator();
        Vector<ParamDecl> pd = header.get(1).asVector();
        if(pd.size() == 0)
            pd = null;

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt block = blockStatement();

        t.newEndLocation(t.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new MethodDecl(t,mods,null,op,pd,rt,block,false);
    }

    // 27. operator_header ::= operator_symbol '(' formal-params? ')'
    private Vector<AST> operatorHeader() {
        Operator op = operatorSymbol();

        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = new Vector<ParamDecl>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            pd = formalParams();
        match(TokenType.RPAREN);

        Vector<AST> header = new Vector<AST>();
        header.append(op);
        header.append(pd);

        return header;
    }

    // 28. operator_symbol ::= binary_operator | unary_operator
    private Operator operatorSymbol() {
        if(nextLA(TokenType.TILDE) || nextLA(TokenType.NOT))
            return unaryOperator();
        return binaryOperator();
    }

    // 29. binary_operator ::= <= | < | > | >= | == | <> | <=> | <: | :> | + | - | * | / | % | **
    private BinaryOp binaryOperator() {
        Token t = currentLA();
        if(nextLA(TokenType.LTEQ)) {
            match(TokenType.LTEQ);
            return new BinaryOp(t,BinaryType.LTEQ);
        }
        else if(nextLA(TokenType.LT)) {
            match(TokenType.LT);
            return new BinaryOp(t,BinaryType.LT);
        }
        else if(nextLA(TokenType.GT)) {
            match(TokenType.GT);
            return new BinaryOp(t,BinaryType.GT);
        }
        else if(nextLA(TokenType.GTEQ)) {
            match(TokenType.GTEQ);
            return new BinaryOp(t,BinaryType.GTEQ);
        }
        else if(nextLA(TokenType.EQEQ)) {
            match(TokenType.EQEQ);
            return new BinaryOp(t,BinaryType.EQEQ);
        }
        else if(nextLA(TokenType.LTGT)) {
            match(TokenType.LTGT);
            return new BinaryOp(t,BinaryType.LTGT);
        }
        else if(nextLA(TokenType.UFO)) {
            match(TokenType.UFO);
            return new BinaryOp(t,BinaryType.UFO);
        }
        else if(nextLA(TokenType.PLUS)) {
            match(TokenType.PLUS);
            return new BinaryOp(t,BinaryType.PLUS);
        }
        else if(nextLA(TokenType.MINUS)) {
            match(TokenType.MINUS);
            return new BinaryOp(t,BinaryType.MINUS);
        }
        else if(nextLA(TokenType.MULT)) {
            match(TokenType.MULT);
            return new BinaryOp(t,BinaryType.MULT);
        }
        else if(nextLA(TokenType.DIV)) {
            match(TokenType.DIV);
            return new BinaryOp(t,BinaryType.DIV);
        }
        else if(nextLA(TokenType.MOD)) {
            match(TokenType.MOD);
            return new BinaryOp(t,BinaryType.MOD);
        }
        match(TokenType.EXP);
        return new BinaryOp(t,BinaryType.EXP);
    }

    // 30. unary-operator ::= ~ | not
    private UnaryOp unaryOperator() {
        Token t = currentLA();
        if(nextLA(TokenType.TILDE)) {
            match(TokenType.TILDE);
            return new UnaryOp(t,UnaryType.NEGATE);
        }
        match(TokenType.NOT);
        return new UnaryOp(t,UnaryType.NOT);
    }

    /*
    ------------------------------------------------------------
                        FUNCTION DECLARATION
    ------------------------------------------------------------
    */

    // 31. function ::= 'def' ( 'pure' | 'recurs' )? function_header '=>' return_type block_statement
    private FuncDecl function() {
        Token t = currentLA();
        boolean isRecursive = false;
        match(TokenType.DEF);

        Modifier mod = null;
        if(nextLA(TokenType.PURE)) {
            mod = new Modifier(currentLA(),Mods.PURE);
            match(TokenType.PURE);
        }
        else if(nextLA(TokenType.RECURS)) {
            mod = new Modifier(currentLA(),Mods.RECURS);
            match(TokenType.RECURS);
        }

        Vector<AST> header = functionHeader();
        Name n = header.get(0).asName();

        Vector<Typeifier> typefs = header.get(1).asVector();
        Vector<ParamDecl> pd = header.get(2).asVector();

        match(TokenType.ARROW);
        Type ret = returnType();
        BlockStmt b = blockStatement();

        t.newEndLocation(b.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new FuncDecl(t,mod,n,typefs,pd,ret,b);
    }

    // 32. function_header ::= ID function_type_params? '(' formal_params? ')'
    private Vector<AST> functionHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Vector<Typeifier> typefs = new Vector<Typeifier>();
        if(nextLA(TokenType.LT)) typefs = functionTypeParams();

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = new Vector<ParamDecl>();
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();
        match(TokenType.RPAREN);

        Vector<AST> header = new Vector<AST>();
        header.append(n);
        header.append(typefs);
        header.append(params);

        return header;
    }

    // 33. function_type_params ::= '<' typeifier ( ',' typeifier )* '>'
    private Vector<Typeifier> functionTypeParams() {
        match(TokenType.LT);
        Vector<Typeifier> typefs = new Vector<Typeifier>(typeifier());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            typefs.append(typeifier());
        }
        match(TokenType.GT);

        return typefs;
    }

    // 34. typeifier ::= ( 'discr' | 'scalar' | 'class' )? ID
    private Typeifier typeifier() {
        Token t = currentLA();

        Tyfiers tf = null;
        if(nextLA(TokenType.DISCR)) {
            tf = Tyfiers.DISCR;
            match(TokenType.DISCR);
        }
        else if(nextLA(TokenType.SCALAR)) {
            tf = Tyfiers.SCALAR;
            match(TokenType.SCALAR);
        }
        else if(nextLA(TokenType.CLASS)) {
            tf = Tyfiers.CLASS;
            match(TokenType.CLASS);
        }

        Name n = new Name(currentLA());
        match(TokenType.ID, t);

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new Typeifier(t,tf,n);
    }

    /*
    ------------------------------------------------------------
                          MAIN FUNCTION
    ------------------------------------------------------------
    */

    // 35. main ::= 'def' 'main' args? '=>' return_type block_statement
    private MainDecl mainFunc() {
        Token t = currentLA();

        match(TokenType.DEF);
        match(TokenType.MAIN);

        Vector<ParamDecl> args = null;
        if(nextLA(TokenType.LPAREN)) args = args();

        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt b = blockStatement();

        t.newEndLocation(b.location.end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new MainDecl(t,args,rt,b);
    }

    // 36. args ::= '(' formal_params? ')'
    private Vector<ParamDecl> args() {
        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = null;
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            pd = formalParams();
        match(TokenType.RPAREN);
        return pd;
    }

    // 37. block-statement ::= '{' declaration* statement* '}'
    private BlockStmt blockStatement() {
        Token t = currentLA();

        match(TokenType.LBRACE);

        Vector<LocalDecl> vd = new Vector<LocalDecl>();
        while(nextLA(TokenType.DEF)) vd.merge(declaration());

        Vector<Statement> st = new Vector<Statement>();
        while(isInStatementFIRST()) st.append(statement());

        match(TokenType.RBRACE,t);
        t.setText(input.getProgramInputForToken(t.getStartPos(), t.getEndPos()));

        return new BlockStmt(t,vd,st);
    }

    // 38. declaration ::= 'def' 'local'? variable_decl
    private Vector<LocalDecl> declaration() {
        Token t = currentLA();

        match(TokenType.DEF);
        if(nextLA(TokenType.LOCAL)) match(TokenType.LOCAL);

        Vector<Var> vars = variableDecl();
        Vector<LocalDecl> locals = new Vector<LocalDecl>();

        for(int i = 0; i < vars.size(); i++) {
            Var v = vars.get(i).asVar();
            t.setText(input.getProgramInputForToken(t.getStartPos(),v.location.end));
            locals.append(new LocalDecl(t ,v, v.type()));
        }

        return locals;
    }

    /*
    ------------------------------------------------------------
                             STATEMENTS
    ------------------------------------------------------------
    */

    /*
       39. statement ::= 'stop'
                       | return_statement
                       | assignment_statement
                       | block_statement
                       | if_statement
                       | while_statement
                       | do_while_statement
                       | for_statement
                       | choice_statement
     */
    private Statement statement() {
        if(nextLA(TokenType.STOP)) {
            Token t = currentLA();
            match(TokenType.STOP);
            return new StopStmt(t);
        }
        else if(nextLA(TokenType.RETURN)) return returnStatement();
        else if(nextLA(TokenType.LBRACE)) return blockStatement();
        else if(nextLA(TokenType.IF)) return ifStatement();
        else if(nextLA(TokenType.WHILE)) return whileStatement();
        else if(nextLA(TokenType.DO)) return doWhileStatement();
        else if(nextLA(TokenType.FOR)) return forStatement();
        else if(nextLA(TokenType.CHOICE)) return choiceStatement();
        return assignmentStatement();
    }

    // 40. return_statement ::= expression?
    private ReturnStmt returnStatement() {
        Token t = currentLA();
        match(TokenType.RETURN);
        if(isInPrimaryExpressionFIRST()) {
            Expression e = expression();

            t.newEndLocation(e.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
            return new ReturnStmt(t,e);
        }
        return new ReturnStmt(t,null);
    }

    // 41. assignment_statement ::= 'set' expression assignment_operator expression
    //                            |  logical_or_expression
    private Statement assignmentStatement() {
        Token t = currentLA();

        if(nextLA(TokenType.SET)) {
            match(TokenType.SET);

            Expression LHS = expression();
            AssignOp op = assignmentOperator();
            Expression RHS = expression();

            t.newEndLocation(RHS.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new AssignStmt(t,LHS,RHS,op);
        }
        Expression e = logicalOrExpression();

        t.newEndLocation(e.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new ExprStmt(t,e);
    }

    // 42. assignment_operator ::= '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**='
    private AssignOp assignmentOperator() {
        Token t = currentLA();
        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            return new AssignOp(t,AssignType.EQ);
        }
        else if(nextLA(TokenType.PLUSEQ)) {
            match(TokenType.PLUSEQ);
            return new AssignOp(t,AssignType.PLUSEQ);
        }
        else if(nextLA(TokenType.MINUSEQ)) {
            match(TokenType.MINUSEQ);
            return new AssignOp(t,AssignType.MINUSEQ);
        }
        else if(nextLA(TokenType.MULTEQ)) {
            match(TokenType.MULTEQ);
            return new AssignOp(t,AssignType.MULTEQ);
        }
        else if(nextLA(TokenType.DIVEQ)) {
            match(TokenType.DIVEQ);
            return new AssignOp(t,AssignType.DIVEQ);
        }
        else if(nextLA(TokenType.MODEQ)) {
            match(TokenType.MODEQ);
            return new AssignOp(t,AssignType.MODEQ);
        }
        match(TokenType.EXPEQ);
        return new AssignOp(t,AssignType.EXPEQ);
    }

    // 43. if_statement ::= if expression block_statement ( elseif_statement )* ( 'else' block_statement)?
    private IfStmt ifStatement() {
        Token t = currentLA();

        match(TokenType.IF);
        Expression e = expression();

        BlockStmt b = blockStatement();

        Vector<IfStmt> elifStmts = new Vector<IfStmt>();
        while(nextLA(TokenType.ELSE) && nextLA(TokenType.IF,1)) {
            IfStmt eIf = elseIfStatement();
            t.newEndLocation(eIf.getLocation().end);
            elifStmts.append(eIf);
        }

        BlockStmt elseBlock = null;
        if(nextLA(TokenType.ELSE)) {
            match(TokenType.ELSE);
            elseBlock = blockStatement();

            t.newEndLocation(elseBlock.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
            return new IfStmt(t,e,b,elifStmts,elseBlock);
        }

        if(elifStmts.size() <= 0)
            t.newEndLocation(b.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new IfStmt(t,e,b,elifStmts,elseBlock);
    }

    // 44. elseif_statement ::= 'else' 'if' expression block_statement
    private IfStmt elseIfStatement() {
        Token t = currentLA();

        match(TokenType.ELSE);
        match(TokenType.IF);

        Expression e = expression();
        BlockStmt b = blockStatement();

        t.newEndLocation(t.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new IfStmt(t,e,b);
    }

    // 45. while_statement ::= 'while' expression ( 'next' assignmentStatement )? block_statement
    private WhileStmt whileStatement() {
        Token t = currentLA();

        match(TokenType.WHILE);
        Expression e = expression();

        Statement nextE = null;
        if(nextLA(TokenType.NEXT)) {
            match(TokenType.NEXT);
            nextE = assignmentStatement();
        }
        BlockStmt b = blockStatement();

        t.newEndLocation(b.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new WhileStmt(t,e,nextE,b);
    }

    // 46. do_while_statement ::= 'do' block_statement ( 'next' assignmentStatement )? 'while' expression
    private DoStmt doWhileStatement() {
        Token t = currentLA();

        match(TokenType.DO);
        BlockStmt b = blockStatement();

        Statement nextE = null;
        if(nextLA(TokenType.NEXT)) {
            match(TokenType.NEXT);
            nextE = assignmentStatement();
        }

        match(TokenType.WHILE);
        Expression e = expression();

        t.newEndLocation(e.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new DoStmt(t,b,nextE,e);
    }

    // for_statement : 'for' '(' range_iterator | array_iterator')' block_statement
    private ForStmt forStatement() {
        Token t = currentLA();

        match(TokenType.FOR);
        match(TokenType.LPAREN);

        Vector<AST> forCondition = rangeIterator();
        Var forVar = forCondition.get(0).asVar();
        Expression LHS = forCondition.get(1).asExpression();
        Expression RHS = forCondition.get(3).asExpression();
        LoopOp loopOp = forCondition.get(2).asOperator().asLoopOp();

        match(TokenType.RPAREN);
        BlockStmt b = blockStatement();

        t.newEndLocation(b.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new ForStmt(t,forVar,LHS,RHS,loopOp,b);
    }

    // range_iterator : 'def' Name 'in' expression range_operator expression ;
    private Vector<AST> rangeIterator() {
        Token t = currentLA();
        Vector<AST> forComponents = new Vector<AST>();

        match(TokenType.DEF);
        forComponents.append(variableDeclInit());
        match(TokenType.IN);

        forComponents.append(expression());
        forComponents.append(rangeOperator());
        forComponents.append(expression());

        return forComponents;
    }

    // range_operator : inclusive | exclusive_right | exclusive_left | exclusive ;
    private LoopOp rangeOperator() {
        if(nextLA(TokenType.INC) && nextLA(TokenType.LT,1)) { return exclusiveRight(); }
        else if(nextLA(TokenType.INC)) { return inclusive(); }
        else if(nextLA(TokenType.LT) && nextLA(TokenType.INC,1) && nextLA(TokenType.LT,2)) { return exclusive(); }
        else { return exclusiveLeft(); }
    }

    // inclusive : '..' ;
    private LoopOp inclusive() {
        Token t = currentLA();

        match(TokenType.INC);

        return new LoopOp(t,LoopType.EXCL_L);
    }

    // exclusive_right : '..<' ;
    private LoopOp exclusiveRight() {
        Token t = currentLA();

        match(TokenType.INC);
        match(TokenType.LT,t);

        return new LoopOp(t,LoopType.EXCL_R);

    }

    // exclusive_left : '<..' ;
    private LoopOp exclusiveLeft() {
        Token t = currentLA();

        match(TokenType.LT);
        match(TokenType.INC,t);

        return new LoopOp(t,LoopType.EXCL_L);
    }

    // exclusive : '<..<'
    private LoopOp exclusive() {
        Token t = currentLA();

        match(TokenType.LT);
        match(TokenType.INC);
        match(TokenType.LT,t);

        return new LoopOp(t,LoopType.EXCL);
    }

    // 48. choice_statement ::= 'choice' expression '{' case_statement* 'other' block_statement '}'
    private ChoiceStmt choiceStatement() {
        Token t = currentLA();

        match(TokenType.CHOICE);
        Expression e = expression();

        match(TokenType.LBRACE);
        Vector<CaseStmt> cStmts = new Vector<CaseStmt>();
        while(nextLA(TokenType.ON)) cStmts.append(caseStatement());

        match(TokenType.OTHER);
        BlockStmt b = blockStatement();
        match(TokenType.RBRACE,t);

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new ChoiceStmt(t,e,cStmts,b);
    }

    // 49. case_statement ::= 'on' label block_statement
    private CaseStmt caseStatement() {
        Token t = currentLA();

        match(TokenType.ON);
        Label l = label();

        BlockStmt b = blockStatement();

        t.newEndLocation(b.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new CaseStmt(t,l,b);
    }

    // 50. label ::= ScalarConstant ('..' ScalarConstant)?
    private Label label() {
        Token t = currentLA();
        Literal lConstant = scalarConstant();

        if(nextLA(TokenType.INC)) {
            match(TokenType.INC);
            Literal rConstant = scalarConstant();

            t.newEndLocation(rConstant.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new Label(t,lConstant,rConstant);
        }
        t.newEndLocation(lConstant.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new Label(t,lConstant);
    }

    // 51. input_statement ::= 'cin' ( '>>' expression )+
    private InStmt inputStatement() {
        Token t = currentLA();

        match(TokenType.CIN);
        match(TokenType.SRIGHT);
        Vector<Expression> inputExprs = new Vector<Expression>(expression());

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new InStmt(t,inputExprs);
    }

    // 52. output_statement ::= 'cout' ( '<<' expression )+
    private OutStmt outputStatement() {
        Token t = currentLA();

        match(TokenType.COUT);
        match(TokenType.SLEFT);
        Vector<Expression> outputExprs = new Vector<Expression>(expression());

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new OutStmt(t,outputExprs);
    }

    /*
    ------------------------------------------------------------
                            EXPRESSIONS
    ------------------------------------------------------------
    */

    // 53. primary_expression ::= ID | constant | '(' expression ')' | input_statement | output_statement
    private Expression primaryExpression() {
        Token t = currentLA();
        if(nextLA(TokenType.LPAREN)) {
            match(TokenType.LPAREN);
            Expression e = expression();
            match(TokenType.RPAREN,t);

            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
            return e;
        }
        else if(nextLA(TokenType.ID)) {
            Name n = new Name(t);
            match(TokenType.ID);

            return new NameExpr(t,n);
        }
        else if(nextLA(TokenType.CIN)) return inputStatement();
        else if(nextLA(TokenType.COUT)) return outputStatement();
        else if(nextLA(TokenType.BREAK)) {
            match(TokenType.BREAK);
            return new BreakStmt(t);
        }
        else if(nextLA(TokenType.CONTINUE)) {
            match(TokenType.CONTINUE);
            return new ContinueStmt(t);
        }
        else if(nextLA(TokenType.ENDL)) {
            match(TokenType.ENDL);
            return new Endl(t);
        }
        return constant();
    }

    // 54. postfix_expression ::= primary_expression ( '[' expression ']'
    //                                               | '(' arguments? ')'
    //                                               | ( '.' | '?.' ) expression )*
    private Expression postfixExpression() {
        Token t = currentLA();
        Expression primary = primaryExpression();

        if(isInPrimaryExpressionFOLLOW()) {
            Expression mainExpr = null, e = null;
            while(isInPrimaryExpressionFOLLOW()) {
                if(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    Expression index = expression();
                    match(TokenType.RBRACK);
                    e = new ArrayExpr(t,primary,index);
                }
                else if(nextLA(TokenType.LPAREN)) {
                    match(TokenType.LPAREN);
                    Vector<Expression> args = new Vector<Expression>();
                    if(!nextLA(TokenType.RPAREN))
                        args = arguments();
                    match(TokenType.RPAREN,t);
                    t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
                    e = new Invocation(t,primary.asExpression().asNameExpr().getName(),args);
                }
                else {
                    if(nextLA(TokenType.ELVIS)) match(TokenType.ELVIS);
                    else match(TokenType.PERIOD);
                    Expression e1 = expression();
                    if(e1.isInvocation()) {
                        e = new Invocation(t,primary.asExpression(),e1.asInvocation().name(),e1.asInvocation().arguments());
                        e1 = null;
                    }
                    else {
                        t.newEndLocation(e1.getLocation().end);
                        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
                        e = new FieldExpr(t,primary.asExpression(),e1.asExpression(),false);
                    }
                }
                mainExpr = e;
            }
            t.newEndLocation(mainExpr.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return mainExpr;
        }

        t.newEndLocation(primary.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return primary;
    }

    // 55. arguments ::= expression ( ',' expression )*
    private Vector<Expression> arguments() {
        Vector<Expression> ex = new Vector<Expression>();
        ex.append(expression());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            ex.append(expression());
        }
        return ex;
    }

    // 56. unary_expression ::= unary_operator cast_expression | postfix_expression
    private Expression unaryExpression() {
        if(nextLA(TokenType.TILDE) || nextLA(TokenType.NOT)) {
            Token t = currentLA();

            UnaryOp uo = unaryOperator();
            Expression e = castExpression();

            t.newEndLocation(e.getLocation().end);
            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new UnaryExpr(t,e,uo);
        }
        return postfixExpression();
    }

    // 57. cast_expression ::= scalar_type '(' cast_expression ')' | unary_expression
    private Expression castExpression() {
        Token t = currentLA();
        if(isInScalarTypeFIRST()) {
            Type st = scalarType();

            match(TokenType.LPAREN);
            Expression e = castExpression();
            match(TokenType.RPAREN,t);

            t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

            return new CastExpr(t,st,e);
        }
        return unaryExpression();
    }

    // 58. power_expression ::= cast_expression ( '**' cast_expression )*
    private Expression powerExpression() {
        Token t = currentLA();
        Expression left = castExpression();

        if(nextLA(TokenType.EXP)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.EXP)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.EXP);
                match(TokenType.EXP);

                Expression right = castExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);

                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 59. multiplication_expression ::= power_expression ( ( '*' | '/' | '%' ) power_expression )*
    private Expression multiplicationExpression() {
        Token t = currentLA();
        Expression left = powerExpression();

        if(isInPowerExpressionFOLLOW()) {
            BinaryExpr mainBE = null, be = null;
            while(isInPowerExpressionFOLLOW()) {
                BinaryOp bo = null;

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
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 60. additive_expression ::= multiplication_expression ( ( '+' | '-' ) multiplication_expression )*
    private Expression additiveExpression() {
        Token t = currentLA();
        Expression left = multiplicationExpression();

        if(nextLA(TokenType.PLUS) || nextLA(TokenType.MINUS)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.PLUS) || nextLA(TokenType.MINUS)) {
                BinaryOp bo = null;
                if(nextLA(TokenType.PLUS)) {
                    bo = new BinaryOp(currentLA(),BinaryType.PLUS);
                    match(TokenType.PLUS);
                }
                else if(nextLA(TokenType.MINUS)) {
                    bo = new BinaryOp(currentLA(),BinaryType.MINUS);
                    match(TokenType.MINUS);
                }

                Expression right = multiplicationExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 61. shift_expression ::= additive_expression ( ( '<<' | '>>' ) additive_expression )*
    private Expression shiftExpression() {
        Token t = currentLA();
        Expression left = additiveExpression();

        if(nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) {
            BinaryExpr mainBE = null, be = null;
            while (nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT)) {
                BinaryOp bo = null;

                if (nextLA(TokenType.SLEFT)) {
                    bo = new BinaryOp(currentLA(), BinaryType.SLEFT);
                    match(TokenType.SLEFT);
                } else if (nextLA(TokenType.SRIGHT)) {
                    bo = new BinaryOp(currentLA(), BinaryType.SRIGHT);
                    match(TokenType.SRIGHT);
                }

                Expression right = additiveExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if (mainBE != null)
                    be = new BinaryExpr(t, mainBE, right, bo);
                else
                    be = new BinaryExpr(t, left, right, bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 62. relational_expression ::= shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )*
    private Expression relationalExpression() {
        Token t = currentLA();
        Expression left = shiftExpression();

        if(isInShiftExpressionFOLLOW()) {
            BinaryExpr mainBE = null, be = null;
            while(isInShiftExpressionFOLLOW()) {
                BinaryOp bo = null;
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
                else if(nextLA(TokenType.GTEQ)) {
                    bo = new BinaryOp(currentLA(),BinaryType.GTEQ);
                    match(TokenType.GTEQ);
                }
                Expression right = shiftExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 63. instanceof_expression ::= relational_expression ( ( 'instanceof' | '!instanceof' | 'as?' ) relational_expression )*
    private Expression instanceOfExpression() {
        Token t = currentLA();
        Expression left = relationalExpression();

        if(nextLA(TokenType.INSTANCEOF) || nextLA(TokenType.NINSTANCEOF) || nextLA(TokenType.AS)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.INSTANCEOF) || nextLA(TokenType.NINSTANCEOF) || nextLA(TokenType.AS)) {
                BinaryOp bo = null;
                if(nextLA(TokenType.INSTANCEOF)) {
                    bo = new BinaryOp(currentLA(),BinaryType.INOF);
                    match(TokenType.INSTANCEOF);
                }
                else if(nextLA(TokenType.NINSTANCEOF)) {
                    bo = new BinaryOp(currentLA(),BinaryType.NINOF);
                    match(TokenType.NINSTANCEOF);
                }
                else if(nextLA(TokenType.AS)) {
                    bo = new BinaryOp(currentLA(), BinaryType.AS);
                    match(TokenType.AS);
                }

                Expression right = relationalExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 64. equality_expression ::= instanceof_expression ( ( '==' | '!=' ) instanceof_expression )*
    private Expression equalityExpression() {
        Token t = currentLA();
        Expression left = instanceOfExpression();

        if(nextLA(TokenType.EQEQ) || nextLA(TokenType.NEQ)) {
            BinaryExpr mainBE = null, be = null;
            while (nextLA(TokenType.EQEQ) || nextLA(TokenType.NEQ)) {
                BinaryOp bo = null;
                if (nextLA(TokenType.EQEQ)) {
                    bo = new BinaryOp(currentLA(), BinaryType.EQEQ);
                    match(TokenType.EQEQ);
                } else if (nextLA(TokenType.NEQ)) {
                    bo = new BinaryOp(currentLA(), BinaryType.NEQ);
                    match(TokenType.NEQ);
                }

                Expression right = instanceOfExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if (mainBE != null)
                    be = new BinaryExpr(t, mainBE, right, bo);
                else
                    be = new BinaryExpr(t, left, right, bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 65. and_expression ::= equality_expression ( '&' equality_expression )*
    private Expression andExpression() {
        Token t = currentLA();
        Expression left = equalityExpression();

        if(nextLA(TokenType.BAND)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.BAND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BAND);
                match(TokenType.BAND);

                Expression right = equalityExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 66. exclusive_or_expression ::= and_expression ( '^' and_expression )*
    private Expression exclusiveOrExpression() {
        Token t = currentLA();
        Expression left = andExpression();

        if(nextLA(TokenType.XOR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.XOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.XOR);
                match(TokenType.XOR);

                Expression right = andExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 67. inclusive_or_expression ::= exclusive_or_expression ( '|' exclusive_or_expression )*
    private Expression inclusiveOrExpression() {
        Token t = currentLA();
        Expression left = exclusiveOrExpression();

        if(nextLA(TokenType.BOR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.BOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BOR);
                match(TokenType.BOR);

                Expression right = exclusiveOrExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 68. logical_and_expression ::= inclusive_or_expression ( 'and' inclusive_or_expression )*
    private Expression logicalAndExpression() {
        Token t = currentLA();
        Expression left = inclusiveOrExpression();

        if(nextLA(TokenType.AND)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.AND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.AND);
                match(TokenType.AND);

                Expression right = inclusiveOrExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 69. logical_or_expression ::= logical_and_expression ( 'or' logical_and_expression )*
    private Expression logicalOrExpression() {
        Token t = currentLA();
        Expression left = logicalAndExpression();

        if(nextLA(TokenType.OR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.OR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.OR);
                match(TokenType.OR);

                Expression right = logicalAndExpression();
                t.newEndLocation(right.getLocation().end);
                t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

                if(mainBE != null)
                    be = new BinaryExpr(t,mainBE,right,bo);
                else
                    be = new BinaryExpr(t,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 70. expression ::= logical_or_expression
    private Expression expression() { return logicalOrExpression(); }

    /*
    ------------------------------------------------------------
                                LITERALS
    ------------------------------------------------------------
    */

    // 71. constant ::= object_constant | array_constant | list_constant | scalar_constant
    private Expression constant() {
        if(nextLA(TokenType.NEW)) return objectConstant();
        else if(nextLA(TokenType.ARRAY)) return arrayConstant();
        else if(nextLA(TokenType.LIST)) return listConstant();
        return scalarConstant();
    }

    // 72. object_constant ::= 'new' ID '(' (object_field ( ',' object_field )* ')'
    private NewExpr objectConstant() {
        Token t = currentLA();

        match(TokenType.NEW);

        Token nameTok = currentLA();
        Name n = new Name(nameTok);
        match(TokenType.ID);

        match(TokenType.LPAREN);
        Vector<Var> vars = new Vector<Var>();
        if(nextLA(TokenType.ID)) {
            vars.append(objectField());
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                vars.append(objectField());
            }
        }

        match(TokenType.RPAREN,t);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new NewExpr(t,new ClassType(nameTok,n),vars);
    }

    // 73. object_field ::= ID '=' expression
    private Var objectField() {
        Token t = currentLA();

        Name n = new Name(t);
        match(TokenType.ID);

        match(TokenType.EQ);
        Expression e = expression();

        t.newEndLocation(e.getLocation().end);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new Var(t,n,e);
    }

    // 74. array_constant ::= 'Array' ( '[' expression ']' )* '(' arguments ')'
    private ArrayLiteral arrayConstant() {
        Token t = currentLA();
        match(TokenType.ARRAY);

        Vector<Expression> exprs = new Vector<Expression>();
        while(nextLA(TokenType.LBRACK)) {
            match(TokenType.LBRACK);
            exprs.append(expression());
            match(TokenType.RBRACK);
        }

        match(TokenType.LPAREN);
        Vector<Expression> args = arguments();
        match(TokenType.RPAREN, t);

        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));
        return new ArrayLiteral(t,exprs,args);
    }

    // 75. list_constant ::= 'List' '(' expression (',' expression)* ')'
    private ListLiteral listConstant() {
        Token t = currentLA();

        match(TokenType.LIST);
        match(TokenType.LPAREN);

        Vector<Expression> exprs = null;
        if(isInPrimaryExpressionFIRST()) {
            exprs = new Vector<Expression>(expression());
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                exprs.append(expression());
            }
        }

        match(TokenType.RPAREN,t);
        t.setText(input.getProgramInputForToken(t.getStartPos(),t.getEndPos()));

        return new ListLiteral(t,exprs);
    }

    // 76. scalar_constant ::= discrete_constant | STRING_LITERAL | TEXT_LITERAL | REAL_LITERAL
    private Literal scalarConstant() {
        Token t = currentLA();

        if(nextLA(TokenType.STR_LIT)) {
            match(TokenType.STR_LIT);
            return new Literal(t, ConstantKind.STR);
        }
        else if(nextLA(TokenType.TEXT_LIT)) {
            match(TokenType.TEXT_LIT);
            return new Literal(t, ConstantKind.TEXT);
        }
        else if(nextLA(TokenType.REAL_LIT)) {
            match(TokenType.REAL_LIT);
            return new Literal(t, ConstantKind.REAL);
        }
        return discreteConstant();
    }

    // 77. discrete_constant ::= INT_LITERAL | CHAR_LITERAL | BOOL_LITERAL
    private Literal discreteConstant() {
        Token t = currentLA();

        if(nextLA(TokenType.INT_LIT)) {
            match(TokenType.INT_LIT);
            return new Literal(t, ConstantKind.INT);
        }
        else if(nextLA(TokenType.CHAR_LIT)) {
            match(TokenType.CHAR_LIT);
            return new Literal(t, ConstantKind.CHAR);
        }

        match(TokenType.BOOL_LIT);
        return new Literal(t, ConstantKind.BOOL);
    }
}
