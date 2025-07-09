package ast.classbody;

import ast.*;
import ast.misc.Var;
import ast.statements.AssignStmt;
import ast.types.Type;
import utilities.Vector;
import utilities.Visitor;

// Constructor Declaration is created after type checking is successful
public class InitDecl extends AST {
    private Vector<AssignStmt> inits;

    public InitDecl() { this(new Vector<>()); }
    public InitDecl(Vector<AssignStmt> p) { this.inits = p; }

    public Vector<AssignStmt> assignStmts() { return this.inits; }
    private void setInits(Vector<AssignStmt> inits) { this.inits = inits; }

    /**
     * Checks if the current AST node is an {@link InitDecl}.
     * @return Boolean
     */
    public boolean isInitDecl() { return true; }

    /**
     * Type cast method for {@link InitDecl}.
     * @return InitDecl
     */
    public InitDecl asInitDecl() { return this; }

    /**
     * {@code update} method.
     * @param pos Position we want to update.
     * @param node Node we want to add to the specified position.
     */
    @Override
    public void update(int pos, AST node) {
        this.inits.remove(pos);
        this.inits.add(pos,node.asStatement().asAssignStmt());
    }

    /**
     * {@code deepCopy} method.
     * @return Deep copy of the current {@link InitDecl}
     */
    @Override
    public AST deepCopy() {
        Vector<AssignStmt> inits = new Vector<>();

        for(AssignStmt as : this.inits)
            inits.add(as.deepCopy().asStatement().asAssignStmt());

        return new InitDeclBuilder()
                   .setInits(inits)
                   .create();
    }

    /**
     * {@code visit} method.
     * @param v Current visitor we are executing.
     */
    @Override
    public void visit(Visitor v) { v.visitInitDecl(this); }

    /**
     * Internal class that builds an {@link InitDecl} object.
     */
    public static class InitDeclBuilder extends NodeBuilder {

        /**
         * {@link InitDecl} object we are building.
         */
        private final InitDecl id = new InitDecl();

        /**
         * Sets the init declaration's {@link #inits}.
         * @param inits Vector of assignment statements that initialize an object
         * @return InitDeclBuilder
         */
        public InitDeclBuilder setInits(Vector<AssignStmt> inits) {
            id.setInits(inits);
            return this;
        }

        /**
         * Creates an {@link InitDecl} object.
         * @return {@link InitDecl}
         */
        public InitDecl create() {
            super.saveMetaData(id);
            id.addChild(id.inits);
            return id;
        }
    }
}
