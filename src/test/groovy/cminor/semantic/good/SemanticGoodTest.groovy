package cminor.semantic.good

import cminor.messages.CompilationMessage
import cminor.semantic.SemanticTest

class SemanticGoodTest extends SemanticTest {

    def "Assignment Statement - Valid LHS"() {
        when: "An assignment statement is written."
            input = '''
                        set a = 5
                        set a.f.b = 'c'
                        set a[3][5] = 3.14
                    '''
            vm.runInterpreter(input)

        then: "No error should occur if the LHS represents a variable."
            notThrown CompilationMessage

    }

    def "Binary Expression - Valid \"instanceof\" and \"!instanceof\""() {
        when: "An instanceof or !instanceof operation is written."
            input = '''
                        a instanceof A
                        a.f.g instanceof B
                        a[3] !instanceof C
                        a() instanceof D
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since a valid LHS was written for the operation."
            notThrown CompilationMessage
    }

    def "Break Statement - Inside For Loop"() {
        when: "A break statement is written inside a for loop."
            input = '''
                        for(def a:Int in 1..5) {
                            break
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Break Statement - Inside Do While Loop"() {
        when: "A break statement is written inside a do while loop."
            input = '''
                        do {
                            break
                        } while(True)
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Break Statement - Inside While Loop"() {
        when: "A break statement is written inside a while loop."
            input = '''
                        while(True) { break }
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Break Statement - Nested in a Loop"() {
        when: "A break statement is nested within different loops."
            input = '''
                        while(True) {
                            if(3 < 5) {
                                break
                            } else {
                                do {
                                    break
                                } while(True)
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since each break keyword can be traced back to a loop."
            notThrown CompilationMessage
    }

    def "Continue Statement - Inside For Loop"() {
        when: "A continue statement is written inside a for loop."
            input = '''
                            for(def a:Int in 1..5) {
                                continue
                            }
                        '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Continue Statement - Inside Do While Loop"() {
        when: "A continue statement is written inside a do while loop."
            input = '''
                            do {
                                continue
                            } while(True)
                        '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Continue Statement - Inside While Loop"() {
        when: "A continue statement is written inside a while loop."
            input = '''
                            while(True) { continue }
                        '''
            vm.runInterpreter(input)

        then: "No error is thrown."
            notThrown CompilationMessage
    }

    def "Continue Statement - Nested in a Loop"() {
        when: "A continue statement is nested within different loops."
            input = '''
                        do {
                            choice(3) {
                                on 1 {
                                    continue
                                }
                                on 3 {
                                    for(def a:Int in 1..5) {
                                        break
                                    } 
                                }
                                other {
                                    while(True) { continue }
                                }
                            }
                        } while(True)
                    '''
            vm.runInterpreter(input)

        then: "No error occurs since each continue keyword can be traced back to a loop."
            notThrown CompilationMessage
    }

    def "Field Declaration - Not Initialized"() {
        when: "A field declaration is written inside of a class."
            input = '''
                        class A {
                            protected x:Int
                            public y:Array[Char]
                            public z:String
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur if none of the fields are initialized in the class."
            notThrown CompilationMessage
    }

    def "Global Declaration - Valid Constant Declarations"() {
        when: "Global constants are declared."
            input = '''
                        def const a:Int = 5
                        def const PI:Real = 3.1415
                        def const name:String = 'John Doe'
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown if all constants are initialized to a known value."
            notThrown CompilationMessage
    }

    def "Global Declaration - Valid Declarations"() {
        when: "Global variables are declared with different values."
            input = '''
                        def global a:Int = 5
                        def global b:Real = uninit
                        def const c:Int = 9
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since all variables are initialized."
            notThrown CompilationMessage
    }

    def "Input Statement - Valid Input Statement"() {
        when: "An input statement is written."
            input = '''
                        cin >> a >> a[4] >> b[4][5][3] >> f.g().a.b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since each input expression is valid."
            notThrown CompilationMessage
    }

    def "Local Declaration - Valid Declarations"() {
        when: "Local variables are declared with different values."
            input = '''
                        def a:Int = 6
                        def b:A = uninit
                        def c:Array[Int] = Array(1,2,3)
                        def d:Char = 'c'
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since all variables are initialized."
            notThrown CompilationMessage
    }

    def "Operator Overload - Valid Unary Overload"() {
        when: "A unary operator overload is declared within a class."
            input = '''
                        class A {
                            public operator ~() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur if it contains no parameters."
            notThrown CompilationMessage
    }

    def "Operator Overload - Valid Binary Overload"() {
        when: "A binary operator overload is declared in a class."
            input = '''
                        class A {
                            public operator -(in a:Int) => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur if it contains exactly one parameter."
            notThrown CompilationMessage
    }

    def "Output Statement - Valid Output Statement"() {
        when: "An output statement is written."
            input = '''
                        cout << a << 3+4-b+3 << f() << not True << endl << a.f().g
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since each output expression is valid."
            notThrown CompilationMessage
    }

    def "Return Statement - Written in Function"() {
        when: "A return statement is written inside a function."
            input = '''
                        def func() => Int {
                            return 3
                        }
                    '''
            vm.runInterpreter(input)

        then: "This is the expected place to write a return statement, so no errors occur."
            notThrown CompilationMessage
    }

    def "Return Statement - Written in Method"() {
        when: "A return statement is written inside a method."
            input = '''
                        class A {
                            public method test() => Int {
                                return 3
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "This is the expected place to write a return statement, so no errors occur."
            notThrown CompilationMessage
    }

    def "Retype Statement - Valid LHS"() {
        when: "A retype statement is written."
            input = '''
                        retype a = new A()
                        retype a[4] = new A()
                        retype a.f.b = new A()
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown if the LHS represents a variable."
            notThrown CompilationMessage
    }
}
