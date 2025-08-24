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
