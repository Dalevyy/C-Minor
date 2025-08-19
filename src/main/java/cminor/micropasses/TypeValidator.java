package cminor.micropasses;

import cminor.ast.classbody.*;
import cminor.ast.expressions.*;
import cminor.ast.misc.*;
import cminor.ast.statements.*;
import cminor.ast.topleveldecls.*;
import cminor.ast.types.*;
import cminor.ast.AST;
import cminor.ast.misc.ParamDecl;
import cminor.ast.statements.LocalDecl;
import cminor.ast.topleveldecls.ClassDecl;
import cminor.ast.topleveldecls.EnumDecl;
import cminor.ast.types.ClassType;
import cminor.ast.types.EnumType;
import cminor.ast.types.Type;
import cminor.ast.types.EnumType.EnumTypeBuilder;
import cminor.messages.MessageHandler;
import cminor.messages.MessageNumber;
import cminor.messages.errors.type.TypeError;
import cminor.utilities.SymbolTable;
import cminor.utilities.Vector;
import cminor.utilities.Visitor;

//TODO: Check if a field has type of its own class or something recursively
//TODO: Make sure a field is not initialized to a value in PropertyGenerator?

/**
 * A micropass checking if all types used in a program are valid.
 * <p><br>
 *     Any time a name is used as a type in a C Minor program, the parser will always treat the name as
 *     a {@link ClassType}. Thus, we need to do a pass over each type to make sure it is indeed valid,
 *     and the type can be traced back to some {@link ast.topleveldecls.TopLevelDecl}.This pass will include
 *     performing type rewrites in order to create {@link EnumType}s alongside the checking for the valid
 *     usage of template classes and functions. This pass needs to be completed before type checking to
 *     ensure that the {@link typechecker.TypeChecker} can properly evaluate all types used for variables and templates.
 * </p>
 * @author  Daniel Levy
 */
public class TypeValidator extends Visitor {

    /**
     * Represents the current scope we are in.
     * Only set by {@link #visitClassDecl(ClassDecl)} and {@link #visitFuncDecl(FuncDecl)}
     */
    private SymbolTable currentScope;

    private TypeValidatorHelper helper;

    /**
     * Flag to denote whether we are visiting a template class or function.
     */
    private boolean insideTemplate;

    /**
     * Creates {@link TypeValidator} in compilation mode.
     */
    public TypeValidator() {
        this.currentScope = null;
        this.insideTemplate = false;
        this.handler = new MessageHandler();
        this.helper = new TypeValidatorHelper();
    }

    /**
     * Creates {@link TypeValidator} in interpretation mode.
     * @param globalScope The global {@link SymbolTable} that will be stored in {@link #currentScope}.
     */
    public TypeValidator(SymbolTable globalScope) {
        this();
        this.currentScope = globalScope;
    }

    /**
     * Sets the current scope to be a class.
     * @param cd Class Declaration
     */
    public void visitClassDecl(ClassDecl cd) {
        if(cd.isTemplate()) {
            insideTemplate = true;
            helper.set(cd.getTypeParams());
        }

        currentScope = cd.getScope();
        super.visitClassDecl(cd);
        currentScope = currentScope.closeScope();
    }

    /**
     * Begins the C Minor type validation phase in compilation mode.
     * <p>
     *     We want to ensure that all imported files are validated first
     *     prior to running this phase on the main compilation unit.
     * </p>
     * @param cu {@link CompilationUnit}
     */
    public void visitCompilationUnit(CompilationUnit cu) {
        for(ImportDecl id : cu.getImports())
            id.getCompilationUnit().visit(this);

        currentScope = cu.getScope();
        super.visitCompilationUnit(cu);
    }


