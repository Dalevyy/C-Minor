package cminor.modifier.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.modifier.ModifierTest

class ModifierBadTest extends ModifierTest {

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

    def "Global Declaration - Constant Value is Updated"() {
        when: "A global constant is declared, and its value is updated."
            input = '''
                        def const a:Int = 3 
                        set a = 5
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since a constant can not change its value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_505
    }

    def "Method Declaration - Calling Non-Public Method"() {
        when: "A non-public method is invoked outside a class."
            input = '''
                        class A { protected method test() => Void {} }
                        
                        def a:A = new A()
                        a.test()
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since protected methods can't be invoked outside a class."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.MOD_ERROR_504
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
