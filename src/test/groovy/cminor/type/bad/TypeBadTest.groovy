package cminor.type.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.type.TypeTest

//TODO : Check instanceof operations :D

class TypeBadTest extends TypeTest {

    def "Assignment Statement - Invalid += Operation"() {
        when: "The += operation is used on two Bool variables."
            input = '''
                        def a:Bool = True, b:Bool = False
                        set a += b
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since Bool is not a supported type for the += operation."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_403
    }

    def "Assignment Statement - Invalid -= Operation"() {
        when: "The -= operation is used on two Char variables."
            input = '''
                        def a:Char = 'c', b:Char = 'd'
                        set a -= b
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since Char is not a supported type for the -= operation."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_404
    }

    def "Assignment Statement Invalid Assignment"() {
        when: "An Int variable is assigned a Real value."
            input = '''
                        def a:Int = 5
                        set a = 3.14
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since an Int variable can't store a Real value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_403
    }

    def "Assignment Statement Invalid Assignment 2"() {
        when: "A Real variable is assigned a Bool value."
            input = '''
                        def a:Real = 9.374
                        set a = True
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since a Real variable can't store a Bool value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_403
    }

    def "Assignment Statement Invalid Assignment 3"() {
        when: "A Bool variable is assigned a String value."
            input = '''
                        def a:Bool = True
                        set a = 'hello there'
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since a Bool variable can't store a String value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_403
    }

    def "Binary Expression - LHS and RHS Have Different Types"() {
        when: "An Int literal is added to a Real literal."
            input = '''
                        3 + 3.14
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the LHS and RHS have different types."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_405
    }

    def "Binary Expression - LHS and RHS Have Different Types 2"() {
        when: "A Bool literal is compared to a Char literal."
            input = '''
                        True < 'd'
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since the LHS and RHS have different types."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_405
    }

    def "Binary Expression - Invalid == Operation"() {
        when: "An Int variable is compared to a Char variable using the == operator."
            input = '''
                        def a:Int = 10
                        def b:Char = 'd'
                        
                        a == b
                    '''
            vm.runInterpreter(input)

        then: "Both types must be the same when using an == comparison, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_405
    }

    def "Binary Expression - Invalid != Operation"() {
        when: "An Int variable is compared to a Char variable using the == operator."
            input = '''
                        def a:Int = 10
                        def b:Char = 'd'
                        a != b
                    '''
            vm.runInterpreter(input)

        then: "Both types must be the same when using an != comparison, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_405
    }

    def "Binary Expression - Invalid < Operation"() {
        when: "Two String variables are compared using the < operation."
            input = '''
                        def global a:String = 'hello'
                        def global b:String = 'hi'
                        a < b
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to compare two Strings with <, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid <= Operation"() {
        when: "Two Bool variables are compared using the <= operation."
            input = '''
                        def global a:Bool = True
                        def global b:Bool = False
                        a <= b
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to compare two Bools with <=, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid > Operation"() {
        when: "Two String variables are compared using the > operation."
            input = '''
                        def global a:String = 'yo'
                        def global b:String = 'hey'
                        a > b
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to compare two Strings with >, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid >= Operation"() {
        when: "Two Bool variables are compared using the >= operation."
            input = '''
                        def global a:Bool = True
                        def global b:Bool = True
                        a >= b
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to compare two Bools with >=, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid + Operation"() {
        when: "Two Bool literals are added together."
            input = '''
                        True + False
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to add two Bool values, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid - Operation"() {
        when: "Two String literals are subtracted."
            input = '''
                        'hi' - 'there' 
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to subtract two String values, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid * Operation"() {
        when: "Two Char literals are multiplied."
            input = '''
                        'c' * 'd'
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to multiply two Char values, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid / Operation"() {
        when: "Two String literals are divided."
            input = '''
                        'hello' / 'world'
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to divide two String values, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid % Operation"() {
        when: "Two Bool literals are moded."
            input = '''
                        True % True
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to mod two Bool values, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
        }

