package cminor.utilities;

import cminor.ast.classbody.FieldDecl;
import cminor.ast.classbody.MethodDecl;
import cminor.ast.expressions.*;
import cminor.ast.misc.*;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.ast.types.Type;

/**
 * A {@link Visitor} class designed to print out a C Minor parse tree.
 * <p>
 *     This visitor is executed after the execution of the {@link parser.Parser}. A user
 *     will be able to print out how the {@link ast.AST} looks though this visitor is primarily
 *     for debugging purposes, so it needs to be enabled before using.
 * </p>
 * @author Daniel Levy
 */
public class Printer extends Visitor {

    /**
     * A {@link PrinterHelper} to assist with formatting the printer's output.
     */
    private final PrinterHelper helper;

    /**
     * Default constructor for {@link Printer}.
     */
    public Printer() { this.helper = new PrinterHelper(); }

    /**
     * Prints out an {@link ArrayExpr} node.
     * @param ae {@link ArrayExpr}
     */
    public void visitArrayExpr(ArrayExpr ae) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Array Expression");

        ae.getArrayTarget().visit(this);

        helper.printSpaces();
        System.out.println("Indices: ");
        for(Expression index : ae.getArrayIndex())
            index.visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link ArrayLiteral} node.
     * @param al {@link ArrayLiteral}
     */
    public void visitArrayLiteral(ArrayLiteral al) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Array Literal");

        if(!al.getArrayDims().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Array Dimensions: ");

            for(Expression dim : al.getArrayDims())
                dim.visit(this);

            helper.decreaseSpaces();
        }

        if(al.getArrayInits().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Array Initial Values: ");

            for(Expression init : al.getArrayInits())
                init.visit(this);

            helper.decreaseSpaces();
        }

        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link AssignStmt} node.
     * @param as {@link AssignStmt}
     */
    public void visitAssignStmt(AssignStmt as) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Assignment Statement");

        as.getLHS().visit(this);
        as.getRHS().visit(this);

        helper.printSpaces();
        System.out.println("Assignment Operator: " + as.getOperator());

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link BinaryExpr} node.
     * @param be {@link BinaryExpr}
     */
    public void visitBinaryExpr(BinaryExpr be) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Binary Expression");

        be.getLHS().visit(this);
        be.getRHS().visit(this);

        helper.printSpaces();
        System.out.println("Binary Operator: " + be.getBinaryOp());
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link BlockStmt} node.
     * @param bs {@link BlockStmt}
     */
    public void visitBlockStmt(BlockStmt bs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Block Statement");

        for(LocalDecl ld : bs.getLocalDecls())
            ld.visit(this);

        for(Statement s : bs.getStatements())
            s.visit(this);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link BreakStmt} node.
     * @param bs {@link BreakStmt}
     */
    public void visitBreakStmt(BreakStmt bs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Break Statement");
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link CaseStmt} node.
     * @param cs {@link CaseStmt}
     */
    public void visitCaseStmt(CaseStmt cs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Case Statement");

        cs.getLabel().visit(this);
        cs.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link CastExpr} node.
     * @param ce {@link CastExpr}
     */
    public void visitCastExpr(CastExpr ce) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Cast Expression");

        helper.printSpaces();
        System.out.println("Case Type: " + ce.getCastType());

        ce.getCastExpr().visit(this);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link CaseStmt} node.
     * @param cs {@link CaseStmt}
     */
    public void visitChoiceStmt(ChoiceStmt cs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Choice Statement");

        cs.getChoiceValue().visit(this);

        if(!cs.getCases().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Cases: ");

            for(CaseStmt currCase : cs.getCases())
                currCase.visit(this);

            helper.decreaseSpaces();
        }

        cs.getDefaultBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ClassDecl} node.
     * @param cd {@link ClassDecl}
     */
    public void visitClassDecl(ClassDecl cd) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Class Declaration");

        helper.printSpaces();
        System.out.println("Modifier: " + cd.mod);

        helper.printSpaces();
        System.out.println("Name: " + cd.getDeclName());

        for(TypeParam tp : cd.getTypeParams())
            tp.visit(this);

        if(cd.getSuperClass() != null) {
            helper.printSpaces();
            System.out.println("Super Class: " + cd.getSuperClass());
        }

        cd.getClassBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link CompilationUnit} node.
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Compilation Unit");

        super.visitCompilationUnit(cu);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ContinueStmt} node.
     * @param cs {@link ContinueStmt}
     */
    public void visitContinueStmt(ContinueStmt cs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Continue Statement");
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link DoStmt} node.
     * @param ds {@link DoStmt}
     */
    public void visitDoStmt(DoStmt ds) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Do Statement");

        ds.getBody().visit(this);
        ds.getCondition().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link EndlStmt} node.
     * @param es {@link EndlStmt}
     */
    public void visitEndlStmt(EndlStmt es) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Endl Statement");
        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link EnumDecl} node.
     * @param ed {@link EnumDecl}
     */
    public void visitEnumDecl(EnumDecl ed) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Enum Declaration");

        helper.printSpaces();
        System.out.println("Name: " + ed.getDeclName());

        for(Var v : ed.getConstants())
            v.visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link FieldDecl} node.
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Field Declaration");

        helper.printSpaces();
        System.out.println("Modifier: " + fd.mod);

        super.visitFieldDecl(fd);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link FieldExpr} node.
     * @param fe {@link FieldExpr}
     */
    public void visitFieldExpr(FieldExpr fe) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Field Expression");

        fe.getTarget().visit(this);
        fe.getAccessExpr().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ForStmt} node.
     * @param fs {@link ForStmt}
     */
    public void visitForStmt(ForStmt fs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("For Statement");

        fs.getControlVariable().visit(this);
        fs.getStartValue().visit(this);
        System.out.println("Loop Operator: " + fs.getLoopOperator());
        fs.getEndValue().visit(this);
        fs.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link FuncDecl} node.
     * @param fd {@link FuncDecl}
     */
    public void visitFuncDecl(FuncDecl fd) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Function Declaration");

        for(TypeParam tp : fd.getTypeParams())
            tp.visit(this);

        helper.printSpaces();
        System.out.println("Modifier: " + fd.mod);

        helper.printSpaces();
        System.out.println("Name: " + fd.getDeclName());

        if(!fd.getParams().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Parameters: ");

            for(ParamDecl pd : fd.getParams())
                pd.visit(this);

            helper.decreaseSpaces();
        }

        fd.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link GlobalDecl} node.
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Global Declaration");

        super.visitGlobalDecl(gd);
        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link IfStmt} node.
     * @param is {@link IfStmt}
     */
    public void visitIfStmt(IfStmt is) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("If Statement");

        is.getCondition().visit(this);
        is.getIfBody().visit(this);

        if(!is.getElifs().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Else If Branches: ");

            for(IfStmt elif : is.getElifs())
                elif.visit(this);

            helper.decreaseSpaces();
        }

        if(is.getElseBody() != null)
            is.getElseBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link ImportDecl} node.
     * @param id {@link ImportDecl}
     */
    public void visitImportDecl(ImportDecl id) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Import Declaration");
        id.getCompilationUnit().visit(this);
        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link InStmt} node.
     * @param ins {@link InStmt}
     */
    public void visitInStmt(InStmt ins) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Input Statement");

        for(Expression input : ins.getInExprs())
            input.visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out an {@link Invocation} node.
     * @param in {@link Invocation}
     */
    public void visitInvocation(Invocation in) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Invocation");

        helper.printSpaces();
        System.out.println("Name: " + in.getName());

        if(!in.getTypeArgs().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Type Arguments: ");

            for(Type t : in.getTypeArgs()) {
                helper.printSpaces();
                System.out.println("Type: " + t);
            }
            helper.decreaseSpaces();
        }

        if(!in.getArgs().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Arguments: ");

            for(Expression arg : in.getArgs())
                arg.visit(this);

            helper.decreaseSpaces();
        }

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link Label} node.
     * @param l {@link Label}
     */
    public void visitLabel(Label l) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Label");

        l.getLeftConstant().visit(this);

        if(l.getRightConstant() != null)
            l.getRightConstant().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ListLiteral} node.
     * @param ll {@link ListLiteral}
     */
    public void visitListLiteral(ListLiteral ll) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("List Literal");

        for(Expression init : ll.getInits())
            init.visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ListStmt} node.
     * @param ls {@link ListStmt}
     */
    public void visitListStmt(ListStmt ls) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("List Statement");

        helper.printSpaces();
        System.out.println("Command: " + ls);

        if(!ls.getAllArgs().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Arguments: ");

            for(Expression arg : ls.getAllArgs())
                arg.visit(this);

            helper.decreaseSpaces();
        }

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link Literal} node.
     * @param li {@link Literal}
     */
    public void visitLiteral(Literal li) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Literal: " + li);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link LocalDecl} node.
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        helper.printSpaces();

        System.out.println("Local Declaration");
        helper.increaseSpaces();

        super.visitLocalDecl(ld);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link MainDecl} node.
     * @param md {@link MainDecl}
     */
    public void visitMainDecl(MainDecl md) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Main Declaration");

        if(!md.getParams().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Parameters: ");

            for(ParamDecl pd : md.getParams())
                pd.visit(this);

            helper.decreaseSpaces();
        }

        helper.printSpaces();
        System.out.println("Return Type: " + md.getReturnType());

        md.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link MethodDecl} node.
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Method Declaration");

        helper.printSpaces();
        System.out.println("Modifier: " + md.mod);

        helper.printSpaces();
        if(!md.isOperatorOverload())
            System.out.println("Name: " + md.getDeclName());
        else
            System.out.println("Operator: " + md.getOperatorOverload());

        if(!md.getParams().isEmpty()) {
            helper.printSpaces();
            helper.increaseSpaces();
            System.out.println("Parameters: ");

            for(ParamDecl pd : md.getParams())
                pd.visit(this);

            helper.decreaseSpaces();
        }

        helper.printSpaces();
        System.out.println("Return Type: " + md.getReturnType());

        md.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link NameExpr} node.
     * @param ne {@link NameExpr}
     */
    public void visitNameExpr(NameExpr ne) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Name Expression: " + ne);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link NewExpr} node.
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("New Expression");

        helper.printSpaces();
        System.out.println("Object Type: " + ne.getClassType());

        if(!ne.getInitialFields().isEmpty()) {
            helper.printSpaces();
            System.out.println("Object Fields:");
            helper.increaseSpaces();

            for(Var v : ne.getInitialFields())
                v.visit(this);

            helper.decreaseSpaces();
        }

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link OutStmt} node.
     * @param os {@link OutStmt}
     */
    public void visitOutStmt(OutStmt os) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Output Statement");

        for(Expression output : os.getOutExprs())
            output.visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ParamDecl} node.
     * @param pd {@link ParamDecl}
     */
    public void visitParamDecl(ParamDecl pd) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Parameter Declaration");

        helper.printSpaces();
        System.out.println("Modifier: " + pd.mod);

        helper.printSpaces();
        System.out.println("Name: " + pd.getDeclName());

        helper.printSpaces();
        System.out.println("Type: " + pd.getType());

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link ReturnStmt} node.
     * @param rs {@link ReturnStmt}
     */
    public void visitReturnStmt(ReturnStmt rs) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Return Statement");

        rs.getReturnValue().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link StopStmt} node.
     * @param ss {@link StopStmt}
     */
    public void visitStopStmt(StopStmt ss) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Stop Statement");
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link TypeParam} node.
     * @param tp {@link TypeParam}
     */
    public void visitTypeParam(TypeParam tp) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Type Parameter");

        if(tp.getPossibleType() != null) {
            helper.printSpaces();
            System.out.println("Type Annotation: " + tp.getPossibleTypeAsString());
        }

        helper.printSpaces();
        System.out.println("Name: " + tp.getDeclName());

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link UnaryExpr} node.
     * @param ue {@link UnaryExpr}
     */
    public void visitUnaryExpr(UnaryExpr ue) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Unary Expression");

        ue.getExpr().visit(this);

        helper.printSpaces();
        System.out.println("Unary Operator: " + ue.getUnaryOp());

        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link Var} node.
     * @param v {@link Var}
     */
    public void visitVar(Var v) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("Variable");

        helper.printSpaces();
        System.out.println("Name: " + v.getVariableName());

        if(v.getDeclaratedType() != null) {
            helper.printSpaces();
            System.out.println("Type: " + v.getDeclaratedType());
        }

        v.getInitialValue().visit(this);
        helper.decreaseSpaces();
    }

    /**
     * Prints out a {@link WhileStmt} node.
     * @param ws {@link WhileStmt}
     */
    public void visitWhileStmt(WhileStmt ws) {
        helper.printSpaces();
        helper.increaseSpaces();
        System.out.println("While Statement");

        ws.getCondition().visit(this);
        ws.getBody().visit(this);

        helper.decreaseSpaces();
    }

    /**
     * A helper class for {@link Printer}.
     */
    private static class PrinterHelper {

        /**
         * The number of spaces that need to be printed out. (Used for formatting!)
         */
        private int spaces;

        /**
         * Default constructor for {@link PrinterHelper}.
         */
        public PrinterHelper() { this.spaces = 0; }

        /**
         * Prints a series of statements to organize and make the tree's presentation better.
         */
        public void printSpaces() {
            StringBuilder s = new StringBuilder();

            for(int i = 0; i < spaces; i++) {
                if(i%2==0)
                    s.append("|");
                s.append(" ");
            }

            s.append("+-- ");
            System.out.print(s);
        }

        /**
         * Increases the number of spaces that will be used in the output.
         */
        public void increaseSpaces() { spaces += 2; }

        /**
         * Removes spaces from the output when we are changing which level we are at in the tree.
         */
        public void decreaseSpaces() { spaces -= 2; }
    }
}
