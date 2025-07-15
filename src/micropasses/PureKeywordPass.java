package micropasses;

import ast.AST;
import ast.classbody.MethodDecl;
import ast.misc.Compilation;
import ast.statements.AssignStmt;
import ast.statements.RetypeStmt;
import ast.topleveldecls.FuncDecl;
import messages.Message;
import messages.MessageType;
import messages.warnings.WarningBuilder;
import messages.warnings.WarningFactory;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

/**
 * Micropass to check for the correct implementation of {@code pure} functions and methods.
 * <p><br>
 *     C Minor supports the use of the {@code pure} keyword. When a function or method is
 *     marked as {@code pure}, the compiler will check if either construct will produce a
 *     side effect. In this case, a side effect will be any change to any non-local variable
 *     not declared in the construct. For every side effect we find, we will produce a
 *     warning to the user, though we will not terminate the compilation process.
 * </p>
 * @author Daniel Levy
 */
public class PureKeywordPass extends Visitor {

    private SymbolTable currentScope;
    private String methodName;
    private boolean insidePureMethod;
    private final WarningFactory generateWarning;
    private final Vector<Message> msgs;

    public PureKeywordPass() {
        this.generateWarning = new WarningFactory();
        this.msgs = new Vector<>();
    }

    public PureKeywordPass(boolean mode) {
        this();
        this.interpretMode = mode;
    }

    /**
     * Determines if a variable is changing state while inside a pure method.
     * <p><br>
     *     This is a helper method that checks if a given declaration from the
     *     {@link AST} matches a set of criteria defined in the method. If this
     *     criteria is met, this means the variable associated with the declaration
     *     is updated in some way and thus could be producing a side effect.
     * </p>
     * @param decl {@link AST} node that could be changing state
     * @return Boolean
     */
    private boolean methodChangesState(AST decl) {
        if(decl.isParamDecl() && (decl.asParamDecl().mod.isOut()
                               || decl.asParamDecl().mod.isInOut()
                               || decl.asParamDecl().mod.isRef()))
            return true;

        return decl.isTopLevelDecl() && decl.asTopLevelDecl().isGlobalDecl();
    }

    public void visitAssignStmt(AssignStmt as) {
        if(insidePureMethod && methodChangesState(currentScope.findName(as.LHS()).decl())) {
            msgs.add(
                new WarningBuilder(generateWarning,currentFile,interpretMode)
                    .addLocation(as)
                    .addWarningType(MessageType.WARNING_1)
                    .addArgs(as.LHS(),methodName)
                    .create()
            );
        }
    }

    public void visitCompilation(Compilation c) {
        currentFile = c.getFile();
        super.visitCompilation(c);

        if(Message.printAllMessages(msgs))
            System.exit(1);
    }

    public void visitFuncDecl(FuncDecl fd) {
        if(fd.mod.isPure()) {
            insidePureMethod = true;
            methodName = fd.toString();
            currentScope = fd.symbolTable;
            super.visitFuncDecl(fd);
            insidePureMethod = false;
        }
    }

    public void visitMethodDecl(MethodDecl md) {
        if(md.mods.isPure()) {
            insidePureMethod = true;
            methodName = md.toString();
            currentScope = md.symbolTable;
            super.visitMethodDecl(md);
            insidePureMethod = false;
        }
    }

    public void visitRetypeStmt(RetypeStmt rt) {
        if(insidePureMethod && methodChangesState(currentScope.findName(rt.LHS()).decl())) {
            msgs.add(
                new WarningBuilder(generateWarning,currentFile,interpretMode)
                    .addLocation(rt)
                    .addWarningType(MessageType.WARNING_1)
                    .addArgs(rt.LHS(),methodName)
                    .create()
            );
        }
    }
}
