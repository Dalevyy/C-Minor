package Parser;

import AST.*;
import AST.Modifier.*;
import AST.ClassBody.*;
import AST.Expressions.*;
import AST.Expressions.Literal.*;
import AST.Operators.*;
import AST.Operators.AssignOp.*;
import AST.Operators.BinaryOp.*;
import AST.Operators.UnaryOp.*;
import AST.Statements.*;
import AST.TopLevelDecls.*;
import AST.Types.*;
import AST.Types.ScalarType.*;
import AST.Types.DiscreteType.*;
import Lexer.*;
import Token.*;

import java.util.ArrayList;

public class Parser {
    private final Lexer input;
    private final int k;              // k = # of lookaheads
    private int lookPos;
    private final Token[] lookaheads;
    private boolean ioFlag;

    public Parser(Lexer input) {
        this.input = input;
        this.k = 3;
        this.lookPos = 0;
        this.lookaheads = new Token[k];
        this.ioFlag = false;
        for(int i = 0; i < k; i++)
            consume();
    }

    private void consume() {
        lookaheads[lookPos] = this.input.nextToken();
        lookPos = (lookPos + 1) % k;
    }

    private void match(TokenType expectedTok) {
        System.out.println(currentLA(0).toString());
        if(nextLA(expectedTok)) consume();
        else throw new IllegalArgumentException("\nWarning! Expected " + expectedTok +
                                                ", but got " + currentLA(0).getTokenType());
    }

    private void match(TokenType expectedTok, Token t) {
        if(nextLA(expectedTok)) {
            if(t != currentLA())
                t.appendText(" " + currentLA().getText());
            t.newEndLocation(currentLA().getLocation());
            consume();
        }
        else throw new IllegalArgumentException("\nWarning! Expected " + expectedTok +
                                                ", but got " + currentLA(0).getTokenType());
    }

    // currentLA : Returns the current token we are checking
    private Token currentLA() { return lookaheads[lookPos%k]; }
    private Token currentLA(int nextPos) { return lookaheads[(lookPos+nextPos)%k]; }

    // nextLA : Returns the next LA at the specified index
    private boolean nextLA(TokenType expectedTok) { return currentLA(0).getTokenType() == expectedTok; }
    private boolean nextLA(TokenType expectedTok, int nextPos) { return currentLA(nextPos).getTokenType() == expectedTok; }