    /**
     * Validates a field variable's type.
     * <p>
     *     When visiting a field variable, we need to check if its declared type represents
     *     a valid type. Even if the type is valid, we also have to make sure the variable
     *     does not create any recursion by referencing the class in the type. This visit
     *     will handle the direct recursive case error handling.
     * </p>
     * @param fd {@link FieldDecl}
     */
    public void visitFieldDecl(FieldDecl fd) {
        fd.setType(helper.verifyType(fd.getType()));

        Type fieldType = fd.getType();
        while(fieldType.isArray() || fieldType.isList())
            fieldType = fieldType.asArray().getBaseType();
        System.out.println(fieldType);

        if(fieldType.isClass()) {
            ClassDecl cd = fd.getClassDecl();

            // ERROR CHECK #1: A class is not allowed to contain a field that references itself.
            if(fieldType.asClass().getClassName().equals(cd.getName())) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(fd)
                       .addErrorNumber(MessageNumber.TYPE_ERROR_466)
                       .addErrorArgs(fd, cd)
                       .generateError();
            }

            // ERROR CHECK #2: A class can not have a field that references an inherited type.
            while(cd.getSuperClass() != null) {
                cd = currentScope.findName(cd.getSuperClass().getClassName()).asTopLevelDecl().asClassDecl();

                if(fd.getType().asClass().getClassName().equals(cd.getName())) {
                    handler.createErrorBuilder(TypeError.class)
                            .addLocation(fd)
                            .addErrorNumber(MessageNumber.TYPE_ERROR_467)
                            .addErrorArgs(fd, cd)
                            .generateError();
                }
            }
        }
    }

    /**
     * Validates a function's return type.
     * <p><br>
     *     During this visit, we will make sure every single type declared
     *     in a function is correct. This mainly includes making sure that
     *     no names will shadow over any type parameters.
     * </p>
     * @param fd {@link FuncDecl}
     */
    public void visitFuncDecl(FuncDecl fd) {
        for(ParamDecl pd : fd.getParams())
            pd.visit(this);

        fd.setReturnType(helper.verifyType(fd.getReturnType()));

        fd.getBody().visit(this);
    }

    /**
     * Validates a global variable's type.
     * <p>
     *     For a global variable, we only need to check if its declared type represents
     *     a valid type. No other checks will be done during this visit.
     * </p>
     * @param gd {@link GlobalDecl}
     */
    public void visitGlobalDecl(GlobalDecl gd) {
        gd.setType(helper.verifyType(gd.getType()));
        if(gd.getInitialValue() != null)
            gd.getInitialValue().visit(this);
    }


    /**
     * Validates the type of a local variable.
     * <p><br>
     *     This visit will rewrite the type of the {@link LocalDecl} if needed. Additionally,
     *     we will check if the local's name shadows a previously defined type parameter
     *     when we are visiting either a template function or class.
     * </p>
     * @param ld {@link LocalDecl}
     */
    public void visitLocalDecl(LocalDecl ld) {
        ld.setType(helper.verifyType(ld.getType()));
        if(ld.getInitialValue() != null)
            ld.getInitialValue().visit(this);
    }

    /**
     * Validates the type of a method.
     * <p><br>
     *     During this visit, we want to make sure the method's parameters do not conflict
     *     with any previously declared type parameters, and we also want to see if we need
     *     to rewrite its return type. From there, we will just validate the types within
     *     its {@link ast.statements.BlockStmt}.
     * </p>
     * @param md {@link MethodDecl}
     */
    public void visitMethodDecl(MethodDecl md) {
        for(ParamDecl pd : md.getParams())
            pd.visit(this);

        md.setReturnType(helper.verifyType(md.getReturnType()));
        md.getBody().visit(this);
    }

    /**
     * Validates the type of a new expression.
     * <p><br>
     *     When a user tries to instantiate a template class, we will check to make sure
     *     the template type is valid and then proceed to instantiate the class for the user.
     *     We will bind the instantiated class to the {@link NewExpr} instead of a variable.
     * </p>
     * @param ne {@link NewExpr}
     */
    public void visitNewExpr(NewExpr ne) {
        if(ne.createsFromTemplate()) {
            //checkIfTemplateTypeIsValid(ne.getClassType());
           // ne.setInstantiatedClass(instantiatesClassTemplate(ne.getClassType()));
        }
    }

    /**
     * Validates the type of a parameter.
     * <p><br>
     *     This visit will rewrite the type of the {@link ParamDecl} if needed. Additionally,
     *     we will check if the parameter's name shadows a previously defined type parameter
     *     when we are visiting methods inside of a template class.
     * </p>
     * @param pd {@link ParamDecl}
     */
    public void visitParamDecl(ParamDecl pd) { pd.setType(helper.verifyType(pd.getType())); }

    private class TypeValidatorHelper {

        /**
         * {@link Vector} containing all possible type parameters (only set when visiting a template).
         */
        private Vector<TypeParam> typeParams;

        private void set(Vector<TypeParam> typeParams) { this.typeParams = typeParams; }

        private void reset() { typeParams = null; }

        /**
         * Performs a potential rewrite on a given structured type.
         * <p>
         *     A structured type in C Minor will represent either an Array, List, or Class type.
         *     The goal of this method is to figure out what potential rewrite needs to be done
         *     for the given type. There are 2 possible scenarios:
         *     <ol>
         *         <li>
         *             Class Types
         *          <p>
         *             By default, any name used as a type in C Minor will automatically be parsed
         *             as a {@link ClassType}. This is not ideal as a type name can represent either
         *             a {@link ClassDecl}, an {@link EnumDecl}, or a type parameter. Thus, we need
         *             to make sure this type is rewritten if it does not correspond to a {@link ClassDecl}
         *             which is the job of {@link #verifyClassType(ClassType)}.
         *         </p>
         *         </li>
         *         <li>
         *             Array or List Types
         *         <p>
         *             When we have an Array or List type, the base type can represent a {@link ClassType}.
         *             Thus, the previous criteria above will apply, and this means we need to check if
         *             we need to perform a rewrite on the base type through {@link #verifyClassType(ClassType)}.
         *         </p>
         *         </li>
         *     </ol>
         * </p>
         * @param structuredType {@link Type} we might need to perform a rewrite of
         * @return {@link Type} => This will return any type.
         */
        private Type verifyType(Type type) {
            // Ignore all non-structured types since they are fine.
            if(!type.isStructured())
                return type;

            // For Arrays and Lists, check if the base type needs to be rewritten if it's a class.
            if(type.isArray() || type.isList()) {
                if(type.asArray().getBaseType().isClass())
                    type.asArray().setBaseType(rewriteClassType(type.asArray().getBaseType().asClass()));
                return type;
            }

            return rewriteClassType(type.asClass());
        }

        private Type rewriteClassType(ClassType ct) {
            /*
                ERROR CHECK #1: The name checker did not resolve the names of class types. This means we will
                                now check if the current class type can resolve to either a class, an enum, or
                                a type parameter. If no resolution can be made, we will print out a type error.
            */
            if(!currentScope.hasNameInProgram(ct)) {
                handler.createErrorBuilder(TypeError.class)
                       .addLocation(ct.getFullLocation())
                       .addErrorNumber(MessageNumber.TYPE_ERROR_443)
                       .addErrorArgs(ct)
                       .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1440)
                       .addSuggestionArgs(ct)
                       .generateError();
            }

            AST typeDeclaration = currentScope.findName(ct.getClassName());

            // Case 1: The type did not correspond to an Enum or class. This means it might be a type parameter.
            if(!typeDeclaration.isTopLevelDecl()) {
                handler.createErrorBuilder(TypeError.class)
                        .addLocation(ct.getFullLocation())
                        .addErrorNumber(MessageNumber.TYPE_ERROR_465)
                        .addErrorArgs(ct)
                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1452)
                        .generateError();
            }

            // Case 2: The given class type derives from an Enum, so rewrite the type into an enum type.
            if(typeDeclaration.asTopLevelDecl().isEnumDecl()) {
                return new EnumTypeBuilder()
                           .setMetaData(ct)
                           .setName(typeDeclaration.asTopLevelDecl().asEnumDecl().getName())
                           .create();
            }

            // Case 3: The given class type is valid, so no rewrite is needed.
            return ct;
        }

        /**
         * Checks if the class type was written correctly by the user.
         * <p>
         *     Since the parser will treat all names used as types as a {@link ClassType},
         *     this method is designed to verify if all {@link ClassType}s are valid. There
         *     are 3 cases we need to deal with:
         *     <ol>
         *         <li>
         *             Enum Types
         *             <p><br>
         *                 Since the parser does not distinguish between a class and enum type,
         *                 we need to perform a type rewrite to generate an enum type any time
         *                 a class type refers back to an enum.
         *             </p>
         *         </li>
         *         <li>
         *             Template Types
         *             <p><br>
         *                 If the class type represents a template type, then we need to make sure
         *                 the user correctly wrote the template type, so we can eventually evaluate
         *                 the type when a class or function is instantiated. Additionally, this method
         *                 will be responsible for creating types during the instantiation of a class/function.
         *             </p>
         *         </li>
         *         <li>
         *             Class Types
         *             <p><br>
         *                 For all other class types, we just need to make sure they exist in the program.
         *             </p>
         *         </li>
         *     </ol>
         * </p>
         * @param ct Current {@link ClassType} we are evaluating
         * @return A {@link Type} representing either the original type or a type that was created
         */
