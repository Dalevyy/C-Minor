package cminor.ast;

import cminor.ast.classbody.ClassNode;
import cminor.ast.expressions.Expression;
import cminor.ast.misc.CompilationUnit;
import cminor.ast.misc.SubNode;
import cminor.ast.operators.Operator;
import cminor.ast.statements.Statement;
import cminor.ast.topleveldecls.TopLevelDecl;
import cminor.ast.types.Type;
import cminor.token.Location;
import cminor.token.Token;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

/**
 * The supertype of all nodes appearing in the C Minor parse tree.
 * <p>
 *     An {@link AST} type represents the nodes that are generated through the creation
 *     of the parse tree. This specific class outlines the implementation requirements
 *     for all subnodes in the parse tree. Additionally, this class is responsible for
 *     handling all metadata requests that are needed throughout the semantic analysis.
 * </p>
 * @author Daniel Levy
 */
public abstract class AST {

    /**
     * Actual code that the {@link AST} represents.
     */
    public String text;

    /**
     * The {@link Location} of the {@link AST} in a user program.
     */
    protected Location location;

    /**
     * Reference to the previous {@link AST} in an upper level of the parse tree.
     */
    protected AST parent;

    /**
     * List of all child nodes that the current node is a parent of.
     */
    protected final Vector<AST> children;

    /**
     * Main constructor for {@link AST}.
     * @param metaData {@link Token} containing all the metadata stored with the {@link AST}.
     */
    public AST(Token metaData) {
        if(metaData != null) {
            this.text = metaData.getText();
            this.location = metaData.getLocation();
        }
        this.children = new Vector<>();
    }

    /**
     * Getter method for {@link #text}.
     * @return String representation of the current {@link AST} node.
     */
    public String getText() { return text; }

    /**
     * Getter method that returns {@link #location}.
     * @return {@link Location} where the AST is at in the program's file.
     */
    public Location getLocation() { return location; }

    /**
     * Getter method that returns {@link #parent}.
     * @return {@link AST} node that appears one level above the current node.
     */
    public AST getParent() { return parent; }

    /**
     * Getter method that returns {@link #children}.
     * @return A {@link Vector<AST>} which represents the current node's {@link #children}.
     */
    public Vector<AST> getChildren() { return children; }

    /**
     * Copies the metadata of a passed {@link AST} node into the current node.
     * @param node An {@link AST} node that we want to copy the metadata of.
     */
    public void copyMetaData(AST node) {
        this.text = node.text;
        this.location = node.location;
        this.parent = node.parent;
    }

    /**
     * Copies the metadata of a passed {@link Token} into the current node. This method
     * should only be used by the parser.
     * @param metaData {@link Token} containing the metadata that needs to be copied.
     */
    public void copyMetaData(Token metaData) {
        this.text = metaData.getText();
        this.location = metaData.getLocation();
    }

    /**
     * Creates a string representation of the node for error handling.
     * @return {@code String} displaying the line and code associated with the current node.
     */
    public String header() {
        if(location != null)
            return location.start.line + "| " + text + "\n";
        return "";
    }

    /**
     * Removes all references of {@code this} node and replaces it with the current node in the {@link AST} hierarchy.
     * @param replacementNode The {@link AST} node we wish to replace the {@code this} node with.
     */
    public void replaceWith(AST replacementNode) {
        replacementNode.copyMetaData(this);

        // We will now traverse the AST hierarchy starting at the 'this' node.
        AST currentNode = this;

        while (currentNode.parent != null) {
            currentNode = currentNode.parent;

            for(int i = 0; i < currentNode.children.size(); i++) {
                // Every time we find a reference to 'this', we will replace it with the replacement node.
                if (currentNode.children.get(i).equals(this)) {
                    currentNode.removeChildNode(i);
                    currentNode.addChildNode(i, replacementNode);
                }
            }
        }

        // We will now let the garbage collector deallocate the 'this' node.
        replacementNode.parent = parent;
        parent = null;

    }

    /**
     * Adds an {@link AST} node into a specified position in {@link #children}.
     * <p>
     *     This method will be called by {@link #replaceWith(AST)} when we are replacing a node
     *     in the {@link AST}. Once a child node is added, we will also make sure to update
     *     the corresponding reference in the specific node.
     * </p>
     * @param pos The position we want to add a node into {@link #children}.
     * @param node The {@link AST} node we wish to add into {@link #children}.
     */
    private void addChildNode(int pos, AST node) {
        children.add(pos,node);
        update(pos,node);
    }

