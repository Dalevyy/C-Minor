package cminor.scope.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.scope.ScopeTest

class ScopeBadTest extends ScopeTest {

    def "Class Declaration - Redeclaration of a Class"() {
        when: "Two classes are declared with the same name."
            input = '''
                        class A {}
                        class A {}
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since this is not allowed."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_307
    }

    def "Class Declaration - Redeclaration of a Top Level Declaration"() {
        when: "A class uses the same name as a previous top level declaration."
            input = '''
                        def global a:Int = 5
                        class a {}
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since the name is reused for a different construct."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_307
    }

    def "Class Declaration - Self Inheritance"() {
        when: "A class tries to inherit from itself."
            input = '''
                        class A inherits A {}
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since it makes no sense."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_308
    }

    def "Class Declaration - Inherited Class Not Declared"() {
        when: "A class tries to inherit from a class that is not declared."
            input = '''
                        class A inherits B {}
                    '''
            vm.runInterpreter(input)

        then: "An error is printed out."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_309
    }

    def "Class Declaration - Not Inheriting From a Class"() {
        when: "A class inherits from a non-class construct defined in the program."
            input = '''
                        def B:Int = 5
                        class A inherits B {}
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since classes can only inherit from other classes"
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_309
    }

    def "Enum Declaration - Redeclaration in the Same Scope"() {
        when: "Two enums with the same name are declared in the top level."
            input = '''
                        def WEEKS type = { MON = 1 }
                        def WEEKS type = { TUES = 2 }
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since there is a name conflict between the two enums."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_321
    }

    def "Enum Declaration - Redeclaration of Constant"() {
        when: "Two different enums declare a constant with the same name."
            input = '''
                        def a type = { b=1,c=2 }
                        def d type = { e=3,b=4 }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since it will not be known which constant the name refers to."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_322
    }

    def "Field Declaration - Redeclaration of Existing Field"() {
        when: "Two fields in the same class are declared with the same name."
            input = '''
                        class A {
                            protected x:Int
                            protected x:Real
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since we are not able to tell which field needs to be used."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_310
    }

    def "For Statement - Redeclaration of Loop Control Variable"() {
        when: "The loop control variable is redeclared in the scope opened by the for loop."
            input = '''
                        for(def a:Int in 1..5) {
                            def a:Int = 7
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error will occur since the control variable is contained in the for loop's scope."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_300
    }

    def "Function Declaration - Redeclaring a Function Overload"() {
        when: "A function is declared with the same parameter types twice."
            input = '''
                        def func(in a:Int) => Void {}
                        def func(in b:Int) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "An error occurs because we are redeclaring an existing overloaded function."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_306
    }

    def "Global Declaration - Redeclaration in the Same Scope"() {
        when: "Two global variables with the same name are declared in the top level."
            input = '''
                        def global a:Int = 5
                        def global a:Int = 5
                    '''
            vm.runInterpreter(input)

        then: "An error will occur since we can't resolve both variable names."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_302
    }

    def "Global Declaration - Self Initialization"() {
        when: "A global variable is initialized using itself."
            input = '''
                        def global a:Int = a
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since there is no value yet assigned to the variable."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_303
    }

    def "Global Declaration - Redeclaring a Top Level Declaration"() {
        when: "A global variable is declared using the same name as a class."
            input = '''
                        class A {}
                        def global A:Int = 5
                    '''
            vm.runInterpreter(input)

        then: "An error occurs since the same name can not be used for multiple top level declarations."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_302
    }

    def "Global Declaration - Redeclaring an Enum Constant"() {
        when: "A global variable is declared using the same name as a constant declared in an enum."
            input = '''
                        def a type = { b=2 }
                        def global b:Int = 5
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the constant and global variable have a name conflict."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_302
    }

    def "If Statement - Redeclaration"() {
        when: "Two variables with the same names are declared in the same if branch."
            input = '''
                        if(3<5) {
                            def a:Int = 5
                            def a:Int = 6
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error will occur since we are redeclaring the first variable."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_300
    }

    def "Local Declaration - Redeclaration in the Same Scope"() {
        when: "Two local variables with the same name are declared in the same scope."
            input = '''
                        def a:Int = 5
                        def a:Int = 5
                    '''
            vm.runInterpreter(input)

        then: "A redeclaration error will occur."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_300
    }

    def "Local Declaration - Self Initialization"() {
        when: "A local variable is initialized to itself."
            input = '''
                        def a:Int = a
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the variable has no value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_301
    }

    def "Local Declaration - Self Initialization 2"() {
        when: "A local variable is initialized to an expression which contains its name."
            input = '''
                        def a:Int = (3*5)+a-9
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the variable has no value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_301
    }

    def "Method Declaration - Redeclaring a Method Overload"() {
        when: "A class uses the same method overload twice."
            input = '''
                        class A {
                            public method print(in a:Int) => Void {}
                            public method print(in b:Int) => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since we are redeclaring an existing overload."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_313
    }

    def "Name Expression - Name is Unknown"() {
        when: "A name is used without being declared."
            input = '''
                        a
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the name can't be resolved."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_304

    }

    def "New Expression - Class Does Not Exist"() {
        when: "An object is instantiated to a class that wasn't declared."
            input = '''
                        def a:A = new A()
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since it's not possible to instantiate the object."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_314
    }

    def "New Expression - Not Instantiating From a Class"() {
        when: "An object is instantiated using a name not associated with a class."
            input = '''
                        def local A:Int = 5
                        def b:A = new A()
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since the object is not able to be instantiated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_314
    }

    def "New Expression - Invalid Field Initialization"() {
        when: "An object is instantiated using a field not declared in the class."
            input = '''
                        class A {
                            protected x:Int
                            protected y:Real
                        }
                        
                        def a:A = new A(x=5,z=6,y=4)
                    '''
            vm.runInterpreter(input)

        then: "An error is created since we can not initialize a field that doesn't exist in the class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_315
    }

    def "New Expression - Field Initialized Twice"() {
        when: "An object is instantiated, and a field is initialized twice."
            input = '''
                        class A {
                            protected x:Int
                            protected y:Real
                        }
                        
                        def a:A = new A(x=5,y=4,x=6)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since a field should only be initialized once."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_316
    }

    def "Parameter Declaration - Redeclaration of a Parameter"() {
        when: "A function is declared with two parameters that share a name."
            input = '''
                        def func(in a:Int, in a:Int) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since we are not able to resolve which parameter the name refers to."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_305
    }

    def "Parent Statement - Parent Keyword Used Outside Class"() {
        when: "The parent keyword is used in any non-class scope."
            input = '''
                        parent
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since we are not able to resolve what the parent is."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_318
    }

    def "Parent Statement - Parent Keyword Used in Base Class"() {
        when: "The parent keyword is used in a class that doesn't inherit from another class."
            input = '''
                        class A {
                            public method print() => Void {
                                parent
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is created since the parent keyword has to reference the base class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_319
    }

    def "Type Parameter - Redeclaration of a Type Parameter"() {
        when: "A template function uses the same type parameter name."
            input = '''
                        def func<discr t, scalar t>() => Void {}
                    '''
            vm.runInterpreter(input)

        then: "An error occurs since we will not know which parameter the name resolves to."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SCOPE_ERROR_330
    }
}
