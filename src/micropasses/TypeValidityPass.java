package micropasses;

import ast.AST;
import ast.classbody.FieldDecl;
import ast.classbody.MethodDecl;
import ast.expressions.Invocation;
import ast.expressions.NewExpr;
import ast.misc.CompilationUnit;
import ast.misc.ParamDecl;
import ast.misc.TypeParam;
import ast.statements.LocalDecl;
import ast.topleveldecls.*;
import ast.types.ClassType;
import ast.types.DiscreteType.Discretes;
import ast.types.EnumType;
import ast.types.EnumType.EnumTypeBuilder;
import ast.types.Type;
import messages.MessageHandler;
import messages.MessageNumber;
import messages.errors.ErrorBuilder;
import messages.errors.scope.ScopeError;
import messages.errors.type.TypeError;
import namechecker.NameChecker;
import utilities.SymbolTable;
import utilities.Vector;
import utilities.Visitor;

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
public class TypeValidityPass extends Visitor {
//
//    /**
//     * Represents the current scope we are in.
//     * Only set by {@link #visitClassDecl(ClassDecl)} and {@link #visitFuncDecl(FuncDecl)}
//     */
//    private SymbolTable currentScope;
//
//    /**
//     * List containing all the type parameters belonging to a template class or function.
//     */
//    private Vector<TypeParam> currentTypeParams;
//
//    /**
//     * List containing the type arguments used to instantiate a template class or function.
//     */
//    private Vector<Type> currentTypeArgs;
//
//    /**
//     * List of all classes that were instantiated.
//     */
//    private final Vector<String> instantiatedClasses;
//
//    /**
//     * List of all functions that were instantiated.
//     */
//    private final Vector<String> instantiatedFunctions;
//
//
//    /**
//     * Creates type validity micropass in compilation mode.
//     */
//    public TypeValidityPass(String fileName) {
//        this.currentScope = null;
//        this.instantiatedClasses = new Vector<>();
//        this.instantiatedFunctions = new Vector<>();
//        this.handler = new MessageHandler(fileName);
//    }
//
//    /**
//     * Creates type validity micropass in interpretation mode.
//     * @param st Compilation Unit Symbol Table
//     */
//    public TypeValidityPass(SymbolTable st) {
//        this.instantiatedClasses = new Vector<>();
//        this.instantiatedFunctions = new Vector<>();
//        this.currentScope = st;
//        this.handler = new MessageHandler();
//    }
//
//    /* ######################################## HELPERS ######################################## */
//
//    /**
//     * Creates a new {@link EnumType} node.
//     * <p><br>
//     *     This method creates an {@code EnumType} to represent an {@link EnumDecl}.
//     *     An {@code EnumType} will contain the name of the enum followed by the type
//     *     of the constant values the enum will represent.
//     * </p>
//     * @param metaData {@link ClassType} we are converting into an {@link EnumType}
//     * @param ed {@link EnumDecl} we are building the {@code EnumType} around
//     * @return Rewritten type now representing an {@link EnumType}
//     */
//    private EnumType buildEnumType(ClassType metaData, EnumDecl ed) {
//        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder();
//
//        if(ed.getConstantType().asEnumType().constantType().isInt())
//            enumTypeBuilder.setConstantType(Discretes.INT);
//        else
//            enumTypeBuilder.setConstantType(Discretes.CHAR);
//
//        enumTypeBuilder.setName(ed.getName());
//        enumTypeBuilder.setMetaData(metaData);
//        return enumTypeBuilder.create();
//    }
//
//    /**
//     * Checks if a variable name corresponds to a type parameter name.
//     * <p><br>
//     *     Similarly to how C++ handles the scope resolution of type parameters,
//     *     C Minor will follow a similar approach and prevent a user from shadowing
//     *     any type parameter by using the parameter's name in a different construct
//     *     within a templated class or function. This method will check for us whether
//     *     or not a name shadows a type parameter, so we can know if we need to
//     *     generate an error message to the user.
//     * </p>
//     * @param name String representation of a variable name
//     * @return Boolean
//     */
//    private boolean nameShadowsTypeParam(String name) {
//        for(TypeParam tp : currentTypeParams)
//            if(name.equals(tp.toString()))
//                return true;
//
//        return false;
//    }
//
//    /**
//     * Performs a potential rewrite on a given structured type.
//     * <p>
//     *     A structured type in C Minor will represent either an Array, List, or Class type.
//     *     The goal of this method is to figure out what potential rewrite needs to be done
//     *     for the given type. There are 2 possible scenarios:
//     *     <ol>
//     *         <li>
//     *             Class Types
//     *         <p>
//     *             By default, any name used as a type in C Minor will automatically be parsed
//     *             as a {@link ClassType}. This is not ideal as a type name can represent either
//     *             a {@link ClassDecl}, an {@link EnumDecl}, or a type parameter. Thus, we need
//     *             to make sure this type is rewritten if it does not correspond to a {@link ClassDecl}
//     *             which is the job of {@link #verifyClassType(ClassType)}.
//     *         </p>
//     *
//     *         </li>
//     *         <li>Array or List Types
//     *         <p>
//     *             When we have an Array or List type, the base type can represent a {@link ClassType}.
//     *             Thus, the previous criteria above will apply, and this means we need to check if
//     *             we need to perform a rewrite on the base type through {@link #verifyClassType(ClassType)}.
//     *         </p>
//     *         </li>
//     *     </ol>
//     * </p>
//     * @param structuredType {@link Type} we might need to perform a rewrite of
//     * @return {@link Type} => This will return any type.
//     */
//    private Type rewriteStructuredType(Type structuredType) {
//        if(structuredType.isArrayType()) {
//            if(structuredType.asArrayType().baseType().isClassType()) {
//                Type newBaseType = verifyClassType(structuredType.asArrayType().baseType().asClassType());
//                structuredType.asArrayType().setBaseType(newBaseType);
//            }
//            return structuredType;
//        }
//
//        if(structuredType.isList()) {
//            if(structuredType.asListType().baseType().isClassType()) {
//                Type newBaseType = verifyClassType(structuredType.asListType().baseType().asClassType());
//                structuredType.asListType().setBaseType(newBaseType);
//            }
//            return structuredType;
//        }
//
//        return verifyClassType(structuredType.asClassType());
//    }
//
//    /**
//     * Checks if the class type was written correctly by the user.
//     * <p>
//     *     Since the parser will treat all names used as types as a {@link ClassType},
//     *     this method is designed to verify if all {@link ClassType}s are valid. There
//     *     are 3 cases we need to deal with:
//     *     <ol>
//     *         <li>
//     *             Enum Types
//     *             <p><br>
//     *                 Since the parser does not distinguish between a class and enum type,
//     *                 we need to perform a type rewrite to generate an enum type any time
//     *                 a class type refers back to an enum.
//     *             </p>
//     *         </li>
//     *         <li>
//     *             Template Types
//     *             <p><br>
//     *                 If the class type represents a template type, then we need to make sure
//     *                 the user correctly wrote the template type, so we can eventually evaluate
//     *                 the type when a class or function is instantiated. Additionally, this method
//     *                 will be responsible for creating types during the instantiation of a class/function.
//     *             </p>
//     *         </li>
//     *         <li>
//     *             Class Types
//     *             <p><br>
//     *                 For all other class types, we just need to make sure they exist in the program.
//     *             </p>
//     *         </li>
//     *     </ol>
//     * </p>
//     * @param ct Current {@link ClassType} we are evaluating
//     * @return A {@link Type} representing either the original type or a type that was created
//     */
//    private Type verifyClassType(ClassType ct) {
//        // ERROR CHECK #1: The name checker did not resolve the names of class types. This means we will
//        //                 now check if the current class type can resolve to either a class, an enum, or
//        //                 a type parameter. If no resolution can be made, we will print out a type error.
//        if(!currentScope.hasNameSomewhere(ct.getClassNameAsString())) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ct.getFullLocation())
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_443)
//                    .addErrorArgs(ct)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1440)
//                    .addSuggestionArgs(ct)
//                    .generateError();
//        }
//
//        // Special Case: If we are instantiating a template class, we want to replace every type parameter
//        //               with the appropriate type argument. Thus, we will look through the template's type
//        //               parameters and find which one corresponds to the current template type. From there,
//        //               we replace the template type with the passed type argument.
//        if(currentTypeArgs != null) {
//            for(int i = 0; i < currentTypeParams.size(); i++) {
//                TypeParam typeParam = currentTypeParams.get(i);
//                if(typeParam.equals(ct.toString()))
//                    return Type.instantiateType(ct, currentTypeArgs.get(i));
//            }
//        }
//
//        AST classTypeDecl = currentScope.findName(ct.getClassNameAsString()).getDecl();
//
//        // Case 1: The class type represents an enum. This means we need to rewrite it to represent an enum type.
//        if(classTypeDecl.isTopLevelDecl() && classTypeDecl.asTopLevelDecl().isEnumDecl())
//            return buildEnumType(ct, classTypeDecl.asTopLevelDecl().asEnumDecl());
//        else if(classTypeDecl.isTopLevelDecl() && classTypeDecl.asTopLevelDecl().isClassDecl()) {
//            // Case 2: The class type represents a template class.
//            if(ct.isTemplatedType())
//                checkIfTemplateTypeIsValid(ct);
//        }
//        // ERROR CHECK #2: If the class type does not represent an enum, a class, or a type parameter,
//        //                 then this means a variable name was used as a type which is not allowed.
//        else if(!classTypeDecl.isSubNode() || !classTypeDecl.asSubNode().isTypeParam()) {
//            handler.createErrorBuilder(TypeError.class)
//                    .addLocation(ct.getFullLocation())
//                    .addErrorNumber(MessageNumber.TYPE_ERROR_465)
//                    .addErrorArgs(ct)
//                    .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1452)
//                    .generateError();
//        }
//
//        // Case 3: The class type represents a normal class. No rewrite is needed so return the class type.
//        return ct;
//    }
//
//    /**
//     * Verifies the validity of a template type.
//     * <p><br>
//     *     If a user writes a template type in place of a class type, this method will
//     *     perform the necessary error checks to ensure the template type was written
//     *     correctly based on its class definition. The {@link typechecker.TypeChecker}
//     *     will handle the rest of the type errors related to assignment compatibility.
//     * </p>
//     * @param ct {@link ClassType} representing a potential template type
//     */
//    private void checkIfTemplateTypeIsValid(ClassType ct) {
//        ClassDecl template = currentScope.findName(ct.getClassNameAsString()).getDecl().asTopLevelDecl().asClassDecl();
//
//        // ERROR CHECK #1: When a template type is written, we want to make sure the correct number of
//        //                 type arguments were passed. This will be based on the number of type parameters
//        //                 the template class was declared with. There are 2 possible errors here.
//        if(template.getTypeParams().size() != ct.typeArgs().size()) {
//            // Case 1: This error is generated when a user writes type arguments for a non-template class type.
//            if(template.getTypeParams().isEmpty()) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ct.getFullLocation())
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_444)
//                        .addErrorArgs(template)
//                        .generateError();
//            }
//            // Case 2: This error is generated when the wrong number of type arguments were used for a template class type.
//            else {
//                ErrorBuilder eb = handler.createErrorBuilder(TypeError.class)
//                                      .addLocation(ct.getFullLocation())
//                                      .addErrorNumber(MessageNumber.TYPE_ERROR_445)
//                                      .addErrorArgs(template)
//                                      .addSuggestionArgs(template, template.getTypeParams().size());
//
//                if(template.getTypeParams().size() == 1)
//                    eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1441).generateError();
//                else
//                    eb.addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1442).generateError();
//            }
//        }
//
//        // We now look through each type parameter of the template class.
//        for (int i = 0; i < template.getTypeParams().size(); i++) {
//            TypeParam typeParam = template.getTypeParams().get(i);
//
//            // ERROR CHECK #2: If a user prefixed the type parameter with a type annotation, then we will check if
//            //                 the passed type argument can be used in the current type argument. If no type annotation
//            //                 was given, this check is not needed, and we will let the type checker handle the rest.
//            if(!typeParam.isValidTypeArg(ct.typeArgs().get(i))) {
//                handler.createErrorBuilder(TypeError.class)
//                        .addLocation(ct.getFullLocation())
//                        .addErrorNumber(MessageNumber.TYPE_ERROR_446)
//                        .addErrorArgs(ct.typeArgs().get(i), template)
//                        .addSuggestionNumber(MessageNumber.TYPE_SUGGEST_1443)
//                        .addSuggestionArgs(template, typeParam.getPossibleType(), i + 1)
//                        .generateError();
//            }
//        }
//    }
//
//    /**
//     * Creates an instance of a template class based on the given type arguments.
//     * <p><br>
//     *     This method is responsible for actually generating an instance of a template
//     *     class that will then be bound to a {@link NewExpr}. We will create a copy of
//     *     the template class and replace all of its type parameters with the given type
//     *     arguments.
//     * </p>
//     * @param ct Template type that we use to instantiate a template class
//     * @return {@link ClassDecl} representing the instantiated class
//     */
//    private ClassDecl instantiatesClassTemplate(ClassType ct) {
//        // If the template class was already instantiated with the given type arguments,
//        // then we don't want to reinstantiate it and instead get the instantiated class.
//        if(instantiatedClasses.contains(ct.toString()))
//            return currentScope.findName(ct).getDecl().asTopLevelDecl().asClassDecl();
//
//        ClassDecl template = currentScope.findName(ct.getClassNameAsString()).getDecl().asTopLevelDecl().asClassDecl();
//        ClassDecl copyOfTemplate = template.deepCopy().asTopLevelDecl().asClassDecl();
//
//        // We need to rerun the name checker, so the copy of the template can
//        // have an independent symbol table from the original template class.
//        copyOfTemplate.visit(new NameChecker(""));
//
//        // We now visit the copy's class in order to replace all type parameters with the given type arguments.
//        currentTypeArgs = ct.typeArgs();
//        copyOfTemplate.visit(this);
//
//        currentTypeArgs = null;
//        copyOfTemplate.removeTypeParams();
//
//        instantiatedClasses.add(ct.toString());
//        currentScope.addNameToRootTable(ct.toString(), copyOfTemplate);
//
//        return copyOfTemplate;
//    }
//
//    /**
//     * Creates an instance of a template function based on the given type arguments.
//     * <p><br>
//     *     This method is responsible for actually generating an instance of a template
//     *     function that will then be bound to an {@link Invocation}. We will create a copy of
//     *     the template function and replace all of its type parameters with the given type
//     *     arguments. This method is identical in structure to {@link #instantiatesClassTemplate(ClassType)},
//     *     but we will be calling this method from the {@link typechecker.TypeChecker} since we
//     *     need to know the types of each passed argument to the invocation, so we can find the correct
//     *     overloaded template function to instantiate.
//     * </p>
//     * @param template Template function we will be instantiating
//     * @param in {@link Invocation} containing the type arguments we will use to instantiate the template with
//     * @return {@link FuncDecl} representing the instantiated function
//     */
//    public FuncDecl instantiatesFuncTemplate(FuncDecl template, Invocation in) {
//        // If the template function was already instantiated with the given type arguments, then we don't want to
//        // reinstantiate it and instead get the already instantiated function. Since a user could overload a template
//        // function with a more specific parameter type, we will also check if there isn't an already matching signature
//        // in the root table for the invocation. If this signature exists, then this implies there is a more specific
//        // template available, thus it needs to be instantiated instead of using the one already available for us.
//        if(instantiatedFunctions.contains(in.templateSignature()) && !currentScope.getRootTable().hasName(in.getSignature()))
//            return currentScope.findName(in.templateSignature()).getDecl().asTopLevelDecl().asFuncDecl();
//
//        FuncDecl copyOfTemplate = template.deepCopy().asTopLevelDecl().asFuncDecl();
//
//        // We need to rerun the name checker, so the copy of the template can
//        // have an independent symbol table from the original template function.
//        copyOfTemplate.visit(new NameChecker(""));
//
//        // We now visit the copy's function in order to replace all type parameters with the given type arguments.
//        currentTypeArgs = in.getTypeArgs();
//        copyOfTemplate.visit(this);
//
//        currentTypeArgs = null;
//        copyOfTemplate.removeTypeParams();
//
//        instantiatedFunctions.add(in.templateSignature());
//        currentScope.addNameToRootTable(in.templateSignature(), copyOfTemplate);
//
//        // If we instantiated a more specific template function, then we want to remove the less specific instantiation
//        // from the symbol table. This prevents us from having to reinstantiate the function we just created.
//        if(currentScope.getRootTable().hasName(in.getSignature()))
//            currentScope.getRootTable().removeName(in.getSignature());
//
//        return copyOfTemplate;
//    }
//
//    /* ######################################## VISITS ######################################## */
//
//    /**
//     * Sets the current scope to be inside of a class.
//     * @param cd Class Declaration
//     */
//    public void visitClassDecl(ClassDecl cd) {
//        SymbolTable oldScope = currentScope;
//        currentScope = cd.getScope();
//
//        if(cd.isTemplate())
//            currentTypeParams = cd.getTypeParams();
//
//        super.visitClassDecl(cd);
//
//        currentScope = oldScope;
//        currentTypeParams = null;
//    }
//
//    /**
//     * Begins the C Minor type validity micropass.
//     * <p><br>
//     *     During compilation mode, {@code visitCompilation} will be the first
//     *     method executed when we start the type validity micropass.
//     * </p>
//     * @param c {@link CompilationUnit}
//     */
//    public void visitCompilationUnit(CompilationUnit c) {
//        currentScope = c.getScope();
//        super.visitCompilationUnit(c);
//    }
//
//    /**
//     * Validates the type of a field.
//     * <p><br>
//     *     This visit will rewrite the type of the {@link FieldDecl} if needed. There
//     *     is no other checks that need to be performed at this stage.
//     * </p>
//     * @param fd {@link FieldDecl}
//     */
//    public void visitFieldDecl(FieldDecl fd) {
//        if(fd.getDeclaredType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(fd.getDeclaredType());
//            //fd.setType(updatedType);
//        }
//
//        if(fd.hasInitialValue())
//            fd.getInitialValue().visit(this);
//    }
//
//    /**
//     * Validates the type of a function.
//     * <p><br>
//     *     During this visit, we will make sure every single type declared
//     *     in a function is correct. This mainly includes making sure that
//     *     no names will shadow over any type parameters.
//     * </p>
//     * @param fd {@link FuncDecl}
//     */
//    public void visitFuncDecl(FuncDecl fd) {
//        SymbolTable oldScope = currentScope;
//        String prevSignature = fd.getSignature();
//        currentScope = fd.getScope();
//
//        if(!fd.getTypeParams().isEmpty())
//            currentTypeParams = fd.getTypeParams();
//
//        for(ParamDecl pd : fd.getParams())
//            pd.visit(this);
//
//        if(fd.getReturnType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(fd.getReturnType());
//            //fd.setReturnType(updatedType);
//        }
//
//        fd.getBody().visit(this);
//
//        // If we are visiting an instantiated function, we need to consider that the name checker would have
//        // stored the function signature into the class scope using generic type parameters. Thus, we should
//        // manually remove the previous method signature and update the key to be the new signature.
//        if(currentTypeArgs != null) {
//            currentScope.removeName(prevSignature);
//            fd.resetSignature();
//            currentScope.addName(fd.getSignature(), fd);
//        }
//
//        currentScope = oldScope;
//        currentTypeParams = null;
//    }
//
//    /**
//     * Validates the type of a global variable.
//     * <p><br>
//     *     This visit will rewrite the type of the {@link GlobalDecl} if needed. There
//     *     is no other checks that need to be performed at this stage.
//     * </p>
//     * @param gd {@link GlobalDecl}
//     */
//    public void visitGlobalDecl(GlobalDecl gd) {
//        if(gd.getDeclaredType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(gd.getDeclaredType());
//            //gd.setType(updatedType);
//        }
//
//        if(gd.hasInitialValue())
//            gd.getInitialValue().visit(this);
//    }
//
//    /**
//     * Checks the type validity of an imported file.
//     * <p><br>
//     *     If a user has any imported files, we will have to make sure all types
//     *     in that file are valid before we work on the main file of the program.
//     *     This visit will handle the type validity check for each imported file.
//     * </p>
//     * @param im {@link ImportDecl}
//     */
//    public void visitImportDecl(ImportDecl im) {
//        SymbolTable oldScope = currentScope;
//
//        im.getCompilationUnit().visit(this);
//
//        currentScope = oldScope;
//    }
//
//    /**
//     * Validates the type of a local variable.
//     * <p><br>
//     *     This visit will rewrite the type of the {@link LocalDecl} if needed. Additionally,
//     *     we will check if the local's name shadows a previously defined type parameter
//     *     when we are visiting either a template function or class.
//     * </p>
//     * @param ld {@link LocalDecl}
//     */
//    public void visitLocalDecl(LocalDecl ld) {
//        // ERROR CHECK #1: If we are visiting a templated class or function, then we will check if
//        //                 the current local variable's name will shadow a type parameter name. The
//        //                 name checker will not have caught this error when we have nested scopes
//        //                 which is why we have to do this check now.
//        if(currentTypeParams != null && nameShadowsTypeParam(ld.toString())) {
//            handler.createErrorBuilder(ScopeError.class)
//                    .addLocation(ld)
//                    .addErrorNumber(MessageNumber.SCOPE_ERROR_328)
//                    .addErrorArgs(ld)
//                    .asScopeErrorBuilder()
//                    .addOriginalDeclaration(currentScope.findName(ld).getDecl())
//                    .generateError();
//        }
//
//        if(ld.getDeclaredType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(ld.getDeclaredType());
//           // ld.setType(updatedType);
//        }
//
//        if(ld.hasInitialValue())
//            ld.getInitialValue().visit(this);
//    }
//
//    /**
//     * Validates the type of a method.
//     * <p><br>
//     *     During this visit, we want to make sure the method's parameters do not conflict
//     *     with any previously declared type parameters, and we also want to see if we need
//     *     to rewrite its return type. From there, we will just validate the types within
//     *     its {@link ast.statements.BlockStmt}.
//     * </p>
//     * @param md {@link MethodDecl}
//     */
//    public void visitMethodDecl(MethodDecl md) {
//        String prevMethodSignature = md.getMethodSignature();
//
//        for(ParamDecl pd : md.getParams())
//            pd.visit(this);
//
//        if(md.getReturnType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(md.getReturnType());
//            //md.setReturnType(updatedType);
//        }
//
//        md.getBody().visit(this);
//
//        // If we are visiting an instantiated class, we need to consider that the name checker would have
//        // stored the method signature into the class scope using generic type parameters. Thus, we should
//        // manually remove the previous method signature and update the key to be the new signature.
//        if(currentTypeArgs != null) {
//            currentScope.removeName(prevMethodSignature);
//            currentScope.addName(md.getMethodSignature(), md);
//        }
//    }
//
//    /**
//     * Validates the type of a new expression.
//     * <p><br>
//     *     When a user tries to instantiate a template class, we will check to make sure
//     *     the template type is valid and then proceed to instantiate the class for the user.
//     *     We will bind the instantiated class to the {@link NewExpr} instead of a variable.
//     * </p>
//     * @param ne {@link NewExpr}
//     */
//    public void visitNewExpr(NewExpr ne) {
//        if(ne.createsFromTemplate()) {
//            checkIfTemplateTypeIsValid(ne.getClassType());
//            ne.setInstantiatedClass(instantiatesClassTemplate(ne.getClassType()));
//        }
//    }
//
//    /**
//     * Validates the type of a parameter.
//     * <p><br>
//     *     This visit will rewrite the type of the {@link ParamDecl} if needed. Additionally,
//     *     we will check if the parameter's name shadows a previously defined type parameter
//     *     when we are visiting methods inside of a template class.
//     * </p>
//     * @param pd {@link ParamDecl}
//     */
//    public void visitParamDecl(ParamDecl pd) {
//        // ERROR CHECK #1: If we are visiting a templated class, then we will check if the parameter's
//        //                 name shadows a type parameter name. Due to how the name checker resolves methods,
//        //                 this particular check was not completed, so we will do it now.
//        if(currentTypeParams != null && nameShadowsTypeParam(pd.toString())) {
//            handler.createErrorBuilder(ScopeError.class)
//                    .addLocation(pd)
//                    .addErrorNumber(MessageNumber.SCOPE_ERROR_328)
//                    .addErrorArgs(pd)
//                    .asScopeErrorBuilder()
//                    .addOriginalDeclaration(currentScope.findName(pd).getDecl())
//                    .generateError();
//        }
//
//        if(pd.getType().isStructuredType()) {
//            Type updatedType = rewriteStructuredType(pd.getType());
//            pd.setType(updatedType);
//        }
//    }
}
