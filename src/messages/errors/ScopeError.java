package messages.errors;

import ast.AST;
import utilities.PrettyPrint;
import utilities.SymbolTable;

public class ScopeError extends Error {

    public enum errorType {
        REDECL,                 // Redeclaring variable in the same scope
        SELF_ASSIGN,            // Declaring and assigning a variable to itself
        NO_DECL,                // Missing declaration
        NO_CLASS_DECL,          // Class has not been declared
        MISSING_FIELD,          // Field was not defined in calss
        FIELD_VAL_GIVEN,        // Field was already given a value in a NewExpr
        INHERIT_SELF            // Class tries to inherit itself
    }

    private String name;
    private errorType error;
    private SymbolTable scope;

    public ScopeError(String name, AST node, SymbolTable scope, errorType error) {
        super(node);
        this.name = name;
        this.error = error;
        this.scope = scope;
    }

    @Override
    public void printMsg() {
        System.out.println(PrettyPrint.YELLOW + "Scoping Error Detected!\n" + PrettyPrint.RESET);
        super.printMsg();
        if(error == errorType.REDECL) { redeclError(); }
        else if(error == errorType.SELF_ASSIGN) { selfAssignError(); }
        else if(error == errorType.NO_DECL) { noDeclError(); }
        else if(error == errorType.NO_CLASS_DECL) { noClassDeclError(); }
        else if(error == errorType.MISSING_FIELD) { missingFieldError(); }
        else if(error == errorType.FIELD_VAL_GIVEN) { fieldValueGivenError(); }
        else if(error == errorType.INHERIT_SELF) { inheritSelfError(); }
    }

    public void redeclError() {
        AST prevDecl = retrieveName(name);
        System.out.println(PrettyPrint.RED + printStartLocation() + ": \'"
                + name + "\' has already been declared on line " + prevDecl.startLine() +
                ".\n" + PrettyPrint.RESET);

        prevDecl.printLine();
        System.out.println(PrettyPrint.RED + "Redeclaration of \'" + name + "\' in the " +
                "same scope is not allowed.");

        System.exit(1);
    }

    public void selfAssignError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": Variable \'"
                + name + "\' can not be initialized to itself." + PrettyPrint.RESET);
        System.exit(1);
    }

    public void noDeclError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": \'"
                + name + "\' has not yet been declared in the current scope.\n" + PrettyPrint.RESET);
        System.exit(1);
    }

    public void noClassDeclError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": Class \'"
                + name + "\' does not exist and can not be instantiated.\n" + PrettyPrint.RESET);
        System.exit(1);
    }

    public void missingFieldError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": Field \'"
                + name + "\' was not defined in the current class.\n" + PrettyPrint.RESET);
        System.exit(1);
    }

    public void fieldValueGivenError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": Field \'"
                + name + "\' was already assigned an initial value.\n" + PrettyPrint.RESET);
        System.exit(1);
    }

    public void inheritSelfError() {
        System.out.println(PrettyPrint.RED + printStartLocation() + ": Class \'"
                + name + "\' can not inherit itself.\n" + PrettyPrint.RESET);
        System.exit(1);
    }

    public AST retrieveName(String name) {
        AST prevDecl = scope.findName(name).declName();

        if(prevDecl.isFieldDecl())
            return prevDecl.asFieldDecl();
        else if(prevDecl.isTopLevelDecl()) {
            if(prevDecl.asTopLevelDecl().isGlobalDecl())
                return prevDecl.asTopLevelDecl().asGlobalDecl();
            else if(prevDecl.asTopLevelDecl().isEnumDecl())
                return prevDecl.asTopLevelDecl().asEnumDecl();
            else
                return prevDecl.asTopLevelDecl().asClassDecl();
        }
        else if(prevDecl.isStatement()) {
            if (prevDecl.asStatement().isLocalDecl())
                return prevDecl.asStatement().asLocalDecl();
        }
        else
            return prevDecl.asParamDecl();

        return null;
    }
}