/*
        private Type verifyClassType(ClassType ct) {
*/



            /*
                Special Case: If we are instantiating a template class, we want to replace every
                type parameter with the appropriate type argument. Thus, we will look through the
                template's type parameters and find which one corresponds to the current template
                type. From there, we replace the template type with the passed type argument.
             */
////            if(currentTypeArgs != null) {
////                for(int i = 0; i < currentTypeParams.size(); i++) {
////                    TypeParam typeParam = currentTypeParams.get(i);
////                    if(typeParam.equals(ct.toString()))
////                        return Type.instantiateType(ct, currentTypeArgs.get(i));
////                }
////            }
//
//            AST classTypeDecl = currentScope.findName(ct.getClassNameAsString()).getDecl();
//
//
//            else if(classTypeDecl.isTopLevelDecl() && classTypeDecl.asTopLevelDecl().isClassDecl()) {
//                // Case 2: The class type represents a template class.
//                if(ct.isTemplatedType())
//                    checkIfTemplateTypeIsValid(ct);
//            }
//            // ERROR CHECK #2: If the class type does not represent an enum, a class, or a type parameter,
//            //                 then this means a variable name was used as a type which is not allowed.
//            else if(!classTypeDecl.isSubNode() || !classTypeDecl.asSubNode().isTypeParam()) {
//            }
//
//            // Case 3: The class type represents a normal class. No rewrite is needed so return the class type.
//            return ct;
//        }