    /**
     * Adds an {@link AST} node to the {@link #children} vector.
     * <p>
     *     We will also set the node's {@link #parent} to be the current node.
     * </p>
     * @param node The {@link AST} that will be added to the end of {@link #children}
     */
    protected void addChildNode(AST node) {
        if(node == null)
            return;

        children.add(node);
        node.parent = this;
    }

    /**
     * Adds a {@link Vector} of AST nodes into the {@link #children} vector.
     * @param nodes The {@link Vector} of nodes we will append to the end of {@link #children}.
     * @param <T> An {@link AST} type
     */
    protected <T extends AST> void addChildNode(Vector<T> nodes) {
        if(nodes.isEmpty())
            return;

        for(AST node : nodes)
            addChildNode(node);
    }

    /**
     * Removes an {@link AST} node from the specified position in {@link #children}.
     * <p>
     *     This method will only be called by {@link #replaceWith(AST)} when we are trying to replace
     *     a node in {@link #children} with another node.
     * </p>
     * @param pos The position we want to remove a node from {@link #children}.
     */
    private void removeChildNode(int pos) { children.remove(pos); }

    /**
     * Updates the {@link #children} vector to replace a child node at a given position with another node.
     * <p>
     *     This method should only be called internally when we are performing deep copies of nodes.
     * </p>
     * @param pos The position in the {@link #children} vector we should update.
     * @param newNode The {@link AST} object we want to put into the vector in place of the old node.
     */
    protected abstract void update(int pos, AST newNode);

    /**
     * Performs a deep copy of the current {@link AST} object.
     * @return A deep copy of the {@link AST} object that calls this method.
     */
    public abstract AST deepCopy();

    /**
     * Visits a specific node based on the passed {@link Visitor} object.
     * @param v The {@link Visitor} we are currently executing.
     */
    public abstract void visit(Visitor v);

    /**
     * Visits every node present in {@link #children}.
     * <p>
     *     This will be the default method executed by each visit method in {@link Visitor}.
     * </p>
     * @param v The {@link Visitor} we are currently executing.
     */
    public void visitChildren(Visitor v) {
        for(AST child : children)
            child.visit(v);
    }

    /**
     * Returns the {@link #text} that the current {@link AST} node is storing.
     * @return String representation of the code fragment the {@link AST} node represents.
     */
    @Override
    public String toString() { return text; }

    /**
     * Checks if 2 AST nodes are equal to each other.
     * <p>
     *     In this case, we will check if both nodes represent the same location in the program
     *     AND they both contain the same exact text.
     * </p>
     * @param node The {@link AST} node we wish to do a comparison with.
     * @return {@code True} if both nodes are equal to each other, {@code False} otherwise.
     */
    public boolean equals(AST node) { return location.equals(node.location) && text.equals(node.text); }

    /**
     * Internal class that will build the metadata for an {@link AST} node.
     */
    protected static class NodeBuilder {

        /**
         * Copies the metadata of an existing {@link AST} node into another {@link AST node}.
         * @param copyNode The {@link AST} node we wish to save the copied metadata into.
         * @param originalNode The {@link AST} node we wish to copy the metadata of.
         * @return {@link NodeBuilder}
         */
        protected NodeBuilder setMetaData(AST copyNode, AST originalNode) {
            copyNode.text = originalNode.text;
            copyNode.location = originalNode.location;
            copyNode.parent = originalNode.parent;
            return this;
        }
    }

    /* #################################### HELPERS #################################### */

    /**
     * Retrieves the closest {@link AST} node that contains the current node.
     * <p>
     *     This method is used when generating errors in order to get the most specific {@link AST} node
     *     that has the current node as a child. The goal is to get additional {@link #text} for the error
     *     message, so the user has a clearer idea about the context in which an error was generated in.
     * </p>
     * @return The {@link AST} node that we will be using when generating an error message.
     */
    public AST getFullLocation() {
        AST currentNode = this;

        while(currentNode.parent != null) {
            currentNode = currentNode.parent;
            if(currentNode.isStatement() && currentNode.asStatement().isExprStmt())
                return currentNode;
        }

        return currentNode;
    }

