package cminor.type.good

import cminor.messages.CompilationMessage
import cminor.type.TypeTest

class TypeGoodTest extends TypeTest {

    def "Binary Expression - Arithmetic Operations (Int)"() {
        when: "Two Int variables are used with arithmetic operators."
            input = ''' 
                        def a:Int = 6, b:Int = 89
                        
                        a+b
                        a-b
                        a*b
                        a/b
                        a*b
                        a**b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since the arithmetic operators support Int values."
            notThrown CompilationMessage
    }

    def "Binary Expression - Arithmetic Operations (Real)"() {
        when: "Two Real variables are used with arithmetic operators."
            input = ''' 
                        def a:Real = 3.24513, b:Real = 3941.342
                        
                        a+b
                        a-b
                        a*b
                        a/b
                        a*b
                        a**b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since the arithmetic operators support Real values."
            notThrown CompilationMessage
    }

    def "Binary Expression - Bitwise Operations (Bool)"() {
        when: "A bitwise operation is performed on two Bool variables."
            input = '''
                        def a:Bool = True, b:Bool = True
                        
                        a & b
                        a | b
                        a ^ b
                    '''
            vm.runInterpreter(input)

        then: "Bool is a Discrete value, so no errors will be generated."
            notThrown CompilationMessage
    }

    def "Binary Expression - Bitwise Operations (Char)"() {
        when: "A bitwise operation is performed on two Char variables."
            input = '''
                        def a:Char = 'm', b:Char = 'd'
                        
                        a & b
                        a | b
                        a ^ b
                    '''
            vm.runInterpreter(input)

        then: "Char is a Discrete value, so no errors will be generated."
            notThrown CompilationMessage
    }

    def "Binary Expression - Bitwise Operations (Int)"() {
        when: "A bitwise operation is performed on two Int variables."
            input = '''
                        def a:Int = 3, b:Int = 7
                        
                        a & b
                        a | b
                        a ^ b
                    '''
            vm.runInterpreter(input)

        then: "Int is a Discrete value, so no errors will be generated."
            notThrown CompilationMessage
    }

    def "Binary Expression - Logical Operations"() {
        when: "Logical operations are used on two Bool variables."
            input = '''     
                        def a:Bool = True, b:Bool = True
                        
                        a and b
                        a or b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since the logical operations only support Bool values."
            notThrown CompilationMessage
    }

    def "Binary Expression - Relational Operations (Char)"() {
        when: "Two Char variables are compared using the relational operators."
            input = '''
                        def a:Char = 'd'
                        def b:Char = 'r'
                       
                        a == b
                        a != b
                        a < b
                        a <= b
                        a > b
                        a >= b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since two Chars are able to be compared using relational operators."
            notThrown CompilationMessage
    }

    def "Binary Expression - Relational Operations (Int)"() {
        when: "Two Int variables are compared using the relational operators."
            input = '''
                        def a:Int = 5
                        def b:Int = 6
                       
                        a == b
                        a != b
                        a < b
                        a <= b
                        a > b
                        a >= b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since two Ints are able to be compared using relational operators."
            notThrown CompilationMessage
    }

    def "Binary Expression - Relational Operations (Real)"() {
        when: "Two Real variables are compared using the relational operators."
            input = '''
                        def a:Real = 102.35312
                        def b:Real = 853.235322
                       
                        a == b
                        a != b
                        a < b
                        a <= b
                        a > b
                        a >= b
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since two Reals are able to be compared using relational operators."
            notThrown CompilationMessage
    }

    def "Binary Expression - String Concatenation"() {
        when: "Two String literals are added."
            input = '''
                        cout << 'hello' + ' world'
                    '''
            vm.runInterpreter(input)

        then: "This is a string concatenation, so no error will be generated."
            notThrown CompilationMessage
    }

