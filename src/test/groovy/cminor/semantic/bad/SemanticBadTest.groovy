package cminor.semantic.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.semantic.SemanticTest

class SemanticBadTest extends SemanticTest {

    def "Break Statement - Not Inside a Loop"() {
        when: "A break statement is written outside of a loop."
            input = '''
                        def a:Int = 5
                        if(a<3) { break }
                        else { set a = 6 }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since there is no loop to break out of."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_712
    }

    def "Continue Statement - Not Inside a Loop"() {
        when: "A continue statement is written outside of a loop."
            input = '''
                        def func() => Void {
                            cout << 'hello'
                            continue
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since there is no loop to continue with."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_713
    }

    def "Global Declaration - Not Initialized"() {
        when: "A global variable is declared without any initial value."
            input = '''
                        def global a:Int 
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since every global variable must be initialized."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_700
    }

    def "Input Statement - Invalid Expression"() {
        when: "An input statement is written using an invocation."
            input = '''
                        cin >> a >> func() >> b[3]
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the expression can't store a value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_711
    }

    def "Input Statement - Invalid Expression 2"() {
        when: "An input statement is written using a binary expression."
            input = '''
                        cin >> 3+a
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since data can't be stored in a binary expression."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_711
    }

    def "Input Statement - Invalid Expression 3"() {
        when: "An input statement is written using an array literal."
            input = '''
                        cin >> Array(1,2,3)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since an array literal can't store an input value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_711
    }

    def "Input Statement - Invalid Expression 4"() {
        when: "An input statement contains an output statement."
            input = '''
                        cin >> cout << 'hi there'
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since this expression makes no sense."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_711
    }

    def "Local Declaration - Not Initialized"() {
        when: "A local variable is declared without any initial value."
            input = '''
                            def local a:Int 
                        '''
            vm.runInterpreter(input)

        then: "An error is generated since every local variable must be initialized."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_700
    }

    def "Operator Overload - Invalid Unary Operator Declaration"() {
        when: "A unary operator overload is declared with parameters within a class."
            input = '''
                        class A {
                            public operator not(in a:Int, in b:Char) => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since a unary overload takes no arguments."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_701

    }

    def "Operator Overload - Invalid Binary Operator Declaration"() {
        when: "A binary operator overload is declared with no parameters in a class."
            input = '''
                        class A {
                            public operator +() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should occur since a binary overload needs exactly one argument."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_702
    }

    def "Output Statement - Invalid Expression"() {
        when: "An output statement contains a new expression"
            input = '''
                        cout << new A(x=5)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since an instantiation can't be printed to the screen."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_714
    }

    def "Output Statement - Invalid Expression 2"() {
        when: "An output statement contains an input statement."
            input = '''     
                        cout << cin >> a
                    '''
            vm.runInterpreter(input)

        then:
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_714
    }

}