    /**
     * Retrieves the closest {@link TopLevelDecl} the current node is contained in.
     * @return {@link AST} representing the closest {@link TopLevelDecl} (if it exists).
     */
    public AST getTopLevelDecl() {
        AST currentNode = this;

        while(currentNode.parent != null && !currentNode.isTopLevelDecl())
            currentNode = currentNode.parent;

        return currentNode;
    }

    /**
     * Retrieves the root {@link CompilationUnit} of the current {@link AST} node.
     * @return A {@link CompilationUnit} node representing the root of the current node. If no {@link CompilationUnit}
     * was found, then {@code null} is returned.
     */
    public CompilationUnit getCompilationUnit() {
        AST currentNode = this;

        while(currentNode.parent != null && !(currentNode.isSubNode() && currentNode.asSubNode().isCompilationUnit()))
            currentNode = currentNode.parent;

        if(!(currentNode.isSubNode() && currentNode.asSubNode().isCompilationUnit()))
            return null;

        return currentNode.asSubNode().asCompilationUnit();
    }

    /**
     * Checks if the current AST node is a {@link ClassNode}.
     * @return {@code True} if the node is a {@link ClassNode}, {@code False} otherwise.
     */
    public boolean isClassNode() { return false; }

    /**
     * Checks if the current AST node is an {@link Expression}.
     * @return {@code True} if the node is an {@link Expression}, {@code False} otherwise.
     */
    public boolean isExpression() { return false; }

    /**
     * Checks if the current AST node is an {@link Operator}.
     * @return {@code True} if the node is an {@link Operator}, {@code False} otherwise.
     */
    public boolean isOperator() { return false; }

    /**
     * Checks if the current AST node is a {@link Statement}.
     * @return {@code True} if the node is a {@link Statement}, {@code False} otherwise.
     */
    public boolean isStatement() { return false; }

    /**
     * Checks if the current AST node is a {@link SubNode}.
     * @return {@code True} if the node is a {@link SubNode}, {@code False} otherwise.
     */
    public boolean isSubNode() { return false; }

    /**
     * Checks if the current AST node is a {@link TopLevelDecl}.
     * @return {@code True} if the node is a {@link TopLevelDecl}, {@code False} otherwise.
     */
    public boolean isTopLevelDecl() { return false; }

    /**
     * Checks if the current AST node is a {@link cminor.ast.types.Type}.
     * @return {@code True} if the node is a {@link cminor.ast.types.Type}, {@code False} otherwise.
     */
    public boolean isType() { return false; }

    /**
     * Explicitly casts the current node into a {@link ClassNode}.
     * @return The current node as a {@link ClassNode}.
     */
    public ClassNode asClassNode() {
        throw new RuntimeException("The current node does not represent a class node.");
    }

    /**
     * Explicitly casts the current node into an {@link Expression}.
     * @return The current node as an {@link Expression}.
     */
    public Expression asExpression() {
        throw new RuntimeException("The current node does not represent an expression.");
    }

    /**
     * Explicitly casts the current node into an {@link Operator}.
     * @return The current node as an {@link Operator}.
     */
    public Operator asOperator() {
        throw new RuntimeException("The current node does not represent an operator.");
    }

    /**
     * Explicitly casts the current node into a {@link Statement}.
     * @return The current node as a {@link Statement}.
     */
    public Statement asStatement() {
        throw new RuntimeException("The current node does not represent a statement.");
    }

    /**
     * Explicitly casts the current node into a {@link SubNode}.
     * @return The current node as a {@link SubNode}.
     */
    public SubNode asSubNode() {
        throw new RuntimeException("The current node does not represent a miscellaneous node.");
    }

    /**
     * Explicitly casts the current node into a {@link TopLevelDecl}.
     * @return The current node as a {@link TopLevelDecl}.
     */
    public TopLevelDecl asTopLevelDecl() {
        throw new RuntimeException("The current node does not represent a top level declaration.");
    }

    /**
     * Explicitly casts the current node into a {@link Type}.
     * @return The current node as a {@link Type}.
     */
    public Type asType() {
        throw new RuntimeException("The current node does not represent a data type.");
    }
}