    private boolean isEnumFIRST() {
        return isScalarTypeFIRST() ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.LIST) ||
               nextLA(TokenType.TUPLE);
    }

    private boolean isScalarTypeFIRST() {
        return nextLA(TokenType.STRING) ||
                nextLA(TokenType.REAL) ||
                nextLA(TokenType.BOOL) ||
                nextLA(TokenType.INT) ||
                nextLA(TokenType.CHAR);
    }

    private boolean isDataDeclFIRST() {
        return nextLA(TokenType.PROPERTY) ||
                nextLA(TokenType.PROTECTED) ||
                nextLA(TokenType.PUBLIC);
    }

    private boolean isStatFIRST() {
        return isConstant() ||
               isScalarTypeFIRST() ||
               nextLA(TokenType.LBRACK) ||
               nextLA(TokenType.NEW) ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.LPAREN) ||
               nextLA(TokenType.NOT) ||
               nextLA(TokenType.TILDE) ||
               nextLA(TokenType.LBRACK) ||
               nextLA(TokenType.RETURN) ||
               nextLA(TokenType.SET) ||
               nextLA(TokenType.IF) ||
               nextLA(TokenType.WHILE) ||
               nextLA(TokenType.FOR) ||
               nextLA(TokenType.DO) ||
               nextLA(TokenType.CHOICE) ||
               nextLA(TokenType.CIN) ||
               nextLA(TokenType.COUT) ||
               nextLA(TokenType.STOP);
    }

    private boolean isPrimExprFIRST() {
        return isConstant() ||
               nextLA(TokenType.ARRAY) ||
               nextLA(TokenType.LIST) ||
               nextLA(TokenType.TUPLE) ||
               nextLA(TokenType.LBRACK) ||
               nextLA(TokenType.LPAREN) ||
               nextLA(TokenType.ID) ||
               nextLA(TokenType.SLICE) ||
               nextLA(TokenType.LENGTH) ||
               nextLA(TokenType.CAST);
    }

    private boolean isAfterPrimExprFIRST() {
        return nextLA(TokenType.LBRACK) ||
                nextLA(TokenType.LPAREN) ||
                nextLA(TokenType.ELVIS) ||
                nextLA(TokenType.PERIOD);
    }

    private boolean isAfterPowerFIRST() {
        return nextLA(TokenType.MULT) ||
                nextLA(TokenType.DIV) ||
                nextLA(TokenType.MOD);
    }

    private boolean isAfterAddFIRST() {
        return nextLA(TokenType.LT) ||
               nextLA(TokenType.GT) ||
               nextLA(TokenType.LTEQ) ||
               nextLA(TokenType.GTEQ);
    }

    private boolean isConstant() {
        return nextLA(TokenType.STR_LIT) ||
                nextLA(TokenType.TEXT_LIT) ||
                nextLA(TokenType.REAL_LIT) ||
                nextLA(TokenType.BOOL_LIT) ||
                nextLA(TokenType.INT_LIT) ||
                nextLA(TokenType.CHAR_LIT);
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

        if(!nextLA(TokenType.EOF))
            System.out.println("EOF Not Reached.");
        return new Compilation(t,enums,globals,classes,funcs,md);
    }

    /*
    ------------------------------------------------------------
                          ENUM DECLARATION
    ------------------------------------------------------------
    */

    // 2. enum_type ::= 'def' ID type? 'type' '=' '{' enum_field ( ',' , enum_field)* '}'
    private EnumDecl enumType() {
        Token t = currentLA();
        match(TokenType.DEF);
        Name n = new Name(currentLA());
        match(TokenType.ID);

        Type ty = null;
        if(isEnumFIRST()) ty = type();

        match(TokenType.TYPE);
        match(TokenType.EQ);
        match(TokenType.LBRACE);

        Vector<Var> vars = new Vector<Var>(enumField());

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            vars.append(enumField());
        }
        match(TokenType.RBRACE);
        return new EnumDecl(t,n,ty,vars);
    }

    // 3. enum_field ::= 'ID' ( '=' constant )?
    private Var enumField() {
        Token t = currentLA();
        Name n = new Name(t);
        match(TokenType.ID);
        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ,t);
            Expression e = constant();
            return new Var(t,n,e);
        }
        return new Var(t,n);
    }

    /*
    ------------------------------------------------------------
              GLOBAL VARIABLES AND VARIABLE DECLARATIONS
    ------------------------------------------------------------
    */

    // 10. global_variable ::= 'def' ( 'const' | 'global' ) variable_decl
    private Vector<GlobalDecl> globalVariable() {
        Token startTok = currentLA();
        match(TokenType.DEF);
        if(nextLA(TokenType.CONST)) match(TokenType.CONST);
        else match(TokenType.GLOBAL);
        ArrayList<AST> varDecls = variableDecl();
        Vector<Var> vars = (Vector<Var>) varDecls.get(0);
        Type varType = (Type) varDecls.get(1);
        Vector<GlobalDecl> globals = new Vector<GlobalDecl>();
        for(int i = 0; i < vars.size(); i++)
            globals.append(new GlobalDecl(startTok,vars.get(i).asVar(), varType));
        return globals;
    }

    // 11. variable_decl ::= variable_decl_list
    private ArrayList<AST> variableDecl() { return variableDeclList(); }

    // 12. variable_decl_list ::= variable_decl_init ( ',' variable_decl_init )* type
    private ArrayList<AST> variableDeclList() {
        Vector<Var> v = new Vector<Var>(variableDeclInit());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            v.append(variableDeclInit());
        }
        Type t = type();
        ArrayList<AST> retValues = new ArrayList<AST>();
        retValues.add(v);
        retValues.add(t);
        return retValues;
    }

    // 13. variable_decl_init ::= ID ( '=' ( expression | 'uninit' ) )?
    private Var variableDeclInit() {
        Token t = currentLA();
        Name n = new Name(t);

        match(TokenType.ID);
        if(nextLA(TokenType.EQ)) {
            match(TokenType.EQ);
            Expression e = null;
            if(nextLA(TokenType.UNINIT)) match(TokenType.UNINIT);
            else e = expression();
            return new Var(t,n,e);
        }

        return new Var(t,n);
    }

    /*
    ------------------------------------------------------------
                                TYPES
    ------------------------------------------------------------
    */

    // 14. type ::= scalar_type
    //            | class_name
    //            | 'List' '[' type ']'
    //            | 'Tuple' '<' type ( ',' type )* '>'
    private Type type() {
        if(nextLA(TokenType.ID))
            return className();
        else if(nextLA(TokenType.LIST)) {
            Token t = currentLA();
            match(TokenType.LIST);
            match(TokenType.LBRACK,t);
            Type ty = type();
            match(TokenType.RBRACK,t);
            return new ListType(t,ty);
        }
        else if(nextLA(TokenType.TUPLE)) {
            match(TokenType.TUPLE);
            match(TokenType.LT);
            type();
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                type();
            }
            match(TokenType.GT);
        }
        return scalarType();
    }

    // 15. scalar_type ::= discrete_type
    //                   | 'String' ( '[' INT_LITERAL ']' )*
    //                   | 'Real' ( ':' INT_LITERAL )? ( '[' INT_LITERAL ']' )*
    private Type scalarType() {
        Token t = currentLA();
        if(nextLA(TokenType.STRING)) {
            match(TokenType.STRING);
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK);
            }
            return new ScalarType(t,Scalars.STR);
        }
        else if(nextLA(TokenType.REAL)) {
            match(TokenType.REAL);
            if(nextLA(TokenType.COLON)) {
                match(TokenType.COLON);
                match(TokenType.INT_LIT);
            }
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK);
            }
            return new ScalarType(t,Scalars.REAL);
        }
        return discreteType();
    }

    // 16. discrete_type ::= 'Bool' ( '[' INT_LITERAL ']' )*
    //                     | 'Int' ( ':' INT_LITERAL '..' INT_LITERAL )? ( '[' INT_LITERAL ']' )*
    //                     | 'Char' ( ':' CHAR_LITERAL '..' CHAR_LITERAL )? ( '[' INT_LITERAL ']' )*
    private DiscreteType discreteType() {
        Token t = currentLA();
        if(nextLA(TokenType.BOOL)) {
            match(TokenType.BOOL);
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK);
            }
            return new DiscreteType(t, Discretes.BOOL);
        }
        else if(nextLA(TokenType.INT)) {
            match(TokenType.INT);
            if(nextLA(TokenType.COLON)) {
                match(TokenType.COLON);
                match(TokenType.INT_LIT);
                match(TokenType.INC);
                match(TokenType.INT_LIT);
            }
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK);
            }
            return new DiscreteType(t, Discretes.INT);
        }
        else if(nextLA(TokenType.CHAR)) {
            match(TokenType.CHAR);
            if(nextLA(TokenType.COLON)) {
                match(TokenType.COLON);
                match(TokenType.CHAR_LIT);
                match(TokenType.INC);
                match(TokenType.CHAR_LIT);
            }
            while(nextLA(TokenType.LBRACK)) {
                match(TokenType.LBRACK);
                match(TokenType.INT_LIT);
                match(TokenType.RBRACK);
            }
            return new DiscreteType(t, Discretes.CHAR);
        }
        else throw new IllegalStateException("Error! Invalid type entered!");
    }

    // 17. class_name ::= ID ( '<' type ( ',' type )* '>' )?
    private ClassType className() {
        Token t = currentLA();
        Name n = new Name(t);
        match(TokenType.ID,t);
        if(nextLA(TokenType.LT)) {
            match(TokenType.LT,t);
            type();
            while(nextLA(TokenType.COMMA)) {
                match(TokenType.COMMA);
                type();
            }
            match(TokenType.GT);
        }
        return new ClassType(t,n);
    }

    /*
    ------------------------------------------------------------
                          CLASS DECLARATION
    ------------------------------------------------------------
    */

    // 18. class_type ::= ( 'abstr' | 'final' )? 'class' ID type_params? super_class? class_body
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

        return new ClassDecl(t,m,n,types,ct,body);
    }

    // 19. type_params ::= '<' type ( ',' type )* '>'
    private Vector<Type> typeParams() {
        Vector<Type> types = new Vector<Type>();

        match(TokenType.LT);
        types.append(type());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            types.append(type());
        }
        match(TokenType.GT);
        return types;
    }

    // 20. super_class ::= 'inherits' ID type_params?
    private ClassType superClass() {
        match(TokenType.INHERITS);

        Token startTok = currentLA();
        Name superName = new Name(startTok);
        match(TokenType.ID);

        Vector<Type> vectorOfTypes = null;
        if(nextLA(TokenType.LT)) vectorOfTypes = typeParams();

        return new ClassType(startTok,superName,vectorOfTypes);
    }

    // 21. class_body ::= '{' data_decl* method_decl* '}'
    private ClassBody classBody() {
        Token t = currentLA();
        match(TokenType.LBRACE);

        Vector<DataDecl> dataDecls = new Vector<DataDecl>();
        while(isDataDeclFIRST() && nextLA(TokenType.ID,1))
           dataDecls.merge(dataDecl());

        Vector<MethodDecl> methodDecls = new Vector<MethodDecl>();
        while(nextLA(TokenType.PROTECTED) || nextLA(TokenType.PUBLIC))
            methodDecls.append(methodDecl());

        match(TokenType.RBRACE);
        return new ClassBody(t,dataDecls,methodDecls);
    }

    /*
    ------------------------------------------------------------
                          FIELD DECLARATION
    ------------------------------------------------------------
    */

    // 22. data_decl ::= ( 'property' | 'protected' | 'public' ) variable_decl
    private Vector<DataDecl> dataDecl() {
        Token startTok = currentLA();

        Modifier m = null;
        if(nextLA(TokenType.PROPERTY)) {
            match(TokenType.PROPERTY);
            m = new Modifier(startTok,Mods.PROPERTY);
        }
        else if(nextLA(TokenType.PROTECTED)) {
            match(TokenType.PROTECTED);
            m = new Modifier(startTok,Mods.PROTECTED);
        }
        else {
            match(TokenType.PUBLIC);
            m = new Modifier(startTok,Mods.PUBLIC);
        }

        ArrayList<AST> vals = variableDecl();
        Vector<DataDecl> dataDecls = new Vector<DataDecl>();
        Vector<Var> vars = (Vector<Var>) vals.get(0);
        Type myType = (Type) vals.get(1);
        for(int i = 0; i < vars.size(); i++)
            dataDecls.append(new DataDecl(startTok,m,vars.get(i).asVar(),myType));
        return dataDecls;
    }

    /*
    ------------------------------------------------------------
                          METHOD DECLARATION
    ------------------------------------------------------------
    */

    // 23. method_decl ::= method_class | operator_class
    private MethodDecl methodDecl() {
        if(nextLA(TokenType.OPERATOR,1) || nextLA(TokenType.OPERATOR,2))
            return operatorClass();
        return methodClass();
    }

    // 24. method_class ::= method_modifier attribute* 'override'? 'method' method_header '=>' return_type block_statement
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
        ArrayList<AST> header = methodHeader();
        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt bs = blockStatement();
        return new MethodDecl(t,mods,(Name)header.get(0),null,(Vector<ParamDecl>)header.get(1),rt,bs,override);
    }

    // 25. method_modifier : 'protected' | 'public' ;
    private Modifier methodModifier() {
        Token t = currentLA();
        if(nextLA(TokenType.PROTECTED)) {
            match(TokenType.PROTECTED);
            return new Modifier(t, Mods.PROTECTED);
        }
        match(TokenType.PUBLIC);
        return new Modifier(t, Mods.PUBLIC);
    }

    // 26. attribute ::= 'final' | 'pure' | 'recurs'
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

    // 27. method-header ::= ID '(' formal-params? ')'
    private ArrayList<AST> methodHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);
        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = null;
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
                pd = formalParams();
        match(TokenType.RPAREN);
        ArrayList<AST> ret = new ArrayList<AST>();
        ret.add(n);
        ret.add(pd);
        return ret;
    }

    // 28. formal_params : param_modifier Name type ( ',' param_modifier Name type)*
    private Vector<ParamDecl> formalParams() {
        Token paramTok = currentLA();
        Vector<ParamDecl> pd = new Vector<ParamDecl>();
        Modifier m = paramModifier();
        Name n = new Name(currentLA());
        match(TokenType.ID);
        Type ty = type();

        pd.append(new ParamDecl(paramTok,m,n,ty));

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            paramTok = currentLA();
            m = paramModifier();
            n = new Name(currentLA());
            match(TokenType.ID);
            ty = type();
            pd.append(new ParamDecl(paramTok,m,n,ty));
        }
        return pd;
    }

    // 29. param_modifier : 'in' | 'out' | 'inout' | 'ref'
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
        else if(nextLA(TokenType.IN) && nextLA(TokenType.OUT,1)) {
            match(TokenType.IN);
            match(TokenType.OUT,t);
            return new Modifier(t,Mods.INOUT);
        }
        match(TokenType.REF);
        return new Modifier(t,Mods.REF);
    }

    // 30. return-type ::= Void | type
    private Type returnType() {
        if(nextLA(TokenType.VOID)) {
            match(TokenType.VOID);
            return null;
        }
        return type();
    }

    // 31. operator_class : operator_modifier 'final'? 'operator' operator_header '=>' return_type block_statement
    private MethodDecl operatorClass() {
        Token t = currentLA();
        Vector<Modifier> mods = new Vector<Modifier>(methodModifier());

        if(nextLA(TokenType.FINAL)) {
            mods.append(new Modifier(currentLA(),Mods.FINAL));
            match(TokenType.FINAL);
        }

        match(TokenType.OPERATOR);
        ArrayList<AST> header = operatorHeader();
        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt block = blockStatement();
        return new MethodDecl(t,mods,null,(Operator)header.get(0),(Vector<ParamDecl>)header.get(1),rt,block,false);
    }

    // 32. operator_header ::= operator_symbol '(' formal-params? ')'
    private ArrayList<AST> operatorHeader() {
        Operator op = operatorSymbol();

        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = null;
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            pd = formalParams();
        match(TokenType.RPAREN);
        ArrayList<AST> ret = new ArrayList<AST>();
        ret.add(op);
        ret.add(pd);
        return ret;
    }

    // 33. operator_symbol ::= binary_operator | unary_operator
    private Operator operatorSymbol() {
        if(nextLA(TokenType.TILDE) || nextLA(TokenType.NOT))
            return unaryOperator();
        return binaryOperator();
    }

    // 34. binary_operator ::= <= | < | > | >= | == | <> | <=> | <: | :> | + | - | * | / | % | **
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

    // 35. unary-operator ::= ~ | not
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

    // 36. function ::= 'def' ( 'pure' | 'recurs' )? function_header '=>' return_type block_statement
    private FuncDecl function() {
        Token t = currentLA();
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

        ArrayList<AST> header = functionHeader();
        match(TokenType.ARROW);
        Type ret = returnType();
        BlockStmt b = blockStatement();
        return new FuncDecl(t,mod,(Name)header.get(0),null,(Vector<ParamDecl>)header.get(1),ret,b);
    }

    // 37. function_header ::= ID function_type_params? '(' formal_params? ')'
    private ArrayList<AST> functionHeader() {
        Name n = new Name(currentLA());
        match(TokenType.ID);

        if(nextLA(TokenType.LT)) functionTypeParams();

        match(TokenType.LPAREN);
        Vector<ParamDecl> params = null;
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            params = formalParams();
        match(TokenType.RPAREN);
        ArrayList<AST> header = new ArrayList<AST>();
        header.add(n);
        header.add(params);
        return header;
    }

    // 38. function_type_params ::= '<' typeifier ( ',' typeifier )* '>'
    private void functionTypeParams() {
        match(TokenType.LT);
        typeifier();

        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            typeifier();
        }
        match(TokenType.GT);
    }

    // 39. typeifier ::= ( 'discr' | 'scalar' | 'class' )? ID
    private void typeifier() {
        if(nextLA(TokenType.DISCR)) match(TokenType.DISCR);
        else if(nextLA(TokenType.SCALAR)) match(TokenType.SCALAR);
        else if(nextLA(TokenType.CLASS)) match(TokenType.CLASS);
        else throw new IllegalStateException("Error! Invalid function typeifer!");

        match(TokenType.ID);
    }

    /*
    ------------------------------------------------------------
                          MAIN FUNCTION
    ------------------------------------------------------------
    */

    // 40. main ::= 'def' 'main' args? '=>' return_type block_statement
    private MainDecl mainFunc() {
        Token t = currentLA();
        match(TokenType.DEF);
        match(TokenType.MAIN,t);
        Vector<ParamDecl> args = null;
        if(nextLA(TokenType.LPAREN)) args = args();
        match(TokenType.ARROW);
        Type rt = returnType();
        BlockStmt b = blockStatement();
        return new MainDecl(t,args,rt,b);
    }

    // 41. args ::= '(' formal_params? ')'
    private Vector<ParamDecl> args() {
        match(TokenType.LPAREN);
        Vector<ParamDecl> pd = null;
        if(nextLA(TokenType.IN) || nextLA(TokenType.OUT) || nextLA(TokenType.INOUT) || nextLA(TokenType.REF))
            pd = formalParams();
        match(TokenType.RPAREN);
        return pd;
    }

    // 42. block-statement ::= '{' declaration* statement* '}'
    private BlockStmt blockStatement() {
        Token t = currentLA();
        match(TokenType.LBRACE,t);
        Vector<LocalDecl> vd = new Vector<LocalDecl>();
        while(nextLA(TokenType.DEF)) vd.merge(declaration());

        Vector<Statement> st = new Vector<Statement>();
        while(isStatFIRST()) st.append(statement());

        match(TokenType.RBRACE,t);
        return new BlockStmt(t,vd,st);
    }

    // 43. declaration ::= 'def' 'local'? variable_decl
    private Vector<LocalDecl> declaration() {
        Token startTok = currentLA();
        match(TokenType.DEF);
        if(nextLA(TokenType.LOCAL)) match(TokenType.LOCAL);
        ArrayList<AST> varDecls = variableDecl();
        Vector<Var> vars = (Vector<Var>) varDecls.get(0);
        Type varType = (Type) varDecls.get(1);
        Vector<LocalDecl> locals = new Vector<LocalDecl>();
        for(int i = 0; i < vars.size(); i++)
            locals.append(new LocalDecl(startTok,vars.get(i).asVar(), varType));
        return locals;
    }

    /*
    ------------------------------------------------------------
                             STATEMENTS
    ------------------------------------------------------------
    */

    // 44. statement ::=    'stop'
    //    | return_statement
    //    | assignment_statement
    //    | block_statement
    //    | if_statement
    //    | while_statement
    //    | do_while_statement
    //    | for_statement
    //    | choice_statement
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

    // 45. return_statement ::= expression?
    private ReturnStmt returnStatement() {
        Token t = currentLA();
        match(TokenType.RETURN);
        if(isPrimExprFIRST()) return new ReturnStmt(t,null,expression());
        return new ReturnStmt(t,null);
    }

    // 46. assignment_statement ::= 'set' expression assignment_operator expression
                            //    |  logical_or_expression
    private Statement assignmentStatement() {
        Token t = currentLA();
        if(nextLA(TokenType.SET)) {
            match(TokenType.SET);
            Expression LHS = expression();
            AssignOp op = assignmentOperator();
            Expression RHS = expression();
            return new AssignStmt(t,LHS,RHS,op);
        }
        return new ExprStmt(t,logicalOrExpression());
    }

    // 47. assignment_operator ::= '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**='
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

    // 48. if_statement ::= if expression block_statement ( elseif_statement )* ( 'else' block_statement)?
    private IfStmt ifStatement() {
        Token t = currentLA();
        match(TokenType.IF);
        Expression e = expression();
        BlockStmt b = blockStatement();

        Vector<IfStmt> elifStmts = new Vector<IfStmt>();
        while(nextLA(TokenType.ELSE) && nextLA(TokenType.IF,1))
            elifStmts.append(elseIfStatement());

        BlockStmt elseB = null;
        if(nextLA(TokenType.ELSE)) {
            match(TokenType.ELSE);
            return new IfStmt(t,e,b,elifStmts,blockStatement());
        }
        return new IfStmt(t,e,b,elifStmts);
    }

    // 49. elseif_statement ::= 'else' 'if' expression block_statement
    private IfStmt elseIfStatement() {
        Token t = currentLA();
        match(TokenType.ELSE);
        match(TokenType.IF);
        Expression e = expression();
        BlockStmt b = blockStatement();
        return new IfStmt(t,e,b,null);
    }

    // 50. while_statement ::= 'while' expression ( 'next' expression )? block_statement
    private WhileStmt whileStatement() {
        Token t = currentLA();
        match(TokenType.WHILE);
        Expression e = expression();
        Expression nextE = null;
        if(nextLA(TokenType.NEXT)) {
            match(TokenType.NEXT);
            nextE = expression();
        }
        BlockStmt b = blockStatement();
        return new WhileStmt(t,e,nextE,b);
    }

    // 51. do_while_statement ::= 'do' block_statement ( 'next' expression )? 'while' expression
    private DoStmt doWhileStatement() {
        Token t = currentLA();
        match(TokenType.DO);
        BlockStmt b = blockStatement();
        Expression nextE = null;
        if(nextLA(TokenType.NEXT)) {
            match(TokenType.NEXT);
            nextE = expression();
        }
        match(TokenType.WHILE);
        Expression e = expression();
        return new DoStmt(t,b,nextE,e);
    }

    // 52. for_statement ::= 'for' expression ( 'next' expression )? do block_statement
    private ForStmt forStatement() {
        Token t = currentLA();
        match(TokenType.FOR);
        Expression e = expression();

        Expression nextE = null;
        if(nextLA(TokenType.NEXT)) {
            match(TokenType.NEXT);
            nextE = expression();
        }
        match(TokenType.DO);
        BlockStmt b = blockStatement();
        return new ForStmt(t,e,nextE,b);
    }

    // 52. choice_statement ::= 'choice' expression '{' case_statement* 'other' block_statement '}'
    private ChoiceStmt choiceStatement() {
        Token t = currentLA();
        match(TokenType.CHOICE);
        Expression e = expression();
        match(TokenType.LBRACE);
        Vector<CaseStmt> cStmts = new Vector<CaseStmt>();
        while(nextLA(TokenType.ON)) cStmts.append(caseStatement());
        match(TokenType.OTHER);
        BlockStmt b = blockStatement();
        match(TokenType.RBRACE);
        return new ChoiceStmt(t,e,cStmts,b);
    }

    // 53. case_statement ::= 'on' label block_statement
    private CaseStmt caseStatement() {
        Token t = currentLA();
        match(TokenType.ON);
        Label l = label();
        BlockStmt b = blockStatement();
        return new CaseStmt(t,l,b);
    }

    // 51. label ::= constant ('..' constant)?
    private Label label() {
        Token t = currentLA();
        Expression e = constant();
        if(nextLA(TokenType.INC)) {
            match(TokenType.INC);
            constant();
        }
        return new Label(t,(Literal)e);
    }

    // 55. input_statement ::= 'cin' ( '>>' expression )+
    private InStmt inputStatement() {
        Token t = currentLA();
        match(TokenType.CIN);
        ioFlag = true;
        Vector<Expression> e = new Vector<Expression>();
        if(nextLA(TokenType.SRIGHT)) {
            while(nextLA(TokenType.SRIGHT)) {
                match(TokenType.SRIGHT);
                e.append(expression());
            }
        }
        ioFlag = false;
        return new InStmt(t,e);
    }

    // 56. output_statement ::= 'cout' ( '<<' expression )+
    private OutStmt outputStatement() {
        Token t = currentLA();
        match(TokenType.COUT);
        ioFlag = true;
        Vector<Expression> e = new Vector<Expression>();
        if(nextLA(TokenType.SLEFT)) {
            while(nextLA(TokenType.SLEFT)) {
                match(TokenType.SLEFT);
                e.append(expression());
            }
        }
        ioFlag = false;
        return new OutStmt(t,e);
    }

    /*
    ------------------------------------------------------------
                            EXPRESSIONS
    ------------------------------------------------------------
    */

    // 56. primary_expression ::= ID | constant | '(' expression ')'
    private Expression primaryExpression() {
        Token t = currentLA();
        if(nextLA(TokenType.LPAREN)) {
            match(TokenType.LPAREN);
            Expression e = expression();
            match(TokenType.RPAREN);
            return e;
        }
        else if(nextLA(TokenType.ID)) {
            Name n = new Name(currentLA());
            match(TokenType.ID);
            return new NameExpr(t,n);
        }
        else if(nextLA(TokenType.CIN))
            return inputStatement();
        else if(nextLA(TokenType.COUT))
            return outputStatement();
        return constant();
    }

    // 57. postfix_expression ::= primary_expression ( '[' expression ']' | '(' arguments ')' | ( '.' | '?.' ) expression)*
    private Expression postfixExpression() {
        Token startTok = currentLA();
        Expression primary = primaryExpression();
        if(isAfterPrimExprFIRST()) {
            Expression mainExpr = null, e = null;
            while(isAfterPrimExprFIRST()) {
                if(nextLA(TokenType.LBRACK)) {
                    match(TokenType.LBRACK);
                    expression();
                    match(TokenType.RBRACK);
                }
                else if(nextLA(TokenType.LPAREN)) {
                    match(TokenType.LPAREN);
                    Vector<Expression> args = arguments();
                    match(TokenType.RPAREN);
                    e = new Invocation(startTok,((NameExpr)primary).getName(),args);
                }
                else {
                    if(nextLA(TokenType.ELVIS)) match(TokenType.ELVIS);
                    else match(TokenType.PERIOD);
                    Expression e1 = expression();
                    e = new FieldExpr(startTok,e1,((NameExpr)primary).getName(),false);
                }
                mainExpr = e;
            }
            return mainExpr;
        }
        return primary;
    }

    // 58. arguments ::= expression ( ',' expression )*
    private Vector<Expression> arguments() {
        Vector<Expression> ex = new Vector<Expression>();
        ex.append(expression());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA);
            ex.append(expression());
        }
        return ex;
    }

    // 59. unary_expression ::= unary_operator cast_expression | postfix_expression
    private Expression unaryExpression() {
        if(nextLA(TokenType.TILDE) || nextLA(TokenType.NOT)) {
            Token t = currentLA();
            UnaryOp uo = unaryOperator();
            Expression e = castExpression();
            return new UnaryExpr(t,e,uo);
        }
        return postfixExpression();
    }

    // 60. cast_expression ::= scalar_type '(' cast_expression ')' | unary_expression
    private Expression castExpression() {
        Token t = currentLA();
        if(isScalarTypeFIRST()) {
            Type st = scalarType();
            match(TokenType.LPAREN);
            Expression e = castExpression();
            match(TokenType.RPAREN);
            return new CastExpr(t,st,e);
        }
        return unaryExpression();
    }

    // 61. power_expression ::= cast_expression ( '**' cast_expression )*
    private Expression powerExpression() {
        Token startTok = currentLA();
        Expression left = castExpression();
        if(nextLA(TokenType.EXP)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.EXP)) {
                System.out.println(currentLA().getLocation().toString());
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.EXP);
               // System.out.println(bo.location.start.toString());
                match(TokenType.EXP);
                Expression right = castExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 62. multiplication_expression ::= power_expression ( ( '*' | '/' | '%' ) power_expression )*
    private Expression multiplicationExpression() {
        Token startTok = currentLA();
        Expression left = powerExpression();
        if(isAfterPowerFIRST()) {
            BinaryExpr mainBE = null, be = null;
            while(isAfterPowerFIRST()) {
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
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 63. additive_expression ::= multiplication_expression ( ( '+' | '-' ) multiplication_expression )*
    private Expression additiveExpression() {
        Token startTok = currentLA();
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
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 64. shift_expression ::= additive_expression ( ( '<<' | '>>' ) additive_expression )*
    private Expression shiftExpression() {
        Token startTok = currentLA();
        Expression left = additiveExpression();

        if(!ioFlag && (nextLA(TokenType.SLEFT) || nextLA(TokenType.SRIGHT))) {
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
                if (mainBE != null)
                    be = new BinaryExpr(startTok, mainBE, right, bo);
                else
                    be = new BinaryExpr(startTok, left, right, bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 65. relational_expression ::= shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )*
    private Expression relationalExpression() {
        Token startTok = currentLA();
        Expression left = shiftExpression();

        if(isAfterAddFIRST()) {
            BinaryExpr mainBE = null, be = null;
            while(isAfterAddFIRST()) {
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
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // TODO: ADD new keywords
    // 66. instanceof_expression ::= relational_expression ( ( 'instanceof' | '!instanceof' | 'as?' ) relational_expression )*
    private Expression instanceOfExpression() {
        Token startTok = currentLA();
        Expression left = relationalExpression();
        if(nextLA(TokenType.INSTANCEOF)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.INSTANCEOF)) {
                BinaryOp bo = new BinaryOp(currentLA(), BinaryType.INOF);
                match(TokenType.INSTANCEOF);
                Expression right = relationalExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 67. equality_expression ::= instanceof_expression ( ( '==' | '!=' ) instanceof_expression )*
    private Expression equalityExpression() {
        Token startTok = currentLA();
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
                if (mainBE != null)
                    be = new BinaryExpr(startTok, mainBE, right, bo);
                else
                    be = new BinaryExpr(startTok, left, right, bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 68. and_expression ::= equality_expression ( '&' equality_expression )*
    private Expression andExpression() {
        Token startTok = currentLA();
        Expression left = equalityExpression();
        if(nextLA(TokenType.BAND)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.BAND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BAND);
                match(TokenType.BAND);
                Expression right = equalityExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 69. exclusive_or_expression ::= and_expression ( '^' and_expression )*
    private Expression exclusiveOrExpression() {
        Token startTok = currentLA();
        Expression left = andExpression();
        if(nextLA(TokenType.XOR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.XOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.XOR);
                match(TokenType.XOR);
                Expression right = andExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 70. inclusive_or_expression ::= exclusive_or_expression ( '|' exclusive_or_expression )*
    private Expression inclusiveOrExpression() {
        Token startTok = currentLA();
        Expression left = exclusiveOrExpression();
        if(nextLA(TokenType.BOR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.BOR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.BOR);
                match(TokenType.BOR);
                Expression right = exclusiveOrExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 71. logical_and_expression ::= inclusive_or_expression ( 'and' inclusive_or_expression )*
    private Expression logicalAndExpression() {
        Token startTok = currentLA();
        Expression left = inclusiveOrExpression();
        if(nextLA(TokenType.AND)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.AND)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.AND);
                match(TokenType.AND);
                Expression right = inclusiveOrExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 72. logical_or_expression ::= logical_and_expression ( 'or' logical_and_expression )*
    private Expression logicalOrExpression() {
        Token startTok = currentLA();
        Expression left = logicalAndExpression();
        if(nextLA(TokenType.OR)) {
            BinaryExpr mainBE = null, be = null;
            while(nextLA(TokenType.OR)) {
                BinaryOp bo = new BinaryOp(currentLA(),BinaryType.OR);
                match(TokenType.OR);
                Expression right = logicalAndExpression();
                if(mainBE != null)
                    be = new BinaryExpr(startTok,mainBE,right,bo);
                else
                    be = new BinaryExpr(startTok,left,right,bo);
                mainBE = be;
            }
            return mainBE;
        }
        return left;
    }

    // 73. expression ::= logical_or_expression
    private Expression expression() { return logicalOrExpression(); }

    /*
    ------------------------------------------------------------
                                LITERALS
    ------------------------------------------------------------
    */

    // 74. constant ::= object_constant | array_constant | list_constant | scalar_constant
    private Expression constant() {
        if(nextLA(TokenType.NEW)) return objectConstant();
        else if(nextLA(TokenType.ARRAY) || nextLA(TokenType.LBRACK))
            arrayConstant();
        else if(nextLA(TokenType.LIST) || nextLA(TokenType.LBRACE))
            return listConstant();
        return scalarConstant();
    }

    // 75. object_constant ::= 'new' ID '(' (object_field ( ',' object_field )* ')'
    private NewExpr objectConstant() {
        Token t = currentLA();
        match(TokenType.NEW,t);
        Name n = new Name(currentLA());
        match(TokenType.ID,t);
        match(TokenType.LPAREN,t);

        Vector<Var> vars = new Vector<Var>(objectField());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA,t);
            vars.append(objectField());
        }
        match(TokenType.RPAREN,t);
        return new NewExpr(t,new ClassType(t,n),vars);
    }

    // 76. object_field ::= ID '=' expression
    private Var objectField() {
        Token t = currentLA();
        Name n = new Name(currentLA());
        match(TokenType.ID,t);
        match(TokenType.EQ,t);
        Expression e = expression();
        return new Var(t,n,e);
    }

    // 77. array_constant ::= 'Array' ( '[' expression ']' )* '(' arguments ')'
    private void arrayConstant() {
        match(TokenType.ARRAY);
        while(nextLA(TokenType.LBRACK)) {
            match(TokenType.LBRACK);
            expression();
            match(TokenType.RBRACK);
        }
        match(TokenType.LPAREN);
        arguments();
        match(TokenType.RPAREN);
    }

    // 78. list_constant ::= 'List' '(' expression (',' expression)* ')'
    private ListLiteral listConstant() {
        Token t = currentLA();
        match(TokenType.LIST,t);
        match(TokenType.LPAREN,t);
        Vector<Expression> seq = new Vector<Expression>(expression());
        while(nextLA(TokenType.COMMA)) {
            match(TokenType.COMMA,t);
            seq.append(expression());
        }
        match(TokenType.RPAREN,t);
        return new ListLiteral(t, seq);
    }

    // 79. scalar_constant ::= discrete_constant | STRING_LITERAL | TEXT_LITERAL | REAL_LITERAL
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

    // 80. discrete_constant ::= INT_LITERAL | CHAR_LITERAL | BOOL_LITERAL
    private Literal discreteConstant() {
        Token t = currentLA();
        Literal l = null;
        if(nextLA(TokenType.INT_LIT)) {
            match(TokenType.INT_LIT);
            l = new Literal(t, ConstantKind.INT);
        }
        else if(nextLA(TokenType.CHAR_LIT)) {
            match(TokenType.CHAR_LIT);
            l = new Literal(t, ConstantKind.CHAR);
        }
        else if(nextLA(TokenType.BOOL_LIT)) {
            match(TokenType.BOOL_LIT);
            l = new Literal(t, ConstantKind.BOOL);
        }
        return l;
    }
}
