package cminor.scope.good

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.scope.ScopeTest

class ScopeGoodTest extends ScopeTest {

    def "Choice Statement - Default Branch"() {
        when: "A choice statement declares a variable in the default branch."
            input = '''
                        def a:Int = 5
                        choice(a) {
                            other { def a:Int = 6 }
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since the variable is unique to its scope."
            notThrown CompilationMessage
    }

    def "Choice Statement - Single Case Statement"() {
        when: "A choice statement contains a single case statement with a variable declaration."
            input = '''
                        def a:Int = 5
                        choice(a) {
                            on 3 { def a:Int = 6 }
                            other { def a:Int = 7 }
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since the variable is unique to its scope."
            notThrown CompilationMessage
    }

    def "Choice Statement - Multiple Case Statements"() {
        when: "There are multiple case statements that declare a variable with the same name."
            input = '''
                        def a:Int = 5
                        choice(a) {
                            on 1 { def a:Int = 6 }
                            on 2 { def a:Int = 7 }
                            on 3 { def a:Int = 8 }
                            on 4 { def a:Int = 9 }
                            other { def a:Int = 10 }   
                        }
                    '''
            vm.runInterpreter(input)

        then: "Each variable will be unique to its declared scope, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Class Declaration - No Duplicate Fields"() {
        when: "A class declares multiple unique fields."
            input = '''
                        class A {
                            protected a:Int
                            protected b:Int
                            protected c:Real
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error should be generated."
            notThrown CompilationMessage
    }

    def "Class Declaration - Template Class"() {
        when: "A template class is defined within a program."
            input = '''
                        class A<discr t> {}
                        class B<discr t, scalar s> {}
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since the template class is unique."
            notThrown CompilationMessage
    }

    def "Do Statement - Accessing Names"() {
        when: "A do while loop is written."
            input = '''
                        def a:Int = 5
                        do {
                            def b:Int = 3
                            cout << b
                            set a -= 5
                        } while(a >= 0)
                    '''
            vm.runInterpreter(input)

        then: "All names used in the loop can be resolved, and no errors are generated."
            notThrown CompilationMessage
    }

    def "Enum Declaration - Multiple Declarations"() {
        when: "Multiple enums are declared in the top level."
            input = '''
                        def a type = {b=3, c=4, d=5 }
                        def e type = {g=6, h=7, i=8 }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since all names used are unique."
            notThrown CompilationMessage
    }

    def "Field Declaration - Redeclaring Top Level Declarations"() {
        when: "A class redeclares top level declarations as fields."
            input = '''
                        def global a:Int = 5
                        def const b:Real = 6.35
                        
                        class A {
                            protected a:Int
                            protected b:Real
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since the class scope is different from the global scope."
            notThrown CompilationMessage
    }

    def "For Statement - Accessing Names"() {
        when: "A for loop is written."
            input = '''
                        for(def a:Int in 1..5) {
                            def b:Int = 0
                            cout << 'Current Loop Iteration' << a
                            b+a
                        }
                    '''
            vm.runInterpreter(input)

        then: "The names used in a for loop are able to be resolved correctly."
            notThrown CompilationMessage
    }

    def "Function Declaration - Single Function"() {
        when: "A single function is declared in the top level."
            input = '''
                        def func(in a:Int) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "No error should be generated."
            notThrown CompilationMessage
    }

    def "Function Declaration - Two Overloads"() {
        when: "Two functions are overloaded with each other."
            input = '''
                        def func(in a:Int) => Void {}
                        def func(in a:Real) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since we are able to resolve both functions used with their parameter types."
            notThrown CompilationMessage
    }

    def "Function Declaration - Multiple Overloads"() {
        when: "A single function has numerous overloads."
            input = ''' 
                        def func(in a:Int) => Void {}
                        def func(in a:Int, out b:Real) => Void {}
                        def func(in c:Real, out a:Int) => Void {}
                        def func(in d:Char) => Void {}
                        def func(in e:Int, in f:Int, in g:String, in h:Real) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "Each overload can be resolved since each one will be unique."
            notThrown CompilationMessage
    }

    def "Function Declaration - Template Function"() {
        when: "A template function is declared."
            input = '''
                        def func<discr t, scalar s, u>(in a:Int, in b:Char) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since all names are unique and can be resolved."
            notThrown CompilationMessage
    }

    def "If Statement - If Branch"() {
        when: "A variable is declared in two separate scopes of an if statement."
            input = '''
                        def a:Int = 5
                        if(a < 6) {
                            def a:Int = 6
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since each variable is unique to its declared scope."
            notThrown CompilationMessage
    }

    def "If Statement - Else Branch"() {
        when: "Variables with the same name are declared in both the if and else branch."
            input = '''
                        def a:Int = 5
                        if(a < 6) {
                            def a:Int = 6
                        } 
                        else {
                            def a:Int = 7
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs because both variables are unique to their respective scopes."
            notThrown CompilationMessage
    }

    def "If Statement - Elifs"() {
        when: "Variables in the same scope are declared in different scopes of an if statement."
            input = '''
                        def local a:Int = 5
                        if(a == 1) { def a:Int = 6 }
                        else if(a == 2) { def a:Int = 7 }
                        else if(a == 3) { def a:Int = 8 }
                        else if(a == 4) { def a:Int = 9 }
                        else { def a:Int = 10 }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs because each individual variable is unique to its declared scope."
            notThrown CompilationMessage
    }

    def "If Statement - Accessing Name"() {
        when: "An if statement tries to access a variable declared in a higher scope."
            input = '''
                        def a:Int = 5
                        if(a<5) {
                            cout << a
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since we are able to resolve the variable that needs to be accessed."
            notThrown CompilationMessage
    }

    def "If Statement - Nested If Statements"() {
        when: "A nested if statement is written."
            input = '''
                        if(3<5) {
                            def a:Int = 5
                            if(a < 3) {
                                def a:Int = 6
                                cout << a
                            } 
                            else if(a > 3) {
                                cout << a
                            }
                            else { 
                                def a:Int = 7
                            }
                        }
                        else {
                            def b:Int = 6
                            if(b < 7) { cout << b }
                            else { set b = 50 }
                        }
                    '''
            vm.runInterpreter(input)

        then: "All names will be able to be resolved correctly."
            notThrown CompilationMessage
    }

    def "Inheritance - Method Overriding"() {
        when: "A method is redefined in a subclass."
             input = '''
                        class A { public method test() => Void {} }
                        class B inherits A { public override method test() => Void {} }
                    '''
            vm.runInterpreter(input)

        then: "No error will be thrown as long as the user remembers to use the 'overrides' keyword."
            notThrown CompilationMessage
    }

    def "Inheritance - Method Overriding 2"() {
        when: "A method is redefined in multiple subclass."
            input = '''
                        class A { public method test() => Void {} }
                        class B inherits A { public override method test() => Void {} }
                        class C inherits B { public override method test() => Void {} }
                        class D inherits C { public override method test() => Void {} }

                    '''
            vm.runInterpreter(input)

        then: "No error will be thrown as long as the user remembers to use the 'overrides' keyword for each method."
            notThrown CompilationMessage
    }

    def "Inheritance - Method Overriding 3"() {
        when: "A subclass overrides all methods from a base class."
            input = '''
                        class A {
                            public method test() => Void {}
                            public method test2() => Void {}
                        }
                        
                        class B inherits A {
                            public override method test() => Void {}
                            public override method test2() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "There should not be any name-related errors generated."
            notThrown CompilationMessage
    }

    def "Inheritance - Proper Field Declarations"() {
        when: "A class hierarchy is declared in a program."
            input = '''
                        class A { protected x:Int }
                        class B inherits A { protected y:Real }
                        class C inherits B { protected z:Char }
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown if all fields declared in each subclass are unique to the parent class fields."
            notThrown CompilationMessage
    }

    def "Global Declaration - Global Scope"() {
        when: "A global variable is declared in the global scope."
            input = '''
                        def global a:Int = 5
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Local Declaration - Solo Scope"() {
        when: "A local variable is declared."
            input = ''' 
                        def a:Int = 5 
                    '''
            vm.runInterpreter(input)

        then: "No error should occur."
            notThrown CompilationMessage
    }

    def "Local Declaration - Two Scopes"() {
        when: "Two local variables with the same name are declared in separate scopes."
            input = '''
                        def a:Int = 5
                        {
                            def a:Int = 5
                        }
                    '''
            vm.runInterpreter(input)

        then: "Both names can be resolved based on their scope, so no error occurs."
            notThrown CompilationMessage
    }

    def "Local Declaration - Nested Scopes"() {
        when: "Multiple local variables with the same names are used in a nested scope."
            input = '''
                        def a:Int = 5
                        {
                            def a:Int = 5
                            {
                                def a:Int = 5
                                {
                                    def a:Int = 5
                                    {
                                        def a:Int = 5
                                    }
                                }
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "Each name will be able to be resolved, and no error occurs."
            notThrown CompilationMessage
    }

    def "Method Declaration - Single Method"() {
        when: "A class is declared with a single method."
            input = '''
                        class A {
                            public method print() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur."
            notThrown CompilationMessage
    }

    def "Method Declaration - Multiple Overloads"() {
        when: "A class is declared with multiple method overloads."
            input = '''
                        class A {
                            public method print() => Void {}
                            public method print(in a:Int) => Void {}
                            public method print(in b:Real, in c:Int) => Void {}
                            public method print(in c:String) => Void {}
                            public method print(in d:Int, in e:Real, out f:Char) => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur since each overload is unique."
            notThrown CompilationMessage
    }

    def "Name Expression - Valid Name"() {
        when: "A variable is declared and then used."
            input = '''
                        def a:Int = 5
                        cout << a
                    '''
            vm.runInterpreter(input)

        then: "No error will occur."
            notThrown CompilationMessage
    }

    def "New Expression - Valid Instantiation"() {
        when: "An object is instantiated."
            input = '''
                        class A {
                            public x:Int
                            public y:Real
                            public z:Char
                        }
                        
                        def a:A = new A(x=5,y=3.14,z='c')
                        def b:A = new A(x=6)
                        def c:A = new A(z='d',y=3.89)
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown if each field in the class is instantiated at most one time."
            notThrown CompilationMessage
    }

    def "Parameter Declaration - Redeclaring a Top Level Declaration"() {
        when: "A parameter uses the same name as a previously declared top level declaration."
            input = '''
                        def global a:Int = 5
                        def func(in a:Int) => Void {}
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since the parameter name is found in a function's scope instead of the global scope."
            notThrown CompilationMessage
    }

    def "Type Parameter - Redeclaring a Top Level Declaration"() {
        when: "A function's type parameter redeclares an existing top level declaration."
            input = '''
                        def global t:Char = 'c'
                        def func<discr t>() => Void {}
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since the type parameter is allowed to shadow the top level declaration."
            notThrown CompilationMessage
    }

    def "While Statement - Accessing Names"() {
        when: "A while loop is written."
            input = '''
                        def a:Int = 0
                        while(a < 5) {
                            def b:Int = 5
                            cout << b
                            set a += 5
                        }
                    '''
            vm.runInterpreter(input)

        then: "All names are able to be resolved, and no errors occur."
            notThrown CompilationMessage
    }
}
