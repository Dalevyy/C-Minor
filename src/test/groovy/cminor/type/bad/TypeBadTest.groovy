package cminor.type.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.type.TypeTest

class TypeBadTest extends TypeTest {

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
}