    def "Binary Expression - Valid ==/!= Operations"() {
        when: "The operators == and != are used with Bools and Strings."
            input = '''
                        True == False
                        True != True
                        
                        'hi' == 'hello'
                        'hey' != 'hi'
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown since Bools and Strings can be compared with the == and != operators."
            notThrown CompilationMessage
    }

    def "Binary Expression - Valid Shift Operations"() {
        when: "Two Int variables are shifted."
            input = '''
                        def a:Int = 5, b:Int = 38
                        a << b
                        a >> b
                    '''
            vm.runInterpreter(input)

        then: "Only Int values may be shifted, so no errors are thrown."
            notThrown CompilationMessage
    }

    def "Global Declaration - Valid Constant Declarations"() {
        when: "Multiple global constants are declared in the program."
            input = '''
                        def const a:Int = 3
                        def const b:Real = 5.38593
                        def const c:String = 'Hi!'
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown as long as each constant is initialized to the correct typed value."
            notThrown CompilationMessage
    }

    def "Global Declaration - Valid Declarations"() {
        when: "Multiple global variables are declared in a program."
            input = '''
                        def global a:Int = 5
                        def global b:Char = uninit
                        def global c:Bool = False
                        def global d:Real = 3.14
                        def global e:String = uninit
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur if the initialized values have the same type as the variable's declared type."
            notThrown CompilationMessage
    }

    def "List - Valid 1D List Initialization"() {
        when: "A user writes a 1D list literal by itself."
            input = '''
                       List(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "No error will occur if the list contains no type errors."
            notThrown CompilationMessage
    }

    def "List - Valid 1D List Initialization 2"() {
        when: "A local variable representing a 1D list is initialized to a 1D list."
            input = '''
                       def a:List[Int] = List(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur since the list is assignment compatible with the list type."
            notThrown CompilationMessage
    }

    def "List - Valid 1D List Initialization 3"() {
        when: "A 1D list is initialized to an empty 1D list."
            input = '''
                        def a:List[Int] = List()
                    '''
            vm.runInterpreter(input)

        then: "A list is allowed to be empty since it is dynamic so no errors occur."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization"() {
        when: "A variable representing a 2D list is initialized to a 2D list."
            input = '''
                        def a:List[List[Int]] = List(List(1,2,3),List(4,5))
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 2"() {
        when: "A variable representing a 2D list is initialized to an empty 2D list."
            input = '''
                        def a:List[List[Int]] = List(List())
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 3"() {
        when: "A variable representing a 2D list is initialized to an empty 2D list."
            input = '''
                        def a:List[List[Int]] = List(List(), List(), List())
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 4"() {
        when:
            input = '''
                        List(List(1,2,3,4,5), List(), List(6,7,8), List())
                    '''
            vm.runInterpreter(input)
        then:
            notThrown CompilationMessage
    }

    def "List - Valid 3D List Initialization"() {
        when: "A variable representing a 3D list is initialized to a 3D list."
            input = '''
                        def a:List[List[List[Int]]] = List(List(List(1,2,3), List(4,5,6)), List(List(6,7,8,9)))
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 3D List Initialization 2"() {
        when: "A 3D list is initialized to an empty 2D list and a 2D Int list."
            input = '''
                        List(List(List()), List(List(1,2,3,4),List(7,8,9)))
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since "
            notThrown CompilationMessage
    }

    def "Local Declaration - Scalars"() {
        when: "Local variables are declared."
            input = '''
                        def a:Int = 5
                        def b:Real = 3.14
                        def c:Char = 'c'
                        def d:Bool = True
                        def e:String = 'hi'
                    '''
            vm.runInterpreter(input)

        then: "No errors are generated if all values were correctly typed."
            notThrown CompilationMessage
    }

    def "Local Declaration - Uninitialized Values"() {
        when: "Local variables are declared and are left uninitialized."
            input = '''
                        def a:Int = uninit
                        def b:Real = uninit
                        def c:Char = uninit
                        def d:Bool = uninit
                        def e:String = uninit
                    '''
            vm.runInterpreter(input)

        then: "A default value should be generated for each variable, and no errors will occur."
            notThrown CompilationMessage
    }
}
