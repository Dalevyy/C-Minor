package cminor.semantic.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.semantic.SemanticTest

//TODO: Fields that use array must specify the array's dimensions!
class SemanticBadTest extends SemanticTest {

    def "Assignment Statement - Invalid LHS"() {
        when: "An assignment statement tries to assign to a binary expression."
            input = '''
                        set a+3 = 5
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since it is not possible to assign to a binary value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_707
    }

    def "Assignment Statement - Invalid LHS 2"() {
        when: "An assignment statement contains an invocation in its LHS."
            input = '''
                        set f() = 3
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since an invocation can't be assigned to."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_707
    }

    def "Binary Expression - Invalid LHS for \"instanceof\""() {
        when: "An instanceof operation is written with an invalid LHS."
            input = '''
                        a+3 instanceof A
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the LHS has to be only a name that can be evaluated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_708
    }

    def "Binary Expression - Invalid LHS for \"!instanceof\""() {
        when: "A !instanceof operation is written with an invalid LHS."
            input = '''
                        ~f() !instanceof A
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the LHS has to be only a name that can be evaluated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_708
    }

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

    def "Field Declaration - Initialized"() {
        when: "A field is initialized inside of a class."
            input = '''
                        class A {
                            protected x:Int = 5
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the user can not preassign values to fields."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_715
    }

    def "Field Declaration - Initialized 2"() {
        when: "A field is initialized using the uninit keyword in a class."
            input = '''
                        class A {
                            protected x:Array[Int] = uninit
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the user can not preassign values to fields."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_715
    }

    def "Global Declaration - Constant Not Initialized"() {
        when: "A constant is declared using the uninit keyword."
            input = '''
                        def const a:Int = uninit
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the user must initialize all constants to some value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_716
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

    def "Return Statement - Outside a Function"() {
        when: "A return statement is written outside a function or method."
            input = '''
                        return 3
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since the value can't be returned to anything."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_717
    }

    def "Retype Statement - Invalid LHS"() {
        when: "The LHS of a retype statement contains a binary expression."
            input = '''
                        retype a+3 = new A(x=5)
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since a binary expression can't be retyped."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SEMANTIC_ERROR_709
    }
}