    def "Binary Expression - Invalid ** Operation"() {
        when: "A Char literal is raised to the power of another Char literal."
            input = '''
                        'c' ** 'd'
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to use exponentiation with two Char literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid << Operation"() {
        when: "A shift left operation is performed on two Bool literals."
            input = '''
                        True << False
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to shift two Bool literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid >> Operation"() {
        when: "A shift right operation is performed on two Real literals."
            input = '''
                        5.3251 >> 7.3521
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to shift two Real literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid & Operation"() {
        when: "A bitwise and operation is performed on two String literals."
            input = '''
                        'hey' & 'hi'
                    '''
        vm.runInterpreter(input)

        then: "It's not possible to perform a bitwise and on two String literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid | Operation"() {
        when: "A bitwise and operation is performed on two Real literals."
            input = '''
                        -0.35135 | 21.3592
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to perform a bitwise or on two Real literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid ^ Operation"() {
        when: "A bitwise xor operation is performed on two Real literals."
            input = '''
                        3.14 ^ 6235.1234
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to perform a bitwise xor on two Real literals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid AND Operation"() {
        when: "The logical operator AND is performed on two Int literals."
            input = '''
                        3 and 5
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to perform an AND operation on two Ints, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Binary Expression - Invalid OR Operation"() {
        when: "The logical operator OR is performed on two Real literals."
            input = '''
                        64.342 and -3935.2342
                    '''
            vm.runInterpreter(input)

        then: "It's not possible to perform an OR operation on two Reals, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_406
    }

    def "Cast Expression - Invalid Int Cast"() {
        when: "A Bool literal is typecasted into an Int."
            input = '''
                        Int(True)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since it's not possible to cast a Bool into an Int."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_409
    }

    def "Cast Expression - Invalid Real Cast"() {
        when: "A Char literal is typecasted into a Real."
            input = '''
                        Real('c')
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since it's not possible to cast a Char into a Real."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_409
    }

    def "Cast Expression - Invalid String Cast"() {
        when: "A Real literal is typecasted into a String."
            input = '''
                        String(3.14)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since it's not possible to cast a Real into a String."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_409
    }

    def "Cast Expression - Invalid String Cast 2"() {
        when: "An Int literal is typecasted into a String."
            input = '''
                        String(29)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since it's not possible to cast an Int into a String."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_409
    }

