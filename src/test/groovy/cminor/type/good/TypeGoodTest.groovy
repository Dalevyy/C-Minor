package cminor.type.good

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.type.TypeTest

class TypeGoodTest extends TypeTest {

    def "Array Expression - Valid Expression"() {
        when: "An array is declared and an element is accessed."
            input = '''
                        def a:Array[Int] = Array(1,2,3,4,5)
                        a[3]
                    '''
            vm.runInterpreter(input)

        then: "There will be no issues as long as the index expression is correct."
            notThrown CompilationMessage
    }

    def "Array Expression - Valid Expression 2"() {
        when: "An array is declared and an element is accessed."
            input = '''
                        def a:Array[Array[Int]] = Array[2][3](Array(1,2),Array(3,4,5))
                        a[2]
                    '''
            vm.runInterpreter(input)

        then: "There will be no issues as long as the index expression is correct."
            notThrown CompilationMessage
    }

    def "Array Literal - Declaring an Empty 1D Array"() {
        when: "An empty 1D array is initialized to a variable."
            input = '''
                        def a:Array[Int] = Array(1) 
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur?"
            notThrown CompilationMessage
    }

    def "Array Literal - Declaring a 1D Array"() {
        when: "A 1D array of Ints is declared and initialized into a variable."
            input = '''
                        def a:Array[Int] = Array(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "The compiler will know each initial value's type matches, so no type errors will be generated."
            notThrown CompilationMessage
    }

    def "Array Literal - Declaring a 1D Array"() {
        when: "A 1D array of Ints is declared with a specific dimension and initialized into a variable."
            input = '''
                        def a:Array[Int] = Array[5](1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "The compiler will know each initial value's type matches, so no type errors will be generated."
            notThrown CompilationMessage
    }

    def "Assignment Statement - String Concatenation"() {
        when: "The += assignment operation is used on Strings."
            input = '''
                        def a:String = 'hello'
                        set a += ' world'
                    '''
            vm.runInterpreter(input)

        then: "This is a form of string concatenation, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Assignment Statement - Valid Assignments"() {
        when: "Multiple declared variables are reassigned values."
            input = '''
                        def global a:Int = 5
                        def b:Char = 'c'
                        def c:String = 'hello there'
                        def global d:Real = 3.14
                        def global e:Bool = False
                        
                        set a = 9
                        set b = 'd'
                        set c = 'hi there'
                        set d = -2.35235
                        set e = True 
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur if the new value is the same type as the variable."
            notThrown CompilationMessage
    }

    def "Assignment Statement - Valid Assignment Operations (Int)"() {
        when: "The assignment operators are used on an Int variable."
            input = '''
                        def a:Int = 5
                        set a += 9
                        set a -= 10
                        set a *= 38
                        set a /= 32
                        set a %= 3
                        set a **= 2
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since the assignment operations are supported by the Int type."
            notThrown CompilationMessage
    }

    def "Assignment Statement - Valid Assignment Operations (Real)"() {
        when: "The assignment operators are used on a Real variable."
            input = '''
                        def a:Real = 3.14
                        set a += 8.394
                        set a -= -385.342
                        set a *= 3525.2342
                        set a /= 2.234
                        set a %= 39.43
                        set a **= -2.53
                    '''
            vm.runInterpreter(input)

        then: "No errors occur since the assignment operations are supported by the Real type."
            notThrown CompilationMessage
    }

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

    def "Cast Expression - Valid Char Casts"() {
        when: "The following Char cast expressions are written."
            input = '''
                        Char(8)
                        Char('d')
                    '''
            vm.runInterpreter(input)

        then: "All Char cast expressions are valid, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Cast Expression - Valid Int Casts"() {
        when: "The following Int cast expressions are written."
            input = '''
                        Int('c')
                        Int(3.14)
                        Int(5)
                    '''
            vm.runInterpreter(input)

        then: "All Int cast expressions are valid, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Cast Expression - Valid Real Casts"() {
        when: "The following Real cast expressions are written."
            input = '''
                        Real(9)
                        Real(3.14)
                    '''
            vm.runInterpreter(input)

        then: "All Real cast expressions are valid, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Cast Expression - Valid String Casts"() {
        when: "The following String cast expressions are written."
            input = '''
                        String('d')
                        String('hello')
                    '''
            vm.runInterpreter(input)

        then: "All String cast expressions are valid, so no errors will occur."
            notThrown CompilationMessage
    }

    def "Choice Statement - Valid Cases"() {
        when: "A choice statement is written for an Int value."
            input = '''
                        def a:Int = 1
                        choice(a) {
                            on 1 {}
                            on 2 {}
                            on 3 {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur if all case labels represent Int literals."
            notThrown CompilationMessage
    }

    def "Choice Statement - Valid Cases 2"() {
        when: "A choice statement is written for a String value."
            input = '''
                        def a:String = 'hello'
                        choice(a) {
                            on 'hi' {}
                            on 'hello' {}
                            on 'sup' {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur if all case labels represent String literals."
            notThrown CompilationMessage
    }

    def "Choice Statement - Valid Range Cases"() {
        when: "A choice statement is written for a Char value."
            input = '''
                        def c:Char = 'd'
                        choice(c) {
                            on 'a' {}
                            on 'c'..'g' {}
                            on 'f'..'u' {} 
                            on 'b' {}
                            other {} 
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur if all labels were written with Char values."
            notThrown CompilationMessage
    }

    def "Do Statement - Valid Conditional Expression"() {
        when: "The conditional expression of a do while loop is a relational operation."
            input = '''
                        def a:Int = 5
                        do { 
                            set a -= 1
                        } while(a >= 0)
                    '''
            vm.runInterpreter(input)

        then: "No error is thrown since the relational operation evaluates to be a Bool."
            notThrown CompilationMessage
    }

    def "Enum Declaration - Valid Enum Declaration"() {
        when: "An enum is declared and all constants are initialized to a value."
            input = '''
                        def WEEKS type = { MON = 1, TUES = 2, WEDS = 3, THURS = 4, FRI = 5 }
                    '''
            vm.runInterpreter(input)

        then: "No errors are thrown as long as the values are correctly typed."
            notThrown CompilationMessage
    }

    def "Enum Declaration - Valid Enum Declaration"() {
        when: "An enum is declared, but no constants are initialized."
            input = '''
                        def WEEKS type = { MON, TUES, WEDS, THURS, FRI }
                    '''
            vm.runInterpreter(input)

        then: "The compiler will assign default values to each constant and no errors are generated."
            notThrown CompilationMessage
    }

    def "Field Expression - Valid Complex Expression"() {
        when: "A complex field expression is written."
            input = '''
                        class A { public x:Int }
                        class B { public a:A }
                        
                        def b:B = new B()
                        cout << b.a.x
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown as long as each name is found in its appropriate target class."
            notThrown CompilationMessage
    }

    def "For Statement - Valid Header"() {
        when: "A for loop is written using an Int header."
            input = '''
                        for(def a:Int in 1..10) {}
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown since the control variable and iteration values are all Ints."
            notThrown CompilationMessage
    }

    def "For Statement - Valid Header 2"() {
        when: "A for loop is written using a Char header."
            input = '''
                        for(def a:Char in 'a'..'e') {}
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown since the control variable and iteration values are all Chars."
            notThrown CompilationMessage
    }

    def "Function Declaration - Function Will Return Value"() {
        when: "A function is written and a return statement is guaranteed to execute."
            input = '''
                        def func() => Int {
                            if(3 < 5) { 
                                if(3 == 5) { return 3 }
                                else if(3 > 5) { return 5 }
                                else { return 4 }
                            }
                            
                            return 1
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors should be thrown since a value will always be returned."
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

    def "Inheritance - Valid Method Calls"() {
        when: "A subtype object tries to call a method from the parent class."
            input = '''
                        class A { public method test() => Void {} }
                        class B inherits A { public method print() => Void {} }
                        
                        def b:B = new B()
                        b.test()
                        b.print()
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur as long as the parent method exists."
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

    def "Method Declaration - Method Will Return Value"() {
        when: "A method is written and a return statement is guaranteed to execute."
            input = '''
                        class A {
                            public method test() => String {
                                while(True) {
                                    if(3.14 > 5.34) { return 'hi' }
                                    break
                                }
                                return 'hello there'
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "No errors should be thrown since a value will always be returned."
            notThrown CompilationMessage
    }

    def "Method Invocation - Valid Invocations"() {
        when: "An object tries to call multiple methods defined in a class."
            input = '''
                        class A {
                            public method test() => Void {}
                            public method test(in a:Int) => Void {}
                            public method test(in a:Real, in b:Char) => Void {}
                            public method print() => Void {}
                        }
                        
                        def a:A = new A()
                        a.test()
                        a.test(5)
                        a.test(3.14,'c')
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur as long as each method was passed the correct argument types."
            notThrown CompilationMessage
    }

    def "New Expression - Valid Object Instantiation"() {
        when: "An object is instantiated."
            input = '''
                        class A {
                            protected x:Int
                            protected y:Real
                            protected z:Char
                        }
                        def a:A = new A(x=5,y=3.14,z='c')
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown as long as each field is given the correct initial value type."
            notThrown CompilationMessage
    }

    def "New Expression - Valid Object Instantiation 2"() {
        when: "An object is instantiated."
            input = '''
                        class A {
                            protected x:Int
                            protected y:Real
                            protected z:Char
                        }
                        def a:A = new A(x=5,z='c')
                    '''
            vm.runInterpreter(input)

        then: "No errors will be thrown as long as the fields that were initialized have the correct value type."
            notThrown CompilationMessage
    }

    def "Return Statement - Valid Return"() {
        when: "An empty return statement is written inside a Void function."
            input = '''
                        def func() => Void { return }
                    '''
            vm.runInterpreter(input)

        then: "No error should be thrown since the return is empty."
            notThrown CompilationMessage
    }

    def "Return Statement - Valid Return 2"() {
        when: "A return statement that returns an Int value is written inside an Int function."
            input = '''
                        def func() => Int { return 5 }
                    '''
            vm.runInterpreter(input)

        then: "No error should be thrown since the return value matches the return type."
            notThrown CompilationMessage
    }

    def "Return Statement - Valid Return 3"() {
        when: "A return statement that returns a Char value is written inside a Char method."
            input = '''
                        class A {
                            public method test() => Char { return 'a' }
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error should be thrown since the return value matches the return type."
            notThrown CompilationMessage
    }

    def "Retype Statement - Valid Retype"() {
        when: "A user tries to retype a parent object into a child type."
            input = '''
                        class A {}
                        class B inherits A {}
                        
                        def a:A = new A()
                        retype a = new B()
                    '''
            vm.runInterpreter(input)

        then: "As long as the child inherits from the parent type, there are no errors."
            notThrown CompilationMessage
    }

    def "Retype Statement - Valid Retype 2"() {
        when: "A user tries to retype a parent object into a child type."
            input = '''
                        class A {}
                        class B inherits A {}
                        class C inherits B {}
                        class D inherits C {}
                        
                        def a:A = new A()
                        while(True) {
                            retype a = new B()
                            if(3 < 5) { retype a = new D() }
                            else {
                                do { retype a = new C() } while(1 == 0)
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "As long as the child inherits from the parent type, there are no errors."
            notThrown CompilationMessage
    }

    def "Unary Expression - Valid Bitwise Negation"() {
        when: "A bitwise negation is performed on any Discrete value."
            input = '''
                        ~3
                        ~True
                        ~'c'
                    '''
            vm.runInterpreter(input)

        then: "No errors will be generated since all Discrete types can be used with the bitwise negation operation."
            notThrown CompilationMessage
    }

    def "Unary Expression - Valid NOT Operation"() {
        when: "A NOT operation is used on Bool values."
            input = '''
                        def a:Bool = True
                        
                        not a
                        not False
                    '''
            vm.runInterpreter(input)

        then: "No errors are generated since only Bool values will work with a NOT operation."
            notThrown CompilationMessage
    }

    def "While Statement - Valid Conditional Expression"() {
        when: "The conditional expression of a while loop is a Bool literal."
            input = '''
                        while(True) {}
                    '''
            vm.runInterpreter(input)

        then: "No error will occur since the conditional expression is a Bool."
            notThrown CompilationMessage
    }

    def "While Statement - Valid Conditional Expression 2"() {
        when: "The conditional expression of a while loop is a relational operation."
            input = '''
                        def a:Int = 0
                        while(a < 5) {
                            set a += 1
                        }
                    '''
            vm.runInterpreter(input)

        then: "No error will occur since the conditional expression is a Bool."
            notThrown CompilationMessage
    }
}
