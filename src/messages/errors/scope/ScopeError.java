package messages.errors.scope;

import ast.AST;
import messages.errors.Error;
import utilities.PrettyPrint;

public class ScopeError extends Error {

    protected AST redeclarationLocation;
    protected String varName;

    @Override
    public String createMessage() {
        return super.createMessage() + createRedeclarationMsg();
    }

    public String createRedeclarationMsg() {
        StringBuilder sb = new StringBuilder();
        if(redeclarationLocation != null) {
            sb.append("\n\nRedeclaration Found:\n")
              .append(PrettyPrint.RED)
              .append("'")
              .append(getRedeclName())
              .append("'")
              .append(" was already declared in the line below.\n")
              .append(PrettyPrint.RESET)
              .append(redeclarationLocation.line());
        }
        return sb.toString();
    }

    private String getRedeclName() {
        if(redeclarationLocation.isTopLevelDecl())
            return redeclarationLocation.toString();
        else if(redeclarationLocation.isStatement() && redeclarationLocation.asStatement().isLocalDecl())
            return redeclarationLocation.toString();
        else if(redeclarationLocation.isParamDecl())
            return redeclarationLocation.asParamDecl().toString();
        else
            throw new RuntimeException("An invalid AST node was saved as a redeclaration.");
    }

    public void setRedeclarationLocation(AST node) { redeclarationLocation = node; }
    public void setVarName(String name) { varName = name; }

    public boolean isScopeError() { return true; }
    public ScopeError asScopeError() { return this; }

    @Override
    public String header() {
        if(fileName != null) {
            return PrettyPrint.YELLOW + "Scoping error detected in "
                    + PrettyPrint.RESET + fileName() + "\n";
        }
        return PrettyPrint.YELLOW + "Scope Error " + errorNumber() + "\n\n" + PrettyPrint.RESET;
    }
}
