package micropasses;

public class FieldInvocationNameChecking {
//    public void checkProperClass(AST node) {
//        Expression target = null;
//        String name = null;
//        if(node.asExpression().isInvocation()) {
//            target = node.asExpression().asInvocation().getTargetExpression();
//            name = node.asExpression().asInvocation().toString();
//        }
//        else {
//            target = node.asExpression().asFieldExpr().getFieldTarget();
//            name = node.asExpression().asFieldExpr().toString();
//        }
//
//        /*
//            ERROR CHECK #1:
//                We first need to make sure the target expression represents a
//                name (since the parser can allow literals to show up as a target)
//        */
//        if(!target.isNameExpr())
//            ScopeError.LocalDeclError(node);
//
//        /*
//            ERROR CHECK #2:
//                We then must check if the target's name has been defined somewhere
//                in the current scope. If it has been defined, then we need to get
//                the declaration the name is binded to. In this case, the name can
//                only be binded to a LocalDecl inside a block or a ParamDecl when we
//                are inside a function or method. We use these declarations to access
//                the appropriate ClassDecl node, so we can check for a specific field
//                or method name.
//        */
//        NameNode decl = currentScope.findName(target.toString());
//        if(decl.getID().isStatement())
//            decl = currentScope.findName(decl.getID().asStatement().asLocalDecl().getType().typeName());
//        else if(decl. getID().isParamDecl())
//            decl = currentScope.findName(decl.getID().asParamDecl().getType().typeName());
//        else
//            ScopeError.LocalDeclError(node);
//
//        /*
//            ERROR CHECK #3:
//                Here, we are checking if we did in fact found the correct ClassDecl.
//                This error is produced ONLY if the class name associated with the object
//                was NOT defined or if the target expression does not represent an object.
//                If the name doesn't represent o
//        */
//        if(decl == null)
//            ScopeError.LocalDeclError(node);
//
//        SymbolTable classSymbolTable = decl.getID().asTopLevelDecl().asClassDecl().symbolTable;
//        decl = classSymbolTable.findName(name);
//
//        /*
//            ERROR CHECK #4:
//                Our final check is making sure the field or method we are trying to access
//                has indeed been defined in the class or not.
//        */
//        if(decl == null)
//            ScopeError.LocalDeclError(node);
//    }
}
