package ast.topleveldecls;

import ast.AST;
import ast.misc.Name;
import ast.misc.NameDecl;
import ast.misc.Var;
import ast.types.EnumType;
import token.*;
import utilities.Vector;
import utilities.Visitor;

/**
 * ThisStmt class represents the declaration of an enumeration inside C
 * Minor
 * @author Daniel Levy
 */
public class EnumDecl extends TopLevelDecl implements NameDecl {

    private EnumType type;
    private Name name;
    private Vector<Var> constants;

    public EnumDecl() { this(new Token(),null,new Vector<>()); }
    public EnumDecl(Token t, Name name, Vector<Var> ef) {
        super(t);
        this.name = name;
        this.constants = ef;

        addChild(this.name);
        addChild(this.constants);
    }

    public Name name() { return name; }
    public EnumType type() { return type; }
    public Vector<Var> constants() { return constants;}

    public boolean isEnumDecl() { return true; }
    public EnumDecl asEnumDecl() { return this; }

    public AST getDecl() { return this; }
    public String getDeclName() { return name.toString(); };

    public void setType(EnumType t) { this.type = t; }

    @Override
    public String toString() { return name.toString(); }

    @Override
    public AST deepCopy() {
        Vector<Var> constants = new Vector<>();
        for(Var v : this.constants)
            constants.add(v.deepCopy().asVar());

        return new EnumDeclBuilder()
                   .setMetaData(this)
                   .setName(this.name.deepCopy().asName())
                   .setConstants(constants)
                   .create();
    }

    @Override
    public void visit(Visitor v) { v.visitEnumDecl(this); }

    public static class EnumDeclBuilder extends NodeBuilder {
        private final EnumDecl ed = new EnumDecl();

        /**
         * Copies the metadata of an existing AST node into the builder.
         * @param node AST node we want to copy.
         * @return EnumDeclBuilder
         */
        public EnumDeclBuilder setMetaData(AST node) {
            super.setMetaData(node);
            return this;
        }

        public EnumDeclBuilder setName(Name name) {
            ed.name = name;
            return this;
        }

        public EnumDeclBuilder setConstants(Vector<Var> constants) {
            ed.constants = constants;
            return this;
        }

        public EnumDecl create() {
            super.saveMetaData(ed);
            ed.addChild(ed.name);
            ed.addChild(ed.constants);
            return ed;
        }
    }
}