//
//        /**
//         * Checks if a variable name corresponds to a type parameter name.
//         * <p><br>
//         *     Similarly to how C++ handles the scope resolution of type parameters,
//         *     C Minor will follow a similar approach and prevent a user from shadowing
//         *     any type parameter by using the parameter's name in a different construct
//         *     within a templated class or function. This method will check for us whether
//         *     or not a name shadows a type parameter, so we can know if we need to
//         *     generate an error message to the user.
//         * </p>
//         * @param name String representation of a variable name
//         * @return Boolean
//         */
//        private boolean nameShadowsTypeParam(String name) {
//            for(TypeParam tp : currentTypeParams)
//                if(name.equals(tp.toString()))
//                    return true;
//
//            return false;
//        }
//
//
//        /**
//         * Verifies the validity of a template type.
//         * <p><br>
//         *     If a user writes a template type in place of a class type, this method will
//         *     perform the necessary error checks to ensure the template type was written
//         *     correctly based on its class definition. The {@link typechecker.TypeChecker}
//         *     will handle the rest of the type errors related to assignment compatibility.
//         * </p>
//         * @param ct {@link ClassType} representing a potential template type
//         */
//        private void checkIfTemplateTypeIsValid(ClassType ct) {
//            ClassDecl template = currentScope.findName(ct.getClassNameAsString()).getDecl().asTopLevelDecl().asClassDecl();
//
//            // ERROR CHECK #1: When a template type is written, we want to make sure the correct number of
//            //                 type arguments were passed. This will be based on the number of type parameters
//            //                 the template class was declared with. There are 2 possible errors here.
//            if(template.getTypeParams().size() != ct.typeArgs().size()) {
//                // Case 1: This error is generated when a user writes type arguments for a non-template class type.
//                if(template.getTypeParams().isEmpty()) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(ct.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_444)
//                            .addErrorArgs(template)
//                            .generateError();
//                }
//                // Case 2: This error is generated when the wrong number of type arguments were used for a template class type.
//                else {
//                    ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                            .addLocation(ct.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_445)
//                            .addErrorArgs(template)
//                            .addSuggestionArgs(template, template.getTypeParams().size());
//
//                    if(template.getTypeParams().size() == 1)
//                        eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1441).generateError();
//                    else
//                        eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1442).generateError();
//                }
//            }
//
//            // We now look through each type parameter of the template class.
//            for (int i = 0; i < template.getTypeParams().size(); i++) {
//                TypeParam typeParam = template.getTypeParams().get(i);
//
//                // ERROR CHECK #2: If a user prefixed the type parameter with a type annotation, then we will check if
//                //                 the passed type argument can be used in the current type argument. If no type annotation
//                //                 was given, this check is not needed, and we will let the type checker handle the rest.
//                if(!typeParam.isValidTypeArg(ct.typeArgs().get(i))) {
//                    handler.createErrorBuilder(TypeError.class)
//                            .addLocation(ct.getFullLocation())
//                            .addErrorNumber(MessageNumber.TYPE_ERROR_446)
//                            .addErrorArgs(ct.typeArgs().get(i), template)
//                            .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1443)
//                            .addSuggestionArgs(template, typeParam.getPossibleType(), i + 1)
//                            .generateError();
//                }
//            }
//        }
//
//        /**
//         * Creates an instance of a template class based on the given type arguments.
//         * <p><br>
//         *     This method is responsible for actually generating an instance of a template
//         *     class that will then be bound to a {@link NewExpr}. We will create a copy of
//         *     the template class and replace all of its type parameters with the given type
//         *     arguments.
//         * </p>
//         * @param ct Template type that we use to instantiate a template class
//         * @return {@link ClassDecl} representing the instantiated class
//         */
//        private ClassDecl instantiatesClassTemplate(ClassType ct) {
//            // If the template class was already instantiated with the given type arguments,
//            // then we don't want to reinstantiate it and instead get the instantiated class.
//            if(instantiatedClasses.contains(ct.toString()))
//                return currentScope.findName(ct).getDecl().asTopLevelDecl().asClassDecl();
//
//            ClassDecl template = currentScope.findName(ct.getClassNameAsString()).getDecl().asTopLevelDecl().asClassDecl();
//            ClassDecl copyOfTemplate = template.deepCopy().asTopLevelDecl().asClassDecl();
//
//            // We need to rerun the name checker, so the copy of the template can
//            // have an independent symbol table from the original template class.
//            copyOfTemplate.visit(new NameChecker(""));
//
//            // We now visit the copy's class in order to replace all type parameters with the given type arguments.
//            currentTypeArgs = ct.typeArgs();
//            copyOfTemplate.visit(this);
//
//            currentTypeArgs = null;
//            copyOfTemplate.removeTypeParams();
//
//            instantiatedClasses.add(ct.toString());
//            currentScope.addNameToRootTable(ct.toString(), copyOfTemplate);
//
//            return copyOfTemplate;
//        }
//
//        /**
//         * Creates an instance of a template function based on the given type arguments.
//         * <p><br>
//         *     This method is responsible for actually generating an instance of a template
//         *     function that will then be bound to an {@link Invocation}. We will create a copy of
//         *     the template function and replace all of its type parameters with the given type
//         *     arguments. This method is identical in structure to {@link #instantiatesClassTemplate(ClassType)},
//         *     but we will be calling this method from the {@link typechecker.TypeChecker} since we
//         *     need to know the types of each passed argument to the invocation, so we can find the correct
//         *     overloaded template function to instantiate.
//         * </p>
//         * @param template Template function we will be instantiating
//         * @param in {@link Invocation} containing the type arguments we will use to instantiate the template with
//         * @return {@link FuncDecl} representing the instantiated function
//         */
//        public FuncDecl instantiatesFuncTemplate(FuncDecl template, Invocation in) {
//            // If the template function was already instantiated with the given type arguments, then we don't want to
//            // reinstantiate it and instead get the already instantiated function. Since a user could overload a template
//            // function with a more specific parameter type, we will also check if there isn't an already matching signature
//            // in the root table for the invocation. If this signature exists, then this implies there is a more specific
//            // template available, thus it needs to be instantiated instead of using the one already available for us.
//            if(instantiatedFunctions.contains(in.templateSignature()) && !currentScope.getRootTable().hasName(in.getSignature()))
//                return currentScope.findName(in.templateSignature()).getDecl().asTopLevelDecl().asFuncDecl();
//
//            FuncDecl copyOfTemplate = template.deepCopy().asTopLevelDecl().asFuncDecl();
//
//            // We need to rerun the name checker, so the copy of the template can
//            // have an independent symbol table from the original template function.
//            copyOfTemplate.visit(new NameChecker(""));
//
//            // We now visit the copy's function in order to replace all type parameters with the given type arguments.
//            currentTypeArgs = in.getTypeArgs();
//            copyOfTemplate.visit(this);
//
//            currentTypeArgs = null;
//            copyOfTemplate.removeTypeParams();
//
//            instantiatedFunctions.add(in.templateSignature());
//            currentScope.addNameToRootTable(in.templateSignature(), copyOfTemplate);
//
//            // If we instantiated a more specific template function, then we want to remove the less specific instantiation
//            // from the symbol table. This prevents us from having to reinstantiate the function we just created.
//            if(currentScope.getRootTable().hasName(in.getSignature()))
//                currentScope.getRootTable().removeName(in.getSignature());
//
//            return copyOfTemplate;
//        }

    }

}