    def "Choice Statement - Invalid Choice Value"() {
        when: "A Real literal is used as a choice value."
            input = '''
                        choice(3.14) {
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "This creates an error since Real values are not supported with choice statements."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_416
    }

    def "Choice Statement - Label Does Not Match Choice Value Type"() {
        when: "A case statement has a Char label, but the choice value is an Int."
            input = '''
                        choice(3) {
                            on 'c' {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "The case label and the choice value have different types, so an error needs to be generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_417
    }

    def "Choice Statement - Range Label Does Not Match Choice Value Type"() {
        when: "A case statement has a range label containing a Char, but the choice value is an Int."
            input = '''
                        choice(3) {
                            on 1..'c' {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "The case label and the choice value have different types, so an error needs to be generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_417
    }

    def "Choice Statement - String in Range Label"() {
        when: "A case statement has a range label representing a String."
            input = '''
                        def a:String = 'hello'
                        choice(a) {
                            on 'hello'..'hi' {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since we can't determine what the range of two String values are."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_418
    }

    def "Choice Statement - Invalid Char Range"() {
        when: "A case statement has a Char range label."
            input = '''
                        choice('c') {
                            on 'x'..'d' {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "The range is invalid since the left Char label is larger than the right Char label."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_419
    }

    def "Choice Statement - Invalid Int Range"() {
        when: "A case statement has an Int range label."
            input = '''
                        choice(3) {
                            on 5..1 {}
                            other {}
                        }
                    '''
            vm.runInterpreter(input)

        then: "The range is invalid since the left Int label is larger than the right Int label."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_419
    }

    def "Do Statement - Invalid Conditional Expression"() {
        when: "A do while loop is written with a non-Bool expression as a condition."
            input = '''
                        do { }
                        while(3+3)
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the conditional expression has to evaluate to be a Bool."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_412
    }

    def "Do Statement - Invalid Conditional Expression 2"() {
        when: "The conditional expression of a do while loop is an output statement"
            input = '''
                        do { } while(cout << 'hi there!')
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since the output statement has no type associated with it."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_412
    }

//    def "Field Declaration - Same Type as Class"() {
//        when: "A field is declared with a type that matches the current class."
//            input = '''
//                        class A {
//                            protected x:A
//                        }
//                    '''
//            vm.runInterpreter(input)
//
//        then: "An error is thrown since a field can't reference a class that it was declared in."
//            error = thrown CompilationMessage
//            error.msg.messageType == MessageNumber.TYPE_ERROR_466
//    }
//
//    def "Field Declaration - Same Type as Class 2"() {
//        when: "A field is declared with an array type that has a base type reference to the current class."
//            input = '''
//                        class A {
//                            protected x:Array[Array[A]]
//                        }
//                    '''
//            vm.runInterpreter(input)
//
//        then: "An error is thrown since a field can't reference a class that it was declared in."
//            error = thrown CompilationMessage
//            error.msg.messageType == MessageNumber.TYPE_ERROR_466
//    }
//
//    def "Field Declaration - Same Type as Inherited Class"() {
//        when: "A subtype declares a field that matches an inherited supertype."
//            input = '''
//                        class A { }
//                        class B inherits A { protected x:A }
//                    '''
//            vm.runInterpreter(input)
//
//        then: "A class will contain an instance of a supertype which shouldn't be allowed."
//            error = thrown CompilationMessage
//            error.msg.messageType == MessageNumber.TYPE_ERROR_467
//
//    }
//
//    def "Field Declaration - Same Type as Inherited Class 2"() {
//        when: "A subtype declares a field that matches an inherited supertype."
//            input = '''
//                            class A {}
//                            class B inherits A {}
//                            class C inherits B {}
//                            Class D inherits C { protected x:B }
//                        '''
//            vm.runInterpreter(input)
//
//        then: "A class will contain an instance of a supertype which shouldn't be allowed."
//            error = thrown CompilationMessage
//            error.msg.messageType == MessageNumber.TYPE_ERROR_467
//
//    }

    // Note: This error can occur because the name checker does not check any types!
    /*
        def "Field Declaration - Recursive Types"() {
            when:
            input = '''
                            class A { protected x:B }
                            class B { protected x:A }
                        '''
            vm.runInterpreter(input)

            then:
            error = thrown CompilationMessage

        }
    */

    def "For Statement - Invalid Loop Control Variable"() {
        when: "A for loop is written with a String control variable."
            input = '''
                        for(def a:String in 1..3) {}
                    '''
            vm.runInterpreter(input)

        then: "A string can not be iterated on, so an error needs to be generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_413
    }

    def "For Statement - Start and End Values Have Different Types"() {
        when: "The starting and ending values of a for loop have different types."
            input = '''
                        for(def a:Int in 1..'c') {}
                    '''
            vm.runInterpreter(input)

        then: "An error should be generated since the types have to match."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_414
    }

    def "For Statement - Type Mismatch for Control Variable and Iteration Values"() {
        when: "The control variable and the iteration values of a for loop have different types."
            input = '''
                        for(def a:Char in 1..5) {}
                    '''
            vm.runInterpreter(input)

        then: "This generates an error since the types have to match in order to have the for loop execute."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_415
    }

    def "Function Declaration - No Guaranteed Return Statement"() {
        when: "A non-Void function does not explicitly return a value outside a control flow statement."
            input = '''
                        def func() => Int {
                            if(3 < 5) { return 3 }
                            else { return 5 }
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should be generated since we can't determine if the function will actually return a value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_422
    }

    def "Global Declaration - Invalid Constant Initialization"() {
        when: "A global variable declared with type Bool is initialized with a value of type Real."
            input = '''
                        def const a:Bool = 3.14
                    '''
            vm.runInterpreter(input)

        then: "Types Bool and Real are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_402
    }

    def "Global Declaration - Invalid Initialization"() {
        when: "A global variable declared with type Int is initialized with a value of type Real."
            input = '''
                        def global a:Int = 1.34 
                    '''
            vm.runInterpreter(input)

        then: "Types Int and Real are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_401
        }

    def "Global Declaration - Invalid Initialization 2"() {
        when: "A global variable declared with type Char is initialized with a value of type Int."
            input = '''
                        def global a:Char = 3 
                    '''
            vm.runInterpreter(input)

        then: "Types Int and Real are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_401
    }

    def "Global Declaration - Invalid Initialization 3"() {
        when: "A global variable declared with type Bool is initialized with a value of type String."
            input = '''
                        def global a:Bool = 'hi' 
                    '''
            vm.runInterpreter(input)

        then: "Types Bool and String are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_401
    }

    def "Global Declaration - Invalid Initialization 4"() {
        when: "A global variable declared with type Real is initialized with a value of type Int."
            input = '''
                        def global a:Real = 7 
                    '''
            vm.runInterpreter(input)

        then: "Types Real and Int are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_401
    }

    def "List - 1D List is Not Homogeneous"() {
        when: "A list is declared with non-homogeneous values."
            input = '''
                        List(1,2,3,'c',5)
                    '''
        vm.runInterpreter(input)

        then: "The character 'c' is an invalid value for the list, so an error is generated."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - 2D List is Not Homogeneous"() {
        when: "A 2D list is initialized to values that are not all 1D lists."
            input = '''
                        List(List(1,2,3),4,5,6)
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since a list needs to be homogenous in terms of values."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - 2D List is Not Homogeneous 2"() {
        when: "A 2D list is initialized to 1D lists of Int and Char."
            input = '''
                        List(List(1,2,3),List('c','d'))
                    '''
            vm.runInterpreter(input)

        then: "Both 1D lists are not list assignment compatible with each other, so generate an error."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - 3D List is Not Homogeneous"() {
        when: "A 3D list is declared with initial values representing a 2D list and a 1D list."
            input = '''
                        List(List(List(1,2,3), List(4,5,6)), List(6,7,8,9))
                    '''
            vm.runInterpreter(input)

        then: "The 1D list creates a list assignment compatibility error, and an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - 3D List is Not Homogeneous 2"() {
        when: "A 3D list is declared with sublists that do not share the same base types."
            input = '''
                        List(List(List('a','b'), List('c')), List(List(5), List('d','e')))
                    '''
            vm.runInterpreter(input)

        then: "The 1D list containing 5 creates an error since it is not list assignment compatible."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - 3D List is Not Homogeneous 2"() {
        when: "A 3D list is declared with sublists that do not share the same base types."
            input = '''
                        List(List(List('a','b'), List('c')), List(List(5), List(6,7)))
                    '''
            vm.runInterpreter(input)

        then: "The 1D list containing 5 creates an error since it is not list assignment compatible."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_447
    }

    def "List - Invalid 1D List Initialization"() {
        when: "A local variable representing a 1D list is initialized to a value that doesn't represent a 1D list."
            input = '''
                        def a:List[Int] = 5
                    '''
            vm.runInterpreter(input)

        then: "This is an assignment compatibility error, so terminate the compilation."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "List - Invalid 1D List Initialization 2"() {
        when: "A local variable representing a 1D list is initialized to a list with the wrong base type."
            input = '''
                        def a:List[Int] = List('c','d','a')
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since we have a base type assignment compatibility error."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "List - Invalid 2D List Initialization"() {
        when: "A variable that stores a 2D list is initialized to a 1D list."
            input = '''
                        def a:List[List[Int]] = List(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "This is a list compatibility error, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "List - Invalid 2D List Initialization 2"() {
        when: "A variable representing a 2D list is initialized to an empty 1D list."
            input = '''
                        def a:List[List[Int]] = List()
                    '''
            vm.runInterpreter(input)

        then: "This will be a list compatibility error, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "List - Invalid 3D List Initialization"() {
        when: "A variable representing a 3D list is initialized to an empty 1D list."
            input = '''
                        def a:List[List[List[Int]]] = List()
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since a 1D list can't be stored into a 3D list variable."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Local Declaration - Invalid Initialization"() {
        when: "A local variable declared with type Int is initialized with a value of type Real."
            input = '''
                        def a:Int = 5.3567
                    '''
            vm.runInterpreter(input)

        then: "Types Int and Real are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Local Declaration - Invalid Initialization 2"() {
        when: "A local variable declared with type Char is initialized with a value of type String."
            input = '''
                        def a:Char = 'hello there'
                    '''
            vm.runInterpreter(input)

        then: "Types Char and String are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Local Declaration - Invalid Initialization 3"() {
        when: "A local variable declared with type Bool is initialized with a value of type String."
            input = '''
                        def local a:Bool = 'hello'
                    '''
            vm.runInterpreter(input)

        then: "Types Bool and String are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Local Declaration - Invalid Initialization 4"() {
        when: "A local variable declared with type Real is initialized with a value of type Int."
            input = '''
                            def local a:Real = 5
                    '''
            vm.runInterpreter(input)

        then: "Types Real and Int are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Local Declaration - Invalid Initialization 5"() {
        when: "A local variable declared with type Int is initialized with a value of type Real."
            input = '''
                        def local a:Int = 5, b:Int = 3.14, c:Int = 10
                    '''
            vm.runInterpreter(input)

        then: "Types Int and Real are not assignment compatible, so an error is thrown."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_400
    }

    def "Method Declaration - No Guaranteed Return Statement"() {
        when: "A non-Void method does not explicitly return a value outside a control flow statement."
            input = '''
                        class A {
                            public method test() => Char {
                                choice(3) {
                                    on 2..5 { return 'y' }
                                    other { return 'n' }
                                }
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error should be generated since we can't determine if the method will actually return a value."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_424
    }

    def "Return Statement - Incorrect Return Value"() {
        when: "A function that should return an Int actually returns a String."
            input = '''
                        def func() => Int {
                            return 'hello'
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error will be generated since the return value's type does not match the return type."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_427
    }

    def "Return Statement - Incorrect Return Value 2"() {
        when: "A method that should return a Char actually returns a Real."
            input = '''
                        class A {
                            public method test() => Char {
                                return 3.15
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error will be generated since the return value's type does not match the return type."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_428
    }

    def "Return Statement - No Value Should Be Returned"() {
        when: "A Void function has a return statement that returns a value."
            input = '''
                        def func() => Void {
                            return 3
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since Void functions should not return anything."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_425
    }

    def "Return Statement - No Value Should Be Returned 2"() {
        when: "A Void method has a return statement that returns a value."
            input = '''
                        class A {
                            public method func() => Void {
                                return 'c'
                            }
                        }
                    '''
            vm.runInterpreter(input)

        then: "An error is thrown since Void methods should not return anything."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_426
    }

    def "Unary Expression - Invalid ~ Operation"() {
        when: "A bitwise not operation is performed on a Real variable."
            input = '''
                        def a:Real = 3.14
                        ~a
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since a Real value can't be used for a bitwise negation."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_408
    }

    def "Unary Expression - Invalid NOT Operation"() {
        when: "A not operation is performed on a String variable."
            input = '''
                        def a:String = 'hello'
                        not a
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since a String value can't be used for a negation."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_408
    }

    def "While Statement - Invalid Conditional Expression"() {
        when: "A while loop is written with a non-Bool expression as a condition."
            input = '''
                        while(8) {}
                    '''
            vm.runInterpreter(input)

        then: "An error should be thrown since the conditional expression has to evaluate to be a Bool."
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.TYPE_ERROR_411
    }
}
