package cminor.modifier.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.modifier.ModifierTest

class ModifierBadTest extends ModifierTest {

    def "Assignment Statement - Enum Constant Can Not Be Reassigned"() {
        when: "An enum constant is reassigned its value."
            input = '''
                        def WEEKS type = { MON = 1 }
                        set MON = 5
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since enum constants can not change their initial values."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_508
    }

    def "Assignment Statement - Global Constant Can Not Be Reassigned"() {
        when: 'A new value is reassigned to a global constant.'
            input = '''
                        def const c:Int = 5
                        set c = 20
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since constants can not change values once they are declared."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_505
    }

    def "Class Declaration - Class Does Not Implement Abstract Class"() {
        when: "A subclass inherits from an abstract class, but not all abstract methods are implemented."
            input = '''
                        abstr class A {
                            public method myMethod1() => Void {}
                            public method myMethod2() => Void {}
                        }
                        
                        class B inherits A {
                            public override method myMethod1() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error needs to be thrown since all abstract methods have to implemented by a subclass."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_501
    }

    def "Class Declaration - Class Inherits From a Final Class"() {
        when: "A class inherits from a class that was marked final."
            input = '''     
                        final class A {}
                        class B inherits A {}
                    '''
            vm.runInterpreter(input)

        then: "This creates an error since final classes are not allowed to be inherited from."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_500
    }

    def "Field Expression - Property Field is Accessed Outside Class"() {
        when: "A property field is accessed outside of its class."
            input = '''
                        class A { property x:Int }
                        def a:A = new A(x=5)
                        cout << a.x
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since property fields can only be accessed inside a class or with a getter/setter."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Field Expression - Protected Field is Accessed Outside Class"() {
        when: "A protected field is accessed outside of its class."
            input = '''
                        class A { protected x:Int }
                        def a:A = new A(x=5)
                        cout << a.x
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since protected fields can only be accessed inside a class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Field Expression - Protected Field is Accessed Outside Class 2"() {
        when: "A non-public field is accessed from a class it was not declared in."
            input = '''
                        class A { protected x:Int }
                        
                        class B {
                            protected a:A
                            public method test() => Void { a.x }
                        }
                        
                        def b:B = new B(a= new A(x=5))
                        b.test()
                    '''
            vm.runInterpreter(input)

        then: "An error needs to be thrown since the field can't be accessed outside its declared class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Field Expression - Protected Field is Accessed Outside Class 3"() {
        when: "A non-public field is accessed from a class it was not declared in."
            input = '''
                        class A { protected x:Int }
                        class B { public a:A }
                        
                        def b:B = new B(a=new A(x=5))
                        cout << b.a.x << endl
                    '''
            vm.runInterpreter(input)

        then: "An error needs to be thrown since the field can't be accessed outside its declared class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Field Expression - Protected Method is Accessed Outside Class"() {
        when: "A protected method is invoked outside of its class."
            input = '''
                        class A {
                            protected method print() => Void {}
                        }
                        
                        def a:A = new A()
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since the method can only be invoked within the class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Field Expression - Protected Method is Accessed Outside Class 2"() {
        when: "A non-public method is invoked from a class it was not declared in."
            input = '''
                        class A {
                            protected method m() => Void {}
                        }
                        
                        class B {
                            protected a:A 
                            public method test() => Void { a.m() }
                        }
                        
                        def b:B = new B(a= new A())
                        b.test()
                    '''
            vm.runInterpreter(input)

        then: "An error needs to be thrown since the method can't be called outside the class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_507
    }

    def "Function Declaration - Recursive Call on Non-Recursive Function"() {
        when: "A recursive call is made inside of a function that wasn't marked recursive."
            input = '''
                        def factorial(in a:Int) => Int {
                            if(a == 0) { return 1 }
                            return a * factorial(a-1)
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error needs to be generated since a runtime error could occur without the user knowing."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_502
    }

    def "Method Declaration - Method Declared Final in Super Class"() {
        when: "A final method in a parent class is overridden in a child class."
            input = '''
                        class A { protected final method m() => Void {} }
                        class B inherits A { protected override method m() => Void {} }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since final methods are not allowed to be overridden."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_509
    }

    def "Method Declaration - Recursive Call on Non-Recursive Method"() {
        when: "A recursive call is made to a method not declared as recursive."
            input = '''
                        class A {
                            public method factorial(in a:Int) => Int {
                                if(a == 0) { return 1 }
                                return a * factorial(a-1)
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "This could result in a stack overflow, so we need to error out to the user."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_503
    }

    def "New Expression - Object Instantiated From Abstract Class"() {
        when: "An object is instantiated from an abstract class."
            input = '''
                        abstr class A {}
                        def a:A = new A()
                    '''
            vm.runInterpreter(input)

        then: "This creates an error since abstract classes are never instantiated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_506
    }
}
