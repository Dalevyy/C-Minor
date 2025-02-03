package modifier_checker;

import ast.*;
import ast.class_body.*;
import ast.expressions.*;
import ast.top_level_decls.*;
import ast.types.*;

import java.util.HashSet;
import utilities.*;

public class ModifierChecker extends Visitor {

    private SymbolTable currentScope;

    public ModifierChecker() {
        currentScope = null;
    }

    public void sortClassMethods(HashSet<String> abs, HashSet<String> con, ClassDecl cd) {

        if(AST.notNull(cd.superClass())) {
            ClassDecl superClass = currentScope.findName(cd.superClass().typeName()).declName().asTopLevelDecl().asClassDecl();
            sortClassMethods(abs,con,superClass);
        }

        for(String conName : con)
            abs.remove(conName);

        Vector<MethodDecl> allMethods = cd.clalssBlock().methodDecls();
        for(int i = 0; i < allMethods.size(); i++) {
            String currMethod = allMethods.get(i).toString();
            if(cd.mod.isAbstract()) {
                abs.add(currMethod);
                con.remove(currMethod);
            }
            else {
                con.add(currMethod);
                abs.remove(currMethod);
            }
        }
    }

    public void abstractClassImplementation(ClassDecl cd) {
        HashSet<String> concretes = new HashSet<String>();
        HashSet<String> abstracts = new HashSet<String>();
        sortClassMethods(abstracts,concretes,cd);

        if(abstracts.size() > 0) {
            System.out.println(PrettyPrint.RED + "Error! All abstract methods were not implemented.");
            System.exit(1);
        }
    }

    /*
        A C Minor class can have 2 possible modifiers.
            1) Final
            2) Abstract
    */
    public void visitClassDecl(ClassDecl cd) {
        currentScope = cd.symbolTable;

        ClassType hasSuperClass = cd.superClass();
        if(AST.notNull(hasSuperClass)) {
            ClassDecl superClass = currentScope.findName(hasSuperClass.toString()).declName().asTopLevelDecl().asClassDecl();
            if(superClass.mod.isFinal())
                System.exit(1); // ERROR
        }

        super.visitClassDecl(cd);

        if(!cd.mod.isAbstract())
            abstractClassImplementation(cd);

        currentScope = currentScope.closeScope();
    }

    /*
        A FieldDecl must have one of the following modifiers:
            1) Public
            2) Protected
            3) Property
    */
    public void visitFieldDecl(FieldDecl fd) {
        super.visitFieldDecl(fd);


        /*
            If a field was marked as a property, then this means
            the user would like us to generate a getter and setter
            for this field. We will do this during ModifierChecker.
        */
        if(fd.mod.isProperty()) {

        }
    }

    public void visitFieldExpr(FieldExpr fe) {
        ClassDecl cd = currentScope.findName(fe.type.typeName()).declName().asTopLevelDecl().asClassDecl();
        FieldDecl fd = cd.symbolTable.findName(fe.name().toString()).declName().asFieldDecl();

        if(!fd.mod.isPublic()) {
            System.out.println(PrettyPrint.RED + "Error! A field declared as protected or property can not be accessed outside its class.");
            System.exit(1);
        }

        fe.fieldTarget().visit(this);

    }

    public void visitInvocation(Invocation in) {
        if(AST.notNull(in.target())) {
            ClassDecl cd = currentScope.findName(in.target().type.typeName()).declName().asTopLevelDecl().asClassDecl();
            MethodDecl md = cd.symbolTable.findName(in.name().toString()).declName().asMethodDecl();

            if(md.mods.isProtected()) {
                System.out.println(PrettyPrint.RED + "Error! A protected method can not be accessed outside a class");
                System.exit(1);
            }
        }
    }

    public void visitMethodDecl(MethodDecl md) {
        super.visitMethodDecl(md);
    }

    public void visitNewExpr(NewExpr ne) {
        ClassDecl cd = currentScope.findName(ne.type.typeName()).declName().asTopLevelDecl().asClassDecl();

        if(cd.mod.isAbstract()) {
            System.out.println(PrettyPrint.RED + "Error! An abstract class can not be instantiated.");
            System.exit(1); // ERROR
        }

        super.visitNewExpr(ne);
    }

    public void visitUnaryExpr(UnaryExpr ue) {
        ue.expr().visit(this);
    }

}
